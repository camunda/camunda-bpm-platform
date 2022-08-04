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
package org.camunda.bpm.engine.test.api.runtime;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.BatchStatistics;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.camunda.bpm.engine.management.SchemaLogEntry;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

/**
 * This class provides utils to verify the sorting of queries of engine entities.
 * Assuming we sort over a property x, there are two valid orderings when some entities
 * have values where x = null: Either, these values precede the overall list, or they trail it.
 * Thus, this class does not use regular comparators but a {@link NullTolerantComparator}
 * that can be used to assert a list of entites in both ways.
 *
 * @author Thorben Lindhauer
 *
 */
public class TestOrderingUtil {

  // EXECUTION

  public static NullTolerantComparator<Execution> executionByProcessInstanceId() {
    return propertyComparator(new PropertyAccessor<Execution, String>() {
      @Override
      public String getProperty(Execution obj) {
        return obj.getProcessInstanceId();
      }
    });
  }

  public static NullTolerantComparator<Execution> executionByProcessDefinitionId() {
    return propertyComparator(new PropertyAccessor<Execution, String>() {
      @Override
      public String getProperty(Execution obj) {
        return ((ExecutionEntity) obj).getProcessDefinitionId();
      }
    });
  }

  public static NullTolerantComparator<Execution> executionByProcessDefinitionKey(ProcessEngine processEngine) {
    final RuntimeService runtimeService = processEngine.getRuntimeService();
    final RepositoryService repositoryService = processEngine.getRepositoryService();

    return propertyComparator(new PropertyAccessor<Execution, String>() {
      @Override
      public String getProperty(Execution obj) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(obj.getProcessInstanceId()).singleResult();
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());
        return processDefinition.getKey();
      }
    });
  }

  //PROCESS INSTANCE

  public static NullTolerantComparator<ProcessInstance> processInstanceByProcessInstanceId() {
    return propertyComparator(new PropertyAccessor<ProcessInstance, String>() {
      @Override public String getProperty(ProcessInstance obj) {
        return obj.getProcessInstanceId();
      }
    });
  }

  public static NullTolerantComparator<ProcessInstance> processInstanceByProcessDefinitionId() {
    return propertyComparator(new PropertyAccessor<ProcessInstance, String>() {
      @Override public String getProperty(ProcessInstance obj) {
        return obj.getProcessDefinitionId();
      }
    });
  }

  public static NullTolerantComparator<ProcessInstance> processInstanceByBusinessKey() {
    return propertyComparator(new PropertyAccessor<ProcessInstance, String>() {
      @Override public String getProperty(ProcessInstance obj) {
        return obj.getBusinessKey();
      }
    });
  }

  // PROCESS DEFINITION

  public static NullTolerantComparator<ProcessDefinition> processDefinitionByDeployTime(ProcessEngine processEngine){
    RepositoryService repositoryService = processEngine.getRepositoryService();
    return propertyComparator(new PropertyAccessor<ProcessDefinition, Date>() {
      @Override
      public Date getProperty(ProcessDefinition obj) {
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(obj.getDeploymentId()).singleResult();
        return deployment.getDeploymentTime();
      }
    });
  }

  // DECISION DEFINITION

  public static NullTolerantComparator<DecisionDefinition> decisionDefinitionByDeployTime(ProcessEngine processEngine) {
    RepositoryService repositoryService = processEngine.getRepositoryService();
    return propertyComparator(new PropertyAccessor<DecisionDefinition, Date>() {
      @Override
      public Date getProperty(DecisionDefinition obj) {
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(obj.getDeploymentId()).singleResult();
        return deployment.getDeploymentTime();
      }
    });
  }

  //HISTORIC PROCESS INSTANCE

  public static NullTolerantComparator<HistoricProcessInstance> historicProcessInstanceByProcessDefinitionId() {
    return propertyComparator(new PropertyAccessor<HistoricProcessInstance, String>() {
      @Override public String getProperty(HistoricProcessInstance obj) {
        return obj.getProcessDefinitionId();
      }
    });
  }

  public static NullTolerantComparator<HistoricProcessInstance> historicProcessInstanceByProcessDefinitionKey() {
    return propertyComparator(new PropertyAccessor<HistoricProcessInstance, String>() {
      @Override public String getProperty(HistoricProcessInstance obj) {
        return obj.getProcessDefinitionKey();
      }
    });
  }

  public static NullTolerantComparator<HistoricProcessInstance> historicProcessInstanceByProcessDefinitionName() {
    return propertyComparator(new PropertyAccessor<HistoricProcessInstance, String>() {
      @Override public String getProperty(HistoricProcessInstance obj) {
        return obj.getProcessDefinitionName();
      }
    });
  }

  public static NullTolerantComparator<HistoricProcessInstance> historicProcessInstanceByProcessDefinitionVersion() {
    return propertyComparator(new PropertyAccessor<HistoricProcessInstance, Integer>() {
      @Override public Integer getProperty(HistoricProcessInstance obj) {
        return obj.getProcessDefinitionVersion();
      }
    });
  }

  public static NullTolerantComparator<HistoricProcessInstance> historicProcessInstanceByProcessInstanceId() {
    return propertyComparator(new PropertyAccessor<HistoricProcessInstance, String>() {
      @Override public String getProperty(HistoricProcessInstance obj) {
        return obj.getId();
      }
    });
  }

  // CASE EXECUTION

  public static NullTolerantComparator<CaseExecution> caseExecutionByDefinitionId() {
    return propertyComparator(new PropertyAccessor<CaseExecution, String>() {
      @Override
      public String getProperty(CaseExecution obj) {
        return obj.getCaseDefinitionId();
      }
    });
  }

  public static NullTolerantComparator<CaseExecution> caseExecutionByDefinitionKey(ProcessEngine processEngine) {
    final RepositoryService repositoryService = processEngine.getRepositoryService();
    return propertyComparator(new PropertyAccessor<CaseExecution, String>() {
      @Override
      public String getProperty(CaseExecution obj) {
        CaseDefinition caseDefinition = repositoryService.getCaseDefinition(obj.getCaseDefinitionId());
        return caseDefinition.getKey();
      }
    });
  }

  public static NullTolerantComparator<CaseExecution> caseExecutionById() {
    return propertyComparator(new PropertyAccessor<CaseExecution, String>() {
      @Override
      public String getProperty(CaseExecution obj) {
        return obj.getId();
      }
    });
  }

  // TASK

  public static NullTolerantComparator<Task> taskById() {
    return propertyComparator(new PropertyAccessor<Task, String>() {
      @Override
      public String getProperty(Task obj) {
        return obj.getId();
      }
    });
  }

  public static NullTolerantComparator<Task> taskByName() {
    return propertyComparator(new PropertyAccessor<Task, String>() {
      @Override
      public String getProperty(Task obj) {
        return obj.getName();
      }
    });
  }

  public static NullTolerantComparator<Task> taskByPriority() {
    return propertyComparator(new PropertyAccessor<Task, Integer>() {
      @Override
      public Integer getProperty(Task obj) {
        return obj.getPriority();
      }
    });
  }

  public static NullTolerantComparator<Task> taskByAssignee() {
    return propertyComparator(new PropertyAccessor<Task, String>() {
      @Override
      public String getProperty(Task obj) {
        return obj.getAssignee();
      }
    });
  }

  public static NullTolerantComparator<Task> taskByDescription() {
    return propertyComparator(new PropertyAccessor<Task, String>() {
      @Override
      public String getProperty(Task obj) {
        return obj.getDescription();
      }
    });
  }

  public static NullTolerantComparator<Task> taskByProcessInstanceId() {
    return propertyComparator(new PropertyAccessor<Task, String>() {
      @Override
      public String getProperty(Task obj) {
        return obj.getProcessInstanceId();
      }
    });
  }

  public static NullTolerantComparator<Task> taskByExecutionId() {
    return propertyComparator(new PropertyAccessor<Task, String>() {
      @Override
      public String getProperty(Task obj) {
        return obj.getExecutionId();
      }
    });
  }

  public static NullTolerantComparator<Task> taskByCreateTime() {
    return propertyComparator(new PropertyAccessor<Task, Date>() {
      @Override
      public Date getProperty(Task obj) {
        return obj.getCreateTime();
      }
    });
  }

  public static NullTolerantComparator<Task> taskByDueDate() {
    return propertyComparator(new PropertyAccessor<Task, Date>() {
      @Override
      public Date getProperty(Task obj) {
        return obj.getDueDate();
      }
    });
  }

  public static NullTolerantComparator<Task> taskByFollowUpDate() {
    return propertyComparator(new PropertyAccessor<Task, Date>() {
      @Override
      public Date getProperty(Task obj) {
        return obj.getFollowUpDate();
      }
    });
  }

  public static NullTolerantComparator<Task> taskByCaseInstanceId() {
    return propertyComparator(new PropertyAccessor<Task, String>() {
      @Override
      public String getProperty(Task obj) {
        return obj.getCaseInstanceId();
      }
    });
  }

  public static NullTolerantComparator<Task> taskByCaseExecutionId() {
    return propertyComparator(new PropertyAccessor<Task, String>() {
      @Override
      public String getProperty(Task obj) {
        return obj.getCaseExecutionId();
      }
    });
  }

  // HISTORIC JOB LOG

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByTimestamp() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, Date>() {
      @Override
      public Date getProperty(HistoricJobLog obj) {
        return obj.getTimestamp();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByJobId() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, String>() {
      @Override
      public String getProperty(HistoricJobLog obj) {
        return obj.getJobId();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByJobDefinitionId() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, String>() {
      @Override
      public String getProperty(HistoricJobLog obj) {
        return obj.getJobDefinitionId();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByJobDueDate() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, Date>() {
      @Override
      public Date getProperty(HistoricJobLog obj) {
        return obj.getJobDueDate();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByJobRetries() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, Integer>() {
      @Override
      public Integer getProperty(HistoricJobLog obj) {
        return obj.getJobRetries();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByActivityId() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, String>() {
      @Override
      public String getProperty(HistoricJobLog obj) {
        return obj.getActivityId();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByExecutionId() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, String>() {
      @Override
      public String getProperty(HistoricJobLog obj) {
        return obj.getExecutionId();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByProcessInstanceId() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, String>() {
      @Override
      public String getProperty(HistoricJobLog obj) {
        return obj.getProcessInstanceId();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByProcessDefinitionId() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, String>() {
      @Override
      public String getProperty(HistoricJobLog obj) {
        return obj.getProcessDefinitionId();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByProcessDefinitionKey(ProcessEngine processEngine) {
    final RepositoryService repositoryService = processEngine.getRepositoryService();

    return propertyComparator(new PropertyAccessor<HistoricJobLog, String>() {
      @Override
      public String getProperty(HistoricJobLog obj) {
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(obj.getProcessDefinitionId());
        return processDefinition.getKey();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByDeploymentId() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, String>() {
      @Override
      public String getProperty(HistoricJobLog obj) {
        return obj.getDeploymentId();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByJobPriority() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, Long>() {
      @Override
      public Long getProperty(HistoricJobLog obj) {
        return obj.getJobPriority();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogPartiallyByOccurence() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, Long>() {
      @Override
      public Long getProperty(HistoricJobLog obj) {
        return ((HistoricJobLogEventEntity) obj).getSequenceCounter();
      }
    });
  }

  public static NullTolerantComparator<HistoricJobLog> historicJobLogByTenantId() {
    return propertyComparator(new PropertyAccessor<HistoricJobLog, String>() {
      @Override
      public String getProperty(HistoricJobLog obj) {
        return obj.getTenantId();
      }
    });
  }

  // jobs

  public static NullTolerantComparator<Job> jobByPriority() {
    return propertyComparator(new PropertyAccessor<Job, Long>() {
      @Override
      public Long getProperty(Job obj) {
        return obj.getPriority();
      }
    });
  }

  // external task

  public static NullTolerantComparator<ExternalTask> externalTaskById() {
    return propertyComparator(new PropertyAccessor<ExternalTask, String>() {
      @Override
      public String getProperty(ExternalTask obj) {
        return obj.getId();
      }
    });
  }

  public static NullTolerantComparator<ExternalTask> externalTaskByProcessInstanceId() {
    return propertyComparator(new PropertyAccessor<ExternalTask, String>() {
      @Override
      public String getProperty(ExternalTask obj) {
        return obj.getProcessInstanceId();
      }
    });
  }

  public static NullTolerantComparator<ExternalTask> externalTaskByProcessDefinitionId() {
    return propertyComparator(new PropertyAccessor<ExternalTask, String>() {
      @Override
      public String getProperty(ExternalTask obj) {
        return obj.getProcessDefinitionId();
      }
    });
  }

  public static NullTolerantComparator<ExternalTask> externalTaskByProcessDefinitionKey() {
    return propertyComparator(new PropertyAccessor<ExternalTask, String>() {
      @Override
      public String getProperty(ExternalTask obj) {
        return obj.getProcessDefinitionKey();
      }
    });
  }

  public static NullTolerantComparator<ExternalTask> externalTaskByLockExpirationTime() {
    return propertyComparator(new PropertyAccessor<ExternalTask, Date>() {
      @Override
      public Date getProperty(ExternalTask obj) {
        return obj.getLockExpirationTime();
      }
    });
  }

  public static NullTolerantComparator<ExternalTask> externalTaskByPriority() {
    return propertyComparator(new PropertyAccessor<ExternalTask, Long>() {
      @Override
      public Long getProperty(ExternalTask obj) {
        return obj.getPriority();
      }
    });
  }

  // batch

  public static NullTolerantComparator<Batch> batchById() {
    return propertyComparator(new PropertyAccessor<Batch, String>() {
      @Override
      public String getProperty(Batch obj) {
        return obj.getId();
      }
    });
  }

  public static NullTolerantComparator<Batch> batchByTenantId() {
    return propertyComparator(new PropertyAccessor<Batch, String>() {
      @Override
      public String getProperty(Batch obj) {
        return obj.getTenantId();
      }
    });
  }

  public static NullTolerantComparator<HistoricBatch> historicBatchById() {
    return propertyComparator(new PropertyAccessor<HistoricBatch, String>() {
      @Override
      public String getProperty(HistoricBatch obj) {
        return obj.getId();
      }
    });
  }

  public static NullTolerantComparator<HistoricBatch> historicBatchByTenantId() {
    return propertyComparator(new PropertyAccessor<HistoricBatch, String>() {
      @Override
      public String getProperty(HistoricBatch obj) {
        return obj.getTenantId();
      }
    });
  }

  public static NullTolerantComparator<HistoricBatch> historicBatchByStartTime() {
    return propertyComparator(new PropertyAccessor<HistoricBatch, Date>() {
      @Override
      public Date getProperty(HistoricBatch obj) {
        return obj.getStartTime();
      }
    });
  }

  public static NullTolerantComparator<HistoricBatch> historicBatchByEndTime() {
    return propertyComparator(new PropertyAccessor<HistoricBatch, Date>() {
      @Override
      public Date getProperty(HistoricBatch obj) {
        return obj.getEndTime();
      }
    });
  }

  public static NullTolerantComparator<BatchStatistics> batchStatisticsById() {
    return propertyComparator(new PropertyAccessor<BatchStatistics, String>() {
      @Override
      public String getProperty(BatchStatistics obj) {
        return obj.getId();
      }
    });
  }

  public static NullTolerantComparator<BatchStatistics> batchStatisticsByTenantId() {
    return propertyComparator(new PropertyAccessor<BatchStatistics, String>() {
      @Override
      public String getProperty(BatchStatistics obj) {
        return obj.getTenantId();
      }
    });
  }

  public static NullTolerantComparator<BatchStatistics> batchStatisticsByStartTime() {
    return propertyComparator(Batch::getStartTime);
  }

  // HISTORIC EXTERNAL TASK LOG

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskByTimestamp() {
    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, Date>() {
      @Override
      public Date getProperty(HistoricExternalTaskLog obj) {
        return obj.getTimestamp();
      }
    });
  }

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskLogByExternalTaskId() {
    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, String>() {
      @Override
      public String getProperty(HistoricExternalTaskLog obj) {
        return obj.getExternalTaskId();
      }
    });
  }

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskLogByRetries() {
    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, Integer>() {
      @Override
      public Integer getProperty(HistoricExternalTaskLog obj) {
        return obj.getRetries();
      }
    });
  }

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskLogByPriority() {
    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, Long>() {
      @Override
      public Long getProperty(HistoricExternalTaskLog obj) {
        return obj.getPriority();
      }
    });
  }

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskLogByTopicName() {
    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, String>() {
      @Override
      public String getProperty(HistoricExternalTaskLog obj) {
        return obj.getTopicName();
      }
    });
  }

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskLogByWorkerId() {
    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, String>() {
      @Override
      public String getProperty(HistoricExternalTaskLog obj) {
        return obj.getWorkerId();
      }
    });
  }

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskLogByActivityId() {
    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, String>() {
      @Override
      public String getProperty(HistoricExternalTaskLog obj) {
        return obj.getActivityId();
      }
    });
  }

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskLogByActivityInstanceId() {
    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, String>() {
      @Override
      public String getProperty(HistoricExternalTaskLog obj) {
        return obj.getActivityInstanceId();
      }
    });
  }

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskLogByExecutionId() {
    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, String>() {
      @Override
      public String getProperty(HistoricExternalTaskLog obj) {
        return obj.getExecutionId();
      }
    });
  }

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskLogByProcessInstanceId() {
    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, String>() {
      @Override
      public String getProperty(HistoricExternalTaskLog obj) {
        return obj.getProcessInstanceId();
      }
    });
  }

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskLogByProcessDefinitionId() {
    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, String>() {
      @Override
      public String getProperty(HistoricExternalTaskLog obj) {
        return obj.getProcessDefinitionId();
      }
    });
  }

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskLogByProcessDefinitionKey(ProcessEngine processEngine) {
    final RepositoryService repositoryService = processEngine.getRepositoryService();

    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, String>() {
      @Override
      public String getProperty(HistoricExternalTaskLog obj) {
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(obj.getProcessDefinitionId());
        return processDefinition.getKey();
      }
    });
  }

  // HISTORIC ENTITIES

  public static NullTolerantComparator<HistoricActivityInstance> historicActivityInstanceByTenantId() {
    return propertyComparator(new PropertyAccessor<HistoricActivityInstance, String>() {
      @Override
      public String getProperty(HistoricActivityInstance obj) {
        return obj.getTenantId();
      }
    });
  }

  public static NullTolerantComparator<HistoricIncident> historicIncidentByTenantId() {
    return propertyComparator(new PropertyAccessor<HistoricIncident, String>() {
      @Override
      public String getProperty(HistoricIncident obj) {
        return obj.getTenantId();
      }
    });
  }

  public static NullTolerantComparator<HistoricDecisionInstance> historicDecisionInstanceByTenantId() {
    return propertyComparator(new PropertyAccessor<HistoricDecisionInstance, String>() {
      @Override
      public String getProperty(HistoricDecisionInstance obj) {
        return obj.getTenantId();
      }
    });
  }

  public static NullTolerantComparator<HistoricDetail> historicDetailByTenantId() {
    return propertyComparator(new PropertyAccessor<HistoricDetail, String>() {
      @Override
      public String getProperty(HistoricDetail obj) {
        return obj.getTenantId();
      }
    });
  }

  public static NullTolerantComparator<HistoricTaskInstance> historicTaskInstanceByTenantId() {
    return propertyComparator(new PropertyAccessor<HistoricTaskInstance, String>() {
      @Override
      public String getProperty(HistoricTaskInstance obj) {
        return obj.getTenantId();
      }
    });
  }

  public static NullTolerantComparator<HistoricVariableInstance> historicVariableInstanceByTenantId() {
    return propertyComparator(new PropertyAccessor<HistoricVariableInstance, String>() {
      @Override
      public String getProperty(HistoricVariableInstance obj) {
        return obj.getTenantId();
      }
    });
  }

  public static NullTolerantComparator<HistoricCaseActivityInstance> historicCaseActivityInstanceByTenantId() {
    return propertyComparator(new PropertyAccessor<HistoricCaseActivityInstance, String>() {
      @Override
      public String getProperty(HistoricCaseActivityInstance obj) {
        return obj.getTenantId();
      }
    });
  }

  public static NullTolerantComparator<HistoricExternalTaskLog> historicExternalTaskLogByTenantId() {
    return propertyComparator(new PropertyAccessor<HistoricExternalTaskLog, String>() {
      @Override
      public String getProperty(HistoricExternalTaskLog obj) {
        return obj.getTenantId();
      }
    });
  }

  // SCHEMA LOG
  public static NullTolerantComparator<SchemaLogEntry> schemaLogEntryByTimestamp() {
    return propertyComparator(new PropertyAccessor<SchemaLogEntry, Date>() {
      @Override
      public Date getProperty(SchemaLogEntry obj) {
        return obj.getTimestamp();
      }
    });
  }

  // general

  public static <T, P extends Comparable<P>> NullTolerantComparator<T> propertyComparator(
      final PropertyAccessor<T, P> accessor) {
    return new NullTolerantComparator<T>() {

      @Override
      public int compare(T o1, T o2) {
        P prop1 = accessor.getProperty(o1);
        P prop2 = accessor.getProperty(o2);

        return prop1.compareTo(prop2);
      }

      @Override
      public boolean hasNullProperty(T object) {
        return accessor.getProperty(object) == null;
      }
    };
  }

  protected interface PropertyAccessor<T, P extends Comparable<P>> {
    P getProperty(T obj);
  }


  public static <T> NullTolerantComparator<T> inverted(final NullTolerantComparator<T> comparator) {
    return new NullTolerantComparator<T>() {
      public int compare(T o1, T o2) {
        return - comparator.compare(o1, o2);
      }

      public boolean hasNullProperty(T object) {
        return comparator.hasNullProperty(object);
      }
    };
  }


  public static <T> NullTolerantComparator<T> hierarchical(final NullTolerantComparator<T> baseComparator,
      final NullTolerantComparator<T>... minorOrderings) {
    return new NullTolerantComparator<T>() {
      public int compare(T o1, T o2, boolean nullPrecedes) {
        int comparison = baseComparator.compare(o1, o2, nullPrecedes);

        int i = 0;
        while (comparison == 0 && i < minorOrderings.length) {
          NullTolerantComparator<T> comparator = minorOrderings[i];
          comparison = comparator.compare(o1, o2, nullPrecedes);
          i++;
        }

        return comparison;
      }

      public int compare(T o1, T o2) {
        throw new UnsupportedOperationException();
      }

      public boolean hasNullProperty(T object) {
        throw new UnsupportedOperationException();
      }
    };
  }

  public abstract static class NullTolerantComparator<T> implements Comparator<T> {

    public int compare(T o1, T o2, boolean nullPrecedes) {
      boolean o1Null = hasNullProperty(o1);
      boolean o2Null = hasNullProperty(o2);

      if (o1Null) {
        if (o2Null) {
          return 0;
        } else {
          if (nullPrecedes) {
            return -1;
          } else {
            return 1;
          }
        }
      } else {

        if (o2Null) {
          if (nullPrecedes) {
            return 1;
          } else {
            return -1;
          }
        }
      }

      return compare(o1, o2);
    }

    public abstract boolean hasNullProperty(T object);
  }

  public static <T> void verifySorting(List<T> actualElements, NullTolerantComparator<T> expectedOrdering) {
    // check two orderings: one in which values with null properties are at the front of the list
    boolean leadingNullOrdering = orderingConsistent(actualElements, expectedOrdering, true);

    if (leadingNullOrdering) {
      return;
    }

    // and one where the values with null properties are at the end of the list
    boolean trailingNullOrdering = orderingConsistent(actualElements, expectedOrdering, false);
    TestCase.assertTrue("Ordering not consistent with comparator", trailingNullOrdering);
  }

  public static <T> boolean orderingConsistent(List<T> actualElements, NullTolerantComparator<T> expectedOrdering, boolean nullPrecedes) {
    for (int i = 0; i < actualElements.size() - 1; i++) {
      T currentExecution = actualElements.get(i);
      T nextExecution = actualElements.get(i + 1);

      int comparison = expectedOrdering.compare(currentExecution, nextExecution, nullPrecedes);
      if (comparison > 0) {
        return false;
      }
    }

    return true;
  }

  public static <T> void verifySortingAndCount(Query<?, T> query, int expectedCount, NullTolerantComparator<T> expectedOrdering) {
    List<T> elements = query.list();
    TestCase.assertEquals(expectedCount, elements.size());

    verifySorting(elements, expectedOrdering);
  }

}
