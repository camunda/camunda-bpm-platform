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

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.db.PersistentObject;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;


/**
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceEntity extends HistoricScopeInstanceEntity implements HistoricTaskInstance, PersistentObject {

  private static final long serialVersionUID = 1L;
  
  protected String executionId;
  protected String name;
  protected String parentTaskId;
  protected String description;
  protected String owner;
  protected String assignee;
  protected String taskDefinitionKey;
  protected int priority;
  protected Date dueDate;
  protected String lastEvent;

  public HistoricTaskInstanceEntity() {
  }
  
  // persistence //////////////////////////////////////////////////////////////
  
  public Object getPersistentState() {    
    // immutable
    return HistoricTaskInstanceEntity.class;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  /** custom endTime behavior: only return end time if 
   * last history event closed the task instance.
   */
  @Override
  public Date getEndTime() {
    if(isEnded()) {      
      return endTime;
      
    } else {
      return null;
      
    }
  }
  
  /** custom endTime behavior: only return duration time if 
   * last history event closed the task instance.
   */
  @Override
  public Long getDurationInMillis() {
    if(isEnded()) {      
      return getDurationInMillis();
      
    } else {
      return null;
      
    }
  }

  protected boolean isEnded() {
    return HistoryEvent.TASK_EVENT_TYPE_COMPLETE.equals(lastEvent)
        || HistoryEvent.TASK_EVENT_TYPE_DELETE.equals(lastEvent);
  }

  public String getExecutionId() {
    return executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public String getAssignee() {
    return assignee;
  }
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }
  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }
  public int getPriority() {
    return priority;
  }
  public void setPriority(int priority) {
    this.priority = priority;
  }
  public Date getDueDate() {
    return dueDate;
  }
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }
  public String getOwner() {
    return owner;
  }
  public void setOwner(String owner) {
    this.owner = owner;
  }
  public String getParentTaskId() {
    return parentTaskId;
  }
  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }
}
