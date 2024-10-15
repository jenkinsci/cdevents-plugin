package io.jenkins.plugins.cdevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cdevents.constants.CDEventConstants;
import hudson.model.*;
import io.cloudevents.CloudEvent;
import io.jenkins.plugins.cdevents.models.JobModel;
import io.jenkins.plugins.cdevents.util.ModelBuilder;
import java.io.IOException;
import java.util.Calendar;
import java.util.Set;

import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BuildCDEventTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Run run;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TaskListener taskListener;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Job job;

    @BeforeEach
    void setUp() {
        reset(run, taskListener, job);
    }

    private MockedStatic<ModelBuilder> getMockedModelBuilder() {
        return mockStatic(ModelBuilder.class);
    }

    /**
     * This test asserts that BuildCDEvent::buildPipelineRunStartedModel returns a
     * CloudEvents object with attention to the type (Pipeline Run Started) and the
     * pipeline name
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    void buildPipelineRunStartedModel() throws IOException, InterruptedException {
        try (MockedStatic<ModelBuilder> modelBuilder = getMockedModelBuilder()) {
            modelBuilder
                    .when(() -> ModelBuilder.buildJobModel(job, run, taskListener))
                    .thenReturn(new JobModel());
            when(run.getParent().getFullDisplayName()).thenReturn("TestJob1");
            when(run.getUrl()).thenReturn("http://localhost/job/1/stage/1");
            when(run.getId()).thenReturn("1");

            CloudEvent cloudEvent = BuildCDEvent.buildPipelineRunStartedModel(run, taskListener);

            assertEquals(
                    Set.of("datacontenttype", "specversion", "id", "source", "time", "type"),
                    cloudEvent.getAttributeNames());
            assertEquals("dev.cdevents.pipelinerun.started.0.1.0", cloudEvent.getType());
            // assert that the JSON string returned by cloudEvent.getData().toString() contains "pipelineName":
            // "TestJob1" in its key-value pairs
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(cloudEvent.getData().toBytes());
            assertEquals(
                    "TestJob1",
                    jsonNode.get("subject").get("content").get("pipelineName").asText());
        }
    }

    /**
     * This test asserts that BuildCDEvent::buildPipelineRunFinishedModel returns a
     * CloudEvents object with attention to the type (Pipeline Run Finished) and the
     * pipeline name
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    void buildPipelineRunFinishedModel() throws IOException, InterruptedException {
        try (MockedStatic<ModelBuilder> modelBuilder = getMockedModelBuilder()) {
            modelBuilder
                    .when(() -> ModelBuilder.buildJobModel(job, run, taskListener))
                    .thenReturn(new JobModel());
            when(run.getParent().getFullDisplayName()).thenReturn("TestJob1");
            when(run.getUrl()).thenReturn("http://localhost/job/1/stage/1");
            when(run.getId()).thenReturn("1");

            CloudEvent cloudEvent = BuildCDEvent.buildPipelineRunFinishedModel(run, taskListener);

            assertEquals(
                    Set.of("datacontenttype", "specversion", "id", "source", "time", "type"),
                    cloudEvent.getAttributeNames());
            assertEquals("dev.cdevents.pipelinerun.finished.0.1.0", cloudEvent.getType());
            // assert that the JSON string returned by cloudEvent.getData().toString() contains "pipelineName":
            // "TestJob1" in its key-value pairs
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(cloudEvent.getData().toBytes());
            assertEquals(
                    "TestJob1",
                    jsonNode.get("subject").get("content").get("pipelineName").asText());
        }
    }

    /**
     * This test asserts that BuildCDEvent::buildPipelineRunFinishedModel handles
     * success as expected (setting the Outcome to Success)
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    void buildPipelineRunFinishedModelSuccess() throws IOException, InterruptedException {
        try (MockedStatic<ModelBuilder> modelBuilder = getMockedModelBuilder()) {
            modelBuilder
                    .when(() -> ModelBuilder.buildJobModel(job, run, taskListener))
                    .thenReturn(new JobModel());
            when(run.getParent().getFullDisplayName()).thenReturn("TestJob1");
            when(run.getUrl()).thenReturn("http://localhost/job/1/stage/1");
            when(run.getId()).thenReturn("1");
            when(run.getResult()).thenReturn(Result.SUCCESS);

            CloudEvent cloudEvent = BuildCDEvent.buildPipelineRunFinishedModel(run, taskListener);

            assertEquals(
                    Set.of("datacontenttype", "specversion", "id", "source", "time", "type"),
                    cloudEvent.getAttributeNames());
            assertEquals("dev.cdevents.pipelinerun.finished.0.1.0", cloudEvent.getType());
            // assert that the JSON string returned by cloudEvent.getData().toString() contains "pipelineName":
            // "TestJob1" in its key-value pairs
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(cloudEvent.getData().toBytes());
            assertEquals(
                    "TestJob1",
                    jsonNode.get("subject").get("content").get("pipelineName").asText());
            assertEquals(
                    "SUCCESS",
                    jsonNode.get("subject").get("content").get("outcome").asText());
        }
    }

    /**
     * This test asserts that BuildCDEvent::buildTaskRunStartedModel returns a
     * CloudEvents object with attention to the type (Task Run Started) and the
     * task name
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    void buildTaskRunStartedModel() throws IOException, InterruptedException {
        try (MockedStatic<ModelBuilder> modelBuilder = getMockedModelBuilder();
             MockedStatic<EventHandler> mockEventHandler = mockStatic(EventHandler.class)) {

            modelBuilder
                    .when(() -> ModelBuilder.buildJobModel(job, run, taskListener))
                    .thenReturn(new JobModel());
            when(run.getParent().getFullDisplayName()).thenReturn("TestJob1");
            when(run.getUrl()).thenReturn("http://localhost/job/1/stage/1");
            when(run.getId()).thenReturn("1");

            ArgumentCaptor<FlowNode> argumentCaptor = ArgumentCaptor.forClass(FlowNode.class);
            StepStartNode mockNode = mock(StepStartNode.class);

            mockEventHandler
                    .when(() -> EventHandler.captureEvent(argumentCaptor.capture()))
                    .thenAnswer(Answers.RETURNS_DEFAULTS);

            CloudEvent cloudEvent = BuildCDEvent.buildTaskRunStartedModel(run, mockNode);

            assertEquals("dev.cdevents.taskrun.started.0.1.0", cloudEvent.getType());
            // assert that the JSON string returned by cloudEvent.getData().toString() contains "pipelineName":
            // "TestJob1" in its key-value pairs
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(cloudEvent.getData().toBytes());
            assertEquals(
                    "TestJob1",
                    jsonNode.get("subject").get("content").get("taskName").asText());
        }
    }

    /**
     * This test asserts that BuildCDEvent::buildTaskRunFinishedModel returns a
     * CloudEvents object with attention to the type (Task Run Finished) and the
     * task name
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    void buildTaskRunFinishedModel() throws IOException, InterruptedException {
        try (MockedStatic<ModelBuilder> modelBuilder = getMockedModelBuilder();
             MockedStatic<EventHandler> mockEventHandler = mockStatic(EventHandler.class)) {

            modelBuilder
                    .when(() -> ModelBuilder.buildJobModel(job, run, taskListener))
                    .thenReturn(new JobModel());
            when(run.getParent().getFullDisplayName()).thenReturn("TestJob1");
            when(run.getUrl()).thenReturn("http://localhost/job/1/stage/1");
            when(run.getId()).thenReturn("1");

            ArgumentCaptor<FlowNode> argumentCaptor = ArgumentCaptor.forClass(FlowNode.class);
            StepEndNode mockNode = mock(StepEndNode.class);

            mockEventHandler
                    .when(() -> EventHandler.captureEvent(argumentCaptor.capture()))
                    .thenAnswer(Answers.RETURNS_DEFAULTS);

            CloudEvent cloudEvent = BuildCDEvent.buildTaskRunFinishedModel(run, mockNode);

            assertEquals("dev.cdevents.taskrun.finished.0.1.0", cloudEvent.getType());
            // assert that the JSON string returned by cloudEvent.getData().toString() contains "pipelineName":
            // "TestJob1" in its key-value pairs
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(cloudEvent.getData().toBytes());
            assertEquals(
                    "TestJob1",
                    jsonNode.get("subject").get("content").get("taskName").asText());
        }
    }
}
