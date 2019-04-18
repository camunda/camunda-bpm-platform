/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.event;


import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.bpmn.helper.CompensationUtil;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Daniel Meyer
 */
public class CompensationEventHandler implements EventHandler {

  @Override
  public String getEventHandlerType() {
    return EventType.COMPENSATE.name();
  }

  @Override
  public void handleEvent(EventSubscriptionEntity eventSubscription, Object payload, Object localPayload, String businessKey, CommandContext commandContext) {
    eventSubscription.delete();

    String configuration = eventSubscription.getConfiguration();
    ensureNotNull("Compensating execution not set for compensate event subscription with id " + eventSubscription.getId(), "configuration", configuration);

    ExecutionEntity compensatingExecution = commandContext.getExecutionManager().findExecutionById(configuration);

    ActivityImpl compensationHandler = eventSubscription.getActivity();

    // activate execution
    compensatingExecution.setActive(true);

    if (compensatingExecution.getActivity().getActivityBehavior() instanceof CompositeActivityBehavior) {
      compensatingExecution.getParent().setActivityInstanceId(compensatingExecution.getActivityInstanceId());
    }

    if (compensationHandler.isScope() && !compensationHandler.isCompensationHandler()) {
      // descend into scope:
      List<EventSubscriptionEntity> eventsForThisScope = compensatingExecution.getCompensateEventSubscriptions();
      CompensationUtil.throwCompensationEvent(eventsForThisScope, compensatingExecution, false);

    } else {
      try {


        if (compensationHandler.isSubProcessScope() && compensationHandler.isTriggeredByEvent()) {
          compensatingExecution.executeActivity(compensationHandler);
        }
        else {
          // since we already have a scope execution, we don't need to create another one
          // for a simple scoped compensation handler
          compensatingExecution.setActivity(compensationHandler);
          compensatingExecution.performOperation(PvmAtomicOperation.ACTIVITY_START);
        }


      } catch (Exception e) {
        throw new ProcessEngineException("Error while handling compensation event " + eventSubscription, e);
      }
    }
  }

}
