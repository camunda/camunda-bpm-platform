/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import java.util.ArrayList;
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

  public JSONObject toJsonObject(DeleteProcessInstanceBatchConfiguration configuration) {
    JSONObject json = new JSONObject();

    JsonUtil.addField(json, DELETE_REASON, configuration.getDeleteReason());
    JsonUtil.addListField(json, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonUtil.addField(json, SKIP_CUSTOM_LISTENERS, configuration.isSkipCustomListeners());
    JsonUtil.addField(json, SKIP_SUBPROCESSES, configuration.isSkipSubprocesses());
    return json;
  }

  public DeleteProcessInstanceBatchConfiguration toObject(JSONObject json) {
    DeleteProcessInstanceBatchConfiguration configuration =
        new DeleteProcessInstanceBatchConfiguration(readProcessInstanceIds(json), json.optBoolean(SKIP_CUSTOM_LISTENERS), json.optBoolean(SKIP_SUBPROCESSES));

    String deleteReason = json.optString(DELETE_REASON);
    if (deleteReason != null && !deleteReason.isEmpty()) {
      configuration.setDeleteReason(deleteReason);
    }

    return configuration;
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
