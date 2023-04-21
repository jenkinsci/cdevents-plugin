/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cdevents.CDEventTypes;
import dev.cdevents.constants.CDEventConstants;
import dev.cdevents.models.PipelineRun;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.cloudevents.CloudEvent;
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

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String convertToJson(Object object) {
        String convertedJson = "";
        try {
            convertedJson = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.WARNING,
                    "Failed to convert the build object into JSON with the following error " + e.getMessage());
            e.printStackTrace();
        }
        return convertedJson;
    }

    public static CloudEvent buildPipelineRunStartedModel(Run run, TaskListener listener)
            throws IOException, InterruptedException {
        String pipelineFullName = run.getParent().getFullDisplayName();
        Object pipelineData = ModelBuilder.buildJobModel(run.getParent(), run, listener);
        LOGGER.log(Level.INFO, "Building PipelineRunStarted model for " + pipelineFullName);
        return CDEventTypes.createPipelineRunStartedEvent(
                CDEventConstants.CDEventTypes.PipelineRunStartedEvent.getEventType(), run.getId(),
                URI.create(run.getParent().getUrl().replaceAll(pipelineFullName, "").replaceAll("//", "/")),
                pipelineFullName, URI.create(run.getUrl()), convertToJson(pipelineData));
    }

    public static CloudEvent buildPipelineRunFinishedModel(Run run, TaskListener listener)
            throws IOException, InterruptedException {
        String pipelineFullName = run.getParent().getFullDisplayName();
        Object pipelineData = ModelBuilder.buildJobModel(run.getParent(), run, listener);
        LOGGER.log(Level.INFO, "Building PipelineRunFinished model for " + pipelineFullName);

        String errors;
        CDEventConstants.Outcome outcome;
        Result runResult = run.getResult();
        if (runResult != null) {
            outcome = OutcomeMapper
                    .mapResultToOutcome(runResult);
            errors = outcome == CDEventConstants.Outcome.OutcomeSuccess ? ""
                    : run.getBuildStatusSummary().toString();
        } else {
            outcome = CDEventConstants.Outcome.OutcomeError;
            errors = "Run was not able to produce a result.";
        }

        return CDEventTypes.createPipelineRunFinishedEvent(
                CDEventConstants.CDEventTypes.PipelineRunFinishedEvent.getEventType(), run.getId(),
                URI.create(run.getParent().getUrl().replaceAll(pipelineFullName, "").replaceAll("//", "/")),
                pipelineFullName, URI.create(run.getUrl()), outcome, errors, convertToJson(pipelineData));
    }

    public static CloudEvent buildPipelineRunQueuedModel(Queue.WaitingItem item) {
        String pipelineFullName = item.task.getFullDisplayName();
        Object pipelineData = ModelBuilder.buildQueuedJobModel(item);
        LOGGER.log(Level.INFO, "Building PipelineRunQueued model for " + pipelineFullName);
        // String eventType, String id, URI source, String pipelineName, URI url, String
        // pipelineRunData
        return CDEventTypes.createPipelineRunQueuedEvent(
                CDEventConstants.CDEventTypes.PipelineRunQueuedEvent.getEventType(), String.valueOf(item.getId()),
                URI.create(item.task.getUrl()), pipelineFullName, URI.create(item.task.getUrl()),
                convertToJson(pipelineData));
    }

    public static CloudEvent buildTaskRunStartedModel(Run run, FlowNode node) {
        String displayName = run.getParent().getFullDisplayName();
        Object taskRunData = ModelBuilder.buildTaskModel(run, node);
        LOGGER.info("Building TaskRunStarted model for " + displayName);

        return CDEventTypes.createTaskRunStartedEvent(
                CDEventConstants.CDEventTypes.TaskRunStartedEvent.getEventType(),
                run.getId(),
                URI.create(run.getParent().getUrl().replaceAll(displayName, "").replaceAll("//", "/")),
                displayName,
                new PipelineRun(), // TODO - implement this
                URI.create(run.getUrl()),
                convertToJson(taskRunData));
    }

    public static CloudEvent buildTaskRunFinishedModel(Run run, FlowNode node) {
        String displayName = run.getParent().getFullDisplayName();
        Object taskRunData = ModelBuilder.buildTaskModel(run, node);

        String errors;
        CDEventConstants.Outcome outcome;
        ErrorAction nodeError = node.getError();
        if (nodeError != null) {
            outcome = OutcomeMapper.mapResultToOutcome(nodeError);
            errors = outcome == CDEventConstants.Outcome.OutcomeSuccess ? ""
                    : nodeError.getDisplayName();
        } else {
            outcome = CDEventConstants.Outcome.OutcomeError;
            errors = "Unable to get Display Name of the Node Error.";
        }

        LOGGER.info("Building TaskRunFinished model for " + displayName);

        return CDEventTypes.createTaskRunFinishedEvent(
                CDEventConstants.CDEventTypes.TaskRunFinishedEvent.getEventType(),
                run.getId(),
                URI.create(run.getParent().getUrl().replaceAll(displayName, "").replaceAll("//", "/")),
                displayName,
                new PipelineRun(), // TODO - implement this
                URI.create(run.getUrl()),
                outcome,
                errors,
                convertToJson(taskRunData));
    }
}
