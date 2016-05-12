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
package org.camunda.bpm.engine.impl.migration.instance;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingActivityInstanceVisitor extends MigratingProcessElementInstanceVisitor {

  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;

  public MigratingActivityInstanceVisitor(boolean skipCustomListeners, boolean skipIoMappings) {
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMappings = skipIoMappings;
  }

  @Override
  protected boolean canMigrate(MigratingProcessElementInstance instance) {
    return instance instanceof MigratingActivityInstance
        || instance instanceof MigratingTransitionInstance;
  }

  protected void instantiateScopes(
      MigratingScopeInstance ancestorScopeInstance,
      MigratingScopeInstanceBranch executionBranch,
      List<ScopeImpl> scopesToInstantiate) {

    if (scopesToInstantiate.isEmpty()) {
      return;
    }

    // must always be an activity instance
    MigratingActivityInstance ancestorActivityInstance = (MigratingActivityInstance) ancestorScopeInstance;

    ExecutionEntity newParentExecution = ancestorActivityInstance.createAttachableExecution();

    Map<PvmActivity, PvmExecutionImpl> createdExecutions =
        newParentExecution.instantiateScopes((List) scopesToInstantiate, skipCustomListeners, skipIoMappings);

    for (ScopeImpl scope : scopesToInstantiate) {
      ExecutionEntity createdExecution = (ExecutionEntity) createdExecutions.get(scope);
      createdExecution.setActivity(null);
      createdExecution.setActive(false);
      executionBranch.visited(new MigratingActivityInstance(scope, createdExecution));
    }
  }

}
