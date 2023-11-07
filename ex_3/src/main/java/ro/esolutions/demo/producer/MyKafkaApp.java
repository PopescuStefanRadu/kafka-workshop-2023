package ro.esolutions.demo.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.SerializationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.esolutions.demo.error.MyKafkaErrorHandler;
import ro.esolutions.demo.model.Car;

@Slf4j
@RestController
public class MyKafkaApp {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @SneakyThrows
    @PostMapping("/sendCar")
    private void sendMessage() {
        ObjectMapper objectMapper = new ObjectMapper();
        Car car = new Car();
        car.setId("id");
        car.setName("janghina");
        kafkaTemplate.send("cars-topic", objectMapper.writeValueAsString(car));
    }

    @KafkaListener(topics = "cars-topic", groupId = "consumer-group-car", errorHandler = "myKafkaErrorHandler")
    public void listenMessages(Car car) {
        log.info("Received Message in group foo: " + car);
    }
}
