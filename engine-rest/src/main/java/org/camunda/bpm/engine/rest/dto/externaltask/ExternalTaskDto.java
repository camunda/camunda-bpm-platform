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
package org.camunda.bpm.engine.rest.dto.externaltask;

import java.util.Date;

import org.camunda.bpm.engine.externaltask.ExternalTask;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskDto {

  protected String activityId;
  protected String activityInstanceId;
  protected String errorMessage;
  protected String executionId;
  protected String id;
  protected Date lockExpirationTime;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processInstanceId;
  protected Integer retries;
  protected boolean suspended;
  protected String workerId;
  protected String topicName;

  public String getActivityId() {
    return activityId;
  }
  public String getActivityInstanceId() {
    return activityInstanceId;
  }
  public String getErrorMessage() {
    return errorMessage;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getId() {
    return id;
  }
  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public Integer getRetries() {
    return retries;
  }
  public boolean isSuspended() {
    return suspended;
  }
  public String getWorkerId() {
    return workerId;
  }
  public String getTopicName() {
    return topicName;
  }

  public static ExternalTaskDto fromExternalTask(ExternalTask task) {
    ExternalTaskDto dto = new ExternalTaskDto();
    dto.activityId = task.getActivityId();
    dto.activityInstanceId = task.getActivityInstanceId();
    dto.errorMessage = task.getErrorMessage();
    dto.executionId = task.getExecutionId();
    dto.id = task.getId();
    dto.lockExpirationTime = task.getLockExpirationTime();
    dto.processDefinitionId = task.getProcessDefinitionId();
    dto.processDefinitionKey = task.getProcessDefinitionKey();
    dto.processInstanceId = task.getProcessInstanceId();
    dto.retries = task.getRetries();
    dto.suspended = task.isSuspended();
    dto.topicName = task.getTopicName();
    dto.workerId = task.getWorkerId();

    return dto;
  }

}
