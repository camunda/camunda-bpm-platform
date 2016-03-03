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

import org.camunda.bpm.engine.impl.migration.MigrationPlanImpl;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.migration.MigrationPlan;

public class MigrationPlanJsonConverter extends JsonObjectConverter<MigrationPlan> {

  public static final MigrationPlanJsonConverter INSTANCE = new MigrationPlanJsonConverter();

  public static final String SOURCE_PROCESS_DEFINITION_ID = "sourceProcessDefinitionId";
  public static final String TARGET_PROCESS_DEFINITION_ID = "targetProcessDefinitionId";
  public static final String INSTRUCTIONS = "instructions";

  public JSONObject toJsonObject(MigrationPlan migrationPlan) {
    JSONObject json = new JSONObject();

    JsonUtil.addField(json, SOURCE_PROCESS_DEFINITION_ID, migrationPlan.getSourceProcessDefinitionId());
    JsonUtil.addField(json, TARGET_PROCESS_DEFINITION_ID, migrationPlan.getTargetProcessDefinitionId());
    JsonUtil.addListField(json, INSTRUCTIONS, MigrationInstructionJsonConverter.INSTANCE, migrationPlan.getInstructions());

    return json;
  }

  public MigrationPlan toObject(JSONObject json) {
    MigrationPlanImpl migrationPlan = new MigrationPlanImpl(json.getString(SOURCE_PROCESS_DEFINITION_ID), json.getString(TARGET_PROCESS_DEFINITION_ID));

    migrationPlan.setInstructions(JsonUtil.jsonArrayAsList(json.getJSONArray(INSTRUCTIONS), MigrationInstructionJsonConverter.INSTANCE));

    return migrationPlan;
  }

}
