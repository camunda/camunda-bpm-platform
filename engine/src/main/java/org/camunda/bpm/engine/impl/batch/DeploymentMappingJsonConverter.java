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

import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.util.JsonUtil;

import com.google.gson.JsonObject;

public class DeploymentMappingJsonConverter extends JsonObjectConverter<DeploymentMapping> {

  public static final DeploymentMappingJsonConverter INSTANCE = new DeploymentMappingJsonConverter();

  protected static final String COUNT = "count";
  protected static final String DEPLOYMENT_ID = "deploymentId";

  @Override
  public JsonObject toJsonObject(DeploymentMapping mapping) {
    JsonObject json = JsonUtil.createObject();
    json.addProperty(DEPLOYMENT_ID, mapping.getDeploymentId());
    json.addProperty(COUNT, mapping.getCount());
    return json;
  }

  @Override
  public DeploymentMapping toObject(JsonObject json) {
    String deploymentId = JsonUtil.isNull(json, DEPLOYMENT_ID) ? null : JsonUtil.getString(json, DEPLOYMENT_ID);
    int count = JsonUtil.getInt(json, COUNT);
    return new DeploymentMapping(deploymentId, count);
  }

}
