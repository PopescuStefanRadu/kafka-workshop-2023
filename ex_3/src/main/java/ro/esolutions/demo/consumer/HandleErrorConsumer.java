package ro.esolutions.demo.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import ro.esolutions.demo.Common;
import ro.esolutions.demo.TopicManager;
import ro.esolutions.demo.model.Car;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class HandleErrorConsumer implements Closeable {
    public static final Duration POLL_DURATION = Duration.ofSeconds(5000);
    private final KafkaConsumer<String, String> consumer;
    private final Collection<String> topics;
    private final CountDownLatch cd = new CountDownLatch(1);
    private volatile boolean shouldRun = true;

    public HandleErrorConsumer(KafkaConsumer<String, String> consumer, Collection<String> topics) {
        this.consumer = consumer;
        this.topics = topics;
    }
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "handle-error-consumer");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Car.class.getCanonicalName());

        TopicManager.createExampleTopic(properties);

        var handleErrorConsumer = new HandleErrorConsumer(new KafkaConsumer<>(properties), List.of(Common.TOPIC_NAME_BOOK_LINES));
        Runtime.getRuntime().addShutdownHook(new Thread(handleErrorConsumer::close));
        
        try {
           handleErrorConsumer.run(); 
        } catch (Exception e) {
            log.error("Exception while running", e);
        } finally {
            handleErrorConsumer.close();
        }
    }

    private void run() {
    }


    @Override
    public void close() {
        shouldRun = false;
        try {
            cd.await();
        } catch (InterruptedException e) {
            log.info("Interrupted while waiting for shutdown");
        }
    }
}
