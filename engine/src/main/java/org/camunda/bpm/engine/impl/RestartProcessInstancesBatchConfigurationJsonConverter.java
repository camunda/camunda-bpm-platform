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
package org.camunda.bpm.engine.impl;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.DeploymentMappingJsonConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.json.ModificationCmdJsonConverter;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import com.google.gson.JsonObject;

public class RestartProcessInstancesBatchConfigurationJsonConverter extends JsonObjectConverter<RestartProcessInstancesBatchConfiguration>{

  public static final RestartProcessInstancesBatchConfigurationJsonConverter INSTANCE = new RestartProcessInstancesBatchConfigurationJsonConverter();

  public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";
  public static final String PROCESS_INSTANCE_ID_MAPPINGS = "processInstanceIdMappings";
  public static final String INSTRUCTIONS = "instructions";
  public static final String PROCESS_DEFINITION_ID = "processDefinitionId";
  public static final String INITIAL_VARIABLES = "initialVariables";
  public static final String SKIP_CUSTOM_LISTENERS = "skipCustomListeners";
  public static final String SKIP_IO_MAPPINGS = "skipIoMappings";
  public static final String WITHOUT_BUSINESS_KEY = "withoutBusinessKey";

  @Override
  public JsonObject toJsonObject(RestartProcessInstancesBatchConfiguration configuration) {
    JsonObject json = JsonUtil.createObject();

    JsonUtil.addListField(json, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonUtil.addListField(json, PROCESS_INSTANCE_ID_MAPPINGS, DeploymentMappingJsonConverter.INSTANCE, configuration.getIdMappings());
    JsonUtil.addField(json, PROCESS_DEFINITION_ID, configuration.getProcessDefinitionId());
    JsonUtil.addListField(json, INSTRUCTIONS, ModificationCmdJsonConverter.INSTANCE, configuration.getInstructions());
    JsonUtil.addField(json, INITIAL_VARIABLES, configuration.isInitialVariables());
    JsonUtil.addField(json, SKIP_CUSTOM_LISTENERS, configuration.isSkipCustomListeners());
    JsonUtil.addField(json, SKIP_IO_MAPPINGS, configuration.isSkipIoMappings());
    JsonUtil.addField(json, WITHOUT_BUSINESS_KEY, configuration.isWithoutBusinessKey());

    return json;
  }

  @Override
  public RestartProcessInstancesBatchConfiguration toObject(JsonObject json) {
    List<String> processInstanceIds = readProcessInstanceIds(json);
    DeploymentMappings idMappings = readIdMappings(json);
    List<AbstractProcessInstanceModificationCommand> instructions = JsonUtil.asList(JsonUtil.getArray(json, INSTRUCTIONS), ModificationCmdJsonConverter.INSTANCE);

    return new RestartProcessInstancesBatchConfiguration(processInstanceIds, idMappings, instructions,
        JsonUtil.getString(json, PROCESS_DEFINITION_ID), JsonUtil.getBoolean(json, INITIAL_VARIABLES),
        JsonUtil.getBoolean(json, SKIP_CUSTOM_LISTENERS), JsonUtil.getBoolean(json, SKIP_IO_MAPPINGS),
        JsonUtil.getBoolean(json, WITHOUT_BUSINESS_KEY));
  }

  protected List<String> readProcessInstanceIds(JsonObject jsonObject) {
    return JsonUtil.asStringList(JsonUtil.getArray(jsonObject, PROCESS_INSTANCE_IDS));
  }

  protected DeploymentMappings readIdMappings(JsonObject jsonObject) {
    return JsonUtil.asList(JsonUtil.getArray(jsonObject, PROCESS_INSTANCE_ID_MAPPINGS), DeploymentMappingJsonConverter.INSTANCE, DeploymentMappings::new);
  }
}
