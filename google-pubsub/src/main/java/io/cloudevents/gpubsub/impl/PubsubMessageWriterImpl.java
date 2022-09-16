package io.cloudevents.gpubsub.impl;

import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.cloudevents.CloudEventData;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.message.MessageWriter;
import io.cloudevents.rw.CloudEventContextWriter;
import io.cloudevents.rw.CloudEventRWException;
import io.cloudevents.rw.CloudEventWriter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PubsubMessageWriterImpl implements MessageWriter<CloudEventWriter<PubsubMessage>, PubsubMessage>, CloudEventWriter<PubsubMessage>{
    private final PubsubMessage.Builder message = PubsubMessage.newBuilder();
    @Override
    public PubsubMessage setEvent(EventFormat format, byte[] value) throws CloudEventRWException {
        // force the header to be this format. Headers will be ignored if the server can't support them
        message.putAttributes(PubsubHeaders.CONTENT_TYPE, format.serializedContentType());
        message.setData(ByteString.copyFrom(value));
        return message.build();

    }

    @Override
    public PubsubMessage end(CloudEventData data) throws CloudEventRWException {
        message.setData(ByteString.copyFrom(data.toBytes()));
        return message.build();
    }

    @Override
    public PubsubMessage end() throws CloudEventRWException {
        return message.build();
    }

    @Override
    public CloudEventContextWriter withContextAttribute(String name, String value) throws CloudEventRWException {
        message.putAttributes(PubsubHeaders.headerMapper(name), value);
        return this;
    }

    @Override
    public CloudEventWriter<PubsubMessage> create(SpecVersion version) throws CloudEventRWException {
        message.putAttributes(PubsubHeaders.SPEC_VERSION, SpecVersion.V1.toString());
        return this;
    }
}
