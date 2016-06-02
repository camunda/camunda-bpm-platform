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
package org.camunda.bpm.engine.cdi.impl.event;

import org.camunda.bpm.engine.cdi.BusinessProcessEvent;
import org.camunda.bpm.engine.cdi.BusinessProcessEventType;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;

import java.util.Date;

/**
 *
 * @author Daniel Meyer
 */
public class CdiBusinessProcessEvent implements BusinessProcessEvent {

  protected final String activityId;
  protected final ProcessDefinition processDefinition;
  protected final String transitionName;
  protected final String processInstanceId;
  protected final String executionId;
  protected final DelegateTask delegateTask;
  protected final BusinessProcessEventType type;
  protected final Date timeStamp;

  public CdiBusinessProcessEvent(String activityId,
                                     String transitionName,
                                     ProcessDefinition processDefinition,
                                     DelegateExecution execution,
                                     BusinessProcessEventType type,
                                     Date timeStamp) {
      this.activityId = activityId;
      this.transitionName = transitionName;
      this.processInstanceId = execution.getProcessInstanceId();
      this.executionId = execution.getId();
      this.type = type;
      this.timeStamp = timeStamp;
      this.processDefinition = processDefinition;
      this.delegateTask = null;
  }

  public CdiBusinessProcessEvent(DelegateTask task, ProcessDefinitionEntity processDefinition, BusinessProcessEventType type, Date timeStamp) {
    this.activityId = null;
    this.transitionName = null;
    this.processInstanceId = task.getProcessInstanceId();
    this.executionId = task.getExecutionId();
    this.type = type;
    this.timeStamp = timeStamp;
    this.processDefinition = processDefinition;
    this.delegateTask = task;
  }

  @Override
  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }

  @Override
  public String getActivityId() {
    return activityId;
  }

  @Override
  public String getTransitionName() {
    return transitionName;
  }

  @Override
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  @Override
  public String getExecutionId() {
    return executionId;
  }

  @Override
  public BusinessProcessEventType getType() {
    return type;
  }

  @Override
  public Date getTimeStamp() {
    return timeStamp;
  }

  @Override
  public DelegateTask getTask() {
    return delegateTask;
  }

  @Override
  public String getTaskId() {
    if (delegateTask != null) {
      return delegateTask.getId();
    }
    return null;
  }

  @Override
  public String getTaskDefinitionKey() {
    if (delegateTask != null) {
      return delegateTask.getTaskDefinitionKey();
    }
    return null;
  }

  @Override
  public String toString() {
    return "Event '" + processDefinition.getKey() + "' ['" + type + "', " + (type == BusinessProcessEventType.TAKE ? transitionName : activityId) + "]";
  }

}
