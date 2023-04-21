/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.sinks;

import dev.cdevents.CDEventTypes;
import dev.cdevents.constants.CDEventConstants;
import io.cloudevents.CloudEvent;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SyslogSinkTest {

    private final CloudEvent cloudEvent = CDEventTypes.createPipelineRunStartedEvent(
            CDEventConstants.CDEventTypes.PipelineRunStartedEvent.getEventType(),
            "1",
            URI.create("http://localhost:8080/jenkins/job/PipelineTest/1/"),
            "unittest",
            URI.create("http://localhost:8080/jenkins/job/PipelineTest/1/"),
            "{\"userId\":null,\"userName\":null,\"name\":\"PipelineTest\",\"displayName\":" +
                    "\"PipelineTest\",\"url\":\"job/PipelineTest/\",\"build\":{\"fullUrl\":\"http://local" +
                    "host:8080/jenkins/job/PipelineTest/16/\",\"number\":16,\"queueId\":8,\"duration\":0," +
                    "\"status\":null,\"url\":\"job/PipelineTest/16/\",\"displayName\":null,\"parameters\"" +
                    ":null,\"scmState\":{\"url\":null,\"branch\":null,\"commit\":null}}}");

    @Test
    void sendCloudEvent_works() {
        SyslogSink syslogSink = new SyslogSink();
        assertDoesNotThrow(() -> syslogSink.sendCloudEvent(cloudEvent));
    }
}