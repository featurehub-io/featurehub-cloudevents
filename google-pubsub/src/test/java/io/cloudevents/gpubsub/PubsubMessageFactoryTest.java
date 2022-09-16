package io.cloudevents.gpubsub;

import com.google.protobuf.Message;
import com.google.pubsub.v1.PubsubMessage;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.cloudevents.gpubsub.impl.PubsubHeadersTest;
import io.cloudevents.jackson.JsonFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class PubsubMessageFactoryTest {
    private CloudEvent event;

    @BeforeEach
    public void before() {
        event = new CloudEventBuilder()
            .withData("application/json", "\"test-data\"".getBytes(StandardCharsets.UTF_8))
            .withSource(URI.create(PubsubHeadersTest.fullUrl))
            .withId("event-id")
            .withTime(OffsetDateTime.now())
            .withType(PubsubHeadersTest.emojiRaw).build();
    }

    private void compare(CloudEvent decoded) {
        assertThat(decoded.getData().toBytes()).isEqualTo(event.getData().toBytes());
        assertThat(decoded.getSource()).isEqualTo(event.getSource());
        assertThat(decoded.getId()).isEqualTo(event.getId());
        assertThat(decoded.getType()).isEqualTo(event.getType());
    }

    @Test
    public void encodeDecodeStructured() {
        PubsubMessage m = PubsubMessageFactory.createWriter().writeStructured(event, new JsonFormat());

        CloudEvent decoded = PubsubMessageFactory.createReader(m).toEvent();

        compare(decoded);
    }

    @Test
    public void encodeDecodeBinary() {
        PubsubMessage m = PubsubMessageFactory.createWriter().writeBinary(event);

        CloudEvent decoded = PubsubMessageFactory.createReader(m).toEvent();

        compare(decoded);
    }
}
