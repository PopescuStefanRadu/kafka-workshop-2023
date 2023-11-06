package ro.esolutions.demo.springconsumerproducer.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BookLinesConsumer {

    @KafkaListener(
            batch = "false",
            autoStartup = "true",
            concurrency = "3",
            groupId = "spring-book-lines-consumer",
            topics = Common.TOPIC_NAME_BOOK_LINES
    )
    public void processBookLine(ConsumerRecord<String, String> record) {
        log.info("Received message, topic: {}, partition: {}, key: {}, value: {}",
                record.topic(), record.partition(), record.key(), record.value());
    }
}
