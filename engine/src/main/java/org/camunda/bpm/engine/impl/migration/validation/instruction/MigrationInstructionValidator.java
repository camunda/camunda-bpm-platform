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

import org.camunda.bpm.engine.migration.MigrationInstructionValidationReport;

/**
 * Checks that a migration instruction is valid for the
 * migration plan. For example if the instruction migrates
 * an activity to a different type.
 */
public interface MigrationInstructionValidator {

  /**
   * Check that a migration instruction is valid for a migration plan. If it is invalid
   * a failure has to added to the validation report.
   *
   *  @param instruction the instruction to validate
   * @param instructions the complete migration plan to validate
   * @param report the validation report
   */
  void validate(ValidatingMigrationInstruction instruction, List<ValidatingMigrationInstruction> instructions, MigrationInstructionValidationReportImpl report);

}
