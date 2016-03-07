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

import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.util.StringUtil;

public class OnlyOnceMappedActivityInstructionValidator implements MigrationInstructionValidator {

  public void validate(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions, MigrationInstructionValidationReportImpl report) {
    ActivityImpl sourceActivity = instruction.getSourceActivity();
    List<ValidatingMigrationInstruction> instructionsForSourceActivity = instructions.getInstructionsBySourceScope(sourceActivity);

    if (instructionsForSourceActivity.size() > 1) {
      addFailure(sourceActivity.getId(), instructionsForSourceActivity, report);
    }
  }

  protected void addFailure(String sourceActivityId, List<ValidatingMigrationInstruction> migrationInstructions, MigrationInstructionValidationReportImpl report) {
    report.addFailure("There are multiple mappings for source activity id '" + sourceActivityId +"': " +
      StringUtil.join(new StringUtil.StringIterator<ValidatingMigrationInstruction>(migrationInstructions.iterator()) {
        public String next() {
          return iterator.next().toString();
        }
      }));
  }

}
