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

package org.camunda.bpm.engine.impl.migration.validation;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationFailure;
import org.camunda.bpm.engine.migration.MigrationPlanValidationReport;

public class MigrationPlanValidationReportImpl implements MigrationPlanValidationReport {

  protected MigrationPlan migrationPlan;
  protected List<MigrationPlanValidationFailureImpl> validationFailures = new ArrayList<MigrationPlanValidationFailureImpl>();

  public MigrationPlanValidationReportImpl(MigrationPlan migrationPlan) {
    this.migrationPlan = migrationPlan;
  }

  public void addValidationFailure(MigrationInstruction migrationInstruction, String errorMessage) {
    validationFailures.add(new MigrationPlanValidationFailureImpl(migrationInstruction, errorMessage));
  }

  public boolean hasFailures() {
    return !validationFailures.isEmpty();
  }

  public MigrationPlan getMigrationPlan() {
    return migrationPlan;
  }

  @Override
  public List<MigrationPlanValidationFailure> getValidationFailures() {
    return (List) validationFailures;
  }

  public void writeTo(StringBuilder sb) {
    sb.append("Migration plan is not valid for process:\n");

    for (MigrationPlanValidationFailureImpl failure : validationFailures) {
      failure.writeTo(sb);
      sb.append("\n");
    }

  }

}
