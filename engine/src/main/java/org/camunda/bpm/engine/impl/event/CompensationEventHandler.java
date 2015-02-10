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

package org.camunda.bpm.engine.impl.event;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.bpmn.helper.CompensationUtil;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.AtomicOperation;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Daniel Meyer
 */
public class CompensationEventHandler implements EventHandler {
  
  public final static String EVENT_HANDLER_TYPE = "compensate";

  public String getEventHandlerType() {
    return EVENT_HANDLER_TYPE;
  }

  public void handleEvent(EventSubscriptionEntity eventSubscription, Object payload, CommandContext commandContext) {

    String configuration = eventSubscription.getConfiguration();
    ensureNotNull("Compensating execution not set for compensate event subscription with id " + eventSubscription.getId(), "configuration", configuration);

    ExecutionEntity compensatingExecution = commandContext.getExecutionManager()
      .findExecutionById(configuration);

    ActivityImpl compensationHandler = eventSubscription.getActivity();

    if ((compensationHandler.getProperty(BpmnParse.PROPERTYNAME_IS_FOR_COMPENSATION) == null
      || !(Boolean) compensationHandler.getProperty(BpmnParse.PROPERTYNAME_IS_FOR_COMPENSATION))
      && compensationHandler.isScope()) {

      // activate execution
      compensatingExecution.setActive(true);
      // descend into scope:
      List<CompensateEventSubscriptionEntity> eventsForThisScope = compensatingExecution.getCompensateEventSubscriptions();
      CompensationUtil.throwCompensationEvent(eventsForThisScope, compensatingExecution, false);

    } else {
      try {

        compensatingExecution.setActivity(compensationHandler);

        // executing the atomic operation makes sure activity start events are fired
        compensatingExecution.performOperation(AtomicOperation.ACTIVITY_START);

      } catch (Exception e) {
        throw new ProcessEngineException("Error while handling compensation event " + eventSubscription, e);
      }

    }
  }

}
