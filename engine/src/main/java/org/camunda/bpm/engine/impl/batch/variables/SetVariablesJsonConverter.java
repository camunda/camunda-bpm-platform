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
package org.camunda.bpm.engine.impl.batch.variables;

import com.google.gson.JsonObject;
import org.camunda.bpm.engine.impl.batch.AbstractBatchConfigurationObjectConverter;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.DeploymentMappingJsonConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.util.JsonUtil;

import java.util.List;

public class SetVariablesJsonConverter extends AbstractBatchConfigurationObjectConverter<BatchConfiguration> {

  public static final SetVariablesJsonConverter INSTANCE = new SetVariablesJsonConverter();

  protected static final String IDS = "ids";
  protected static final String ID_MAPPINGS = "idMappings";

  @Override
  public JsonObject writeConfiguration(final BatchConfiguration configuration) {
    JsonObject json = JsonUtil.createObject();

    JsonUtil.addListField(json, IDS, configuration.getIds());
    JsonUtil.addListField(json, ID_MAPPINGS, DeploymentMappingJsonConverter.INSTANCE,
        configuration.getIdMappings());

    return json;
  }

  @Override
  public BatchConfiguration readConfiguration(final JsonObject jsonObject) {
    List<String> instanceIds = JsonUtil.asStringList(JsonUtil.getArray(jsonObject, IDS));

    DeploymentMappings mappings = JsonUtil.asList(JsonUtil.getArray(jsonObject, ID_MAPPINGS),
        DeploymentMappingJsonConverter.INSTANCE, DeploymentMappings::new);

    return new BatchConfiguration(instanceIds, mappings);
  }

}
