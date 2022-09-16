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
package org.camunda.bpm.engine.impl.json;

import java.util.List;

import org.camunda.bpm.engine.impl.ModificationBatchConfiguration;
import org.camunda.bpm.engine.impl.batch.AbstractBatchConfigurationObjectConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappingJsonConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import com.google.gson.JsonObject;

public class ModificationBatchConfigurationJsonConverter
    extends AbstractBatchConfigurationObjectConverter<ModificationBatchConfiguration> {

  public static final ModificationBatchConfigurationJsonConverter INSTANCE = new ModificationBatchConfigurationJsonConverter();
  public static final String INSTRUCTIONS = "instructions";
  public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";
  public static final String PROCESS_INSTANCE_ID_MAPPINGS = "processInstanceIdMappings";
  public static final String SKIP_LISTENERS = "skipListeners";
  public static final String SKIP_IO_MAPPINGS = "skipIoMappings";
  public static final String PROCESS_DEFINITION_ID = "processDefinitionId";

  @Override
  public JsonObject writeConfiguration(ModificationBatchConfiguration configuration) {
    JsonObject json = JsonUtil.createObject();

    JsonUtil.addListField(json, INSTRUCTIONS, ModificationCmdJsonConverter.INSTANCE, configuration.getInstructions());
    JsonUtil.addListField(json, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonUtil.addListField(json, PROCESS_INSTANCE_ID_MAPPINGS, DeploymentMappingJsonConverter.INSTANCE, configuration.getIdMappings());
    JsonUtil.addField(json, PROCESS_DEFINITION_ID, configuration.getProcessDefinitionId());
    JsonUtil.addField(json, SKIP_LISTENERS, configuration.isSkipCustomListeners());
    JsonUtil.addField(json, SKIP_IO_MAPPINGS, configuration.isSkipIoMappings());

    return json;
  }

  @Override
  public ModificationBatchConfiguration readConfiguration(JsonObject json) {

    List<String> processInstanceIds = readProcessInstanceIds(json);
    DeploymentMappings mappings = readIdMappings(json);
    String processDefinitionId = JsonUtil.getString(json, PROCESS_DEFINITION_ID);
    List<AbstractProcessInstanceModificationCommand> instructions = JsonUtil.asList(JsonUtil.getArray(json, INSTRUCTIONS),
        ModificationCmdJsonConverter.INSTANCE);
    boolean skipCustomListeners = JsonUtil.getBoolean(json, SKIP_LISTENERS);
    boolean skipIoMappings = JsonUtil.getBoolean(json, SKIP_IO_MAPPINGS);

    return new ModificationBatchConfiguration(
        processInstanceIds,
        mappings,
        processDefinitionId,
        instructions,
        skipCustomListeners,
        skipIoMappings);
  }

  protected List<String> readProcessInstanceIds(JsonObject jsonObject) {
    return JsonUtil.asStringList(JsonUtil.getArray(jsonObject, PROCESS_INSTANCE_IDS));
  }

  protected DeploymentMappings readIdMappings(JsonObject json) {
    return JsonUtil.asList(JsonUtil.getArray(json, PROCESS_INSTANCE_ID_MAPPINGS), DeploymentMappingJsonConverter.INSTANCE, DeploymentMappings::new);
  }
}
