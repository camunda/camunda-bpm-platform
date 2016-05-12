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

package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.event.CompensationEventHandler;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;



/**
 * @author Daniel Meyer
 */
public class CompensateEventSubscriptionEntity extends EventSubscriptionEntity {

  private static final long serialVersionUID = 1L;

  public CompensateEventSubscriptionEntity() {
  }

  private CompensateEventSubscriptionEntity(ExecutionEntity executionEntity) {
    super(executionEntity);
    eventType=CompensationEventHandler.EVENT_HANDLER_TYPE;
  }

  public static CompensateEventSubscriptionEntity createAndInsert(ExecutionEntity executionEntity, ActivityImpl activity) {
    CompensateEventSubscriptionEntity eventSubscription = new CompensateEventSubscriptionEntity(executionEntity);
    eventSubscription.setActivity(activity);
    eventSubscription.setTenantId(executionEntity.getTenantId());
    eventSubscription.insert();
    return eventSubscription;
  }

  // custom processing behavior //////////////////////////////////////////////////////////////////////////////

  @Override
  protected void processEventSync(Object payload) {
    delete();
    super.processEventSync(payload);
  }

  public CompensateEventSubscriptionEntity moveUnder(ExecutionEntity newExecution) {

    delete();

    CompensateEventSubscriptionEntity newSubscription = createAndInsert(newExecution, getActivity());
    newSubscription.setConfiguration(configuration);
    // use the original date
    newSubscription.setCreated(created);

    return newSubscription;
  }

  public ExecutionEntity getCompensatingExecution() {
    if (configuration != null) {
      return Context.getCommandContext().getExecutionManager().findExecutionById(configuration);
    }
    else {
      return null;
    }
  }

}
