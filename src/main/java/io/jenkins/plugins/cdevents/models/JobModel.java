/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;

public class JobModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String JENKINS_SOURCE = "org.jenkinsci.job.";
    private String userId;
    private String userName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status;
    private String name;
    private String displayName;
    private String url;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BuildModel build;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date createdDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date updatedDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String configFile;
    @JsonIgnore
    private String stage;

    public JobModel() {
        super();
    }

    public JobModel(JobModel that) {
        this.userId = that.getUserId();
        this.userName = that.getUserName();
        this.status = that.getStatus();
        this.name = that.getName();
        this.displayName = that.getDisplayName();
        this.url = that.getUrl();
        this.build = that.getBuild() == null ? null : new BuildModel(that.getBuild());
        this.createdDate = that.getCreatedDate() == null ? null : new Date(that.getCreatedDate().getTime());
        this.updatedDate = that.getUpdatedDate() == null ? null : new Date(that.getUpdatedDate().getTime());
        this.configFile = that.getConfigFile();
        this.stage = that.getStage();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BuildModel getBuild() {
        if (build == null) return null;
        return new BuildModel(build);
    }

    public void setBuild(BuildModel build) {
        this.build = new BuildModel(build);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getCreatedDate() {
        if (createdDate == null) return null;
        return new Date(createdDate.getTime());
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = new Date(createdDate.getTime());
    }

    public Date getUpdatedDate() {
        if (updatedDate == null) return null;
        return new Date(updatedDate.getTime());
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = new Date(this.updatedDate.getTime());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

}