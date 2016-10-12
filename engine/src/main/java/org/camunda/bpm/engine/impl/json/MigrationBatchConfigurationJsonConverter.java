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

package org.camunda.bpm.engine.impl.json;

import org.camunda.bpm.engine.impl.migration.batch.MigrationBatchConfiguration;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MigrationBatchConfigurationJsonConverter extends JsonObjectConverter<MigrationBatchConfiguration> {

  public static final MigrationBatchConfigurationJsonConverter INSTANCE = new MigrationBatchConfigurationJsonConverter();

  public static final String MIGRATION_PLAN = "migrationPlan";
  public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";
  public static final String SKIP_LISTENERS = "skipListeners";
  public static final String SKIP_IO_MAPPINGS = "skipIoMappings";

  public JSONObject toJsonObject(MigrationBatchConfiguration configuration) {
    JSONObject json = new JSONObject();

    JsonUtil.addField(json, MIGRATION_PLAN, MigrationPlanJsonConverter.INSTANCE, configuration.getMigrationPlan());
    JsonUtil.addListField(json, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonUtil.addField(json, SKIP_LISTENERS, configuration.isSkipCustomListeners());
    JsonUtil.addField(json, SKIP_IO_MAPPINGS, configuration.isSkipIoMappings());

    return json;
  }

  public MigrationBatchConfiguration toObject(JSONObject json) {
    MigrationBatchConfiguration configuration = new MigrationBatchConfiguration(readProcessInstanceIds(json));

    configuration.setMigrationPlan(JsonUtil.jsonObject(json.getJSONObject(MIGRATION_PLAN), MigrationPlanJsonConverter.INSTANCE));
    configuration.setSkipCustomListeners(json.getBoolean(SKIP_LISTENERS));
    configuration.setSkipIoMappings(json.getBoolean(SKIP_IO_MAPPINGS));

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
