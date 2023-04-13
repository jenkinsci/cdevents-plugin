/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents;

import hudson.model.Queue;
import io.cloudevents.CloudEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class EventHandlerTest {

    @Test
    void testpipelineRunQueuedEvent() {
        try (MockedStatic<BuildCDEvent> mockBuildCDEvent = mockStatic(BuildCDEvent.class);
             MockedStatic<CDEventsGlobalConfig> mockCDEventsGlobalConfig = mockStatic(CDEventsGlobalConfig.class,
                     Answers.RETURNS_DEEP_STUBS)) {
            CloudEvent mockCloudEvent = mock(CloudEvent.class);
            Queue.WaitingItem mockItem = mock(Queue.WaitingItem.class);
            mockBuildCDEvent.when(() -> BuildCDEvent.buildPipelineRunQueuedModel(any())).thenReturn(mockCloudEvent);
            mockCDEventsGlobalConfig.when(() -> CDEventsGlobalConfig.get().getSinkType()).thenReturn("syslog");

            assertDoesNotThrow(() -> EventHandler.captureEvent(EventState.QUEUED, mockItem, "enterWaiting"));
        }
    }
}