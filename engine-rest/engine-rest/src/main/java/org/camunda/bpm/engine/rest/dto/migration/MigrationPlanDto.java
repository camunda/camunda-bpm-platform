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

package org.camunda.bpm.engine.rest.dto.migration;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationInstructionBuilder;
import org.camunda.bpm.engine.migration.MigrationPlan;

public class MigrationPlanDto {

  protected String sourceProcessDefinitionId;
  protected String targetProcessDefinitionId;
  protected List<MigrationInstructionDto> instructions;

  public String getSourceProcessDefinitionId() {
    return sourceProcessDefinitionId;
  }

  public void setSourceProcessDefinitionId(String sourceProcessDefinitionId) {
    this.sourceProcessDefinitionId = sourceProcessDefinitionId;
  }

  public String getTargetProcessDefinitionId() {
    return targetProcessDefinitionId;
  }

  public void setTargetProcessDefinitionId(String targetProcessDefinitionId) {
    this.targetProcessDefinitionId = targetProcessDefinitionId;
  }

  public List<MigrationInstructionDto> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<MigrationInstructionDto> instructions) {
    this.instructions = instructions;
  }

  public static MigrationPlanDto from(MigrationPlan migrationPlan) {
    MigrationPlanDto dto = new MigrationPlanDto();

    dto.setSourceProcessDefinitionId(migrationPlan.getSourceProcessDefinitionId());
    dto.setTargetProcessDefinitionId(migrationPlan.getTargetProcessDefinitionId());

    ArrayList<MigrationInstructionDto> instructionDtos = new ArrayList<MigrationInstructionDto>();
    if (migrationPlan.getInstructions() != null) {
      for (MigrationInstruction migrationInstruction : migrationPlan.getInstructions()) {
        MigrationInstructionDto migrationInstructionDto = MigrationInstructionDto.from(migrationInstruction);
        instructionDtos.add(migrationInstructionDto);
      }
    }
    dto.setInstructions(instructionDtos);

    return dto;
  }

  public static MigrationPlan toMigrationPlan(ProcessEngine processEngine, MigrationPlanDto migrationPlanDto) {
    MigrationPlanBuilder migrationPlanBuilder = processEngine.getRuntimeService().createMigrationPlan(migrationPlanDto.getSourceProcessDefinitionId(), migrationPlanDto.getTargetProcessDefinitionId());

    if (migrationPlanDto.getInstructions() != null) {
      for (MigrationInstructionDto migrationInstructionDto : migrationPlanDto.getInstructions()) {
        MigrationInstructionBuilder migrationInstructionBuilder = migrationPlanBuilder.mapActivities(migrationInstructionDto.getSourceActivityIds().get(0), migrationInstructionDto.getTargetActivityIds().get(0));
        if (Boolean.TRUE.equals(migrationInstructionDto.isUpdateEventTrigger())) {
          migrationInstructionBuilder = migrationInstructionBuilder.updateEventTrigger();
        }

        migrationPlanBuilder = migrationInstructionBuilder;
      }
    }

    return migrationPlanBuilder.build();
  }

}
