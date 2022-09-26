package io.cloudevents.example.kinesis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.retrieval.polling.PollingConfig;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class SimpleListener {
    private final KinesisClient kinesisClient;
    // being the same appName means if you run this multiple times, it will load balance the incoming data on
    // the stream using the partition key.
    private final String appName = "simple-app";

    private static final Logger log = LoggerFactory.getLogger(SimpleListener.class);

    public SimpleListener(String streamName, String region) {
        kinesisClient = new KinesisClient(streamName, region);
    }

    public void run() throws InterruptedException {
        /*
         * Sets up configuration for the KCL, including DynamoDB and CloudWatch dependencies. The final argument, a
         * ShardRecordProcessorFactory, is where the logic for record processing lives, and is located in a private
         * class below.
         */
        DynamoDbAsyncClient dynamoClient = DynamoDbAsyncClient.builder().region(kinesisClient.region)
            .credentialsProvider(kinesisClient.credentialsProvider)
            .endpointOverride(URI.create("http://localhost:4566")).build();
        CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder().region(kinesisClient.region)
            .credentialsProvider(kinesisClient.credentialsProvider)
            .endpointOverride(URI.create("http://localhost:4566")).build();
        ConfigsBuilder configsBuilder = new ConfigsBuilder(kinesisClient.streamName, appName, kinesisClient.kinesisClient, dynamoClient,
            cloudWatchClient, UUID.randomUUID().toString(), new CloudEventRecordProcessorFactory());

        /*
         * The Scheduler (also called Worker in earlier versions of the KCL) is the entry point to the KCL. This
         * instance is configured with defaults provided by the ConfigsBuilder.
         */
        Scheduler scheduler = new Scheduler(
            configsBuilder.checkpointConfig(),
            configsBuilder.coordinatorConfig(),
            configsBuilder.leaseManagementConfig(),
            configsBuilder.lifecycleConfig(),
            configsBuilder.metricsConfig(),
            configsBuilder.processorConfig(),
            configsBuilder.retrievalConfig().retrievalSpecificConfig(new PollingConfig(kinesisClient.streamName, kinesisClient.kinesisClient))
        );

        /*
         * Kickoff the Scheduler. Record processing of the stream of dummy data will continue indefinitely
         * until an exit is triggered.
         */
        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.setDaemon(true);
        schedulerThread.start();

        log.info("Now just waiting for data");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down kinesis client");
            Future<Boolean> gracefulShutdownFuture = scheduler.startGracefulShutdown();
            if (gracefulShutdownFuture == null) {
                return;
            }
            log.info("Waiting up to 20 seconds for shutdown to complete.");
            try {
                gracefulShutdownFuture.get(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.info("Interrupted while waiting for graceful shutdown. Continuing.");
            } catch (ExecutionException e) {
                log.error("Exception while executing graceful shutdown.", e);
            } catch (TimeoutException e) {
                log.error("Timeout while waiting for shutdown.  Scheduler may not have exited.");
            }
            log.info("Completed, shutting down kinesis listener.");
        }));

        Thread.currentThread().join();
    }
}

public class KinesisListener {
    public static String STREAM = "cloudevents-stream";
    public static String REGION = "us-east-1";

    public static void main(String[] args) throws InterruptedException {
        new SimpleListener(STREAM, REGION).run();
    }
}
