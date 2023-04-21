/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.listeners;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;
import io.jenkins.plugins.cdevents.EventState;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/*
 * Note to self: https://javadoc.jenkins.io/hudson/model/queue/QueueListener.html
 * Queue is highly synchronized objects, and these callbacks are invoked synchronously. To avoid the risk of deadlocks
 * and general slow down, please minimize the amount of work callbacks will perform, and push any sizable work to
 * asynchronous execution via Executor, such as Computer.threadPoolForRemoting.
 * */

@SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE",
        justification = "CompletableFutures are stored so that we can dynamically determine if we want to run async or not (to be implemented later)")
@Extension
public class CDQueueListener extends QueueListener {

    private static final boolean RUN_ASYNC = true;

    public CDQueueListener() {
        super();
    }

    @Override
    @SuppressFBWarnings
    public void onEnterWaiting(Queue.WaitingItem wi) {
        CompletableFuture future = FutureRunner.captureEvent(EventState.QUEUED, wi, "enterWaiting");

        if (!RUN_ASYNC) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
