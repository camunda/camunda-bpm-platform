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
package org.camunda.bpm.engine.rest.util.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MigrationPlanDtoBuilder {

  public static final String PROP_SOURCE_PROCESS_DEFINITION_ID = "sourceProcessDefinitionId";
  public static final String PROP_TARGET_PROCESS_DEFINITION_ID = "targetProcessDefinitionId";
  public static final String PROP_INSTRUCTIONS = "instructions";

  protected final Map<String, Object> migrationPlan;

  public MigrationPlanDtoBuilder(String sourceProcessDefinitionId, String targetProcessDefinitionId) {
    migrationPlan = new HashMap<String, Object>();
    migrationPlan.put(PROP_SOURCE_PROCESS_DEFINITION_ID, sourceProcessDefinitionId);
    migrationPlan.put(PROP_TARGET_PROCESS_DEFINITION_ID, targetProcessDefinitionId);
  }

  public MigrationPlanDtoBuilder instructions(List<Map<String, Object>> instructions) {
    migrationPlan.put(PROP_INSTRUCTIONS, instructions);
    return this;
  }

  public MigrationPlanDtoBuilder instruction(String sourceActivityId, String targetActivityId) {
    return instruction(sourceActivityId, targetActivityId, null);
  }

  public MigrationPlanDtoBuilder instruction(String sourceActivityId, String targetActivityId, Boolean updateEventTrigger) {
    List<Map<String, Object>> instructions = (List<Map<String, Object>>) migrationPlan.get(PROP_INSTRUCTIONS);
    if (instructions == null) {
      instructions = new ArrayList<Map<String, Object>>();
      migrationPlan.put(PROP_INSTRUCTIONS, instructions);
    }

    Map<String, Object> migrationInstruction = new MigrationInstructionDtoBuilder()
      .migrate(sourceActivityId, targetActivityId, updateEventTrigger)
      .build();

    instructions.add(migrationInstruction);
    return this;
  }

  public Map<String, Object> build() {
    return migrationPlan;
  }
}
