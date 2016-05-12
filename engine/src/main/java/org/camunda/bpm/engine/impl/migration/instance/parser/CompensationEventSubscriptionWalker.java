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
package org.camunda.bpm.engine.impl.migration.instance.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.tree.ReferenceWalker;

/**
 * Ensures that event subscriptions are visited in a top-down fashion, i.e.
 * for a compensation handler in a scope that has an event scope execution, it is guaranteed
 * that first the scope subscription is visited, and then the compensation handler
 *
 * @author Thorben Lindhauer
 */
public class CompensationEventSubscriptionWalker extends ReferenceWalker<CompensateEventSubscriptionEntity> {

  public CompensationEventSubscriptionWalker(Collection<MigratingActivityInstance> collection) {
    super(collectCompensationEventSubscriptions(collection));
  }

  protected static List<CompensateEventSubscriptionEntity> collectCompensationEventSubscriptions(Collection<MigratingActivityInstance> activityInstances) {
    List<CompensateEventSubscriptionEntity> eventSubscriptions = new ArrayList<CompensateEventSubscriptionEntity>();
    for (MigratingActivityInstance activityInstance : activityInstances) {
      if (activityInstance.getSourceScope().isScope()) {
        ExecutionEntity scopeExecution = activityInstance.resolveRepresentativeExecution();
        eventSubscriptions.addAll(scopeExecution.getCompensateEventSubscriptions());
      }
    }
    return eventSubscriptions;
  }

  @Override
  protected Collection<CompensateEventSubscriptionEntity> nextElements() {
    CompensateEventSubscriptionEntity eventSubscriptionEntity = getCurrentElement();
    ExecutionEntity compensatingExecution = eventSubscriptionEntity.getCompensatingExecution();
    if (compensatingExecution != null) {
      return compensatingExecution.getCompensateEventSubscriptions();
    }
    else {
      return Collections.emptyList();
    }
  }

}
