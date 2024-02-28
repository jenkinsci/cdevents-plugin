/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.FormValidation;
import io.jenkins.plugins.cdevents.sinks.KinesisSink;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.Objects;
import java.util.stream.Stream;

@Extension
public class CDEventsGlobalConfig extends GlobalConfiguration {

    private String sinkType;
    private String httpSinkUrl;
    private String kinesisStreamName;
    private String kinesisRegion;
    private String kinesisEndpoint;
    private String iamRole;

    @SuppressFBWarnings(value = {"CD_CIRCULAR_DEPENDENCY", "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR"}, justification = "Circular dependency is false positive triggered by jenkins.model.GlobalConfiguration. " + "Overridable method call in constructor is unavoidable.")
    public CDEventsGlobalConfig() {
        load();
    }

    public static CDEventsGlobalConfig get() {
        return ExtensionList.lookupSingleton(CDEventsGlobalConfig.class);
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public String getSinkType() {
        return this.sinkType;
    }

    @DataBoundSetter
    public void setSinkType(String sinkType) {
        this.sinkType = sinkType;
        save();
    }

    public boolean sinkTypeEquals(String value) {
        return this.sinkType.equals(value);
    }

    public String getHttpSinkUrl() {
        return this.httpSinkUrl;
    }

    @DataBoundSetter
    public void setHttpSinkUrl(String httpSinkUrl) {
        this.httpSinkUrl = httpSinkUrl;
        save();
    }

    public String getKinesisStreamName() {
        return this.kinesisStreamName;
    }

    @DataBoundSetter
    public void setKinesisStreamName(String kinesisStreamName) {
        this.kinesisStreamName = kinesisStreamName;
        KinesisSink.nullifyKinesisClient();
        save();
    }

    public String getKinesisRegion() {
        if (this.kinesisRegion == null) {
            this.kinesisRegion = Stream.of(System.getenv("AWS_REGION"), System.getenv("AWS_DEFAULT_REGION")).filter(Objects::nonNull).findFirst().orElse(null);
        }
        return this.kinesisRegion;
    }

    @DataBoundSetter
    public void setKinesisRegion(String kinesisRegion) {
        this.kinesisRegion = kinesisRegion;
        KinesisSink.nullifyKinesisClient();
        save();
    }

    public String getKinesisEndpoint() {
        return this.kinesisEndpoint;
    }

    @DataBoundSetter
    public void setKinesisEndpoint(String kinesisEndpoint) {
        this.kinesisEndpoint = kinesisEndpoint;
        KinesisSink.nullifyKinesisClient();
        save();
    }

    public String getIamRole() {
        return this.iamRole;
    }

    @DataBoundSetter
    public void setIamRole(String iamRole) {
        this.iamRole = iamRole;
        KinesisSink.nullifyKinesisClient();
        save();
    }

    public FormValidation doCheckKinesisStreamName(@QueryParameter("kinesisStreamName") String kinesisStreamName) {
        if (isNullOrEmpty(kinesisStreamName)) {
            return FormValidation.error("Kinesis stream cannot be blank");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckKinesisRegion(@QueryParameter("kinesisRegion") String kinesisRegion) {
        if (isNullOrEmpty(kinesisRegion)) {
            return FormValidation.error("Kinesis region cannot be blank");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckKinesisEndpoint(@QueryParameter("kinesisEndpoint") String kinesisEndpoint, @QueryParameter("kinesisRegion") String kinesisRegion) {
        if (!isNullOrEmpty(kinesisEndpoint) && isNullOrEmpty(kinesisRegion)) {
            FormValidation.error("Kinesis requires a defined region for a custom endpoint");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckIamRole(@QueryParameter("iamRole") String iamRole) {
        if (isNullOrEmpty(iamRole)) {
            return FormValidation.error("IAM Role cannot be blank");
        }
        return FormValidation.ok();
    }
}
