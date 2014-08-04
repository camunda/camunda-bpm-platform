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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.incident.FailedJobIncidentHandler;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

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

  private final static Logger LOG = Logger.getLogger(JobEntity.class.getName());

  public static final boolean DEFAULT_EXCLUSIVE = true;
  public static final int DEFAULT_RETRIES = 3;
  private static final int MAX_EXCEPTION_MESSAGE_LENGTH = 255;

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

  public void execute(CommandContext commandContext) {
    ExecutionEntity execution = null;
    if (executionId != null) {
      execution = commandContext.getExecutionManager().findExecutionById(executionId);
      ensureNotNull("Cannot find execution with id '" + executionId + "' referenced from job '" + this + "'", "execution", execution);
    }

    Map<String, JobHandler> jobHandlers = Context.getProcessEngineConfiguration().getJobHandlers();
    JobHandler jobHandler = jobHandlers.get(jobHandlerType);

    jobHandler.execute(jobHandlerConfiguration, execution, commandContext);
  }

  public void insert() {
    DbSqlSession dbSqlSession = Context
      .getCommandContext()
      .getDbSqlSession();

    dbSqlSession.insert(this);

    // add link to execution and deployment
    if(executionId != null) {
      ExecutionEntity execution = Context.getCommandContext()
        .getExecutionManager()
        .findExecutionById(executionId);
      execution.addJob(this);
      this.deploymentId = execution.getProcessDefinition().getDeploymentId();
    }
  }

  public void delete() {
    delete(false);
  }

  public void delete(boolean incidentResolved) {
    DbSqlSession dbSqlSession = Context
        .getCommandContext()
        .getDbSqlSession();

    dbSqlSession.delete(this);

    // Also delete the job's exception byte array
    if (exceptionByteArrayId != null) {
      Context.getCommandContext().getByteArrayManager().deleteByteArrayById(exceptionByteArrayId);
    }

    // remove link to execution
    if(executionId != null) {
      ExecutionEntity execution = Context.getCommandContext()
        .getExecutionManager()
        .findExecutionById(executionId);
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

  // getters and setters //////////////////////////////////////////////////////

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public int getRetries() {
    return retries;
  }

  public void setRetries(int retries) {
    // Assuming: if the number of retries will
    // be changed from 0 to x (x >= 1), means
    // that the corresponding incident is resolved.
    if (this.retries == 0 && retries > 0) {
      removeFailedJobIncident(true);
    }

    // If the retries will be set to 0, an
    // incident has to be created.
    if(retries == 0) {
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

      String incidentHandlerType = FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE;

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
        .handleIncident(getProcessDefinitionId(), null, executionId, id, exceptionMessage);

    }
  }

  protected void removeFailedJobIncident(boolean incidentResolved) {
    IncidentHandler handler = Context
        .getProcessEngineConfiguration()
        .getIncidentHandler(FailedJobIncidentHandler.INCIDENT_HANDLER_TYPE);

    if (incidentResolved) {
      handler.resolveIncident(getProcessDefinitionId(), null, executionId, id);
    } else {
      handler.deleteIncident(getProcessDefinitionId(), null, executionId, id);
    }
  }

  public String getExceptionStacktrace() {
    String exception = null;
    ByteArrayEntity byteArray = getExceptionByteArray();
    if(byteArray != null) {
      try {
        exception = new String(byteArray.getBytes(), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new ProcessEngineException("UTF-8 is not a supported encoding");
      }
    }
    return exception;
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
    byte[] exceptionBytes = null;
    if(exception == null) {
      exceptionBytes = null;
    } else {

      try {
        exceptionBytes = exception.getBytes("UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new ProcessEngineException("UTF-8 is not a supported encoding");
      }
    }

    ByteArrayEntity byteArray = getExceptionByteArray();
    if(byteArray == null) {
      byteArray = new ByteArrayEntity("job.exceptionByteArray", exceptionBytes);
      Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(byteArray);
      exceptionByteArrayId = byteArray.getId();
      exceptionByteArray = byteArray;
    } else {
      byteArray.setBytes(exceptionBytes);
    }
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

  private ByteArrayEntity getExceptionByteArray() {
    if ((exceptionByteArray == null) && (exceptionByteArrayId != null)) {
      exceptionByteArray = Context
        .getCommandContext()
        .getDbSqlSession()
        .selectById(ByteArrayEntity.class, exceptionByteArrayId);
    }
    return exceptionByteArray;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
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
           + "]";
  }
}
