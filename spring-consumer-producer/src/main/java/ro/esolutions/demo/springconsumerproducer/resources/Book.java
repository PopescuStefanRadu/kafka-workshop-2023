package ro.esolutions.demo.springconsumerproducer.resources;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ro.esolutions.demo.springconsumerproducer.kafka.LinesProducer;
import ro.esolutions.demo.springconsumerproducer.resources.models.BookContent;

@RestController
@RequiredArgsConstructor
public class Book {
    private final LinesProducer linesProducer;

    @PostMapping("/sendBook")
    public void sendBook(@RequestBody BookContent content) {
        linesProducer.sendBook(content.getContent());
    }
}
