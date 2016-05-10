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

package org.camunda.bpm.engine.rest.util.migration;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.rest.dto.migration.MigrationExecutionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationInstructionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationPlanDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;

public class MigrationExecutionDtoBuilder {

  protected final MigrationExecutionDto migrationExecutionDto;

  public MigrationExecutionDtoBuilder() {
    migrationExecutionDto = new MigrationExecutionDto();
  }

  public MigrationExecutionDtoBuilder processInstances(String... processInstanceIds) {
    migrationExecutionDto.setProcessInstanceIds(Arrays.asList(processInstanceIds));
    return this;
  }

  public MigrationExecutionDtoBuilder processInstanceQuery(ProcessInstanceQueryDto processInstanceQuery) {
    migrationExecutionDto.setProcessInstanceQuery(processInstanceQuery);
    return this;
  }

  public MigrationPlanExecutionDtoBuilder migrationPlan(String sourceProcessDefinitionId, String targetProcessDefinitionId) {
    return new MigrationPlanExecutionDtoBuilder(this, sourceProcessDefinitionId, targetProcessDefinitionId);
  }

  public MigrationExecutionDtoBuilder migrationPlan(MigrationPlanDto migrationPlanDto) {
    migrationExecutionDto.setMigrationPlan(migrationPlanDto);
    return this;
  }

  public MigrationExecutionDtoBuilder skipCustomListeners(boolean skipCustomListeners) {
    migrationExecutionDto.setSkipCustomListeners(skipCustomListeners);
    return this;
  }

  public MigrationExecutionDtoBuilder skipIoMappings(boolean skipIoMappings) {
    migrationExecutionDto.setSkipIoMappings(skipIoMappings);
    return this;
  }

  public MigrationExecutionDto build() {
    return migrationExecutionDto;
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
    public MigrationPlanExecutionDtoBuilder instructions(List<MigrationInstructionDto> instructions) {
      super.instructions(instructions);
      return this;
    }

    @Override
    public MigrationPlanDto build() {
      throw new UnsupportedOperationException("Please use the done() method to finish the migration plan building");
    }

    public MigrationExecutionDtoBuilder done() {
      MigrationPlanDto migrationPlanDto = super.build();
      return migrationExecutionDtoBuilder.migrationPlan(migrationPlanDto);
    }

  }

}
