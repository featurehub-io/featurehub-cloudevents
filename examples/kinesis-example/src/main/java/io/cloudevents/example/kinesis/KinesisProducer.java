package io.cloudevents.example.kinesis;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.kinesis.KinesisMessageFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

class SimpleProducer {
    private final KinesisClient client;
    private static final Logger log = LoggerFactory.getLogger(SimpleProducer.class);

    public SimpleProducer(String streamName, String region) {
        client = new KinesisClient(streamName, region);
    }

    public void run() throws URISyntaxException {
        URI source = new URI("http://localhost");
        for(int count = 0; count < 10; count ++) {
            CloudEvent event = CloudEventBuilder.v1().withTime(OffsetDateTime.now())
                .withType("counter-record-v1")
                .withId(Integer.toString(count))
                .withSource(source)
                .withData("text/plain", Integer.toString(count).getBytes(StandardCharsets.UTF_8)).build();

            try {
                client.kinesisClient.putRecord(KinesisMessageFactory.createWriter().writeBinary(event)
                    .partitionKey(UUID.randomUUID().toString())  // if you have two or more clients, making it random will cause load balancing
//                    .partitionKey("cloud-event")  // if this is the same, they come out in order.
                    .streamName(client.streamName)
                    .build()).get();
            } catch (InterruptedException e) {
                log.info("Interrupted, assuming shutdown.");
            } catch (ExecutionException e) {
                log.error("Exception while sending data to Kinesis. Will try again next cycle.", e);
            }
        }
    }
}

public class KinesisProducer {
    public static String STREAM = "cloudevents-stream";
    public static String REGION = "us-east-1";

    public static void main(String[] args) throws URISyntaxException {
        new SimpleProducer(STREAM, REGION).run();
    }
}
