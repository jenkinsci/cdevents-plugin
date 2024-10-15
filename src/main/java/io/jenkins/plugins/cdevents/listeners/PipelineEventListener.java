/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.listeners;

import hudson.Extension;
import io.jenkins.plugins.cdevents.EventHandler;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

@Extension
public class PipelineEventListener implements GraphListener {

    public PipelineEventListener() {
        super();
    }

    @Override
    public void onNewHead(FlowNode node) {
        if (node instanceof StepStartNode || node instanceof StepEndNode) {
            EventHandler.captureEvent(node);
        }
    }
}