package io.cloudevents.gpubsub.impl;

import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.cloudevents.CloudEvent;
import io.cloudevents.SpecVersion;
import org.junit.jupiter.api.Test;
import io.cloudevents.gpubsub.PubsubMessageFactory;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PubsubMessageReaderImplTest {
    @Test
    public void basicReaderTest() {
        byte[] body = "hello-world".getBytes(StandardCharsets.UTF_8);
        PubsubMessage.Builder message = PubsubMessage.newBuilder()
            .putAttributes(PubsubHeaders.SPEC_VERSION, SpecVersion.V1.toString())
            .putAttributes(PubsubHeaders.CONTENT_TYPE, "application/json")
            .putAttributes(PubsubHeaders.headerMapper("subject"), "subject-test")
            .putAttributes(PubsubHeaders.headerMapper("id"), "hello-id")
            .putAttributes(PubsubHeaders.headerMapper("type"), "hello/v1")
            .putAttributes(PubsubHeaders.headerMapper("test"), "test-value")
            .putAttributes(PubsubHeaders.headerMapper("source"), "plum")
            .setData(ByteString.copyFrom(body))
            ;

        PubsubMessageReaderImpl reader = new PubsubMessageReaderImpl(SpecVersion.V1, message.build());

        assertThat(reader.isCloudEventsHeader(PubsubHeaders.CE_EVENT_FORMAT + "x")).isTrue();
        assertThat(reader.isCloudEventsHeader("burp")).isFalse();
        assertThat(reader.isContentTypeHeader("burp")).isFalse();
        assertThat(reader.isContentTypeHeader(PubsubHeaders.CONTENT_TYPE)).isTrue();
        assertThat(reader.toCloudEventsKey(PubsubHeaders.CE_PREFIX + "k")).isEqualTo("k");

        Map<String, String> data = new HashMap<>();
        reader.forEachHeader((key, value) -> data.put(key, new String(value)));

        assertThat(data.size()).isEqualTo(7);

        assertThat(data.get(PubsubHeaders.headerMapper("test"))).isEqualTo("test-value");
        assertThat(data.get(PubsubHeaders.CONTENT_TYPE)).isEqualTo("application/json");

        CloudEvent cloudEvent = PubsubMessageFactory.createReader(message.build()).toEvent();

        assertThat(cloudEvent.getSubject()).isEqualTo("subject-test");
        assertThat(cloudEvent.getId()).isEqualTo("hello-id");
        assertThat(cloudEvent.getType()).isEqualTo("hello/v1");
        assertThat(cloudEvent.getExtension("test")).isEqualTo("test-value");

    }
}
