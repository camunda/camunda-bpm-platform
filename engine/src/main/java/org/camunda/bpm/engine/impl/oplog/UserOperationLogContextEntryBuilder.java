/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.oplog;

import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_CREATE;

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
    entry.setTenantId(job.getTenantId());

    ExecutionEntity execution = job.getExecution();
    if (execution != null) {
      entry.setRootProcessInstanceId(execution.getRootProcessInstanceId());
    }

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(JobDefinitionEntity jobDefinition) {
    entry.setJobDefinitionId(jobDefinition.getId());
    entry.setProcessDefinitionId(jobDefinition.getProcessDefinitionId());
    entry.setProcessDefinitionKey(jobDefinition.getProcessDefinitionKey());
    entry.setTenantId(jobDefinition.getTenantId());

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
    entry.setRootProcessInstanceId(execution.getRootProcessInstanceId());
    entry.setProcessDefinitionId(execution.getProcessDefinitionId());
    entry.setTenantId(execution.getTenantId());

    ProcessDefinitionEntity processDefinition = execution.getProcessDefinition();
    entry.setProcessDefinitionKey(processDefinition.getKey());
    entry.setDeploymentId(processDefinition.getDeploymentId());

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(ProcessDefinitionEntity processDefinition) {
    entry.setProcessDefinitionId(processDefinition.getId());
    entry.setProcessDefinitionKey(processDefinition.getKey());
    entry.setDeploymentId(processDefinition.getDeploymentId());
    entry.setTenantId(processDefinition.getTenantId());

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(TaskEntity task, List<PropertyChange> propertyChanges) {

    if (propertyChanges == null || propertyChanges.isEmpty()) {
      if (OPERATION_TYPE_CREATE.equals(entry.getOperationType())) {
        propertyChanges = Arrays.asList(PropertyChange.EMPTY_CHANGE);
      }
    }
    entry.setPropertyChanges(propertyChanges);

    ProcessDefinitionEntity definition = task.getProcessDefinition();
    if (definition != null) {
      entry.setProcessDefinitionKey(definition.getKey());
      entry.setDeploymentId(definition.getDeploymentId());
      entry.setTenantId(definition.getTenantId());
    } else if (task.getCaseDefinitionId() != null) {
      entry.setDeploymentId(task.getCaseDefinition().getDeploymentId());
      entry.setTenantId(task.getCaseDefinition().getTenantId());
    }

    entry.setProcessDefinitionId(task.getProcessDefinitionId());
    entry.setProcessInstanceId(task.getProcessInstanceId());
    entry.setExecutionId(task.getExecutionId());
    entry.setCaseDefinitionId(task.getCaseDefinitionId());
    entry.setCaseInstanceId(task.getCaseInstanceId());
    entry.setCaseExecutionId(task.getCaseExecutionId());
    entry.setTaskId(task.getId());
    if (entry.getTenantId() == null && task.getTenantId() != null) { // standalone task with tenant info
      entry.setTenantId(task.getTenantId());
    }

    ExecutionEntity execution = task.getExecution();
    if (execution != null) {
      entry.setRootProcessInstanceId(execution.getRootProcessInstanceId());
    }

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(HistoricTaskInstance task, List<PropertyChange> propertyChanges) {

    if (propertyChanges == null || propertyChanges.isEmpty()) {
      if (OPERATION_TYPE_CREATE.equals(entry.getOperationType())) {
        propertyChanges = Arrays.asList(PropertyChange.EMPTY_CHANGE);
      }
    }
    entry.setPropertyChanges(propertyChanges);

    entry.setProcessDefinitionKey(task.getProcessDefinitionKey());
    entry.setProcessDefinitionId(task.getProcessDefinitionId());
    entry.setProcessInstanceId(task.getProcessInstanceId());
    entry.setExecutionId(task.getExecutionId());
    entry.setCaseDefinitionId(task.getCaseDefinitionId());
    entry.setCaseInstanceId(task.getCaseInstanceId());
    entry.setCaseExecutionId(task.getCaseExecutionId());
    entry.setTaskId(task.getId());
    entry.setRootProcessInstanceId(task.getRootProcessInstanceId());
    entry.setTenantId(task.getTenantId());

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(ExecutionEntity processInstance, List<PropertyChange> propertyChanges) {

    if (propertyChanges == null || propertyChanges.isEmpty()) {
      if (OPERATION_TYPE_CREATE.equals(entry.getOperationType())) {
        propertyChanges = Arrays.asList(PropertyChange.EMPTY_CHANGE);
      }
    }
    entry.setPropertyChanges(propertyChanges);
    entry.setRootProcessInstanceId(processInstance.getRootProcessInstanceId());
    entry.setProcessInstanceId(processInstance.getProcessInstanceId());
    entry.setProcessDefinitionId(processInstance.getProcessDefinitionId());
    entry.setExecutionId(processInstance.getId());
    entry.setCaseInstanceId(processInstance.getCaseInstanceId());
    entry.setTenantId(processInstance.getTenantId());

    ProcessDefinitionEntity definition = processInstance.getProcessDefinition();
    if (definition != null) {
      entry.setProcessDefinitionKey(definition.getKey());
      entry.setDeploymentId(definition.getDeploymentId());
    }

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(HistoryEvent historyEvent, ResourceDefinitionEntity<?> definition, List<PropertyChange> propertyChanges) {

    if (propertyChanges == null || propertyChanges.isEmpty()) {
      if (OPERATION_TYPE_CREATE.equals(entry.getOperationType())) {
        propertyChanges = Arrays.asList(PropertyChange.EMPTY_CHANGE);
      }
    }
    entry.setPropertyChanges(propertyChanges);
    entry.setRootProcessInstanceId(historyEvent.getRootProcessInstanceId());
    entry.setProcessDefinitionId(historyEvent.getProcessDefinitionId());
    entry.setProcessInstanceId(historyEvent.getProcessInstanceId());
    entry.setExecutionId(historyEvent.getExecutionId());
    entry.setCaseDefinitionId(historyEvent.getCaseDefinitionId());
    entry.setCaseInstanceId(historyEvent.getCaseInstanceId());
    entry.setCaseExecutionId(historyEvent.getCaseExecutionId());

    if (definition != null) {
      if (definition instanceof ProcessDefinitionEntity) {
        entry.setProcessDefinitionKey(definition.getKey());
      }
      entry.setDeploymentId(definition.getDeploymentId());
      entry.setTenantId(definition.getTenantId());
    }

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(HistoricVariableInstanceEntity variable, ResourceDefinitionEntity<?> definition, List<PropertyChange> propertyChanges) {

    if (propertyChanges == null || propertyChanges.isEmpty()) {
      if (OPERATION_TYPE_CREATE.equals(entry.getOperationType())) {
        propertyChanges = Arrays.asList(PropertyChange.EMPTY_CHANGE);
      }
    }
    entry.setPropertyChanges(propertyChanges);
    entry.setRootProcessInstanceId(variable.getRootProcessInstanceId());
    entry.setProcessDefinitionId(variable.getProcessDefinitionId());
    entry.setProcessInstanceId(variable.getProcessInstanceId());
    entry.setExecutionId(variable.getExecutionId());
    entry.setCaseDefinitionId(variable.getCaseDefinitionId());
    entry.setCaseInstanceId(variable.getCaseInstanceId());
    entry.setCaseExecutionId(variable.getCaseExecutionId());
    entry.setTaskId(variable.getTaskId());
    entry.setTenantId(variable.getTenantId());

    if (definition != null) {
      if (definition instanceof ProcessDefinitionEntity) {
        entry.setProcessDefinitionKey(definition.getKey());
      }
      entry.setDeploymentId(definition.getDeploymentId());
    }

    return this;
  }

  public UserOperationLogContextEntryBuilder inContextOf(ExternalTaskEntity task, ExecutionEntity execution, ProcessDefinitionEntity definition) {
    if (execution != null) {
      inContextOf(execution);
    } else if (definition != null) {
      inContextOf(definition);
    }
    entry.setExternalTaskId(task.getId());
    entry.setTenantId(task.getTenantId());
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

  public UserOperationLogContextEntryBuilder caseDefinitionId(String caseDefinitionId) {
    entry.setCaseDefinitionId(caseDefinitionId);
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

  public UserOperationLogContextEntryBuilder taskId(String taskId) {
    entry.setTaskId(taskId);
    return this;
  }

  public UserOperationLogContextEntryBuilder caseInstanceId(String caseInstanceId) {
    entry.setCaseInstanceId(caseInstanceId);
    return this;
  }

  public UserOperationLogContextEntryBuilder category(String category) {
    entry.setCategory(category);
    return this;
  }

  public UserOperationLogContextEntryBuilder annotation(String annotation) {
    entry.setAnnotation(annotation);
    return this;
  }

  public UserOperationLogContextEntryBuilder tenantId(String tenantId) {
    entry.setTenantId(tenantId);
    return this;
  }
}
