package io.cloudevents.example.kinesis;

import io.cloudevents.CloudEvent;
import io.cloudevents.kinesis.KinesisMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.kinesis.lifecycle.events.InitializationInput;
import software.amazon.kinesis.lifecycle.events.LeaseLostInput;
import software.amazon.kinesis.lifecycle.events.ProcessRecordsInput;
import software.amazon.kinesis.lifecycle.events.ShardEndedInput;
import software.amazon.kinesis.lifecycle.events.ShutdownRequestedInput;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

public class CloudEventRecordProcessorFactory implements ShardRecordProcessorFactory {
    @Override
    public ShardRecordProcessor shardRecordProcessor() {
        return new CloudEventRecordProcessor();
    }
}

class CloudEventRecordProcessor implements ShardRecordProcessor {
    private static final Logger log = LoggerFactory.getLogger(CloudEventRecordProcessor.class);

    @Override
    public void initialize(InitializationInput initializationInput) {

    }

    @Override
    public void processRecords(ProcessRecordsInput processRecordsInput) {
        processRecordsInput.records().forEach(record -> {
            final CloudEvent cloudEvent = KinesisMessageFactory.createReader(record).toEvent();

            log.info("cloud event -> {}", cloudEvent.toString());
        });
    }

    @Override
    public void leaseLost(LeaseLostInput leaseLostInput) {
    }

    @Override
    public void shardEnded(ShardEndedInput shardEndedInput) {
    }

    @Override
    public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
    }
}
