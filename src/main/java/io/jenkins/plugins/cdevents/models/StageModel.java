/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.models;

import java.io.Serializable;
import java.util.Map;

public class StageModel implements Serializable {
    private String stageName;
    private String stageNodeUrl;
    private Map<String, Object> arguments;
    private String log;

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getStageNodeUrl() {
        return stageNodeUrl;
    }

    public void setStageNodeUrl(String stageNodeUrl) {
        this.stageNodeUrl = stageNodeUrl;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
