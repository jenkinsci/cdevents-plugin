/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import dev.cdevents.CDEvents;
import dev.cdevents.constants.CDEventConstants;
import dev.cdevents.events.PipelineRunFinishedCDEvent;
import io.cloudevents.CloudEvent;
import io.jenkins.plugins.cdevents.CDEventsGlobalConfig;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class HttpSinkTest {

    @Test
    void serializeCloudEvent_returnsStructuredJson() throws Exception {
        CloudEvent cloudEvent = pipelineRunFinishedEvent();

        String payload = HttpSink.serializeCloudEvent(cloudEvent);
        JsonNode root = new ObjectMapper().readTree(payload);

        assertFalse(payload.startsWith("CloudEvent{"));
        assertEquals("dev.cdevents.pipelinerun.finished.0.1.0", root.get("type").asText());
        assertEquals("0.3", root.get("specversion").asText());
        assertEquals("application/json", root.get("datacontenttype").asText());
        assertEquals("unittest", root.get("data").get("subject").get("content").get("pipelineName").asText());
        assertEquals(1, root.get("data").get("customData").get("build").get("number").asInt());
    }

    @Test
    void sendCloudEvent_usesHttpStatusForPlainTextResponse() throws Exception {
        AtomicInteger status = new AtomicInteger(200);
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        Jenkins jenkins = mock(Jenkins.class);
        CDEventsGlobalConfig config = mock(CDEventsGlobalConfig.class);
        try (MockedStatic<Jenkins> mockJenkins = mockStatic(Jenkins.class);
             MockedStatic<CDEventsGlobalConfig> mockConfig = mockStatic(CDEventsGlobalConfig.class)) {
            mockJenkins.when(Jenkins::get).thenReturn(jenkins);
            mockConfig.when(CDEventsGlobalConfig::get).thenReturn(config);
            when(config.getHttpSinkUrl()).thenReturn("http://localhost:" + server.getAddress().getPort() + "/");

            assertDoesNotThrow(() -> new HttpSink().sendCloudEvent(pipelineRunFinishedEvent()));

            status.set(500);
            IOException error = assertThrows(IOException.class,
                    () -> new HttpSink().sendCloudEvent(pipelineRunFinishedEvent()));
            assertEquals("HTTP sink endpoint returned HTTP 500", error.getMessage());
        } finally {
            server.stop(0);
        }
    }

    private static CloudEvent pipelineRunFinishedEvent() {
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
        return CDEvents.cdEventAsCloudEvent(event);
    }
}
