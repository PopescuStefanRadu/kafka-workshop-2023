package ro.esolutions.demo.springconsumerproducer.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinesProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendBook(final String book) {
        String[] lines = book.split("\n");
        for (String line : lines) {
            kafkaTemplate.send(Common.TOPIC_NAME_BOOK_LINES, line);
        }
    }
}
