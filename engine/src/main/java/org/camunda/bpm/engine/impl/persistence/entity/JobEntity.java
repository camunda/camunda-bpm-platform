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
package org.camunda.bpm.engine.impl.persistence.entity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.JobExceptionUtil.createJobExceptionByteArray;
import static org.camunda.bpm.engine.impl.util.JobExceptionUtil.getJobExceptionStacktrace;
import static org.camunda.bpm.engine.impl.util.StringUtil.toByteArray;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobPriorityProvider;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.management.JobDefinition;
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
public abstract class JobEntity implements Serializable, Job, DbEntity, HasDbRevision {

  private final static EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  public static final boolean DEFAULT_EXCLUSIVE = true;
  public static final int DEFAULT_RETRIES = 3;

  /**
   * Note: {@link String#length()} counts Unicode supplementary
   * characters twice, so for a String consisting only of those,
   * the limit is effectively MAX_EXCEPTION_MESSAGE_LENGTH / 2
   */
  public static int MAX_EXCEPTION_MESSAGE_LENGTH = 666;

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected Date duedate;

  protected String lockOwner = null;
  protected Date lockExpirationTime = null;

  protected String executionId = null;
  protected String processInstanceId = null;

  protected String processDefinitionId = null;
  protected String processDefinitionKey = null;

  protected boolean isExclusive = DEFAULT_EXCLUSIVE;

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

  // runtime state /////////////////////////////
  protected boolean executing = false;
  protected String activityId;
  protected JobDefinition jobDefinition;
  protected ExecutionEntity execution;

  // sequence counter //////////////////////////
  protected long sequenceCounter = 1;

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
    ensureNotNull("Cannot find job handler '" + jobHandlerType + "' from job '" + this + "'", "jobHandler", jobHandler);
    jobHandler.execute(jobHandlerConfiguration, execution, commandContext);
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

  public void insert() {
    CommandContext commandContext = Context.getCommandContext();

    // add link to execution and deployment
    ExecutionEntity execution = getExecution();
    if (execution != null) {
      execution.addJob(this);

      ProcessDefinitionImpl processDefinition = (ProcessDefinitionImpl) execution.getProcessDefinition();
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
    commandContext.getJobManager().deleteJob(this, !executing);

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

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("executionId", executionId);
    persistentState.put("lockOwner", lockOwner);
    persistentState.put("lockExpirationTime", lockExpirationTime);
    persistentState.put("retries", retries);
    persistentState.put("duedate", duedate);
    persistentState.put("exceptionMessage", exceptionMessage);
    persistentState.put("suspensionState", suspensionState);
    persistentState.put("processDefinitionId", processDefinitionId);
    persistentState.put("jobDefinitionId", jobDefinitionId);
    persistentState.put("deploymentId", deploymentId);
    persistentState.put("jobHandlerConfiguration", jobHandlerConfiguration);
    persistentState.put("priority", priority);
    if(exceptionByteArrayId != null) {
      persistentState.put("exceptionByteArrayId", exceptionByteArrayId);
    }
    return persistentState;
  }

  public int getRevisionNext() {
    return revision+1;
  }

  public void setExecution(ExecutionEntity execution) {
    executionId = execution.getId();
    processInstanceId = execution.getProcessInstanceId();
    execution.addJob(this);
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

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  protected ExecutionEntity getExecution() {
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
          return;
        }

      }

      processEngineConfiguration
        .getIncidentHandler(incidentHandlerType)
        .handleIncident(getProcessDefinitionId(), getActivityId(), executionId, id, exceptionMessage);

    }
  }

  protected void removeFailedJobIncident(boolean incidentResolved) {
    IncidentHandler handler = Context
        .getProcessEngineConfiguration()
        .getIncidentHandler(Incident.FAILED_JOB_HANDLER_TYPE);

    if (incidentResolved) {
      handler.resolveIncident(getProcessDefinitionId(), null, executionId, id);
    } else {
      handler.deleteIncident(getProcessDefinitionId(), null, executionId, id);
    }
  }

  public String getExceptionStacktrace() {
    ByteArrayEntity byteArray = getExceptionByteArray();
    return getJobExceptionStacktrace(byteArray);
  }

  public void setSuspensionState(int state) {
    this.suspensionState = state;
  }

  public int getSuspensionState() {
    return suspensionState;
  }

  public boolean isSuspended() {
    return suspensionState == SuspensionState.SUSPENDED.getStateCode();
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public void setLockOwner(String claimedBy) {
    this.lockOwner = claimedBy;
  }

  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }

  public void setLockExpirationTime(Date claimedUntil) {
    this.lockExpirationTime = claimedUntil;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }
  public boolean isExclusive() {
    return isExclusive;
  }

  public void setExclusive(boolean isExclusive) {
    this.isExclusive = isExclusive;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getDuedate() {
    return duedate;
  }

  public void setDuedate(Date duedate) {
    this.duedate = duedate;
  }

  public void setExceptionStacktrace(String exception) {
    byte[] exceptionBytes = toByteArray(exception);

    ByteArrayEntity byteArray = getExceptionByteArray();

    if(byteArray == null) {
      byteArray = createJobExceptionByteArray(exceptionBytes);
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

  public String getJobHandlerType() {
    return jobHandlerType;
  }

  public void setJobHandlerType(String jobHandlerType) {
    this.jobHandlerType = jobHandlerType;
  }

  public String getJobHandlerConfiguration() {
    return jobHandlerConfiguration;
  }

  public void setJobHandlerConfiguration(String jobHandlerConfiguration) {
    this.jobHandlerConfiguration = jobHandlerConfiguration;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getExceptionMessage() {
    return exceptionMessage;
  }

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

  protected void ensureJobDefinitionInitialized() {
    if (jobDefinition == null && jobDefinitionId != null) {
      jobDefinition = Context
          .getCommandContext()
          .getJobDefinitionManager()
          .findById(jobDefinitionId);
    }
  }

  public void setExceptionMessage(String exceptionMessage) {
    if(exceptionMessage != null && exceptionMessage.length() > MAX_EXCEPTION_MESSAGE_LENGTH) {
      this.exceptionMessage = exceptionMessage.substring(0, MAX_EXCEPTION_MESSAGE_LENGTH);
    } else {
      this.exceptionMessage = exceptionMessage;
    }
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

  public boolean isExecuting() {
    return executing;
  }

  public void setExecuting(boolean executing) {
    this.executing = executing;
  }

  public String getActivityId() {
    ensureActivityIdInitialized();
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public long getPriority() {
    return priority;
  }

  public void setPriority(long priority) {
    this.priority = priority;
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

  public abstract String getType();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

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
           + ", deploymentId=" + deploymentId
           + ", priority=" + priority
           + "]";
  }

}
