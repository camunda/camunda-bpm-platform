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
package org.camunda.bpm.engine.rest.helper;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockExternalTaskBuilder {

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
  protected String topicName;
  protected String workerId;
  protected String tenantId;
  protected VariableMap variables = Variables.createVariables();
  protected long priority;

  public MockExternalTaskBuilder activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  public MockExternalTaskBuilder activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public MockExternalTaskBuilder errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  public MockExternalTaskBuilder executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public MockExternalTaskBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockExternalTaskBuilder lockExpirationTime(Date lockExpirationTime) {
    this.lockExpirationTime = lockExpirationTime;
    return this;
  }

  public MockExternalTaskBuilder processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public MockExternalTaskBuilder processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  public MockExternalTaskBuilder processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public MockExternalTaskBuilder retries(Integer retries) {
    this.retries = retries;
    return this;
  }

  public MockExternalTaskBuilder suspended(boolean suspended) {
    this.suspended = suspended;
    return this;
  }

  public MockExternalTaskBuilder topicName(String topicName) {
    this.topicName = topicName;
    return this;
  }

  public MockExternalTaskBuilder workerId(String workerId) {
    this.workerId = workerId;
    return this;
  }

  public MockExternalTaskBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public MockExternalTaskBuilder variable(String variableName, TypedValue value) {
    this.variables.putValueTyped(variableName, value);
    return this;
  }
  
  public MockExternalTaskBuilder priority(long priority) {
    this.priority = priority;
    return this;
  }

  public ExternalTask buildExternalTask() {
    ExternalTask task = mock(ExternalTask.class);
    when(task.getActivityId()).thenReturn(activityId);
    when(task.getActivityInstanceId()).thenReturn(activityInstanceId);
    when(task.getErrorMessage()).thenReturn(errorMessage);
    when(task.getExecutionId()).thenReturn(executionId);
    when(task.getId()).thenReturn(id);
    when(task.getLockExpirationTime()).thenReturn(lockExpirationTime);
    when(task.getProcessDefinitionId()).thenReturn(processDefinitionId);
    when(task.getProcessDefinitionKey()).thenReturn(processDefinitionKey);
    when(task.getProcessInstanceId()).thenReturn(processInstanceId);
    when(task.getRetries()).thenReturn(retries);
    when(task.isSuspended()).thenReturn(suspended);
    when(task.getTopicName()).thenReturn(topicName);
    when(task.getWorkerId()).thenReturn(workerId);
    when(task.getTenantId()).thenReturn(tenantId);
    when(task.getPriority()).thenReturn(priority);

    return task;
  }

  public LockedExternalTask buildLockedExternalTask() {
    LockedExternalTask task = mock(LockedExternalTask.class);
    when(task.getActivityId()).thenReturn(activityId);
    when(task.getActivityInstanceId()).thenReturn(activityInstanceId);
    when(task.getErrorMessage()).thenReturn(errorMessage);
    when(task.getExecutionId()).thenReturn(executionId);
    when(task.getId()).thenReturn(id);
    when(task.getLockExpirationTime()).thenReturn(lockExpirationTime);
    when(task.getProcessDefinitionId()).thenReturn(processDefinitionId);
    when(task.getProcessDefinitionKey()).thenReturn(processDefinitionKey);
    when(task.getProcessInstanceId()).thenReturn(processInstanceId);
    when(task.getRetries()).thenReturn(retries);
    when(task.getTopicName()).thenReturn(topicName);
    when(task.getWorkerId()).thenReturn(workerId);
    when(task.getTenantId()).thenReturn(tenantId);
    when(task.getVariables()).thenReturn(variables);
    when(task.getPriority()).thenReturn(priority);

    return task;

  }

}
