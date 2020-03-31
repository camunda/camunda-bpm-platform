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
package org.camunda.bpm.engine.impl.batch.deletion;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.DeploymentMappingJsonConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.util.JsonUtil;

import com.google.gson.JsonObject;

/**
 * Perform serialization of DeleteProcessInstanceBatchConfiguration into JSON format.
 *
 * @author Askar Akhmerov
 */
public class DeleteProcessInstanceBatchConfigurationJsonConverter extends JsonObjectConverter<DeleteProcessInstanceBatchConfiguration> {
  public static final DeleteProcessInstanceBatchConfigurationJsonConverter INSTANCE = new DeleteProcessInstanceBatchConfigurationJsonConverter();

  public static final String DELETE_REASON = "deleteReason";
  public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";
  public static final String PROCESS_INSTANCE_ID_MAPPINGS = "processInstanceIdMappings";
  public static final String SKIP_CUSTOM_LISTENERS = "skipCustomListeners";
  public static final String SKIP_SUBPROCESSES = "skipSubprocesses";
  public static final String FAIL_IF_NOT_EXISTS = "failIfNotExists";

  public JsonObject toJsonObject(DeleteProcessInstanceBatchConfiguration configuration) {
    JsonObject json = JsonUtil.createObject();

    JsonUtil.addField(json, DELETE_REASON, configuration.getDeleteReason());
    JsonUtil.addListField(json, PROCESS_INSTANCE_ID_MAPPINGS, DeploymentMappingJsonConverter.INSTANCE, configuration.getIdMappings());
    JsonUtil.addListField(json, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonUtil.addField(json, SKIP_CUSTOM_LISTENERS, configuration.isSkipCustomListeners());
    JsonUtil.addField(json, SKIP_SUBPROCESSES, configuration.isSkipSubprocesses());
    JsonUtil.addField(json, FAIL_IF_NOT_EXISTS, configuration.isFailIfNotExists());
    return json;
  }

  public DeleteProcessInstanceBatchConfiguration toObject(JsonObject json) {
    DeleteProcessInstanceBatchConfiguration configuration =
      new DeleteProcessInstanceBatchConfiguration(readProcessInstanceIds(json), readIdMappings(json), null,
          JsonUtil.getBoolean(json, SKIP_CUSTOM_LISTENERS), JsonUtil.getBoolean(json, SKIP_SUBPROCESSES),
          JsonUtil.getBoolean(json, FAIL_IF_NOT_EXISTS));

    String deleteReason = JsonUtil.getString(json, DELETE_REASON);

    if (deleteReason != null && !deleteReason.isEmpty()) {
      configuration.setDeleteReason(deleteReason);
    }

    return configuration;
  }

  protected List<String> readProcessInstanceIds(JsonObject jsonObject) {
    return JsonUtil.asStringList(JsonUtil.getArray(jsonObject, PROCESS_INSTANCE_IDS));
  }

  protected DeploymentMappings readIdMappings(JsonObject json) {
    return JsonUtil.asList(JsonUtil.getArray(json, PROCESS_INSTANCE_ID_MAPPINGS), DeploymentMappingJsonConverter.INSTANCE, DeploymentMappings::new);
  }
}
