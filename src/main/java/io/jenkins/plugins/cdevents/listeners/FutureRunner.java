/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.listeners;

import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.cdevents.EventHandler;
import io.jenkins.plugins.cdevents.EventState;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class FutureRunner {

    public static CompletableFuture<Void> captureEvent(EventState eventState, Run run, TaskListener listener,
                                                       String eventType) {
        return CompletableFuture.runAsync(() -> {
            try {
                EventHandler.captureEvent(eventState, run, listener, eventType);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Void> captureEvent(EventState eventState, Queue.WaitingItem item,
                                                       String eventType) {
        return CompletableFuture.runAsync(() -> EventHandler.captureEvent(EventState.QUEUED, item, "enterWaiting"));
    }

    public static CompletableFuture<Void> captureEvent(FlowNode node) {
        return CompletableFuture.runAsync(() -> EventHandler.captureEvent(node));
    }
}
