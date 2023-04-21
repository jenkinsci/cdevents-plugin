/**
 * Copyright FMR LLC <opensource@fidelity.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.cdevents.sinks;

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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpSink extends CDEventsSink {

    private static final Logger LOGGER = Logger.getLogger("HttpSink");
    private static final String sinkUrl = CDEventsGlobalConfig.get().getHttpSinkUrl();
    private final String host = Jenkins.get().proxy.name;
    private final int port = Jenkins.get().proxy.port;

    @Override
    public void sendCloudEvent(CloudEvent cloudEvent) throws IOException {
        LOGGER.log(Level.INFO, "Now attempting to send to the HTTP endpoint " + sinkUrl
                + " the following CloudEvent " + cloudEvent);
        HttpPost httpPost = new HttpPost(sinkUrl);
        // TODO need to test the string conversion and using the Apache HTTP library is
        // successful
        StringEntity entity = new StringEntity(cloudEvent.toString());
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        try (CloseableHttpClient client = HttpClientBuilder.create()
                .useSystemProperties()
                .setProxy(new HttpHost(host, port))
                .build();
             CloseableHttpResponse response = client.execute(httpPost)) {
            JSONObject sinkResponse = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            LOGGER.log(Level.INFO, "Response from HTTP Sink Endpoint: " + sinkResponse);
        }
    }
}