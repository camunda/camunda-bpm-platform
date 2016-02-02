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

import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class RemoveFlowScopeValidator implements MigrationInstructionInstanceValidator {

  @Override
  public void validate(MigratingProcessInstance migratingProcessInstance, MigratingActivityInstance migratingActivityInstance,
      MigrationInstructionInstanceValidationReport validationReport) {
    ActivityInstance activityInstance = migratingActivityInstance.getActivityInstance();

    if (!isProcessInstance(activityInstance)) {
      MigratingActivityInstance parentInstance = migratingProcessInstance.getMigratingInstance(activityInstance.getParentActivityInstanceId());

      // at the moment, an activity instance to which no instruction applies is not part of the
      // migrating activity instance
      if (parentInstance == null) {
        validationReport.addValidationFailure(migratingActivityInstance, "The parent activity instance is not being migrated");
      }
    }
  }

  protected boolean isProcessInstance(ActivityInstance activityInstance) {
    return activityInstance.getParentActivityInstanceId() == null;
  }

}
