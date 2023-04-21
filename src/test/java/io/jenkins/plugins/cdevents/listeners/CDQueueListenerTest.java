/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.listeners;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Queue;
import io.jenkins.plugins.cdevents.EventState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
        justification = "Tests are just checking that exceptions are not thrown. Feel free to add more robust tests")
@ExtendWith(MockitoExtension.class)
class CDQueueListenerTest {

    @Test
    void onEnterWaiting() {
        try (MockedStatic<FutureRunner> mockFutureRunner = mockStatic(FutureRunner.class)) {
            ArgumentCaptor<EventState> eventStateArgumentCaptor = ArgumentCaptor.forClass(EventState.class);
            ArgumentCaptor<Queue.WaitingItem> waitingItemArgumentCaptor = ArgumentCaptor
                    .forClass(Queue.WaitingItem.class);
            ArgumentCaptor<String> eventTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);

            Queue.WaitingItem mockItem = mock(Queue.WaitingItem.class);

            mockFutureRunner.when(() -> FutureRunner.captureEvent(eventStateArgumentCaptor.capture(),
                    waitingItemArgumentCaptor.capture(),
                    eventTypeArgumentCaptor.capture())).thenReturn(new CompletableFuture<Void>());

            CDQueueListener cdQueueListener = new CDQueueListener();
            cdQueueListener.onEnterWaiting(mockItem);

            assertEquals(EventState.QUEUED, eventStateArgumentCaptor.getValue());
            assertEquals(mockItem, waitingItemArgumentCaptor.getValue());
            assertEquals("enterWaiting", eventTypeArgumentCaptor.getValue());
        }
    }
}