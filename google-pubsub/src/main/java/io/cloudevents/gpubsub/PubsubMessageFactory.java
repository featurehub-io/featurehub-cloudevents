package io.cloudevents.gpubsub;

import com.google.pubsub.v1.PubsubMessage;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.message.MessageReader;
import io.cloudevents.core.message.MessageWriter;
import io.cloudevents.core.message.impl.GenericStructuredMessageReader;
import io.cloudevents.core.message.impl.MessageUtils;
import io.cloudevents.gpubsub.impl.PubsubHeaders;
import io.cloudevents.gpubsub.impl.PubsubMessageReaderImpl;
import io.cloudevents.gpubsub.impl.PubsubMessageWriterImpl;
import io.cloudevents.rw.CloudEventRWException;
import io.cloudevents.rw.CloudEventWriter;

public class PubsubMessageFactory {
    private PubsubMessageFactory() {}

    /**
     * Create a {@link io.cloudevents.core.message.MessageReader} to read {@link PubsubMessage}.
     *
     * @param message the message to convert to {@link io.cloudevents.core.message.MessageReader}
     * @return the new {@link io.cloudevents.core.message.MessageReader}
     * @throws CloudEventRWException if something goes wrong while resolving the {@link SpecVersion} or if the message has unknown encoding
     */
    public static MessageReader createReader(PubsubMessage message) throws CloudEventRWException {
        return MessageUtils.parseStructuredOrBinaryMessage(
            () -> message.getAttributesOrThrow(PubsubHeaders.CONTENT_TYPE),
            format -> new GenericStructuredMessageReader(format, message.getData().toByteArray()),
            () -> message.getAttributesOrThrow(PubsubHeaders.SPEC_VERSION),
            sv -> new PubsubMessageReaderImpl(sv, message)
        );
    }

    /**
     * Returns a message writer for Google PubSub messages.
     *
     * @return a PubsubMessage object
     */
    public static MessageWriter<CloudEventWriter<PubsubMessage>, PubsubMessage> createWriter() {
        return new PubsubMessageWriterImpl();
    }
}
