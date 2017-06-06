/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.batch.suspension;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

public class SuspendProcessInstanceBatchConfigurationJsonConverter extends JsonObjectConverter<SuspendProcessInstanceBatchConfiguration> {
  public static final SuspendProcessInstanceBatchConfigurationJsonConverter INSTANCE = new SuspendProcessInstanceBatchConfigurationJsonConverter();

  public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";

  public JSONObject toJsonObject(SuspendProcessInstanceBatchConfiguration configuration) {
    JSONObject json = new JSONObject();

    JsonUtil.addListField(json, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonUtil.addField(json, "suspended", configuration.getSuspended());
    return json;
  }

  public SuspendProcessInstanceBatchConfiguration toObject(JSONObject json) {
    SuspendProcessInstanceBatchConfiguration configuration =
      new SuspendProcessInstanceBatchConfiguration(readProcessInstanceIds(json), json.getBoolean("suspended"));

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
