package io.jenkins.plugins.cdevents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.cloudevents.CloudEvent;
import io.jenkins.plugins.cdevents.models.JobModel;
import io.jenkins.plugins.cdevents.util.ModelBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildCDEventTest {

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
            modelBuilder.when(() -> ModelBuilder.buildJobModel(job, run, taskListener)).thenReturn(new JobModel());
            when(run.getParent().getFullDisplayName()).thenReturn("TestJob1");
            when(run.getUrl()).thenReturn("http://localhost/job/1/stage/1");
            when(run.getId()).thenReturn("1");

            CloudEvent cloudEvent = BuildCDEvent.buildPipelineRunStartedModel(run, taskListener);

            assertEquals(Set.of("datacontenttype", "specversion", "id", "source", "time", "type"), cloudEvent.getAttributeNames());
            assertEquals("dev.cdevents.pipelinerun.started.0.1.0", cloudEvent.getType());
            // assert that the JSON string returned by cloudEvent.getData().toString() contains "pipelineName": "TestJob1" in its key-value pairs
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(cloudEvent.getData().toBytes());
            assertEquals("TestJob1", jsonNode.get("subject").get("content").get("pipelineName").asText());
        }
    }
}