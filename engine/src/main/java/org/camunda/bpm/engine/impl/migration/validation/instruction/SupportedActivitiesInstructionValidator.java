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

import org.camunda.bpm.engine.impl.migration.validation.activity.HasNoEventSubProcessChildActivityValidator;
import org.camunda.bpm.engine.impl.migration.validation.activity.HasNoEventSubProcessParentActivityValidator;
import org.camunda.bpm.engine.impl.migration.validation.activity.NotMultiInstanceChildActivityValidator;
import org.camunda.bpm.engine.impl.migration.validation.activity.SupportedActivityValidator;
import org.camunda.bpm.engine.impl.migration.validation.activity.SupportedBoundaryEventActivityValidator;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

public class SupportedActivitiesInstructionValidator implements MigrationInstructionValidator {

  public void validate(ValidatingMigrationInstruction instruction, List<ValidatingMigrationInstruction> instructions, MigrationInstructionValidationReportImpl report) {
    ActivityImpl sourceActivity = instruction.getSourceActivity();
    validateSourceActivity(instruction, sourceActivity, report);

    ActivityImpl targetActivity = instruction.getTargetActivity();
    validateTargetActivity(instruction, targetActivity, report);
  }

  public void validateSourceActivity(ValidatingMigrationInstruction instruction, ActivityImpl activity, MigrationInstructionValidationReportImpl report) {
    if (!SupportedActivityValidator.INSTANCE.valid(activity)) {
      report.addFailure("Type of the source activity '" + activity.getId() + "' is not supported by the migration");
    }

    if (!SupportedBoundaryEventActivityValidator.INSTANCE.valid(activity)) {
      report.addFailure("Type of the source boundary event '" + activity.getId() + "' is not supported by migration");
    }

    if (!NotMultiInstanceChildActivityValidator.INSTANCE.valid(activity)) {
      report.addFailure("Source activity '" + activity.getId() + "' is child of a multi instances");
    }

    if (!HasNoEventSubProcessParentActivityValidator.INSTANCE.valid(activity)) {
      report.addFailure("Source activity '" + activity.getId() + "' is child of an event sub process");
    }

    if (!HasNoEventSubProcessChildActivityValidator.INSTANCE.valid(activity)) {
      report.addFailure("Source activity '" + activity.getId() + "' has an event sub process child");
    }
  }

  public void validateTargetActivity(ValidatingMigrationInstruction instruction, ActivityImpl activity, MigrationInstructionValidationReportImpl report) {
    if (!SupportedActivityValidator.INSTANCE.valid(activity)) {
      report.addFailure("Type of the target activity '" + activity.getId() + "' is not supported by the migration");
    }

    if (!SupportedBoundaryEventActivityValidator.INSTANCE.valid(activity)) {
      report.addFailure("Type of the target boundary event '" + activity.getId() + "' is not supported by migration");
    }

    if (!NotMultiInstanceChildActivityValidator.INSTANCE.valid(activity)) {
      report.addFailure("Target activity '" + activity.getId() + "' is child of a multi instances");
    }

    if (!HasNoEventSubProcessParentActivityValidator.INSTANCE.valid(activity)) {
      report.addFailure("Target activity '" + activity.getId() + "' is child of an event sub process");
    }

    if (!HasNoEventSubProcessChildActivityValidator.INSTANCE.valid(activity)) {
      report.addFailure("Target activity '" + activity.getId() + "' has an event sub process child");
    }
  }

}
