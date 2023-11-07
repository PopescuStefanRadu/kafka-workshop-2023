package ro.esolutions.demo.producers;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import ro.esolutions.demo.Common;
import ro.esolutions.demo.TopicManager;

import java.io.BufferedInputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ProducerApp {

    public static void main(String[] args) {
        var props = producerProperties();

        var shouldRun = new AtomicBoolean(true);
        var cd = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shouldRun.set(false);
            try {
                cd.await();
            } catch (InterruptedException e) {
                log.info("Interrupted while waiting for producer to finish");
            }
        }));
        var random = new Random();

        TopicManager.createExampleTopic(props);

        Scanner scanner = new Scanner(new BufferedInputStream(Objects.requireNonNull(ProducerApp.class.getResourceAsStream("/pride-prejudice.txt"))));

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        try {
            while (shouldRun.get() && scanner.hasNextLine()) {
                var waitMs = random.nextInt(9) + 1;
                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException ignored) {
                    log.info("Producer interrupted,shutting down");
                    return;
                }
                ProducerRecord<String, String> record = new ProducerRecord<>(Common.TOPIC_NAME_BOOK_LINES, scanner.nextLine());
                producer.send(record, ProducerApp::logException);
            }
        } finally {
            producer.close(Duration.of(30, ChronoUnit.SECONDS));
            cd.countDown();
        }
    }


    private static void logException(RecordMetadata meta, Exception ex) {
        if (ex != null) {
            log.error("Could not send record", ex);
        }
    }

    private static Properties producerProperties() {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Common.BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.LINGER_MS_CONFIG, 50);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // default
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, 100);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "example-producer");
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30 * 1000); // default
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10 * 1024 * 1024);
        return props;
    }
}
