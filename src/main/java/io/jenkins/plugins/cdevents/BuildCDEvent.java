/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents;

import dev.cdevents.CDEvents;
import dev.cdevents.constants.CDEventConstants;
import dev.cdevents.events.*;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.cloudevents.CloudEvent;
import io.jenkins.plugins.cdevents.models.JobModel;
import io.jenkins.plugins.cdevents.models.QueuedJobModel;
import io.jenkins.plugins.cdevents.models.StageModel;
import io.jenkins.plugins.cdevents.util.ModelBuilder;
import io.jenkins.plugins.cdevents.util.OutcomeMapper;
import org.jenkinsci.plugins.workflow.actions.ErrorAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildCDEvent {

    private static final Logger LOGGER = Logger.getLogger("BuildCDEvent");

    public static CloudEvent buildPipelineRunStartedModel(Run run,
                                                          TaskListener listener) throws IOException,
            InterruptedException {
        String pipelineFullName = run.getParent().getFullDisplayName();
        JobModel pipelineData = ModelBuilder.buildJobModel(run.getParent(), run, listener);
        LOGGER.log(Level.INFO, "Building PipelineRunStarted model for " + pipelineFullName);

        PipelineRunStartedCDEvent event = new PipelineRunStartedCDEvent();
        event.setSubjectSource(URI.create(run.getParent()
                .getUrl()
                .replaceAll(pipelineFullName, "")
                .replaceAll("//", "/")));
        event.setSubjectId(run.getId());
        event.setSource(URI.create(run.getUrl()));
        event.setSubjectPipelineName(pipelineFullName);
        event.setSubjectUrl(URI.create(run.getUrl()));
        event.setCustomData(pipelineData);
        event.setCustomDataContentType("application/json");

        return CDEvents.cdEventAsCloudEvent(event);
    }

    public static CloudEvent buildPipelineRunFinishedModel(Run run,
                                                           TaskListener listener) throws IOException,
            InterruptedException {
        String pipelineFullName = run.getParent().getFullDisplayName();
        JobModel pipelineData = ModelBuilder.buildJobModel(run.getParent(), run, listener);
        LOGGER.log(Level.INFO, "Building PipelineRunFinished model for " + pipelineFullName);

        String errors;
        CDEventConstants.Outcome outcome;
        Result runResult = run.getResult();
        if (runResult != null) {
            outcome = OutcomeMapper.mapResultToOutcome(runResult);
            errors = outcome == CDEventConstants.Outcome.SUCCESS ? "" : run.getBuildStatusSummary().toString();
        } else {
            outcome = CDEventConstants.Outcome.ERROR;
            errors = "Run was not able to produce a result.";
        }

        PipelineRunFinishedCDEvent event = new PipelineRunFinishedCDEvent();

        event.setSubjectSource(URI.create(run.getParent()
                .getUrl()
                .replaceAll(pipelineFullName, "")
                .replaceAll("//", "/")));
        event.setSubjectId(run.getId());
        event.setSource(URI.create(run.getUrl()));
        event.setSubjectPipelineName(pipelineFullName);
        event.setCustomData(pipelineData);
        event.setCustomDataContentType("application/json");
        event.setSubjectOutcome(outcome);
        event.setSubjectErrors(errors);

        return CDEvents.cdEventAsCloudEvent(event);
    }

    public static CloudEvent buildPipelineRunQueuedModel(Queue.WaitingItem item) {
        String pipelineFullName = item.task.getFullDisplayName();
        QueuedJobModel pipelineData = ModelBuilder.buildQueuedJobModel(item);
        LOGGER.log(Level.INFO, "Building PipelineRunQueued model for " + pipelineFullName);

        PipelineRunQueuedCDEvent event = new PipelineRunQueuedCDEvent();
        event.setSubjectSource(URI.create(item.task.getUrl()));
        event.setSubjectId(String.valueOf(item.getId()));
        event.setSource(URI.create(item.task.getUrl()));
        event.setSubjectPipelineName(pipelineFullName);
        event.setCustomData(pipelineData);
        event.setCustomDataContentType("application/json");

        return CDEvents.cdEventAsCloudEvent(event);
    }

    public static CloudEvent buildTaskRunStartedModel(Run run, FlowNode node) {
        String displayName = run.getParent().getFullDisplayName();
        StageModel taskRunData = ModelBuilder.buildTaskModel(run, node);
        LOGGER.info("Building TaskRunStarted model for " + displayName);

        TaskRunStartedCDEvent event = new TaskRunStartedCDEvent();

        event.setSubjectSource(URI.create(run.getParent().getUrl().replaceAll(displayName, "").replaceAll("//", "/")));
        event.setSource(URI.create(run.getUrl()));
        event.setSubjectId(run.getId());
        event.setSubjectTaskName(displayName);
        event.setSubjectPipelineRunId(run.getId());
        event.setSubjectPipelineRunSource(URI.create(run.getUrl()));
        event.setCustomData(taskRunData);
        event.setCustomDataContentType("application/json");

        return CDEvents.cdEventAsCloudEvent(event);
    }

    public static CloudEvent buildTaskRunFinishedModel(Run run, FlowNode node) {
        String displayName = run.getParent().getFullDisplayName();
        StageModel taskRunData = ModelBuilder.buildTaskModel(run, node);

        String errors;
        CDEventConstants.Outcome outcome;
        ErrorAction nodeError = node.getError();
        if (nodeError != null) {
            outcome = OutcomeMapper.mapResultToOutcome(nodeError);
            errors = outcome == CDEventConstants.Outcome.SUCCESS ? "" : nodeError.getDisplayName();
        } else {
            outcome = CDEventConstants.Outcome.ERROR;
            errors = "Unable to get Display Name of the Node Error.";
        }

        LOGGER.info("Building TaskRunFinished model for " + displayName);

        TaskRunFinishedCDEvent event = new TaskRunFinishedCDEvent();
        event.setSubjectSource(URI.create(run.getParent().getUrl().replaceAll(displayName, "").replaceAll("//", "/")));
        event.setSource(URI.create(run.getUrl()));
        event.setSubjectId(run.getId());
        event.setSubjectTaskName(displayName);
        event.setSubjectPipelineRunId(run.getId());
        event.setSubjectPipelineRunSource(URI.create(run.getUrl()));
        event.setCustomData(taskRunData);
        event.setCustomDataContentType("application/json");

        event.setSubjectOutcome(outcome);
        event.setSubjectErrors(errors);

        return CDEvents.cdEventAsCloudEvent(event);
    }
}
