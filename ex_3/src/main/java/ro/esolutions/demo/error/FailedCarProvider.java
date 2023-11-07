package ro.esolutions.demo.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;

import java.util.function.Function;

@Slf4j
public class FailedCarProvider implements Function<FailedDeserializationInfo, BadThing> {
    @Override
    public BadThing apply(FailedDeserializationInfo info) {
        log.error("My error info message" + info);
        return new BadThing(info);
    }
}

