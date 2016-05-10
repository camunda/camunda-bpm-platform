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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.rest.dto.migration.MigrationInstructionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationPlanDto;

public class MigrationPlanDtoBuilder {

  protected final MigrationPlanDto migrationPlanDto;

  public MigrationPlanDtoBuilder(String sourceProcessDefinitionId, String targetProcessDefinitionId) {
    migrationPlanDto = new MigrationPlanDto();
    migrationPlanDto.setSourceProcessDefinitionId(sourceProcessDefinitionId);
    migrationPlanDto.setTargetProcessDefinitionId(targetProcessDefinitionId);
  }

  public MigrationPlanDtoBuilder instructions(List<MigrationInstructionDto> instructions) {
    migrationPlanDto.setInstructions(instructions);
    return this;
  }

  public MigrationPlanDtoBuilder instruction(String sourceActivityId, String targetActivityId) {
    return instruction(sourceActivityId, targetActivityId, null);
  }

  public MigrationPlanDtoBuilder instruction(String sourceActivityId, String targetActivityId, Boolean updateEventTrigger) {
    List<MigrationInstructionDto> instructions = migrationPlanDto.getInstructions();
    if (instructions == null) {
      instructions = new ArrayList<MigrationInstructionDto>();
      migrationPlanDto.setInstructions(instructions);
    }

    MigrationInstructionDto migrationInstruction = new MigrationInstructionDtoBuilder()
      .migrate(sourceActivityId, targetActivityId, updateEventTrigger)
      .build();

    instructions.add(migrationInstruction);
    return this;
  }

  public MigrationPlanDto build() {
    return migrationPlanDto;
  }

}
