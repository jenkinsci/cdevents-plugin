/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.cdevents.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.cdevents.EventHandler;
import io.jenkins.plugins.cdevents.EventState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressFBWarnings(
        value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
        justification = "Tests are just checking that exceptions are not thrown. Feel free to add more robust tests")
@ExtendWith(MockitoExtension.class)
public class CDJobListenerTest {

    @Mock
    Run mockRun;

    @Mock
    TaskListener mockListener;

    @BeforeEach
    void setup() {
        reset(mockRun, mockListener);
    }

    @Test
    void testCDJobListenerOnStarted() {
        try (MockedStatic<EventHandler> mockEventHandler = mockStatic(EventHandler.class)) {
            ArgumentCaptor<EventState> eventStateArgumentCaptor = ArgumentCaptor.forClass(EventState.class);
            ArgumentCaptor<Run> runArgumentCaptor = ArgumentCaptor.forClass(Run.class);
            ArgumentCaptor<TaskListener> taskListenerArgumentCaptor = ArgumentCaptor.forClass(TaskListener.class);
            ArgumentCaptor<String> eventTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);

            mockEventHandler
                    .when(() -> EventHandler.captureEvent(
                            eventStateArgumentCaptor.capture(),
                            runArgumentCaptor.capture(),
                            taskListenerArgumentCaptor.capture(),
                            eventTypeArgumentCaptor.capture()))
                    .then(foo -> null);

            CDJobListener cdJobListener = new CDJobListener();
            cdJobListener.onStarted(mockRun, mockListener);

            assertEquals(EventState.STARTED, eventStateArgumentCaptor.getValue());
            assertEquals(mockRun, runArgumentCaptor.getValue());
            assertEquals(mockListener, taskListenerArgumentCaptor.getValue());
            assertEquals("pipelineRun", eventTypeArgumentCaptor.getValue());
        }
    }

    @Test
    void testCDJobListenerOnCompleted() {
        try (MockedStatic<EventHandler> mockEventHandler = mockStatic(EventHandler.class)) {
            ArgumentCaptor<EventState> eventStateArgumentCaptor = ArgumentCaptor.forClass(EventState.class);
            ArgumentCaptor<Run> runArgumentCaptor = ArgumentCaptor.forClass(Run.class);
            ArgumentCaptor<TaskListener> taskListenerArgumentCaptor = ArgumentCaptor.forClass(TaskListener.class);
            ArgumentCaptor<String> eventTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);

            mockEventHandler
                    .when(() -> EventHandler.captureEvent(
                            eventStateArgumentCaptor.capture(),
                            runArgumentCaptor.capture(),
                            taskListenerArgumentCaptor.capture(),
                            eventTypeArgumentCaptor.capture()))
                    .then(foo -> null);

            CDJobListener cdJobListener = new CDJobListener();
            cdJobListener.onCompleted(mockRun, mockListener);

            assertEquals(EventState.FINISHED, eventStateArgumentCaptor.getValue());
            assertEquals(mockRun, runArgumentCaptor.getValue());
            assertEquals(mockListener, taskListenerArgumentCaptor.getValue());
            assertEquals("pipelineRun", eventTypeArgumentCaptor.getValue());
        }
    }

    @Test
    void testCDJobListenerOnError() {
        try (MockedStatic<EventHandler> mockEventHandler = mockStatic(EventHandler.class)) {
            ArgumentCaptor<EventState> eventStateArgumentCaptor = ArgumentCaptor.forClass(EventState.class);
            ArgumentCaptor<Run> runArgumentCaptor = ArgumentCaptor.forClass(Run.class);
            ArgumentCaptor<TaskListener> taskListenerArgumentCaptor = ArgumentCaptor.forClass(TaskListener.class);
            ArgumentCaptor<String> eventTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);

            mockEventHandler
                    .when(() -> EventHandler.captureEvent(
                            eventStateArgumentCaptor.capture(),
                            runArgumentCaptor.capture(),
                            taskListenerArgumentCaptor.capture(),
                            eventTypeArgumentCaptor.capture()))
                    .thenThrow(new InterruptedException());

            CDJobListener cdJobListener = new CDJobListener();

            try {
                cdJobListener.onCompleted(mockRun, mockListener);
            } catch (Exception e) {
                assertEquals(InterruptedException.class, e.getClass());
            }

        }
    }
}
