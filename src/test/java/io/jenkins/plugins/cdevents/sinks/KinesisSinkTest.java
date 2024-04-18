/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.sinks;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import dev.cdevents.CDEvents;
import dev.cdevents.events.PipelineRunStartedCDEvent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Plugin;
import io.cloudevents.CloudEvent;
import io.jenkins.plugins.cdevents.CDEventsGlobalConfig;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressFBWarnings(value = {"RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
        "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"},
        justification = "Tests are just checking that exceptions are not thrown. Feel free to add more robust tests")
@ExtendWith(MockitoExtension.class)
class KinesisSinkTest {

    private CloudEvent cloudEvent;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AmazonKinesis mockKinesisClient;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AmazonKinesisClientBuilder mockClientBuilder;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Jenkins mockJenkins;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CDEventsGlobalConfig mockGlobalConfig;

    @BeforeEach
    void setup() {
        reset(mockClientBuilder, mockKinesisClient, mockJenkins, mockGlobalConfig);

        /*when creating new object of any CDEvent type, the event will be initialized with
        context.id, context.type, context.version, context.timestamp and subject.type */
        PipelineRunStartedCDEvent event = new PipelineRunStartedCDEvent();

        /* set the required context fields to the PipelineRunStartedCDEvent */
        event.setSource(URI.create("http://localhost:8080/jenkins/job/PipelineTest/1/"));

        /* set the required subject fields to the PipelineRunStartedCDEvent */
        event.setSubjectId("1");
        event.setSubjectSource(URI.create("http://localhost:8080/jenkins/job/PipelineTest/1/"));
        event.setSubjectUrl(URI.create("http://localhost:8080/jenkins/job/PipelineTest/1/"));
        event.setSubjectPipelineName("unittest");

        event.setCustomDataContentType("application/json");
        event.setCustomData("{\"userId\":null,\"userName\":null,\"name\":\"PipelineTest\",\"displayName\":" +
                "\"PipelineTest\",\"url\":\"job/PipelineTest/\",\"build\":{\"fullUrl\":\"http://local" + "host:8080" + "/jenkins/job/PipelineTest/16/\",\"number\":16,\"queueId\":8,\"duration\":0," + "\"status\":null," + "\"url\":\"job/PipelineTest/16/\",\"displayName\":null,\"parameters\"" + ":null,\"scmState\":{\"url" + "\":null,\"branch\":null,\"commit\":null}}}");

        /* Create a CloudEvent from a PipelineRunStartedCDEvent */
        cloudEvent = CDEvents.cdEventAsCloudEvent(event);
    }

    private MockedStatic<CDEventsGlobalConfig> getMockCDEventsGlobalConfigStatic() {
        return mockStatic(CDEventsGlobalConfig.class, Answers.RETURNS_DEEP_STUBS);
    }

    private void configureMockCDEventsGlobalConfigStatic(MockedStatic<CDEventsGlobalConfig> mockCDEventsGlobalConfigStatic) {
        mockCDEventsGlobalConfigStatic.when(CDEventsGlobalConfig::get).thenReturn(mockGlobalConfig);
        when(mockGlobalConfig.getKinesisStreamName()).thenReturn("hello");
    }

    private MockedStatic<AmazonKinesisClientBuilder> getMockClientBuilderStatic() {
        return mockStatic(AmazonKinesisClientBuilder.class, Answers.RETURNS_DEEP_STUBS);
    }

    private void configureMockClientBuilderStatic(MockedStatic<AmazonKinesisClientBuilder> mockClientBuilderStatic) {
        mockClientBuilderStatic.when(AmazonKinesisClientBuilder::standard).thenReturn(mockClientBuilder);
        when(mockClientBuilder.withRegion(anyString())).thenReturn(mockClientBuilder);
        when(mockClientBuilder.withEndpointConfiguration(any())).thenReturn(mockClientBuilder);
        when(mockClientBuilder.build()).thenReturn(mockKinesisClient);
    }

    private MockedStatic<Jenkins> getMockJenkinsStatic() {
        return mockStatic(Jenkins.class, Answers.RETURNS_DEEP_STUBS);
    }

    private void configureMockJenkinsStatic(MockedStatic<Jenkins> mockJenkinsStatic) {
        mockJenkinsStatic.when(Jenkins::get).thenReturn(mockJenkins);
        when(mockJenkins.getPlugin(anyString())).thenReturn(new Plugin.DummyImpl());
    }

    @Test
    void sinkTest() {
        try (MockedStatic<Jenkins> mockJenkinsStatic = getMockJenkinsStatic(); MockedStatic<CDEventsGlobalConfig> mockCDEventsGlobalConfigStatic = getMockCDEventsGlobalConfigStatic(); MockedStatic<AmazonKinesisClientBuilder> mockClientBuilderStatic = getMockClientBuilderStatic()) {
            configureMockJenkinsStatic(mockJenkinsStatic);
            configureMockCDEventsGlobalConfigStatic(mockCDEventsGlobalConfigStatic);
            configureMockClientBuilderStatic(mockClientBuilderStatic);

            KinesisSink sink = new KinesisSink();
            assertDoesNotThrow(() -> sink.sendCloudEvent(cloudEvent));
        }
    }

    @Test
    void constructorFailsNoPlugin() {
        try (MockedStatic<Jenkins> mockJenkinsStatic = getMockJenkinsStatic()) {
            configureMockJenkinsStatic(mockJenkinsStatic);

            reset(mockJenkins);
            when(mockJenkins.getPlugin(anyString())).thenReturn(null);

            assertThrows(NoClassDefFoundError.class, () -> new KinesisSink());
        }
    }

    @Test
    void constructorFailsNoStreamName() {
        try (MockedStatic<Jenkins> mockJenkinsStatic = getMockJenkinsStatic(); MockedStatic<CDEventsGlobalConfig> mockCDEventsGlobalConfigStatic = getMockCDEventsGlobalConfigStatic()) {
            configureMockJenkinsStatic(mockJenkinsStatic);
            configureMockCDEventsGlobalConfigStatic(mockCDEventsGlobalConfigStatic);

            reset(mockGlobalConfig);
            when(mockGlobalConfig.getKinesisStreamName()).thenReturn(null);

            assertThrows(NullPointerException.class, () -> new KinesisSink());
        }
    }

    @Test
    void constructorFailsEmptyStreamName() {
        try (MockedStatic<Jenkins> mockJenkinsStatic = getMockJenkinsStatic(); MockedStatic<CDEventsGlobalConfig> mockCDEventsGlobalConfigStatic = getMockCDEventsGlobalConfigStatic()){
            configureMockJenkinsStatic(mockJenkinsStatic);
            configureMockCDEventsGlobalConfigStatic(mockCDEventsGlobalConfigStatic);

            reset(mockGlobalConfig);
            when(mockGlobalConfig.getKinesisStreamName()).thenReturn("");

            assertThrows(NullPointerException.class, () -> new KinesisSink());
        }
    }
}