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
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationCompensationInstanceVisitor extends MigratingProcessElementInstanceVisitor {

  @Override
  protected boolean canMigrate(MigratingProcessElementInstance instance) {
    return instance instanceof MigratingEventScopeInstance
        || instance instanceof MigratingCompensationEventSubscriptionInstance;
  }

  @Override
  protected void instantiateScopes(
      MigratingScopeInstance ancestorScopeInstance,
      MigratingScopeInstanceBranch executionBranch,
      List<ScopeImpl> scopesToInstantiate) {

    if (scopesToInstantiate.isEmpty()) {
      return;
    }

    ExecutionEntity ancestorScopeExecution = ancestorScopeInstance.resolveRepresentativeExecution();

    ExecutionEntity parentExecution = ancestorScopeExecution;

    for (ScopeImpl scope : scopesToInstantiate) {
      ExecutionEntity compensationScopeExecution = parentExecution.createExecution();
      compensationScopeExecution.setScope(true);
      compensationScopeExecution.setEventScope(true);

      compensationScopeExecution.setActivity((PvmActivity) scope);
      compensationScopeExecution.setActive(false);
      compensationScopeExecution.activityInstanceStarting();
      compensationScopeExecution.enterActivityInstance();

      EventSubscriptionEntity eventSubscription = EventSubscriptionEntity.createAndInsert(parentExecution, EventType.COMPENSATE, (ActivityImpl) scope);
      eventSubscription.setConfiguration(compensationScopeExecution.getId());

      executionBranch.visited(new MigratingEventScopeInstance(eventSubscription, compensationScopeExecution, scope));

      parentExecution = compensationScopeExecution;
    }

  }

}
