package io.cloudevents.gpubsub.impl;

import com.google.pubsub.v1.PubsubMessage;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.cloudevents.gpubsub.PubsubMessageFactory;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.types.Time;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PubsubMessageWriterImplTest {
    CloudEventBuilder builder;

    @BeforeEach
    public void setupBuilder() {
        builder = new CloudEventBuilder()
            .withData("application/json", "test-data".getBytes(StandardCharsets.UTF_8))
            .withSubject("subject-v1")
            .withSource(URI.create(PubsubHeadersTest.fullUrl))
            .withTime(Time.parseTime(PubsubHeadersTest.rawTime))
            .withId("event-id")
            .withType(PubsubHeadersTest.emojiRaw)
            .withExtension("testdata", "contents-of-test-data");
        builder.withContextAttribute("sausage", "cumberlands");
    }

    @Test
    public void structuredTest() {
        final CloudEvent event = builder.build();
        final PubsubMessage message = PubsubMessageFactory
            .createWriter()
            .writeStructured(event, new JsonFormat());

        assertThat(message.getAttributesCount()).isEqualTo(1);
        assertThat(message.getAttributesOrThrow(PubsubHeaders.CONTENT_TYPE)).isEqualTo("application/cloudevents+json");
        // we don't care what is in it, simply that it is the same
        assertThat(message.getData().toByteArray()).isEqualTo(new JsonFormat().serialize(event));
    }

    @Test
    public void binaryTest() {
        final PubsubMessage message = PubsubMessageFactory
            .createWriter()
            .writeBinary(builder.build());

        Map<String, String> headers = message.getAttributesMap();

        assertThat(headers.get("ce-subject")).isEqualTo("subject-v1");
        assertThat(headers.get("ce-source")).isEqualTo(PubsubHeadersTest.fullUrl);
        assertThat(headers.get("ce-time")).isEqualTo(PubsubHeadersTest.rawTime);
        assertThat(headers.get("ce-type")).isEqualTo(PubsubHeadersTest.emojiRaw);
        assertThat(headers.get("ce-id")).isEqualTo("event-id");
        assertThat(headers.get("content-type")).isEqualTo("application/json");
        assertThat(headers.get("ce-testdata")).isEqualTo("contents-of-test-data");

        assertThat(new String(message.getData().toByteArray(), StandardCharsets.UTF_8)).isEqualTo("test-data");
    }
}
