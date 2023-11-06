package ro.esolutions.demo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Common {
    public static final String BOOTSTRAP_SERVERS = "kafka1:9092,kafka2:9093,kafka3:9094";
    public static final String TOPIC_NAME_BOOK_LINES = "book-lines";

    public static Properties exampleTopicConsumerConfig() {
        final var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Common.BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}
