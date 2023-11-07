package ro.esolutions.demo.springconsumerproducer;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import ro.esolutions.demo.springconsumerproducer.kafka.Common;

import java.time.Duration;

@SpringBootApplication
public class SpringConsumerProducerApplication {
    public static final Duration RETENTION = Duration.ofSeconds(5000);

    @Bean
    public NewTopic exampleTopic() {
        return TopicBuilder.name(Common.TOPIC_NAME_BOOK_LINES)
                .partitions(3)
                .replicas(3)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(RETENTION.toMillis()))
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringConsumerProducerApplication.class, args);
    }

}
