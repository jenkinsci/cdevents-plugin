/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.sinks;

import dev.cdevents.CDEvents;
import dev.cdevents.events.PipelineRunStartedCDEvent;
import io.cloudevents.CloudEvent;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SyslogSinkTest {

    @Test
    void sendCloudEvent_works() {
        SyslogSink syslogSink = new SyslogSink();

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
        CloudEvent cloudEvent = CDEvents.cdEventAsCloudEvent(event);

        assertDoesNotThrow(() -> syslogSink.sendCloudEvent(cloudEvent));
    }
}