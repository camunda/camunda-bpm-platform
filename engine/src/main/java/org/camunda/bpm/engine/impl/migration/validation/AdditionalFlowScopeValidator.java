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
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * Validates that an activity instance is migrated below its migrated parent activity instance
 * with at most one new flow scope in between
 *
 * @author Thorben Lindhauer
 */
public class AdditionalFlowScopeValidator implements MigrationInstructionInstanceValidator {

  @Override
  public void validate(MigratingProcessInstance migratingProcessInstance, MigratingActivityInstance migratingActivityInstance,
      MigrationInstructionInstanceValidationReport validationReport) {
    ActivityInstance activityInstance = migratingActivityInstance.getActivityInstance();
    MigratingActivityInstance parentInstance = migratingProcessInstance.getMigratingInstance(activityInstance.getParentActivityInstanceId());
    ScopeImpl targetScope = migratingActivityInstance.getTargetScope();

    if (parentInstance != null && targetScope != targetScope.getProcessDefinition()) {
      ScopeImpl parentInstanceTargetScope = parentInstance.getTargetScope();
      ScopeImpl flowScope = targetScope.getFlowScope();
      if (flowScope != parentInstanceTargetScope) {
        if (flowScope != flowScope.getProcessDefinition() && flowScope.getFlowScope() != parentInstanceTargetScope) {
          validationReport.addValidationFailure(migratingActivityInstance, "Parent activity instance must be migrated to"
              + " the parent or grandparent scope");
        }
      }
    }

  }

}
