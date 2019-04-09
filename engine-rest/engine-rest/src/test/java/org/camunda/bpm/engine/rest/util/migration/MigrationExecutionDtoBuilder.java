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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;

public class MigrationExecutionDtoBuilder {

  public static final String PROP_PROCESS_INSTANCE_IDS = "processInstanceIds";
  public static final String PROP_PROCESS_INSTANCE_QUERY = "processInstanceQuery";
  public static final String PROP_MIGRATION_PLAN = "migrationPlan";
  public static final String PROP_SKIP_CUSTOM_LISTENERS = "skipCustomListeners";
  public static final String PROP_SKIP_IO_MAPPINGS = "skipIoMappings";

  protected final Map<String, Object> migrationExecution;

  public MigrationExecutionDtoBuilder() {
    migrationExecution = new HashMap<String, Object>();
  }

  public MigrationExecutionDtoBuilder processInstances(String... processInstanceIds) {
    migrationExecution.put(PROP_PROCESS_INSTANCE_IDS, Arrays.asList(processInstanceIds));
    return this;
  }

  public MigrationExecutionDtoBuilder processInstanceQuery(ProcessInstanceQueryDto processInstanceQuery) {
    migrationExecution.put(PROP_PROCESS_INSTANCE_QUERY, processInstanceQuery);
    return this;
  }

  public MigrationPlanExecutionDtoBuilder migrationPlan(String sourceProcessDefinitionId, String targetProcessDefinitionId) {
    return new MigrationPlanExecutionDtoBuilder(this, sourceProcessDefinitionId, targetProcessDefinitionId);
  }

  public MigrationExecutionDtoBuilder migrationPlan(Map<String, Object> migrationPlan) {
    migrationExecution.put(PROP_MIGRATION_PLAN, migrationPlan);
    return this;
  }

  public MigrationExecutionDtoBuilder skipCustomListeners(boolean skipCustomListeners) {
    migrationExecution.put(PROP_SKIP_CUSTOM_LISTENERS, skipCustomListeners);
    return this;
  }

  public MigrationExecutionDtoBuilder skipIoMappings(boolean skipIoMappings) {
    migrationExecution.put(PROP_SKIP_IO_MAPPINGS, skipIoMappings);
    return this;
  }

  public Map<String, Object> build() {
    return migrationExecution;
  }

  public class MigrationPlanExecutionDtoBuilder extends MigrationPlanDtoBuilder {

    protected final MigrationExecutionDtoBuilder migrationExecutionDtoBuilder;

    public MigrationPlanExecutionDtoBuilder(MigrationExecutionDtoBuilder migrationExecutionDtoBuilder, String sourceProcessDefinitionId, String targetProcessDefinitionId) {
      super(sourceProcessDefinitionId, targetProcessDefinitionId);
      this.migrationExecutionDtoBuilder = migrationExecutionDtoBuilder;
    }

    @Override
    public MigrationPlanExecutionDtoBuilder instruction(String sourceActivityId, String targetActivityId) {
      super.instruction(sourceActivityId, targetActivityId);
      return this;
    }

    @Override
    public MigrationPlanExecutionDtoBuilder instruction(String sourceActivityId, String targetActivityId, Boolean updateEventTrigger) {
      super.instruction(sourceActivityId, targetActivityId, updateEventTrigger);
      return this;
    }

    @Override
    public MigrationPlanExecutionDtoBuilder instructions(List<Map<String, Object>> instructions) {
      super.instructions(instructions);
      return this;
    }

    @Override
    public Map<String, Object> build() {
      throw new UnsupportedOperationException("Please use the done() method to finish the migration plan building");
    }

    public MigrationExecutionDtoBuilder done() {
      Map<String, Object> migrationPlan = super.build();
      return migrationExecutionDtoBuilder.migrationPlan(migrationPlan);
    }
  }

}
