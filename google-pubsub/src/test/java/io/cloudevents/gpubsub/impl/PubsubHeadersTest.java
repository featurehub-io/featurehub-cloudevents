package io.cloudevents.gpubsub.impl;

import io.cloudevents.core.v1.CloudEventV1;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PubsubHeadersTest {
    public static final String emojiRaw = "Euro € 😀";
    public static final String fullUrl = "http://localhost:8080/my/frog-died";
    public static final String rawTime = "2018-04-26T14:48:09+02:00";

    @Test
    public void headerMappingTest() {
        assertThat(PubsubHeaders.headerMapper("fred")).isEqualTo(PubsubHeaders.CE_PREFIX + "fred");
        assertThat(PubsubHeaders.headerMapper(CloudEventV1.DATACONTENTTYPE)).isEqualTo(PubsubHeaders.CONTENT_TYPE);
    }

    @Test
    public void headerUnmapperTest() {
        assertThat(PubsubHeaders.headerUnmapper(PubsubHeaders.CE_PREFIX + "fred")).isEqualTo("fred");
        assertThat(PubsubHeaders.headerUnmapper(PubsubHeaders.CONTENT_TYPE)).isEqualTo(CloudEventV1.DATACONTENTTYPE);
    }
}
