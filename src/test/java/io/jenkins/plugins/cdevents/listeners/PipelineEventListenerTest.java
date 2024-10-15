/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package io.jenkins.plugins.cdevents.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.cdevents.EventHandler;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

@SuppressFBWarnings(
        value = {
            "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
            "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
            "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"
        },
        justification = "Tests are just checking that exceptions are not thrown. Feel free to add more robust tests")
public class PipelineEventListenerTest {

    // test that listener is triggered with StepStartNode
    @Test
    void testOnStepStartNode() {
        try (MockedStatic<EventHandler> mockEventHandler = mockStatic(EventHandler.class)) {
            ArgumentCaptor<FlowNode> argumentCaptor = ArgumentCaptor.forClass(FlowNode.class);
            StepStartNode mockNode = mock(StepStartNode.class);

            mockEventHandler
                    .when(() -> EventHandler.captureEvent(argumentCaptor.capture()))
                    .thenAnswer(Answers.RETURNS_DEFAULTS);

            PipelineEventListener pipelineEventListener = new PipelineEventListener();
            pipelineEventListener.onNewHead(mockNode);

            assertEquals(mockNode, argumentCaptor.getValue());
            assertEquals(
                    StepStartNode.class.toString(),
                    argumentCaptor.getValue().getClass().toString());
        }
    }

    // test that listener is triggered with StepEndNode
    @Test
    void testOnStepEndNode() {
        try (MockedStatic<EventHandler> mockEventHandler = mockStatic(EventHandler.class)) {
            ArgumentCaptor<FlowNode> argumentCaptor = ArgumentCaptor.forClass(FlowNode.class);
            StepEndNode mockNode = mock(StepEndNode.class);
            mockEventHandler
                    .when(() -> EventHandler.captureEvent(argumentCaptor.capture()))
                    .thenAnswer(Answers.RETURNS_DEFAULTS);

            PipelineEventListener pipelineEventListener = new PipelineEventListener();
            pipelineEventListener.onNewHead(mockNode);

            assertEquals(mockNode, argumentCaptor.getValue());
            assertEquals(
                    StepEndNode.class.toString(),
                    argumentCaptor.getValue().getClass().toString());
        }
    }

    @Test
    void testDoesNothingOnOtherNodes() {
        try (MockedStatic<EventHandler> mockEventHandler = mockStatic(EventHandler.class)) {
            ArgumentCaptor<FlowNode> argumentCaptor = ArgumentCaptor.forClass(FlowNode.class);
            StepAtomNode mockNode = mock(StepAtomNode.class);
            mockEventHandler
                    .when(() -> EventHandler.captureEvent(argumentCaptor.capture()))
                    .thenAnswer(Answers.RETURNS_DEFAULTS);

            PipelineEventListener pipelineEventListener = new PipelineEventListener();
            pipelineEventListener.onNewHead(mockNode);

            mockEventHandler.verify(() -> EventHandler.captureEvent(argumentCaptor.capture()), never());
        }
    }
}
