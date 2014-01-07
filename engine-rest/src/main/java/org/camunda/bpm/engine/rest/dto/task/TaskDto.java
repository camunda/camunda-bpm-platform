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
package org.camunda.bpm.engine.rest.dto.task;

import java.util.Date;

import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;

public class TaskDto {

  private String id;
  private String name;
  private String assignee;
  private Date created;
  private Date due;
  private Date followUp;
  private DelegationState delegationState;
  private String description;
  private String executionId;
  private String owner;
  private String parentTaskId;
  private int priority;
  private String processDefinitionId;
  private String processInstanceId;
  private String taskDefinitionKey;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }
  
  public String getAssignee() {
    return assignee;
  }

  public Date getCreated() {
    return created;
  }

  public Date getDue() {
    return due;
  }

  public DelegationState getDelegationState() {
    return delegationState;
  }

  public String getDescription() {
    return description;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getOwner() {
    return owner;
  }

  public String getParentTaskId() {
    return parentTaskId;
  }

  public int getPriority() {
    return priority;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public Date getFollowUp() {
    return followUp;
  }

  public static TaskDto fromTask(Task task) {
    TaskDto dto = new TaskDto();
    dto.id = task.getId();
    dto.name = task.getName();
    dto.assignee = task.getAssignee();
    dto.created = task.getCreateTime();
    dto.due = task.getDueDate();
    dto.followUp = task.getFollowUpDate();
    dto.delegationState = task.getDelegationState();
    dto.description = task.getDescription();
    dto.executionId = task.getExecutionId();
    dto.owner = task.getOwner();
    dto.parentTaskId = task.getParentTaskId();
    dto.priority = task.getPriority();
    dto.processDefinitionId = task.getProcessDefinitionId();
    dto.processInstanceId = task.getProcessInstanceId();
    dto.taskDefinitionKey = task.getTaskDefinitionKey();
    return dto;
  }
}
