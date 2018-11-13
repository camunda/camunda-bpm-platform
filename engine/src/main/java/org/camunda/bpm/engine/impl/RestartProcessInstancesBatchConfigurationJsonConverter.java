/*
 * Copyright © 2012 - 2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.json.ModificationCmdJsonConverter;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

public class RestartProcessInstancesBatchConfigurationJsonConverter extends JsonObjectConverter<RestartProcessInstancesBatchConfiguration>{

  public static final RestartProcessInstancesBatchConfigurationJsonConverter INSTANCE = new RestartProcessInstancesBatchConfigurationJsonConverter();
  
  public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";
  public static final String INSTRUCTIONS = "instructions";
  public static final String PROCESS_DEFINITION_ID = "processDefinitionId";
  public static final String INITIAL_VARIABLES = "initialVariables";
  public static final String SKIP_CUSTOM_LISTENERS = "skipCustomListeners";
  public static final String SKIP_IO_MAPPINGS = "skipIoMappings";
  public static final String WITHOUT_BUSINESS_KEY = "withoutBusinessKey";

  @Override
  public JSONObject toJsonObject(RestartProcessInstancesBatchConfiguration configuration) {
    JSONObject json = new JSONObject();
    
    JsonUtil.addListField(json, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonUtil.addField(json, PROCESS_DEFINITION_ID, configuration.getProcessDefinitionId());
    JsonUtil.addListField(json, INSTRUCTIONS, ModificationCmdJsonConverter.INSTANCE, configuration.getInstructions());
    JsonUtil.addField(json, INITIAL_VARIABLES, configuration.isInitialVariables());
    JsonUtil.addField(json, SKIP_CUSTOM_LISTENERS, configuration.isSkipCustomListeners());
    JsonUtil.addField(json, SKIP_IO_MAPPINGS, configuration.isSkipIoMappings());
    JsonUtil.addField(json, WITHOUT_BUSINESS_KEY, configuration.isWithoutBusinessKey());
    
    return json;
  }

  @Override
  public RestartProcessInstancesBatchConfiguration toObject(JSONObject json) {
    List<String> processInstanceIds = readProcessInstanceIds(json);
    List<AbstractProcessInstanceModificationCommand> instructions = JsonUtil.jsonArrayAsList(json.getJSONArray(INSTRUCTIONS), ModificationCmdJsonConverter.INSTANCE);
    
    return new RestartProcessInstancesBatchConfiguration(processInstanceIds, instructions, json.getString(PROCESS_DEFINITION_ID),
        json.getBoolean(INITIAL_VARIABLES), json.getBoolean(SKIP_CUSTOM_LISTENERS), json.getBoolean(SKIP_IO_MAPPINGS), json.getBoolean(WITHOUT_BUSINESS_KEY));
  }

  protected List<String> readProcessInstanceIds(JSONObject jsonObject) {
    List<Object> objects = JsonUtil.jsonArrayAsList(jsonObject.getJSONArray(PROCESS_INSTANCE_IDS));
    List<String> processInstanceIds = new ArrayList<String>();
    for (Object object : objects) {
      processInstanceIds.add((String) object);
    }
    return processInstanceIds;
  }
}
