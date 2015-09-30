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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.incident.FailedExternalTaskIncidentHandler;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskEntity implements ExternalTask, DbEntity, HasDbRevision {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected String id;
  protected int revision;

  protected String topicName;
  protected String workerId;
  protected Date lockExpirationTime;
  protected Integer retries;
  protected String errorMessage;

  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();
  protected String executionId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String activityId;
  protected String activityInstanceId;

  protected ExecutionEntity execution;


  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getTopicName() {
    return topicName;
  }
  public void setTopicName(String topic) {
    this.topicName = topic;
  }
  public String getWorkerId() {
    return workerId;
  }
  public void setWorkerId(String workerId) {
    this.workerId = workerId;
  }
  public Date getLockExpirationTime() {
    return lockExpirationTime;
  }
  public void setLockExpirationTime(Date lockExpirationTime) {
    this.lockExpirationTime = lockExpirationTime;
  }
  public String getExecutionId() {
    return executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }
  public String getActivityId() {
    return activityId;
  }
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }
  public String getActivityInstanceId() {
    return activityInstanceId;
  }
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }
  public int getRevisionNext() {
    return revision + 1;
  }
  public int getSuspensionState() {
    return suspensionState;
  }
  public void setSuspensionState(int suspensionState) {
    this.suspensionState = suspensionState;
  }
  public boolean isSuspended() {
    return suspensionState == SuspensionState.SUSPENDED.getStateCode();
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
  public Integer getRetries() {
    return retries;
  }
  public void setRetries(Integer retries) {
    this.retries = retries;
  }
  public String getErrorMessage() {
    return errorMessage;
  }
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
  public boolean areRetriesLeft() {
    return retries == null || retries > 0;
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new  HashMap<String, Object>();
    persistentState.put("topic", topicName);
    persistentState.put("workerId", workerId);
    persistentState.put("lockExpirationTime", lockExpirationTime);
    persistentState.put("retries", retries);
    persistentState.put("errorMessage", errorMessage);
    persistentState.put("executionId", executionId);
    persistentState.put("processInstanceId", processInstanceId);
    persistentState.put("processDefinitionId", processDefinitionId);
    persistentState.put("processDefinitionKey", processDefinitionKey);
    persistentState.put("activityId", activityId);
    persistentState.put("activityInstanceId", activityInstanceId);
    persistentState.put("suspensionState", suspensionState);

    return persistentState;
  }

  public void insert() {
    Context.getCommandContext()
      .getExternalTaskManager()
      .insert(this);

    getExecution().addExternalTask(this);
  }

  public void delete() {
    getExecution().removeExternalTask(this);

    Context.getCommandContext()
      .getExternalTaskManager()
      .delete(this);
  }

  public void complete(Map<String, Object> variables) {
    ensureActive();

    ExecutionEntity associatedExecution = getExecution();

    if (variables != null) {
      associatedExecution.setVariables(variables);
    }

    delete();

    associatedExecution.signal(null, null);
  }

  public void failed(String errorMessage, int retries, long retryDuration) {
    ensureActive();

    this.errorMessage = errorMessage;
    this.lockExpirationTime = new Date(ClockUtil.getCurrentTime().getTime() + retryDuration);
    setRetriesAndManageIncidents(retries);
  }

  public void setRetriesAndManageIncidents(int retries) {

    if (areRetriesLeft() && retries <= 0) {
      createIncident();
    }
    else if (!areRetriesLeft() && retries > 0) {
      removeIncident(true);
    }

    setRetries(retries);
  }

  protected void createIncident() {
    IncidentHandler incidentHandler = Context
        .getProcessEngineConfiguration()
        .getIncidentHandler(FailedExternalTaskIncidentHandler.INCIDENT_HANDLER_TYPE);

    incidentHandler.handleIncident(processDefinitionId, activityId, executionId, id, errorMessage);
  }

  protected void removeIncident(boolean incidentResolved) {
    IncidentHandler handler = Context
        .getProcessEngineConfiguration()
        .getIncidentHandler(FailedExternalTaskIncidentHandler.INCIDENT_HANDLER_TYPE);

    if (incidentResolved) {
      handler.resolveIncident(getProcessDefinitionId(), null, executionId, id);
    } else {
      handler.deleteIncident(getProcessDefinitionId(), null, executionId, id);
    }
  }

  public void lock(String workerId, long lockDuration) {
    this.workerId = workerId;
    this.lockExpirationTime = new Date(ClockUtil.getCurrentTime().getTime() + lockDuration);
  }

  public ExecutionEntity getExecution() {
    ensureExecutionInitialized();
    return execution;
  }

  public void setExecution(ExecutionEntity execution) {
    this.execution = execution;
  }

  protected void ensureExecutionInitialized() {
    if (execution == null) {
      execution = Context.getCommandContext().getExecutionManager().findExecutionById(executionId);
      EnsureUtil.ensureNotNull(
          "Cannot find execution with id " + executionId + " for external task " + id,
          "execution",
          execution);
    }
  }

  protected void ensureActive() {
    if (suspensionState == SuspensionState.SUSPENDED.getStateCode()) {
      throw LOG.suspendedEntityException(EntityTypes.EXTERNAL_TASK, id);
    }
  }

  public String toString() {
    return "ExternalTaskEntity ["
        + "id=" + id
        + ", revision=" + revision
        + ", topicName=" + topicName
        + ", workerId=" + workerId
        + ", lockExpirationTime=" + lockExpirationTime
        + ", executionId=" + executionId + "]";
  }

  public void unlock() {
    workerId = null;
    lockExpirationTime = null;
  }

  public static ExternalTaskEntity createAndInsert(ExecutionEntity execution, String topic) {
    ExternalTaskEntity externalTask = new ExternalTaskEntity();

    externalTask.setTopicName(topic);
    externalTask.setExecutionId(execution.getId());
    externalTask.setProcessInstanceId(execution.getProcessInstanceId());
    externalTask.setProcessDefinitionId(execution.getProcessDefinitionId());
    externalTask.setActivityId(execution.getActivityId());
    externalTask.setActivityInstanceId(execution.getActivityInstanceId());

    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();
    externalTask.setProcessDefinitionKey(processDefinition.getKey());

    externalTask.insert();

    return externalTask;
  }
}
