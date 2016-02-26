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
package org.camunda.bpm.engine.migration;

import java.util.List;

/**
 * Collects the migration instruction validation reports for
 * all instructions of the migration plan which contain failures.
 */
public interface MigrationPlanValidationReport {

  /**
   * @return the migration plan of the validation report
   */
  MigrationPlan getMigrationPlan();

  /**
   * @return true if instructions reports exist, false otherwise
   */
  boolean hasInstructionReports();

  /**
   * @return all instruction reports
   */
  List<MigrationInstructionValidationReport> getInstructionReports();

}
