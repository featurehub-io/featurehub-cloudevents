package io.cloudevents.kinesis;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.message.MessageReader;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class KinesisMessageFactoryTest {
    public static final String emojiRaw = "Euro € 😀";
    public static final String fullUrl = "http://localhost:8080/my/frog-died";
    public static final String rawTime = "2018-04-26T14:48:09+02:00";

    private CloudEvent event;
    private OffsetDateTime now;

    @BeforeEach
    public void before() {
        now = OffsetDateTime.now();
        event = new CloudEventBuilder()
            .withData("application/json", "\"test-data\"".getBytes(StandardCharsets.UTF_8))
            .withSource(URI.create(fullUrl))
            .withId("event-id")
            .withTime(now)
            .withType(emojiRaw).build();
    }

    private void compare(CloudEvent decoded) {
        if (decoded.getData() == null) {
            assertThat(event.getData()).isNull();
        } else {
            assertThat(decoded.getData().toBytes()).isEqualTo(event.getData().toBytes());
        }
        assertThat(decoded.getSource()).isEqualTo(event.getSource());
        assertThat(decoded.getId()).isEqualTo(event.getId());
        assertThat(decoded.getType()).isEqualTo(event.getType());
    }

    @Test
    public void basicStructuredTest() {
        final PutRecordRequest request = KinesisMessageFactory.createWriter().writeStructured(event, new JsonFormat()).build();

        final MessageReader reader = KinesisMessageFactory.createReader(KinesisClientRecord.builder().data(request.data().asByteBuffer()).build());

        compare(reader.toEvent());
    }

    @Test
    public void basicBinaryTest() {
        final PutRecordRequest request = KinesisMessageFactory.createWriter().writeBinary(event).build();

        final MessageReader reader = KinesisMessageFactory.createReader(KinesisClientRecord.builder().data(request.data().asByteBuffer()).build());

        compare(reader.toEvent());
    }

    @Test
    public void basicBinaryNoData() {
        event = new CloudEventBuilder()
            .withSource(URI.create(fullUrl))
            .withId("event-id")
            .withTime(now)
            .withType(emojiRaw).build();

        final PutRecordRequest request = KinesisMessageFactory.createWriter().writeBinary(event).build();

        final MessageReader reader = KinesisMessageFactory.createReader(KinesisClientRecord.builder().data(request.data().asByteBuffer()).build());

        compare(reader.toEvent());
    }
}
