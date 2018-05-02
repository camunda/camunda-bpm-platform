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
package org.camunda.bpm.engine.impl.externaltask;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class LockedExternalTaskImpl implements LockedExternalTask {

  protected String id;
  protected String topicName;
  protected String workerId;
  protected Date lockExpirationTime;
  protected Integer retries;
  protected String errorMessage;
  protected String errorDetails;
  protected String processInstanceId;
  protected String executionId;
  protected String activityId;
  protected String activityInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String tenantId;
  protected long priority;
  protected VariableMapImpl variables;
  protected String businessKey;

  public String getId() {
    return id;
  }

  public String getTopicName() {
    return topicName;
  }

  public String getWorkerId() {
    return workerId;
  }

  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }

  public Integer getRetries() {
    return retries;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
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

  public VariableMap getVariables() {
    return variables;
  }

  public String getErrorDetails() {
    return errorDetails;
  }

  @Override
  public long getPriority() {
    return priority;
  }

  @Override
  public String getBusinessKey() {
    return businessKey;
  }

  /**
   * Construct representation of locked ExternalTask from corresponding entity.
   * During mapping variables will be collected,during collection variables will not be deserialized
   * and scope will not be set to local.
   *
   * @see {@link org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope#collectVariables(VariableMapImpl, Collection, boolean, boolean)}
   *
   * @param externalTaskEntity - source persistent entity to use for fields
   * @param variablesToFetch - list of variable names to fetch, if null then all variables will be fetched
   * @param isLocal - if true only local variables will be collected
   *
   * @return object with all fields copied from the ExternalTaskEntity, error details fetched from the
   * database and variables attached
   */
  public static LockedExternalTaskImpl fromEntity(ExternalTaskEntity externalTaskEntity, List<String> variablesToFetch, boolean isLocal, boolean deserializeVariables) {
    LockedExternalTaskImpl result = new LockedExternalTaskImpl();
    result.id = externalTaskEntity.getId();
    result.topicName = externalTaskEntity.getTopicName();
    result.workerId = externalTaskEntity.getWorkerId();
    result.lockExpirationTime = externalTaskEntity.getLockExpirationTime();
    result.retries = externalTaskEntity.getRetries();
    result.errorMessage = externalTaskEntity.getErrorMessage();
    result.errorDetails = externalTaskEntity.getErrorDetails();

    result.processInstanceId = externalTaskEntity.getProcessInstanceId();
    result.executionId = externalTaskEntity.getExecutionId();
    result.activityId = externalTaskEntity.getActivityId();
    result.activityInstanceId = externalTaskEntity.getActivityInstanceId();
    result.processDefinitionId = externalTaskEntity.getProcessDefinitionId();
    result.processDefinitionKey = externalTaskEntity.getProcessDefinitionKey();
    result.tenantId = externalTaskEntity.getTenantId();
    result.priority = externalTaskEntity.getPriority();
    result.businessKey = externalTaskEntity.getBusinessKey();

    ExecutionEntity execution = externalTaskEntity.getExecution();
    result.variables = new VariableMapImpl();
    execution.collectVariables(result.variables, variablesToFetch, isLocal, deserializeVariables);

    return result;
  }
}
