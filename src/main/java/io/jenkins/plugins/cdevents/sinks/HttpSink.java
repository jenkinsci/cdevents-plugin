/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.sinks;

import io.cloudevents.core.format.EventSerializationException;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.CloudEvent;
import io.jenkins.plugins.cdevents.CDEventsGlobalConfig;
import io.jenkins.plugins.cdevents.CDEventsSink;
import jenkins.model.Jenkins;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import hudson.ProxyConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpSink extends CDEventsSink {

    private static final Logger LOGGER = Logger.getLogger("HttpSink");
    private static final String STRUCTURED_CONTENT_TYPE = "application/cloudevents+json";
    private final ProxyConfiguration proxy = Jenkins.get().proxy;

    static String serializeCloudEvent(CloudEvent cloudEvent) throws IOException {
        try {
            byte[] serialized = new JsonFormat().serialize(cloudEvent);
            return new String(serialized, StandardCharsets.UTF_8);
        } catch (EventSerializationException e) {
            throw new IOException("Failed to serialize CloudEvent to structured JSON", e);
        }
    }

    private static String getSinkUrl() {
        return CDEventsGlobalConfig.get().getHttpSinkUrl();
    }

    @Override
    public void sendCloudEvent(CloudEvent cloudEvent) throws IOException {
        String sinkUrl = getSinkUrl();
        LOGGER.log(Level.INFO, "Now attempting to send to the HTTP endpoint " + sinkUrl
                + " the following CloudEvent " + cloudEvent);
        HttpPost httpPost = new HttpPost(sinkUrl);
        StringEntity entity = new StringEntity(serializeCloudEvent(cloudEvent), StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", STRUCTURED_CONTENT_TYPE);

        HttpHost httpProxy = (proxy != null ? new HttpHost(proxy.name, proxy.port) : null);
        try (CloseableHttpClient client = HttpClientBuilder.create()
                .useSystemProperties()
                .setProxy(httpProxy)
                .build();
             CloseableHttpResponse response = client.execute(httpPost)) {
            JSONObject sinkResponse = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            LOGGER.log(Level.INFO, "Response from HTTP Sink Endpoint: " + sinkResponse);
        }
    }
}
