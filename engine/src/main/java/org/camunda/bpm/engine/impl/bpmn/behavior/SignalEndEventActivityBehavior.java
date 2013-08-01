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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;


/**
 * @author Kristin Polenz
 */
public class SignalEndEventActivityBehavior extends FlowNodeActivityBehavior {
  
  protected EventSubscriptionDeclaration signalDefinition;
  
  public SignalEndEventActivityBehavior(EventSubscriptionDeclaration signalDefinition) {
    this.signalDefinition = signalDefinition;
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    
    CommandContext commandContext = Context.getCommandContext();
    
    List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByEventName = commandContext
        .getEventSubscriptionManager()
        .findSignalEventSubscriptionsByEventName(signalDefinition.getEventName());
      
      for (SignalEventSubscriptionEntity signalEventSubscriptionEntity : findSignalEventSubscriptionsByEventName) {
        signalEventSubscriptionEntity.eventReceived(null, signalDefinition.isAsync());
      }

    leave(execution);
  }
  
  public EventSubscriptionDeclaration getSignalDefinition() {
    return signalDefinition;
  }
  
  public void setSignalDefinition(EventSubscriptionDeclaration signalDefinition) {
    this.signalDefinition = signalDefinition;
  }
}
