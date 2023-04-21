/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.models;

import java.io.Serializable;

public class ScmState implements Serializable {

    private static final long serialVersionUID = 1L;

    private String url;
    private String branch;
    private String commit;

    public ScmState() {
        super();
    }

    public ScmState(ScmState that) {
        this.url = that.getUrl();
        this.branch = that.getBranch();
        this.commit = that.getCommit();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

}