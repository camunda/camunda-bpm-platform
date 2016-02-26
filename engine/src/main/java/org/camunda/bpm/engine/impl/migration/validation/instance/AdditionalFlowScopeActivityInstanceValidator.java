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

package org.camunda.bpm.engine.impl.migration.validation.instance;

import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

public class AdditionalFlowScopeActivityInstanceValidator implements MigratingActivityInstanceValidator {

  public void validate(MigratingActivityInstance migratingInstance, MigratingProcessInstance migratingProcessInstance, MigratingActivityInstanceValidationReportImpl instanceReport) {
    MigratingActivityInstance ancestorInstance = getClosestPreservedAncestorInstance(migratingInstance);
    ScopeImpl targetScope = migratingInstance.getTargetScope();

    // note: this also detects and rejects horizontal migration. In that case, the parent activity instance
    // is migrated to a scope that is not present in this activity instance's target scope parent hierarchy
    if (ancestorInstance != null && targetScope != null && targetScope != targetScope.getProcessDefinition()) {
      ScopeImpl parentInstanceTargetScope = ancestorInstance.getTargetScope();
      if (parentInstanceTargetScope != null && !parentInstanceTargetScope.isAncestorFlowScopeOf(targetScope)) {
        instanceReport.addFailure("Closest migrating ancestor activity instance is migrated to activity '" +
          parentInstanceTargetScope.getId() + "' which is not an ancestor of target activity '" + targetScope.getId() + "'");
      }
    }

  }

  protected MigratingActivityInstance getClosestPreservedAncestorInstance(MigratingActivityInstance activityInstance) {
    MigratingActivityInstance parent = activityInstance.getParent();

    while (parent != null && parent.getTargetScope() == null) {
      parent = parent.getParent();
    }

    return parent;
  }

}
