package ro.esolutions.demo.consumers;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import ro.esolutions.demo.Common;
import ro.esolutions.demo.TopicManager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class BatchConsumer {
    static final Duration POLL_DURATION = Duration.of(2, ChronoUnit.SECONDS);
    static final int PARALLELISM = 3;

    public static void main(String[] args) {
        var props = consumerProperties();

        TopicManager.createExampleTopic(props);

        for (int i = 0; i < PARALLELISM; i++) {
            new Thread(() -> startNewConsumer(props), "BatchConsumer-" + i).start();
        }
    }

    private static void startNewConsumer(final Properties props) {
        CountDownLatch cd = new CountDownLatch(1);
        AtomicBoolean shouldRun = new AtomicBoolean(true);
        Thread ct = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            String threadName = ct.getName();
            log.info("Shutting down {}", threadName);
            shouldRun.set(false);
            try {
                cd.await();
            } catch (InterruptedException ex) {
                log.error("Interrupted while shutting down {}", threadName, ex);
            }
            log.info("Successfully shut down {}", threadName);
        }));

        try (var consumer = new KafkaConsumer<String, String>(props)) {
            List<String> topicsToSubscribeTo = List.of(Common.TOPIC_NAME_BOOK_LINES);
            consumer.subscribe(topicsToSubscribeTo);

            while (shouldRun.get()) {
                ConsumerRecords<String, String> records = consumer.poll(POLL_DURATION);

                for (ConsumerRecord<String, String> record : records) {
                    log.info("Received message, topic: {}, partition: {}, key: {}, value: {}",
                            record.topic(), record.partition(), record.key(), record.value());
                }

                consumer.commitSync();
            }
        } finally {
            cd.countDown();
        }
    }

    private static Properties consumerProperties() {
        final var props = Common.exampleTopicConsumerConfig();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Common.TOPIC_NAME_BOOK_LINES);
        return props;
    }
}
