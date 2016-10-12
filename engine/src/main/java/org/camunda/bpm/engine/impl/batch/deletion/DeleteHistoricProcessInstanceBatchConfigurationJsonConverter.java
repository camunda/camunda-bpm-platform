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

import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Askar Akhmerov
 */
public class DeleteHistoricProcessInstanceBatchConfigurationJsonConverter extends JsonObjectConverter<BatchConfiguration> {

  public static final DeleteHistoricProcessInstanceBatchConfigurationJsonConverter INSTANCE = new DeleteHistoricProcessInstanceBatchConfigurationJsonConverter();

  public static final String HISTORIC_PROCESS_INSTANCE_IDS = "historicProcessInstanceIds";

  public JSONObject toJsonObject(BatchConfiguration configuration) {
    JSONObject json = new JSONObject();

    JsonUtil.addListField(json, HISTORIC_PROCESS_INSTANCE_IDS, configuration.getIds());
    return json;
  }

  public BatchConfiguration toObject(JSONObject json) {
    BatchConfiguration configuration = new BatchConfiguration(readProcessInstanceIds(json));
    return configuration;
  }

  protected List<String> readProcessInstanceIds(JSONObject jsonObject) {
    List<Object> objects = JsonUtil.jsonArrayAsList(jsonObject.getJSONArray(HISTORIC_PROCESS_INSTANCE_IDS));
    List<String> processInstanceIds = new ArrayList<String>();
    for (Object object : objects) {
      processInstanceIds.add((String) object);
    }
    return processInstanceIds;
  }
}
