/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.listeners;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.plugins.cdevents.EventHandler;
import io.jenkins.plugins.cdevents.EventState;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE",
        justification = "CompletableFutures are stored so that we can dynamically determine if we want to run async or not (to be implemented later)")
@Extension
public class CDJobListener extends RunListener<Run> {

    private static final Logger LOGGER = Logger.getLogger("CDJobListener");

    public CDJobListener() {
        super();
    }

    @Override
    public void onStarted(Run run, TaskListener listener) {
        handleEvent(run, listener, EventState.STARTED);
    }

    @Override
    public void onCompleted(Run run, TaskListener listener) {
        handleEvent(run, listener, EventState.FINISHED);
    }

    private void handleEvent(Run run, TaskListener listener, EventState state) {
        try {
            EventHandler.captureEvent(state, run, listener, "pipelineRun");
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.WARNING, "Error while capturing event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
