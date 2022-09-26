package io.cloudevents.kinesis.impl;

import io.cloudevents.CloudEventData;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.message.MessageWriter;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.rw.CloudEventContextWriter;
import io.cloudevents.rw.CloudEventRWException;
import io.cloudevents.rw.CloudEventWriter;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * As Kinesis does not support headers, if we are passed a structured message, we forced its format to be json encoding and
 * write out the data and return the new PutRecordRequest
 */

@ParametersAreNonnullByDefault
public class KinesisMessageWriterBuilderImpl implements MessageWriter<CloudEventWriter<PutRecordRequest.Builder>, PutRecordRequest.Builder>, CloudEventWriter<PutRecordRequest.Builder> {
    PutRecordRequest.Builder record = PutRecordRequest.builder();
    CloudEventBuilder event = null;

    /* this is called when writeStructured is requested */
    @Override
    public PutRecordRequest.Builder setEvent(EventFormat format, byte[] value) throws CloudEventRWException {
        if (!(format instanceof JsonFormat)) {
            throw CloudEventRWException.newUnknownEncodingException();
        }

        record.data(SdkBytes.fromByteArray(value));

        return record;
    }

    /* this is called when writeBinary is called */
    @Override
    public CloudEventWriter<PutRecordRequest.Builder> create(SpecVersion version) throws CloudEventRWException {
        event = CloudEventBuilder.fromSpecVersion(version);
        return this;
    }

    /* this is called when writeBinary is called and there is data */
    @Override
    public PutRecordRequest.Builder end(CloudEventData data) throws CloudEventRWException {
        event.withData(data.toBytes());
        return end();
    }

    /* this is called when writeBinary is called and there is no data */
    @Override
    public PutRecordRequest.Builder end() throws CloudEventRWException {
        return record.data(SdkBytes.fromByteArray(new JsonFormat().serialize(event.build())))
            ;
    }

    /* this is called when writeBinary is called */
    @Override
    public CloudEventContextWriter withContextAttribute(String name, String value) throws CloudEventRWException {
        event.withContextAttribute(name, value);
        return this;
    }
}
