/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.sinks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.format.EventSerializationException;
import io.cloudevents.jackson.JsonFormat;
import io.jenkins.plugins.cdevents.CDEventsSink;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SyslogSink extends CDEventsSink {

    private static final Logger LOGGER = Logger.getLogger("SyslogSink");
    private final Level logLevel;
    private final ObjectMapper objectMapper;
    private final boolean prettyPrint;

    public SyslogSink() {
        super();
        this.logLevel = Level.INFO; // TODO: parameterize this from settings
        this.prettyPrint = true; // TODO: parameterize this from settings
        objectMapper = new ObjectMapper();
        if (prettyPrint) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
    }

    @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING",
            justification = "Default encoding is used for both serialization and deserialization")
    @Override
    public void sendCloudEvent(CloudEvent cloudEvent) throws JsonProcessingException {
        // rewrite the object with our ObjectMapper to include pretty print and other
        // features
        try {
            byte[] serialized = new JsonFormat().serialize(cloudEvent);
            String rawJson = new String(serialized, StandardCharsets.UTF_8);
            Object jsonObj = objectMapper.readValue(rawJson, Object.class);
            LOGGER.log(logLevel, objectMapper.writeValueAsString(jsonObj));
        } catch (EventSerializationException e) {
            e.printStackTrace();
        }
    }
}
