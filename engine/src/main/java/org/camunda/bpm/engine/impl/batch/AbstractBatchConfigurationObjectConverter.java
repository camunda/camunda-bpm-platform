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
package org.camunda.bpm.engine.impl.batch;

import com.google.gson.JsonObject;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.util.JsonUtil;

public abstract class AbstractBatchConfigurationObjectConverter<T extends BatchConfiguration> extends JsonObjectConverter<T> {

  protected static final String BATCH_ID = "batchId";

  public abstract JsonObject writeConfiguration(T object);

  public abstract T readConfiguration(JsonObject jsonObject);

  @Override
  public final JsonObject toJsonObject(T object) {
    JsonObject json = writeConfiguration(object);
    JsonUtil.addField(json, BATCH_ID, object.getBatchId());
    return json;
  }

  @Override
  public final T toObject(JsonObject jsonObject) {
    T configuration = readConfiguration(jsonObject);
    configuration.setBatchId(JsonUtil.getString(jsonObject, BATCH_ID));
    return configuration;
  }
}
