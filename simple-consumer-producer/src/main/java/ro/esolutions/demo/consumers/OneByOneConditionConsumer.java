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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
public class OneByOneConditionConsumer implements Closeable {
    public static final Duration POLL_DURATION = Duration.of(5, ChronoUnit.SECONDS);
    private volatile boolean shouldRun = true;
    private final KafkaConsumer<String, String> consumer;
    private final Collection<String> topics;

    // Guarded by this.lock and observed by this.cond
    private volatile boolean shutdownCompleted = true;
    private final Lock shutdownLock = new ReentrantLock();
    private final Condition shutdownCond = shutdownLock.newCondition();

    public OneByOneConditionConsumer(KafkaConsumer<String, String> consumer, Collection<String> topics) {
        this.consumer = consumer;
        this.topics = topics;
    }


    public static void main(String[] args) {
        var props = Common.exampleTopicConsumerConfig();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "one-by-one-condition-book-lines-consumer");

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
        shutdownLock.lock();
        try {
            while (!shutdownCompleted) {
                try {
                    shutdownCond.await();
                } catch (InterruptedException e) {
                    log.info("Interrupted while awaiting shutdown", e);
                }
            }
        } finally {
            shutdownLock.unlock();
        }
        log.info("Shut down complete");
    }

    public void run(Consumer<ConsumerRecord<String, String>> consumerFn) {
        shutdownLock.lock();
        try {
            shutdownCompleted = false;
        } finally {
            shutdownLock.unlock();
        }
        consumer.subscribe(topics);

        try {
            while (shouldRun) {
                ConsumerRecords<String, String> poll = consumer.poll(POLL_DURATION);
                for (ConsumerRecord<String, String> record : poll) {
                    if (!shouldRun) {
                        break;
                    }
                    consumerFn.accept(record);
                    consumer.commitSync(Map.of(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset())));
                }
            }
        } finally {
            shutdownLock.lock();
            try {
                consumer.close();
                shutdownCompleted = true;
                shutdownCond.signal();
            } finally {
                shutdownLock.unlock();
            }
        }
    }
}
