/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.cdevents.sinks;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class KinesisSink extends CDEventsSink {

    public static final Logger LOGGER = LoggerFactory.getLogger(KinesisSink.class);
    private volatile static AmazonKinesis kinesis;
    private volatile static String streamName;
    private volatile static String region;
    private volatile static String endpoint;
    private volatile static String iamRole;

    public KinesisSink() {
        //Constructs a new instance of the KinesisSink
        if (Jenkins.get().getPlugin("aws-java-sdk") == null
                && Jenkins.get().getPlugin("aws-java-sdk-kinesis") == null) {
            throw new NoClassDefFoundError(
                    "Jenkins plugin aws-java-sdk " +
                            "or aws-java-sdk-kinesis " +
                            "must be installed to use Kinesis sink");
        }

        streamName = CDEventsGlobalConfig.get().getKinesisStreamName();
        if (StringUtils.isBlank(streamName)) {
            throw new NullPointerException(
                    "Kinesis stream name cannot be blank");
        }

        rebuildKinesisClient();
    }

    public static synchronized void nullifyKinesisClient() {
        // Sets the Kinesis client instance to null.
        kinesis = null;
    }

    public static synchronized void rebuildKinesisClient() {
        // Build and instantiate the Kinesis client
        if (kinesis == null) {
            streamName = CDEventsGlobalConfig.get()
                    .getKinesisStreamName().trim();
            region = CDEventsGlobalConfig.get().getKinesisRegion();
            endpoint = CDEventsGlobalConfig.get().getKinesisEndpoint();
            iamRole = CDEventsGlobalConfig.get().getIamRole();
            String roleSessionName = "cdevents-plugin";

            AmazonKinesisClientBuilder kinesisBuilder =
                    AmazonKinesisClientBuilder.standard();
            if (!StringUtils.isBlank(region)) {
                kinesisBuilder.withRegion(region);
            }
            if (!StringUtils.isBlank(region)) {
                AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                        new AwsClientBuilder.EndpointConfiguration(
                        endpoint, region);
                kinesisBuilder.withEndpointConfiguration(endpointConfiguration);
            }
            if (!StringUtils.isBlank(iamRole)) {
                STSAssumeRoleSessionCredentialsProvider credentialsProvider =
                        new STSAssumeRoleSessionCredentialsProvider.Builder(
                        iamRole, roleSessionName).build();
                kinesisBuilder.withCredentials(credentialsProvider);
            }

            LOGGER.info("Instantiating new Kinesis client" +
                            " {stream={}, region={}, endpoint={}, iamRole={}",
                    streamName, region, endpoint, iamRole);
            kinesis = kinesisBuilder.build();
        }
    }

    @Override
    public void sendCloudEvent(CloudEvent cloudEvent) {
        PutRecordRequest putRecordRequest = new PutRecordRequest();
        putRecordRequest.setStreamName(streamName);

        ByteBuffer serialized =
                ByteBuffer.wrap(new JsonFormat().serialize(cloudEvent));
        putRecordRequest.setData(serialized);

        putRecordRequest.setPartitionKey(cloudEvent.getType());

        PutRecordResult result = kinesis.putRecord(putRecordRequest);
        LOGGER.info("Kinesis putRecord result: {}", result);
    }
}
