package ro.esolutions.demo.consumers;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import ro.esolutions.demo.Common;
import ro.esolutions.demo.TopicManager;

import java.io.Closeable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@Slf4j
public class OneByOneLatchConsumer implements Closeable {

    public static final Duration POLL_DURATION = Duration.of(5, ChronoUnit.SECONDS);
    private volatile boolean shouldRun = true;
    private final KafkaConsumer<String, String> consumer;
    private final Collection<String> topics;

    private final CountDownLatch cd = new CountDownLatch(1);

    public OneByOneLatchConsumer(KafkaConsumer<String, String> consumer, Collection<String> topics) {
        this.consumer = consumer;
        this.topics = topics;
    }


    public static void main(String[] args) {
        var props = Common.exampleTopicConsumerConfig();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "one-by-one-latch-book-lines-consumer");

        TopicManager.createExampleTopic(props);

        var consumer = new OneByOneConditionConsumer(new KafkaConsumer<>(props), List.of(Common.TOPIC_NAME_BOOK_LINES));
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::close));
        try {
            consumer.run(record -> log.info("Received message, topic: {}, partition: {}, key: {}, value: {}",
                    record.topic(), record.partition(), record.key(), record.value()));
        } finally {
            consumer.close();
        }
    }

    @Override
    public void close() {
        log.info("Shutting down");
        shouldRun = false;
        try {
            cd.await();
        } catch (InterruptedException e) {
            log.info("Interrupted while waiting for consumer to shut down", e);
        }
        log.info("Shut down complete");
    }

    public void run(Consumer<ConsumerRecord<String, String>> consumerFn) {
        consumer.subscribe(topics);
        try {
            while (shouldRun) {
                ConsumerRecords<String, String> poll = consumer.poll(POLL_DURATION);
                for (ConsumerRecord<String, String> record : poll) {
                    if (!shouldRun) {
                        break;
                    }
                    consumerFn.accept(record);

                    TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
                    OffsetAndMetadata offset = new OffsetAndMetadata(record.offset());
                    consumer.commitSync(Map.of(topicPartition, offset));
                }
            }
        } finally {
            consumer.close();
            cd.countDown();
        }
    }
}
