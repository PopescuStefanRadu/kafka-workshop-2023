package ro.esolutions.demo.error;

import org.springframework.kafka.support.serializer.FailedDeserializationInfo;
import ro.esolutions.demo.model.Car;

public class BadThing extends Car {

    private final FailedDeserializationInfo failedDeserializationInfo;

    public BadThing(FailedDeserializationInfo failedDeserializationInfo) {
        this.failedDeserializationInfo = failedDeserializationInfo;
    }

    public FailedDeserializationInfo getFailedDeserializationInfo() {
        return this.failedDeserializationInfo;
    }

}
