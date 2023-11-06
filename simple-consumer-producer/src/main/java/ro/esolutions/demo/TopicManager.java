package ro.esolutions.demo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TopicManager {
    static final Integer THREE_MINUTES = 3 * 60 * 1000;

    public static void createExampleTopic(final Properties props) {
        try (final AdminClient admin = AdminClient.create(props)) {

            final NewTopic exampleTopic = new NewTopic(Common.TOPIC_NAME_BOOK_LINES, 3, (short) 3);
            exampleTopic.configs(Map.of(
                    TopicConfig.RETENTION_MS_CONFIG, THREE_MINUTES.toString()
            ));

            admin.createTopics(List.of(exampleTopic));
        }
    }
}
