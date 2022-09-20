/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.eventmesh.connector.knative.cloudevent.impl;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.asynchttpclient.Response;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.message.MessageWriter;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.cloudevents.rw.CloudEventContextWriter;
import io.cloudevents.rw.CloudEventRWException;
import io.cloudevents.rw.CloudEventWriter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class KnativeMessageWriter implements MessageWriter<CloudEventWriter<String>, String>, CloudEventWriter<String> {

    public CloudEvent message;

    public KnativeMessageWriter(Properties properties) {
        String s = "{ \"msg\": [\"" + properties.get("data") + "\"]}";
        this.message = new CloudEventBuilder()
            .withId(properties.getProperty(KnativeHeaders.CE_ID))
            .withSource(URI.create(properties.getProperty(KnativeHeaders.CE_SOURCE)))
            .withType(properties.getProperty(KnativeHeaders.CE_TYPE))
            .withDataContentType(properties.getProperty(KnativeHeaders.CONTENT_TYPE))
            .withData(s.getBytes(StandardCharsets.UTF_8))
            .build();
    }

    public KnativeMessageWriter(String topic, Response response) {
        String responseBody = response.getResponseBody();
        JSONArray array = JSON.parseArray(responseBody);
        JSONObject object = array.getJSONObject(0);

        Properties properties = new Properties();
        properties.put(KnativeHeaders.CONTENT_TYPE, object.getJSONObject("event").getJSONObject("attributes").get("datacontenttype"));
        properties.put(KnativeHeaders.CE_ID, object.getJSONObject("event").getJSONObject("attributes").get("id"));
        properties.put(KnativeHeaders.CE_SPECVERSION, object.getJSONObject("event").getJSONObject("attributes").get("specversion"));
        properties.put(KnativeHeaders.CE_TYPE, object.getJSONObject("event").getJSONObject("attributes").get("type"));
        properties.put(KnativeHeaders.CE_SOURCE, object.getJSONObject("event").getJSONObject("attributes").get("source"));
        properties.put("data", object.getJSONObject("event").getJSONObject("data").get("msg"));

        String s = "{ \"msg\": [\"" + properties.get("data") + "\"]}";
        this.message = new CloudEventBuilder()
            .withSubject(topic)
            .withId(properties.getProperty(KnativeHeaders.CE_ID))
            .withSource(URI.create(properties.getProperty(KnativeHeaders.CE_SOURCE)))
            .withType(properties.getProperty(KnativeHeaders.CE_TYPE))
            .withDataContentType(properties.getProperty(KnativeHeaders.CONTENT_TYPE))
            .withData(s.getBytes(StandardCharsets.UTF_8))
            .build();
    }

    @Override
    public String end(CloudEventData data) throws CloudEventRWException {
        return data.toString();
    }

    @Override
    public String end() throws CloudEventRWException {
        return message.getData().toString();
    }

    @Override
    public CloudEventContextWriter withContextAttribute(String name, String value) throws CloudEventRWException {
        return null;
    }

    @Override
    public String setEvent(EventFormat format, byte[] value) throws CloudEventRWException {
        return null;
    }

    @Override
    public CloudEventWriter<String> create(SpecVersion version) throws CloudEventRWException {
        return null;
    }
}
