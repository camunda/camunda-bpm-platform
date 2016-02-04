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
package org.camunda.bpm.engine.impl.migration.validation;

import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;

/**
 * @author Thorben Lindhauer
 *
 */
public class DefaultMigrationPlanValidator implements MigrationPlanValidator {

  public void validateMigrationPlan(ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition,
                                    MigrationPlan migrationPlan, MigrationPlanValidationReportImpl validationReport) {

    Set<String> alreadyMappedSourceActivityIds = new HashSet<String>();

    for (MigrationInstruction instruction : migrationPlan.getInstructions()) {
      try {
        validateMigrationInstruction(sourceProcessDefinition, targetProcessDefinition, instruction);
        validateEveryActivityIsOnlyOnceMapped(instruction, alreadyMappedSourceActivityIds);
      }
      catch (BadUserRequestException e) {
        validationReport.addValidationFailure(instruction, e.getMessage());
      }
    }

  }

  public void validateMigrationInstruction(ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition,
                                           MigrationInstruction instruction) {
    ensureOneToOneMapping(instruction, sourceProcessDefinition, targetProcessDefinition);
    ensureActivitiesCanBeMigrated(instruction, sourceProcessDefinition, targetProcessDefinition);
  }

  protected void validateEveryActivityIsOnlyOnceMapped(MigrationInstruction instruction, Set<String> alreadyMappedSourceActivityIds) {
    for (String sourceActivityId : instruction.getSourceActivityIds()) {
      if (alreadyMappedSourceActivityIds.contains(sourceActivityId)) {
        throw new BadUserRequestException("the source activity with id '" + sourceActivityId + "' was already mapped");
      }
      alreadyMappedSourceActivityIds.add(sourceActivityId);
    }
  }


  protected void ensureOneToOneMapping(MigrationInstruction instruction, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
    if (!MigrationInstructionValidators.ONE_TO_ONE_VALIDATOR.isInstructionValid(instruction, sourceProcessDefinition, targetProcessDefinition)) {
      throw new BadUserRequestException("only one to one mappings of existing activities are supported");
    }
  }

  protected void ensureActivitiesCanBeMigrated(MigrationInstruction instruction, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
    if (!MigrationInstructionValidators.ACTIVITIES_CAN_BE_MIGRATED.isInstructionValid(instruction, sourceProcessDefinition, targetProcessDefinition)) {
      throw new BadUserRequestException("the mapped activities are either null or not supported");
    }
  }

}
