/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
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
package org.camunda.bpm.engine.impl.batch.deletion;

import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Perform serialization of DeleteProcessInstanceBatchConfiguration into JSON format.
 *
 * @author Askar Akhmerov
 */
public class DeleteProcessInstanceBatchConfigurationJsonConverter extends JsonObjectConverter<DeleteProcessInstanceBatchConfiguration> {
  public static final DeleteProcessInstanceBatchConfigurationJsonConverter INSTANCE = new DeleteProcessInstanceBatchConfigurationJsonConverter();

  public static final String DELETE_REASON = "deleteReason";
  public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";
  public static final String SKIP_CUSTOM_LISTENERS = "skipCustomListeners";
  public static final String SKIP_SUBPROCESSES = "skipSubprocesses";

  public JsonObject toJsonObject(DeleteProcessInstanceBatchConfiguration configuration) {
    JsonObject json = JsonUtil.createObject();

    JsonUtil.addField(json, DELETE_REASON, configuration.getDeleteReason());
    JsonUtil.addListField(json, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonUtil.addField(json, SKIP_CUSTOM_LISTENERS, configuration.isSkipCustomListeners());
    JsonUtil.addField(json, SKIP_SUBPROCESSES, configuration.isSkipSubprocesses());
    return json;
  }

  public DeleteProcessInstanceBatchConfiguration toObject(JsonObject json) {
    DeleteProcessInstanceBatchConfiguration configuration =
      new DeleteProcessInstanceBatchConfiguration(readProcessInstanceIds(json), JsonUtil.getBoolean(json, SKIP_CUSTOM_LISTENERS), JsonUtil.getBoolean(json, SKIP_SUBPROCESSES));

    String deleteReason = JsonUtil.getString(json, DELETE_REASON);
    if (deleteReason != null && !deleteReason.isEmpty()) {
      configuration.setDeleteReason(deleteReason);
    }

    return configuration;
  }

  protected List<String> readProcessInstanceIds(JsonObject jsonObject) {
    return JsonUtil.asStringList(JsonUtil.getArray(jsonObject, PROCESS_INSTANCE_IDS));
  }
}
