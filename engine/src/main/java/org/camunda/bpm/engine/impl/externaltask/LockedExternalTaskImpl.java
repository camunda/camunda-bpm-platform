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

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.VariableMapImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Thorben Lindhauer
 *
 */
public class LockedExternalTaskImpl implements LockedExternalTask {

  protected String id;
  protected String topicName;
  protected String workerId;
  protected Date lockExpirationTime;
  protected String processInstanceId;
  protected String executionId;
  protected String activityId;
  protected String activityInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected VariableMapImpl variables;

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

  public VariableMap getVariables() {
    return variables;
  }

  public static LockedExternalTaskImpl fromEntity(ExternalTaskEntity externalTaskEntity, List<String> variablesToFetch) {
    LockedExternalTaskImpl result = new LockedExternalTaskImpl();
    result.id = externalTaskEntity.getId();
    result.topicName = externalTaskEntity.getTopicName();
    result.workerId = externalTaskEntity.getWorkerId();
    result.lockExpirationTime = externalTaskEntity.getLockExpirationTime();

    ExecutionEntity execution = externalTaskEntity.getExecution();
    result.processInstanceId = execution.getProcessInstanceId();
    result.executionId = execution.getId();
    result.activityId = execution.getActivityId();
    result.activityInstanceId = execution.getActivityInstanceId();
    result.processDefinitionId = execution.getProcessDefinitionId();
    result.processDefinitionKey = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedProcessDefinitionById(execution.getProcessDefinitionId())
        .getKey();

    result.variables = new VariableMapImpl();
    execution.collectVariables(result.variables, variablesToFetch, false, false);

    return result;

  }
}
