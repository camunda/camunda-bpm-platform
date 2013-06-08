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
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricScopeInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * <p>Producer for {@link HistoricActivityInstanceEventEntity}s.</p>
 * 
 * <p>This producer is configured with an {@link #eventType} property which may be
 * one of the types defined in {@link HistoricActivityInstanceEventEntity}
 * (start, end, cancel, ...)</p>
 * 
 * @author Daniel Meyer
 * @author Marcel Wieczorek
 * 
 */
public class HistoricActivityInstanceEventProducer extends HistoricScopeInstanceEventProducer {

  public HistoricActivityInstanceEventProducer(String eventType) {
    super(eventType);
  }

  /** creates the actual event instance. */
  protected HistoricActivityInstanceEventEntity createEventInstance(DelegateExecution execution) {
    return new HistoricActivityInstanceEventEntity();
  }

  @Override
  public HistoryEvent createHistoryEvent(DelegateExecution execution, DelegateTask task) {
    HistoricActivityInstanceEventEntity eventInstance = (HistoricActivityInstanceEventEntity) createHistoryEvent(execution);
    String id = task.getId();
    String assignee = task.getAssignee();
    eventInstance.setTaskId(id);
    eventInstance.setTaskAssignee(assignee);

    return eventInstance;
  }

  /** initializes the event */
  protected void initEvent(DelegateExecution execution, HistoricScopeInstanceEventEntity evt) {
    super.initEvent(execution, evt);

    final ExecutionEntity executionEntity = (ExecutionEntity) execution;
    final HistoricActivityInstanceEventEntity activityInstance = (HistoricActivityInstanceEventEntity) evt;

    final ExecutionEntity subProcessInstance = executionEntity.getSubProcessInstance();
    if (subProcessInstance != null) {
      activityInstance.setCalledProcessInstanceId(subProcessInstance.getId());
    }
  }

}
