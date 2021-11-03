/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.util;

import java.lang.reflect.Type;
import org.camunda.bpm.engine.impl.telemetry.dto.CommandImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.MetricImpl;
import org.camunda.bpm.engine.telemetry.Command;
import org.camunda.bpm.engine.telemetry.Metric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public final class JsonTestUtil {

  public static Gson createTelemetryDataMapper() {
    return new GsonBuilder()
      .registerTypeAdapter(Metric.class, new MetricDeserializer())
      .registerTypeAdapter(Command.class, new CommandDeserializer())
      .create();
  }

  private static class MetricDeserializer implements JsonDeserializer<Metric> {

    @Override
    public Metric deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      long count = json.getAsJsonObject().get("count").getAsLong();

      return new MetricImpl(count);
    }
  }

  private static class CommandDeserializer implements JsonDeserializer<Command> {

    @Override
    public Command deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      long count = json.getAsJsonObject().get("count").getAsLong();

      return new CommandImpl(count);
    }
  }
}
