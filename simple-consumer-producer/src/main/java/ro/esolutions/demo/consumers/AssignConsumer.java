package ro.esolutions.demo.consumers;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import ro.esolutions.demo.Common;
import ro.esolutions.demo.TopicManager;

import java.io.Closeable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@Slf4j
public class AssignConsumer implements Closeable {
    public static final Duration POLL_DURATION = Duration.of(5, ChronoUnit.SECONDS);
    private final KafkaConsumer<String, String> consumer;
    private final Collection<String> topics;
    private final CountDownLatch cd = new CountDownLatch(1);
    private volatile boolean shouldRun = true;

    public AssignConsumer(KafkaConsumer<String, String> consumer, Collection<String> topics) {
        this.consumer = consumer;
        this.topics = topics;
    }

    public static void main(String[] args) {
        Properties props = Common.exampleTopicConsumerConfig();
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "assign-consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        TopicManager.createExampleTopic(props);

        var assignConsumer = new AssignConsumer(new KafkaConsumer<>(props), List.of(Common.TOPIC_NAME_BOOK_LINES));
        Runtime.getRuntime().addShutdownHook(new Thread(assignConsumer::close));

        try {
            assignConsumer.run(record -> log.info("Received message, topic: {}, partition: {}, key: {}, value: {}",
                    record.topic(), record.partition(), record.key(), record.value()));
        } catch (Exception e) {
            log.error("Exception while running", e);
        } finally {
            assignConsumer.close();
        }
    }


    public void run(Consumer<ConsumerRecord<String, String>> consumerFn) {
        var topicPartitions = new ArrayList<TopicPartition>(topics.size() * 3);
        for (String topic : topics) {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            for (PartitionInfo partitionInfo : partitionInfos) {
                topicPartitions.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
            }
        }

        // does not use consumer groups, can manually control everything. can seek to whichever offset
        consumer.assign(topicPartitions);
        try {
            while (shouldRun) {
                ConsumerRecords<String, String> records = consumer.poll(POLL_DURATION);
                for (ConsumerRecord<String, String> record : records) {
                    consumerFn.accept(record);
                }
//            consumer.commitSync(); throws exception
            }
        } finally {
            consumer.close();
            cd.countDown();
        }
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
