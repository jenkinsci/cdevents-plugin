/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents;

import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.cloudevents.CloudEvent;
import io.jenkins.plugins.cdevents.sinks.HttpSink;
import io.jenkins.plugins.cdevents.sinks.KinesisSink;
import io.jenkins.plugins.cdevents.sinks.SyslogSink;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventHandler {

    private static final Logger LOGGER = Logger.getLogger("EventHandler");

    public static void captureEvent(EventState eventState, Run run, TaskListener listener, String eventType)
            throws IOException, InterruptedException {
        CloudEvent cdEvent = null;
        LOGGER.log(Level.INFO, "captureEvent of type " + eventType);
        switch (eventType) {
            case "pipelineRun":
                cdEvent = pipelineRunEvent(eventState, run, listener);
                break;
            case "taskRun": // TODO
                LOGGER.log(Level.WARNING, "capture event taskRun not yet implemented");
                break;
            default:
                LOGGER.log(Level.WARNING, "no capture event matched " + eventType);
                break;
        }
        sendEvent(cdEvent);
    }

    public static void captureEvent(EventState eventState, Queue.WaitingItem item, String eventType) {
        CloudEvent cdEvent = null;
        LOGGER.log(Level.INFO, "captureEvent of type " + eventType);
        if (eventType.equals("enterWaiting")) {
            cdEvent = pipelineRunEvent(eventState, item);
        } else {
            LOGGER.log(Level.WARNING, "no capture event matched " + eventType);
        }
        sendEvent(cdEvent);
    }

    private static CloudEvent pipelineRunEvent(EventState eventState, Queue.WaitingItem item) {
        CloudEvent pipelineRunEvent = null;
        LOGGER.log(Level.INFO, "pipelineRunEvent of type " + eventState.toString().toLowerCase());
        if (eventState.toString().equalsIgnoreCase("queued")) {
            pipelineRunEvent = BuildCDEvent.buildPipelineRunQueuedModel(item);
        } else {
            LOGGER.log(Level.WARNING, "No event action " + eventState + " was found for pipelineRun");
        }
        return pipelineRunEvent;
    }

    private static CloudEvent pipelineRunEvent(EventState eventState, Run run, TaskListener listener)
            throws IOException, InterruptedException {
        CloudEvent pipelineRunEvent = null;
        LOGGER.log(Level.INFO, "pipelineRunEvent of type " + eventState.toString().toLowerCase());
        switch (eventState.toString().toLowerCase()) {
            case "started":
                pipelineRunEvent = BuildCDEvent.buildPipelineRunStartedModel(run, listener);
                break;
            case "finished":
                pipelineRunEvent = BuildCDEvent.buildPipelineRunFinishedModel(run, listener);
                break;
            default:
                LOGGER.log(Level.WARNING, "No event action " + eventState + " was found for pipelineRun");
                break;
        }
        return pipelineRunEvent;
    }

    private static void sendEvent(CloudEvent cloudEvent) {
        String sinkType = CDEventsGlobalConfig.get().getSinkType();
        CDEventsSink sink = null;
        try {
            switch (sinkType) {
                case "kinesis":
                    sink = new KinesisSink();
                    break;
                case "http":
                    sink = new HttpSink();
                    break;
                case "syslog":
                    sink = new SyslogSink();
                    break;
                default:
                    LOGGER.log(Level.WARNING, "The following sink type " + sinkType + " is not supported");
                    break;
            }
            sink.sendCloudEvent(cloudEvent);
        } catch (Throwable error) {
            LOGGER.log(Level.WARNING,
                    "Failed when attempting to send to " + sinkType + " sink. Error: " + error.getMessage());
        }
    }

    public static void captureEvent(FlowNode node) {
        CloudEvent cdEvent = null;

        EventState eventState = EventState.FINISHED;
        if (node instanceof StepStartNode) {
            eventState = EventState.STARTED;
        }

        Queue.Executable exec;
        try {
            exec = node.getExecution().getOwner().getExecutable();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (exec instanceof Run) {
            Run run = (Run<?, ?>) exec;
            cdEvent = taskRunEvent(eventState, run, node);
        } else {
            LOGGER.warning("Unable to get Run object from FlowNode " + node.toString());
        }

        sendEvent(cdEvent);
    }

    private static CloudEvent taskRunEvent(EventState eventState, Run run, FlowNode node) {
        CloudEvent taskRunEvent = null;

        switch (eventState.toString().toLowerCase()) {
            case "started":
                taskRunEvent = BuildCDEvent.buildTaskRunStartedModel(run, node);
                break;
            case "finished":
                taskRunEvent = BuildCDEvent.buildTaskRunFinishedModel(run, node);
                break;
            default:
                LOGGER.log(Level.WARNING, "No event action " + eventState + " was found for taskRun");
                break;
        }

        return taskRunEvent;
    }
}
