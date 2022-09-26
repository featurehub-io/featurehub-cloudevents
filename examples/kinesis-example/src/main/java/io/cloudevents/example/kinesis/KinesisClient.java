package io.cloudevents.example.kinesis;

import org.apache.commons.lang3.ObjectUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.KinesisClientUtil;

import java.net.URI;

public class KinesisClient {
    public final String streamName;
    public final Region region;
    public final KinesisAsyncClient kinesisClient;
    public final AwsCredentialsProvider credentialsProvider = new AwsCredentialsProvider() {
        @Override
        public AwsCredentials resolveCredentials() {
            return AwsBasicCredentials.create("testing", "testing");
        }
    };

    public KinesisClient(String streamName, String region) {
        this.streamName = streamName;
        this.region = Region.of(ObjectUtils.firstNonNull(region, "us-east-1"));
        this.kinesisClient = KinesisClientUtil.createKinesisAsyncClient(
            KinesisAsyncClient.builder()
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create("http://localhost:4566")).region(this.region));
    }

}
