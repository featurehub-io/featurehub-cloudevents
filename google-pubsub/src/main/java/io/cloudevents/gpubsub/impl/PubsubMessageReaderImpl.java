package io.cloudevents.gpubsub.impl;

import com.google.pubsub.v1.PubsubMessage;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.data.BytesCloudEventData;
import io.cloudevents.core.message.impl.BaseGenericBinaryMessageReaderImpl;

import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

public class PubsubMessageReaderImpl extends BaseGenericBinaryMessageReaderImpl<String, byte[]> {
    private final PubsubMessage message;

    public PubsubMessageReaderImpl(SpecVersion version, PubsubMessage message) {
        super(version, message.getData().isEmpty() ? null : BytesCloudEventData.wrap(message.getData().toByteArray()));

        this.message = message;
    }

    @Override
    protected boolean isContentTypeHeader(String key) {
        return key.equals(PubsubHeaders.CONTENT_TYPE);
    }

    @Override
    protected boolean isCloudEventsHeader(String key) {
        return key.startsWith(PubsubHeaders.CE_PREFIX);
    }

    @Override
    protected String toCloudEventsKey(String key) {
        return PubsubHeaders.headerUnmapper(key);
    }

    @Override
    protected void forEachHeader(BiConsumer<String, byte[]> fn) {
        message.getAttributesMap().forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                fn.accept(key, value.getBytes(StandardCharsets.UTF_8));
            }
        });
    }

    @Override
    protected String toCloudEventsValue(byte[] value) {
        return new String(value, StandardCharsets.UTF_8);
    }
}
