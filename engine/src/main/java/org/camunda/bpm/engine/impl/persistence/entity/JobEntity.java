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
package org.camunda.bpm.engine.impl.persistence.entity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.ExceptionUtil.createJobExceptionByteArray;
import static org.camunda.bpm.engine.impl.util.StringUtil.toByteArray;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.DbEntityLifecycleAware;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandling;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobPriorityProvider;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.ResourceTypes;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;

/**
 * Stub of the common parts of a Job. You will normally work with a subclass of
 * JobEntity, such as {@link TimerEntity} or {@link MessageEntity}.
 *
 * @author Tom Baeyens
 * @author Nick Burch
 * @author Dave Syer
 * @author Frederik Heremans
 */
public abstract class JobEntity extends AcquirableJobEntity
    implements Serializable, Job, DbEntity,
    HasDbRevision, HasDbReferences, DbEntityLifecycleAware {

  private final static EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  public static final int DEFAULT_RETRIES = 3;

  private static final long serialVersionUID = 1L;

  protected String executionId = null;

  protected String processDefinitionId = null;
  protected String processDefinitionKey = null;

  protected int retries = DEFAULT_RETRIES;

  // entity is active by default
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();

  protected String jobHandlerType = null;
  protected String jobHandlerConfiguration = null;

  protected ByteArrayEntity exceptionByteArray;
  protected String exceptionByteArrayId;

  protected String exceptionMessage;

  protected String deploymentId;

  protected String jobDefinitionId;

  protected long priority = DefaultJobPriorityProvider.DEFAULT_PRIORITY;

  protected String tenantId;

  protected Date createTime;

  // runtime state /////////////////////////////
  protected String activityId;
  protected JobDefinition jobDefinition;
  protected ExecutionEntity execution;

  // sequence counter //////////////////////////
  protected long sequenceCounter = 1;

  // last failure log id ///////////////////////
  protected String lastFailureLogId;

  // last failing activity id ///////////////////////
  protected String failedActivityId;

  protected Map<String, Class> persistedDependentEntities;

  public void execute(CommandContext commandContext) {
    if (executionId != null) {
      ExecutionEntity execution = getExecution();
      ensureNotNull("Cannot find execution with id '" + executionId + "' referenced from job '" + this + "'", "execution", execution);
    }

    // initialize activity id
    getActivityId();

    // increment sequence counter before job execution
    incrementSequenceCounter();

    preExecute(commandContext);
    JobHandler jobHandler = getJobHandler();
    JobHandlerConfiguration configuration = getJobHandlerConfiguration();
    ensureNotNull("Cannot find job handler '" + jobHandlerType + "' from job '" + this + "'", "jobHandler", jobHandler);
    jobHandler.execute(configuration, execution, commandContext, tenantId);
    postExecute(commandContext);
  }

  protected void preExecute(CommandContext commandContext) {
    // nothing to do
  }

  protected void postExecute(CommandContext commandContext) {
    LOG.debugJobExecuted(this);
    delete(true);
    commandContext.getHistoricJobLogManager().fireJobSuccessfulEvent(this);
  }

  public void init(CommandContext commandContext) {
    // nothing to do
  }

  public void insert() {
    CommandContext commandContext = Context.getCommandContext();

    // add link to execution and deployment
    ExecutionEntity execution = getExecution();
    if (execution != null) {
      execution.addJob(this);

      ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
      this.deploymentId = processDefinition.getDeploymentId();
    }

    commandContext
      .getJobManager()
      .insertJob(this);
  }

  public void delete() {
    delete(false);
  }

  public void delete(boolean incidentResolved) {
    CommandContext commandContext = Context.getCommandContext();

    incrementSequenceCounter();

    // clean additional data related to this job
    JobHandler jobHandler = getJobHandler();
    if (jobHandler != null) {
      jobHandler.onDelete(getJobHandlerConfiguration(), this);
    }

    // fire delete event if this job is not being executed
    boolean executingJob = this.equals(commandContext.getCurrentJob());
    commandContext.getJobManager().deleteJob(this, !executingJob);

    // Also delete the job's exception byte array
    if (exceptionByteArrayId != null) {
      commandContext.getByteArrayManager().deleteByteArrayById(exceptionByteArrayId);
    }

    // remove link to execution
    ExecutionEntity execution = getExecution();
    if (execution != null) {
      execution.removeJob(this);
    }

    removeFailedJobIncident(incidentResolved);
  }

  @Override
  public Object getPersistentState() {
    Map<String, Object> persistentState = (HashMap) super.getPersistentState();
    persistentState.put("executionId", executionId);
    persistentState.put("retries", retries);
    persistentState.put("exceptionMessage", exceptionMessage);
    persistentState.put("suspensionState", suspensionState);
    persistentState.put("processDefinitionId", processDefinitionId);
    persistentState.put("jobDefinitionId", jobDefinitionId);
    persistentState.put("deploymentId", deploymentId);
    persistentState.put("jobHandlerConfiguration", jobHandlerConfiguration);
    persistentState.put("priority", priority);
    persistentState.put("tenantId", tenantId);
    if(exceptionByteArrayId != null) {
      persistentState.put("exceptionByteArrayId", exceptionByteArrayId);
    }
    return persistentState;
  }

  public void setExecution(ExecutionEntity execution) {
    if (execution != null) {
      this.execution = execution;
      executionId = execution.getId();
      processInstanceId = execution.getProcessInstanceId();
      // if the execution is suspended, suspend the job entity as well to prevent unwanted job execution
      if(execution.isSuspended()) {
        suspensionState = execution.getSuspensionState();
      }
      this.execution.addJob(this);
    }
    else {
      this.execution.removeJob(this);
      this.execution = execution;
      processInstanceId = null;
      executionId = null;
    }
  }

  // sequence counter /////////////////////////////////////////////////////////

  public long getSequenceCounter() {
    return sequenceCounter;
  }

  public void setSequenceCounter(long sequenceCounter) {
    this.sequenceCounter = sequenceCounter;
  }

  public void incrementSequenceCounter() {
    sequenceCounter++;
  }

  // getters and setters //////////////////////////////////////////////////////

  @Override
  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public ExecutionEntity getExecution() {
    ensureExecutionInitialized();
    return execution;
  }

  protected void ensureExecutionInitialized() {
    if (execution == null && executionId != null) {
      execution = Context
          .getCommandContext()
          .getExecutionManager()
          .findExecutionById(executionId);
    }
  }

  @Override
  public int getRetries() {
    return retries;
  }

  public void setRetries(int retries) {
    // if retries should be set to a negative value set it to 0
    if (retries < 0) {
      retries = 0;
    }

    // Assuming: if the number of retries will
    // be changed from 0 to x (x >= 1), means
    // that the corresponding incident is resolved.
    if (this.retries == 0 && retries > 0) {
      removeFailedJobIncident(true);
    }

    // If the retries will be set to 0, an
    // incident has to be created.
    if(retries == 0 && this.retries > 0) {
      createFailedJobIncident();
    }
    this.retries = retries;
  }

  // special setter for MyBatis which does not influence incidents
  public void setRetriesFromPersistence(int retries) {
    this.retries = retries;
  }

  protected void createFailedJobIncident() {
    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    if (processEngineConfiguration
        .isCreateIncidentOnFailedJobEnabled()) {

      String incidentHandlerType = Incident.FAILED_JOB_HANDLER_TYPE;

      // make sure job has an ID set:
      if(id == null) {
        id = processEngineConfiguration
            .getIdGenerator()
            .getNextId();

      } else {
        // check whether there exists already an incident
        // for this job
        List<Incident> failedJobIncidents = Context
            .getCommandContext()
            .getIncidentManager()
            .findIncidentByConfigurationAndIncidentType(id, incidentHandlerType);

        if (!failedJobIncidents.isEmpty()) {
          // update the historic job log id in the historic incidents (if available)
          for (Incident incident : failedJobIncidents) {
            HistoricIncidentEntity historicIncidentEvent = Context
                .getCommandContext()
                .getHistoricIncidentManager()
                .findHistoricIncidentById(incident.getId());
            if (historicIncidentEvent != null) {
              historicIncidentEvent.setHistoryConfiguration(getLastFailureLogId());
              Context.getCommandContext().getDbEntityManager().merge(historicIncidentEvent);
            }
          }
          return;
        }

      }


      IncidentContext incidentContext = createIncidentContext();
      incidentContext.setActivityId(getActivityId());
      incidentContext.setHistoryConfiguration(getLastFailureLogId());
      incidentContext.setFailedActivityId(getFailedActivityId());

      IncidentHandling.createIncident(incidentHandlerType, incidentContext, exceptionMessage);

    }
  }

  protected void removeFailedJobIncident(boolean incidentResolved) {
    IncidentContext incidentContext = createIncidentContext();
    IncidentHandling.removeIncidents(Incident.FAILED_JOB_HANDLER_TYPE, incidentContext, incidentResolved);
  }

  protected IncidentContext createIncidentContext() {
    IncidentContext incidentContext = new IncidentContext();
    incidentContext.setProcessDefinitionId(processDefinitionId);
    incidentContext.setExecutionId(executionId);
    incidentContext.setTenantId(tenantId);
    incidentContext.setConfiguration(id);
    incidentContext.setJobDefinitionId(jobDefinitionId);

    return incidentContext;
  }

  public String getExceptionStacktrace() {
    ByteArrayEntity byteArray = getExceptionByteArray();
    return ExceptionUtil.getExceptionStacktrace(byteArray);
  }

  public void setSuspensionState(int state) {
    this.suspensionState = state;
  }

  public int getSuspensionState() {
    return suspensionState;
  }

  @Override
  public boolean isSuspended() {
    return suspensionState == SuspensionState.SUSPENDED.getStateCode();
  }

  @Override
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @Override
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public void setExceptionStacktrace(String exception) {
    byte[] exceptionBytes = toByteArray(exception);

    ByteArrayEntity byteArray = getExceptionByteArray();

    if(byteArray == null) {
      byteArray = createJobExceptionByteArray(exceptionBytes, ResourceTypes.RUNTIME);
      exceptionByteArrayId = byteArray.getId();
      exceptionByteArray = byteArray;
    }
    else {
      byteArray.setBytes(exceptionBytes);
    }
  }

  protected JobHandler getJobHandler() {
    Map<String, JobHandler> jobHandlers = Context.getProcessEngineConfiguration().getJobHandlers();
    return jobHandlers.get(jobHandlerType);
  }

  public JobHandlerConfiguration getJobHandlerConfiguration() {
    return getJobHandler().newConfiguration(jobHandlerConfiguration);
  }

  public void setJobHandlerConfiguration(JobHandlerConfiguration configuration) {
    this.jobHandlerConfiguration = configuration.toCanonicalString();
  }

  public String getJobHandlerType() {
    return jobHandlerType;
  }

  public void setJobHandlerType(String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
  }

  public String getJobHandlerConfigurationRaw() {
    return jobHandlerConfiguration;
  }

  public void setJobHandlerConfigurationRaw(String jobHandlerConfiguration) {
    this.jobHandlerConfiguration = jobHandlerConfiguration;
  }

  @Override
  public String getExceptionMessage() {
    return exceptionMessage;
  }

  @Override
  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

  public JobDefinition getJobDefinition() {
    ensureJobDefinitionInitialized();
    return jobDefinition;
  }

  public void setJobDefinition(JobDefinition jobDefinition) {
    this.jobDefinition = jobDefinition;
    if (jobDefinition != null) {
      jobDefinitionId = jobDefinition.getId();
    }
    else {
      jobDefinitionId = null;
    }
  }

  protected void ensureJobDefinitionInitialized() {
    if (jobDefinition == null && jobDefinitionId != null) {
      jobDefinition = Context
          .getCommandContext()
          .getJobDefinitionManager()
          .findById(jobDefinitionId);
    }
  }

  public void setExceptionMessage(String exceptionMessage) {
    this.exceptionMessage = StringUtil.trimToMaximumLengthAllowed(exceptionMessage);
  }

  public String getExceptionByteArrayId() {
    return exceptionByteArrayId;
  }

  protected ByteArrayEntity getExceptionByteArray() {
    ensureExceptionByteArrayInitialized();
    return exceptionByteArray;
  }

  protected void ensureExceptionByteArrayInitialized() {
    if (exceptionByteArray == null && exceptionByteArrayId != null) {
      exceptionByteArray = Context
        .getCommandContext()
        .getDbEntityManager()
        .selectById(ByteArrayEntity.class, exceptionByteArrayId);
    }
  }

  protected void clearFailedJobException() {
    ByteArrayEntity byteArray = getExceptionByteArray();

    // Avoid NPE when the job was reconfigured by another
    // node in the meantime
    if (byteArray != null) {

      Context.getCommandContext()
          .getDbEntityManager()
          .delete(byteArray);
    }

    this.exceptionByteArrayId = null;
    this.exceptionMessage = null;
  }

  @Override
  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public boolean isInInconsistentLockState() {
    return (lockOwner != null && lockExpirationTime == null)
        || (retries == 0 && (lockOwner != null || lockExpirationTime != null));
  }

  public void resetLock() {
    this.lockOwner = null;
    this.lockExpirationTime = null;
  }

  public String getActivityId() {
    ensureActivityIdInitialized();
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  @Override
  public long getPriority() {
    return priority;
  }

  public void setPriority(long priority) {
    this.priority = priority;
  }

  @Override
  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  protected void ensureActivityIdInitialized() {
    if (activityId == null) {
      JobDefinition jobDefinition = getJobDefinition();
      if (jobDefinition != null) {
        activityId = jobDefinition.getActivityId();
      }
      else {
        ExecutionEntity execution = getExecution();
        if (execution != null) {
          activityId = execution.getActivityId();
        }
      }
    }
  }

  /**
   *
   * Unlock from current lock owner
   *
   */

  public void unlock() {
    this.lockOwner = null;
    this.lockExpirationTime = null;
  }

  public abstract String getType();

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    JobEntity other = (JobEntity) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public Set<String> getReferencedEntityIds() {
    Set<String> referencedEntityIds = new HashSet<>();
    return referencedEntityIds;
  }

  @Override
  public Map<String, Class> getReferencedEntitiesIdAndClass() {
    Map<String, Class> referenceIdAndClass = new HashMap<>();

    if (exceptionByteArrayId != null) {
      referenceIdAndClass.put(exceptionByteArrayId, ByteArrayEntity.class);
    }

    return referenceIdAndClass;
  }

  @Override
  public Map<String, Class> getDependentEntities() {
    return persistedDependentEntities;
  }

  @Override
  public void postLoad() {
    if (exceptionByteArrayId != null) {
      persistedDependentEntities = new HashMap<>();
      persistedDependentEntities.put(exceptionByteArrayId, ByteArrayEntity.class);
    }
    else {
      persistedDependentEntities = Collections.EMPTY_MAP;
    }
  }

  public String getLastFailureLogId() {
    return lastFailureLogId;
  }

  public void setLastFailureLogId(String lastFailureLogId) {
    this.lastFailureLogId = lastFailureLogId;
  }

  public String getFailedActivityId() {
    return failedActivityId;
  }

  public void setFailedActivityId(String failedActivityId) {
    this.failedActivityId = failedActivityId;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", revision=" + revision
           + ", duedate=" + duedate
           + ", lockOwner=" + lockOwner
           + ", lockExpirationTime=" + lockExpirationTime
           + ", executionId=" + executionId
           + ", processInstanceId=" + processInstanceId
           + ", isExclusive=" + isExclusive
           + ", isExclusive=" + isExclusive
           + ", jobDefinitionId=" + jobDefinitionId
           + ", jobHandlerType=" + jobHandlerType
           + ", jobHandlerConfiguration=" + jobHandlerConfiguration
           + ", exceptionByteArray=" + exceptionByteArray
           + ", exceptionByteArrayId=" + exceptionByteArrayId
           + ", exceptionMessage=" + exceptionMessage
           + ", failedActivityId=" + failedActivityId
           + ", deploymentId=" + deploymentId
           + ", priority=" + priority
           + ", tenantId=" + tenantId
           + "]";
  }

}
