/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.sinks;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;
import io.jenkins.plugins.cdevents.CDEventsGlobalConfig;
import io.jenkins.plugins.cdevents.CDEventsSink;
import jenkins.model.Jenkins;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class KinesisSink extends CDEventsSink {

    public static final Logger LOGGER = Logger.getLogger(KinesisSink.class.getName());
    private volatile static AmazonKinesis kinesis;
    private volatile static String streamName;
    private volatile static String region;
    private volatile static String endpoint;

    public KinesisSink() {
        if (Jenkins.get().getPlugin("aws-java-sdk") == null
                && Jenkins.get().getPlugin("aws-java-sdk-kinesis") == null) {
            throw new NoClassDefFoundError(
                    "Jenkins plugin aws-java-sdk or aws-java-sdk-kinesis must be installed to use Kinesis sink");
        }

        if (CDEventsGlobalConfig.get().getKinesisStreamName() == null
                && CDEventsGlobalConfig.get().getKinesisStreamName().trim().isEmpty()) {
            throw new NullPointerException("Kinesis stream name cannot be blank");
        }

        rebuildKinesisClient();
    }

    public static synchronized void nullifyKinesisClient() {
        kinesis = null;
    }

    public static synchronized void rebuildKinesisClient() {
        if (kinesis == null) {
            streamName = CDEventsGlobalConfig.get().getKinesisStreamName().trim();
            region = CDEventsGlobalConfig.get().getKinesisRegion();
            endpoint = CDEventsGlobalConfig.get().getKinesisEndpoint();

            AmazonKinesisClientBuilder kinesisBuilder = AmazonKinesisClientBuilder.standard();
            if (region != null && !region.isEmpty()) {
                kinesisBuilder.withRegion(region);
            }
            if (endpoint != null && !endpoint.isEmpty()) {
                AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(
                        endpoint, region);
                kinesisBuilder.withEndpointConfiguration(endpointConfiguration);
            }

            LOGGER.info(String.format("Instantiating new Kinesis client {stream=%s, region=%s, endpoint=%s}",
                    streamName, region, endpoint));
            kinesis = kinesisBuilder.build();
        }
    }

    @Override
    public void sendCloudEvent(CloudEvent cloudEvent) {
        PutRecordRequest putRecordRequest = new PutRecordRequest();
        putRecordRequest.setStreamName(streamName);

        ByteBuffer serialized = ByteBuffer.wrap(new JsonFormat().serialize(cloudEvent));
        putRecordRequest.setData(serialized);

        putRecordRequest.setPartitionKey(cloudEvent.getType());

        PutRecordResult result = kinesis.putRecord(putRecordRequest);
        LOGGER.info("Kinesis putRecord result: " + result.toString());
    }
}
