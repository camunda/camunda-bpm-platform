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
package org.camunda.bpm.engine.impl.oplog;

import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_CREATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;

public class UserOperationLogContextEntryBuilder {

  protected UserOperationLogContextEntry entry;

  public static UserOperationLogContextEntryBuilder entry(String operationType, String entityType) {
    UserOperationLogContextEntryBuilder builder = new UserOperationLogContextEntryBuilder();
    builder.entry = new UserOperationLogContextEntry(operationType, entityType);
    return builder;
  }

  public UserOperationLogContextEntryBuilder inContextOf(JobEntity job) {
    entry.setJobDefinitionId(job.getJobDefinitionId());
    entry.setProcessInstanceId(job.getProcessInstanceId());
    entry.setProcessDefinitionId(job.getProcessDefinitionId());
    entry.setProcessDefinitionKey(job.getProcessDefinitionKey());
    entry.setDeploymentId(job.getDeploymentId());

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(JobDefinitionEntity jobDefinition) {
    entry.setJobDefinitionId(jobDefinition.getId());
    entry.setProcessDefinitionId(jobDefinition.getProcessDefinitionId());
    entry.setProcessDefinitionKey(jobDefinition.getProcessDefinitionKey());

    if (jobDefinition.getProcessDefinitionId() != null) {
      ProcessDefinitionEntity processDefinition = Context
          .getProcessEngineConfiguration()
          .getDeploymentCache()
          .findDeployedProcessDefinitionById(jobDefinition.getProcessDefinitionId());
      entry.setDeploymentId(processDefinition.getDeploymentId());
    }

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(ExecutionEntity execution) {
    entry.setProcessInstanceId(execution.getProcessInstanceId());
    entry.setProcessDefinitionId(execution.getProcessDefinitionId());

    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();
    entry.setProcessDefinitionKey(processDefinition.getKey());
    entry.setDeploymentId(processDefinition.getDeploymentId());

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(ProcessDefinitionEntity processDefinition) {
    entry.setProcessDefinitionId(processDefinition.getId());
    entry.setProcessDefinitionKey(processDefinition.getKey());
    entry.setDeploymentId(processDefinition.getDeploymentId());

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(TaskEntity task, List<PropertyChange> propertyChanges) {

    if (propertyChanges == null || propertyChanges.isEmpty()) {
      if (OPERATION_TYPE_CREATE.equals(entry.getOperationType())) {
        propertyChanges = Arrays.asList(PropertyChange.EMPTY_CHANGE);
      }
    }
    entry.setPropertyChanges(propertyChanges);

    ResourceDefinitionEntity definition = task.getProcessDefinition();
    if (definition != null) {
      entry.setProcessDefinitionKey(definition.getKey());
      entry.setDeploymentId(definition.getDeploymentId());
    } else if (task.getCaseDefinitionId() != null) {
      definition = task.getCaseDefinition();
      entry.setDeploymentId(definition.getDeploymentId());
    }

    entry.setProcessDefinitionId(task.getProcessDefinitionId());
    entry.setProcessInstanceId(task.getProcessInstanceId());
    entry.setExecutionId(task.getExecutionId());
    entry.setCaseDefinitionId(task.getCaseDefinitionId());
    entry.setCaseInstanceId(task.getCaseInstanceId());
    entry.setCaseExecutionId(task.getCaseExecutionId());
    entry.setTaskId(task.getId());

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(ExecutionEntity processInstance, List<PropertyChange> propertyChanges) {

    if (propertyChanges == null || propertyChanges.isEmpty()) {
      if (OPERATION_TYPE_CREATE.equals(entry.getOperationType())) {
        propertyChanges = Arrays.asList(PropertyChange.EMPTY_CHANGE);
      }
    }
    entry.setPropertyChanges(propertyChanges);
    entry.setProcessInstanceId(processInstance.getProcessInstanceId());
    entry.setProcessDefinitionId(processInstance.getProcessDefinitionId());
    entry.setExecutionId(processInstance.getId());
    entry.setCaseInstanceId(processInstance.getCaseInstanceId());

    ResourceDefinitionEntity definition = processInstance.getProcessDefinition();
    if (definition != null) {
      entry.setProcessDefinitionKey(definition.getKey());
      entry.setDeploymentId(definition.getDeploymentId());
    }

    return this;
  }

  public UserOperationLogContextEntryBuilder propertyChanges(List<PropertyChange> propertyChanges) {
    entry.setPropertyChanges(propertyChanges);
    return this;
  }

  public UserOperationLogContextEntryBuilder propertyChanges(PropertyChange propertyChange) {
    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(propertyChange);
    entry.setPropertyChanges(propertyChanges);
    return this;
  }

  public UserOperationLogContextEntry create() {
    return entry;
  }

  public UserOperationLogContextEntryBuilder jobId(String jobId) {
    entry.setJobId(jobId);
    return this;
  }

  public UserOperationLogContextEntryBuilder jobDefinitionId(String jobDefinitionId) {
    entry.setJobDefinitionId(jobDefinitionId);
    return this;
  }

  public UserOperationLogContextEntryBuilder processDefinitionId(String processDefinitionId) {
    entry.setProcessDefinitionId(processDefinitionId);
    return this;
  }

  public UserOperationLogContextEntryBuilder processDefinitionKey(String processDefinitionKey) {
    entry.setProcessDefinitionKey(processDefinitionKey);
    return this;
  }

  public UserOperationLogContextEntryBuilder processInstanceId(String processInstanceId) {
    entry.setProcessInstanceId(processInstanceId);
    return this;
  }

  public UserOperationLogContextEntryBuilder deploymentId(String deploymentId) {
    entry.setDeploymentId(deploymentId);
    return this;
  }

  public UserOperationLogContextEntryBuilder batchId(String batchId) {
    entry.setBatchId(batchId);
    return this;
  }

}
