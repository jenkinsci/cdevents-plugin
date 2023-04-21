/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QueuedJobModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String url;
    private String userId;
    private String userName;
    private List<String> causes;
    private long id;

    public QueuedJobModel() {
        super();
    }

    public QueuedJobModel(QueuedJobModel that) {
        this.name = that.getName();
        this.url = that.getUrl();
        this.userId = that.getUserId();
        this.userName = that.getUserName();
        this.causes = that.getCauses() == null ? null : List.copyOf(that.getCauses());
        this.id = that.getId();
    }

    public List<String> getCauses() {
        if (causes == null) {
            return null;
        }
        return List.copyOf(causes);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public void addCause(String shortDescription) {
        if (causes == null) {
            this.causes = new ArrayList<>();
        }
        causes.add(shortDescription);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
