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
package org.camunda.bpm.engine.impl.audit.producer;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.audit.ActivityInstanceAuditEvent;
import org.camunda.bpm.engine.impl.audit.AuditEvent;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * <p>Producer for {@link ActivityInstanceAuditEvent}s. This producer is configured with
 * an {@link #eventType} property which may be one of the types defined in 
 * {@link ActivityInstanceAuditEvent} (start, end, cancel, ...)</p>
 *  
 * @author Daniel Meyer
 *
 */
public class ActivityAuditEventProducer extends AbstractAuditEventProducer {
  
  protected final String eventType;

  public ActivityAuditEventProducer(String eventType) {
    this.eventType = eventType;
  }

  protected AuditEvent createAuditEvent(DelegateExecution execution) {
    
    ActivityInstanceAuditEvent evt = new ActivityInstanceAuditEvent();    
    
    // initialize the event
    initAuditEvent(evt, execution);
    
    return evt;
  }
  
  protected void initAuditEvent(ActivityInstanceAuditEvent evt, DelegateExecution execution) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    super.initAuditEvent(evt, execution);
          
    evt.setActivityId(executionEntity.getActivityId());
    evt.setActivityName((String) executionEntity.getActivity().getProperty("name"));
    evt.setActivityType((String) executionEntity.getActivity().getProperty("type"));
    
    // set the event type
    evt.setEventType(eventType);
  }

}
