/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents;

import io.cloudevents.CloudEvent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public abstract class CDEventsSink {

    abstract public void sendCloudEvent(CloudEvent cloudEvent)
            throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException;

}