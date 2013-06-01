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
package org.camunda.bpm.engine.impl.history.producer;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * <p>{@link HistoryEventProducer} for producing {@link HistoryEvent}s when a process 
 * instance is stated.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class HistoricProcessInstanceStartEventProducer extends HistoricProcessInstanceEventProducer {

  public HistoricProcessInstanceStartEventProducer() {
    super(HistoricActivityInstanceEventEntity.ACTIVITY_EVENT_TYPE_START);
  }
  
  protected void initEvent(DelegateExecution execution, HistoricActivityInstanceEventEntity evt) {

    final ExecutionEntity executionEntity = (ExecutionEntity) execution;
    final HistoricProcessInstanceEventEntity hpie = (HistoricProcessInstanceEventEntity) evt;

    // call common init behavior
    super.initEvent(execution, evt);
    
    // set super process instnace id
    ExecutionEntity superExecution = executionEntity.getSuperExecution();
    if(superExecution != null) {
      hpie.setSuperProcessInstanceId(superExecution.getProcessInstanceId());
    }
    
    // set start user Id
    hpie.setStartUserId(Authentication.getAuthenticatedUserId());
    
  }
  
}
