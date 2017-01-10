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
package org.camunda.bpm.engine.rest.dto.history;

import org.camunda.bpm.engine.history.HistoricExternalTaskLog;

import java.util.Date;

public class HistoricExternalTaskLogDto {

  protected String id;
  protected Date timestamp;

  protected String externalTaskId;
  protected String topicName;
  protected String workerId;
  protected long priority;
  protected Integer retries;
  protected String errorMessage;

  protected String activityId;
  protected String activityInstanceId;
  protected String executionId;

  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String tenantId;

  protected boolean creationLog;
  protected boolean failureLog;
  protected boolean successLog;
  protected boolean deletionLog;

  public String getId() {
    return id;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public String getExternalTaskId() {
    return externalTaskId;
  }

  public String getTopicName() {
    return topicName;
  }

  public String getWorkerId() {
    return workerId;
  }

  public long getPriority() {
    return priority;
  }

  public Integer getRetries() {
    return retries;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getTenantId() {
    return tenantId;
  }

  public boolean isCreationLog() {
    return creationLog;
  }

  public boolean isFailureLog() {
    return failureLog;
  }

  public boolean isSuccessLog() {
    return successLog;
  }

  public boolean isDeletionLog() {
    return deletionLog;
  }

  public static HistoricExternalTaskLogDto fromHistoricExternalTaskLog(HistoricExternalTaskLog historicExternalTaskLog) {
    HistoricExternalTaskLogDto result = new HistoricExternalTaskLogDto();

    result.id = historicExternalTaskLog.getId();
    result.timestamp = historicExternalTaskLog.getTimestamp();

    result.externalTaskId = historicExternalTaskLog.getExternalTaskId();
    result.topicName = historicExternalTaskLog.getTopicName();
    result.workerId = historicExternalTaskLog.getWorkerId();
    result.priority = historicExternalTaskLog.getPriority();
    result.retries = historicExternalTaskLog.getRetries();
    result.errorMessage = historicExternalTaskLog.getErrorMessage();

    result.activityId = historicExternalTaskLog.getActivityId();
    result.activityInstanceId = historicExternalTaskLog.getActivityInstanceId();
    result.executionId = historicExternalTaskLog.getExecutionId();

    result.processInstanceId = historicExternalTaskLog.getProcessInstanceId();
    result.processDefinitionId = historicExternalTaskLog.getProcessDefinitionId();
    result.processDefinitionKey = historicExternalTaskLog.getProcessDefinitionKey();
    result.tenantId = historicExternalTaskLog.getTenantId();

    result.creationLog = historicExternalTaskLog.isCreationLog();
    result.failureLog = historicExternalTaskLog.isFailureLog();
    result.successLog = historicExternalTaskLog.isSuccessLog();
    result.deletionLog = historicExternalTaskLog.isDeletionLog();

    return result;
  }
}
