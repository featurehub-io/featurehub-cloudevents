package io.cloudevents.kinesis;

import io.cloudevents.SpecVersion;
import io.cloudevents.core.message.MessageReader;
import io.cloudevents.core.message.MessageWriter;
import io.cloudevents.core.message.impl.GenericStructuredMessageReader;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.kinesis.impl.KinesisMessageWriterBuilderImpl;
import io.cloudevents.rw.CloudEventRWException;
import io.cloudevents.rw.CloudEventWriter;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.nio.ByteBuffer;

public class KinesisMessageFactory {
    private KinesisMessageFactory() {
    }

    /**
     * Create a {@link io.cloudevents.core.message.MessageReader} to read {@link KinesisClientRecord}. Kinesis has no header
     * support and can only support generic messages
     *
     * @param message the message to convert to {@link io.cloudevents.core.message.MessageReader}
     * @return the new {@link io.cloudevents.core.message.MessageReader}
     * @throws CloudEventRWException if something goes wrong while resolving the {@link SpecVersion} or if the message has unknown encoding
     */
    public static MessageReader createReader(KinesisClientRecord message) throws CloudEventRWException {
        final ByteBuffer data = message.data();
        byte[] bData = new byte[data.remaining()];
        data.get(bData);

        return new GenericStructuredMessageReader(new JsonFormat(), bData);
    }

    /**
     * Returns a message writer for Kinesis messages.
     *
     * @return a PubsubMessage object
     */
    public static MessageWriter<CloudEventWriter<PutRecordRequest.Builder>, PutRecordRequest.Builder> createWriter() {
        return new KinesisMessageWriterBuilderImpl();
    }

}
