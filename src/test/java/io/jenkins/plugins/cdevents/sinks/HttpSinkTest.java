/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cdevents.CDEvents;
import dev.cdevents.constants.CDEventConstants;
import dev.cdevents.events.PipelineRunFinishedCDEvent;
import io.cloudevents.CloudEvent;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HttpSinkTest {

    @Test
    void serializeCloudEvent_returnsStructuredJson() throws Exception {
        PipelineRunFinishedCDEvent event = new PipelineRunFinishedCDEvent();
        event.setSource(URI.create("http://localhost:8080/jenkins/job/PipelineTest/1/"));
        event.setSubjectId("1");
        event.setSubjectSource(URI.create("http://localhost:8080/jenkins/job/PipelineTest/1/"));
        event.setSubjectPipelineName("unittest");
        event.setSubjectOutcome(CDEventConstants.Outcome.SUCCESS);
        event.setSubjectErrors("");
        event.setCustomDataContentType("application/json");
        event.setCustomData(Map.of(
                "name", "PipelineTest",
                "build", Map.of("number", 1)));

        CloudEvent cloudEvent = CDEvents.cdEventAsCloudEvent(event);

        String payload = HttpSink.serializeCloudEvent(cloudEvent);
        JsonNode root = new ObjectMapper().readTree(payload);

        assertFalse(payload.startsWith("CloudEvent{"));
        assertEquals("dev.cdevents.pipelinerun.finished.0.1.0", root.get("type").asText());
        assertEquals("0.3", root.get("specversion").asText());
        assertEquals("application/json", root.get("datacontenttype").asText());
        assertEquals("unittest", root.get("data").get("subject").get("content").get("pipelineName").asText());
        assertEquals(1, root.get("data").get("customData").get("build").get("number").asInt());
    }
}
