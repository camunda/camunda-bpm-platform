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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.migration.MigrationInstructionValidationReport;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationReport;

public class MigrationPlanValidationReportImpl implements MigrationPlanValidationReport {

  protected MigrationPlan migrationPlan;
  protected List<MigrationInstructionValidationReport> instructionReports = new ArrayList<MigrationInstructionValidationReport>();

  public MigrationPlanValidationReportImpl(MigrationPlan migrationPlan) {
    this.migrationPlan = migrationPlan;
  }

  public MigrationPlan getMigrationPlan() {
    return migrationPlan;
  }

  public void addInstructionReport(MigrationInstructionValidationReport instructionReport) {
    instructionReports.add(instructionReport);
  }

  public boolean hasInstructionReports() {
    return !instructionReports.isEmpty();
  }

  public List<MigrationInstructionValidationReport> getInstructionReports() {
    return instructionReports;
  }

  public void writeTo(StringBuilder sb) {
    sb.append("Migration plan for process definition '")
      .append(migrationPlan.getSourceProcessDefinitionId())
      .append("' to '")
      .append(migrationPlan.getTargetProcessDefinitionId())
      .append("' is not valid:\n");

    for (MigrationInstructionValidationReport instructionReport : instructionReports) {
      sb.append("\t Migration instruction ").append(instructionReport.getMigrationInstruction()).append(" is not valid:\n");
      for (String failure : instructionReport.getFailures()) {
        sb.append("\t\t").append(failure).append("\n");
      }
    }
  }

}
