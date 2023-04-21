/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.util;

import hudson.EnvVars;
import hudson.console.AnnotatedLargeText;
import hudson.model.*;
import io.jenkins.plugins.cdevents.models.*;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.actions.ArgumentsAction;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.io.IOException;
import java.io.StringWriter;

public class ModelBuilder {

    public static JobModel buildJobModel(Job parent, Run run, TaskListener listener) throws IOException, InterruptedException {

        String rootUrl = Jenkins.get().getRootUrl();

        JobModel jobModel = new JobModel();

        jobModel.setName(parent.getName());
        jobModel.setDisplayName(parent.getDisplayName());
        jobModel.setUrl(parent.getUrl());

        String userName = Jenkins.getAuthentication2().getCredentials().toString();
        User user = Jenkins.get().getUser(userName);
        if (user != null) {
            jobModel.setUserId(user.getId());
            jobModel.setUserName(user.getFullName());
        }

        Result result = run.getResult();

        BuildModel buildModel = new BuildModel();
        buildModel.setNumber(run.getNumber());
        buildModel.setQueueId(run.getQueueId());
        buildModel.setUrl(run.getUrl());
        buildModel.setDuration(run.getDuration());
        ParametersAction parametersAction = run.getAction(ParametersAction.class);
        if (parametersAction != null) {
            EnvVars envVars = new EnvVars();
            for (ParameterValue parameterValue : parametersAction.getParameters()) {
                if (!parameterValue.isSensitive()) {
                    parameterValue.buildEnvironment(run, envVars);
                }
            }
            buildModel.setParameters(envVars);
        }
        jobModel.setBuild(buildModel);

        EnvVars envVars = run.getEnvironment(listener);

        ScmState scmState = new ScmState();
        if (envVars.get("GIT_URL") != null) {
            scmState.setUrl(envVars.get("GIT_URL"));
        }
        if (envVars.get("GIT_BRANCH") != null) {
            scmState.setBranch(envVars.get("GIT_BRANCH"));
        }
        if (envVars.get("GIT_COMMIT") != null) {
            scmState.setCommit(envVars.get("GIT_COMMIT"));
        }
        buildModel.setScmState(scmState);
        if (result != null) {
            buildModel.setStatus(result.toString());
        }
        if (rootUrl != null) {
            buildModel.setFullUrl(rootUrl + run.getUrl());
        }

        return jobModel;
    }

    public static QueuedJobModel buildQueuedJobModel(Queue.WaitingItem item) {
        String rootUrl = Jenkins.get().getRootUrl();

        QueuedJobModel jobModel = new QueuedJobModel();

        jobModel.setName(item.task.getFullDisplayName());
        jobModel.setUrl(rootUrl + item.task.getUrl() + item.getId() + '/');
        jobModel.setId(item.getId());

        String userName = Jenkins.getAuthentication2().getCredentials().toString();
        User user = Jenkins.get().getUser(userName);
        if (user != null) {
            jobModel.setUserId(user.getId());
            jobModel.setUserName(user.getFullName());
        }

        item.getCauses().forEach(cause -> jobModel.addCause(cause.getShortDescription()));

        return jobModel;
    }

    public static StageModel buildTaskModel(Run run, FlowNode node) {
        StageModel stageModel = new StageModel();

        stageModel.setStageName(node.getDisplayName());
        try {
            stageModel.setStageNodeUrl(node.getUrl());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (node instanceof StepEndNode) {
            StepEndNode stepEndNode = (StepEndNode) node;
            stepEndNode.getParents().forEach(parent -> {
                ArgumentsAction argumentsAction = parent.getAction(ArgumentsAction.class);
                if (argumentsAction != null) {
                    stageModel.setArguments(argumentsAction.getArguments());
                }

                LogAction logAction = parent.getAction(LogAction.class);
                if (logAction != null) {
                    StringWriter writer = new StringWriter();
                    try {
                        AnnotatedLargeText logText = logAction.getLogText();
                        long unused = logText.writeLogTo(0, writer);
                        stageModel.setLog(writer.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        return stageModel;
    }
}
