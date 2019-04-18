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

import com.google.gson.JsonObject;
import org.camunda.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.migration.MigrationInstruction;

public class MigrationInstructionJsonConverter extends JsonObjectConverter<MigrationInstruction> {

  public static final MigrationInstructionJsonConverter INSTANCE = new MigrationInstructionJsonConverter();

  public static final String SOURCE_ACTIVITY_IDS = "sourceActivityIds";
  public static final String TARGET_ACTIVITY_IDS = "targetActivityIds";
  public static final String UPDATE_EVENT_TRIGGER = "updateEventTrigger";

  public JsonObject toJsonObject(MigrationInstruction instruction) {
    JsonObject json = JsonUtil.createObject();

    JsonUtil.addArrayField(json, SOURCE_ACTIVITY_IDS, new String[]{instruction.getSourceActivityId()});
    JsonUtil.addArrayField(json, TARGET_ACTIVITY_IDS, new String[]{instruction.getTargetActivityId()});
    JsonUtil.addField(json, UPDATE_EVENT_TRIGGER, instruction.isUpdateEventTrigger());

    return json;
  }

  public MigrationInstruction toObject(JsonObject json) {
    return new MigrationInstructionImpl(
      readSourceActivityId(json),
      readTargetActivityId(json),
      JsonUtil.getBoolean(json, UPDATE_EVENT_TRIGGER)
    );
  }

  protected String readSourceActivityId(JsonObject json) {
    return JsonUtil.getString(JsonUtil.getArray(json, SOURCE_ACTIVITY_IDS));
  }

  protected String readTargetActivityId(JsonObject json) {
    return JsonUtil.getString(JsonUtil.getArray(json, TARGET_ACTIVITY_IDS));
  }


}
