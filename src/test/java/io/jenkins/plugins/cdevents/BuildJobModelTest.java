/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents;

import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.cdevents.models.JobModel;
import io.jenkins.plugins.cdevents.util.ModelBuilder;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BuildJobModelTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Job mockJob;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Run mockRun;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TaskListener mockTaskListener;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Jenkins jenkins;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Authentication jenkinsAuth;

    @BeforeEach
    void setup() {
        reset(mockJob);
        reset(mockRun);
        reset(mockTaskListener);
        reset(jenkins);
        reset(jenkinsAuth);
    }

    /**
     * TODO: Write a more robust test. It is possible to mock out the Jenkins object
     * via MockedStatic
     * and Mockito deep stubs, but more understanding of the Job/Build/ScmState
     * models and their relation
     * to the CDEvent spec is needed. Otherwise, this is just a big getter/setter
     * test.
     */
    @Test
    void buildJobModelTest() throws IOException, InterruptedException {
        try (MockedStatic<Jenkins> jenkinsStatic = mockStatic(Jenkins.class)) {
            jenkinsStatic.when(() -> Jenkins.get()).thenReturn(jenkins);
            jenkinsStatic.when(() -> Jenkins.getAuthentication2()).thenReturn(jenkinsAuth);
            when(jenkins.getRootUrl()).thenReturn("http://localhost");
            when(jenkinsAuth.getCredentials()).thenReturn("fake credentials");
            when(mockRun.getNumber()).thenReturn(1);
            when(mockRun.getQueueId()).thenReturn(1L);
            when(mockRun.getUrl()).thenReturn("http://localhost");
            when(mockRun.getDuration()).thenReturn(1L);

            when(mockRun.getAction(any())).thenReturn(null);

            JobModel jobModel = ModelBuilder.buildJobModel(mockJob, mockRun, mockTaskListener);

            assertEquals(jobModel.getName(), mockJob.getName());
            assertEquals(jobModel.getDisplayName(), mockJob.getDisplayName());
            assertEquals(jobModel.getUrl(), mockJob.getUrl());

            assertEquals(jobModel.getUserId(), jenkins.getUser(null).getId());
            assertEquals(jobModel.getUserName(), jenkins.getUser(null).getFullName());

            assertEquals(jobModel.getBuild().getNumber(), 1);

        }
    }
}
