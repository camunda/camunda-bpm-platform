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

package org.camunda.bpm.engine.impl.migration.validation.instruction;

import java.util.List;

import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

public class SameEventScopeInstructionValidator implements MigrationInstructionValidator {

  public void validate(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions, MigrationInstructionValidationReportImpl report) {
    ScopeImpl sourceEventScope = instruction.getSourceActivity().getEventScope();
    ScopeImpl targetEventScope = instruction.getTargetActivity().getEventScope();

    if (sourceEventScope == null && targetEventScope == null) {
      return;
    }

    if (targetEventScope == null) {
      addFailure(instruction, report);
    }
    else {
      ScopeImpl mappedSourceEventScope = findMappedEventScope(sourceEventScope, instructions);
      if (mappedSourceEventScope == null || !mappedSourceEventScope.getId().equals(targetEventScope.getId())) {
        addFailure(instruction, report);
      }
    }
  }

  protected ScopeImpl findMappedEventScope(ScopeImpl sourceEventScope, ValidatingMigrationInstructions instructions) {
    if (sourceEventScope != null) {
      List<ValidatingMigrationInstruction> eventScopeInstructions = instructions.getInstructionsBySourceScope(sourceEventScope);
      if (eventScopeInstructions.size() > 0) {
        return eventScopeInstructions.get(0).getTargetActivity();
      }
    }
    return null;
  }

  protected void addFailure(ValidatingMigrationInstruction instruction, MigrationInstructionValidationReportImpl report) {
    report.addFailure("Event scope of the activity has changed and wasn't migrated");
  }

}
