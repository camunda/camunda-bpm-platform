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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.behavior.ExternalTaskActivityBehavior;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.runtime.Incident;

import static org.camunda.bpm.engine.impl.util.ExceptionUtil.createExceptionByteArray;
import static org.camunda.bpm.engine.impl.util.StringUtil.toByteArray;

/**
 * @author Thorben Lindhauer
 * @author Askar Akhmerov
 *
 */
public class ExternalTaskEntity implements ExternalTask, DbEntity, HasDbRevision, HasDbReferences {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;
  private static final String EXCEPTION_NAME = "externalTask.exceptionByteArray";

  /**
   * Note: {@link String#length()} counts Unicode supplementary
   * characters twice, so for a String consisting only of those,
   * the limit is effectively MAX_EXCEPTION_MESSAGE_LENGTH / 2
   */
  public static final int MAX_EXCEPTION_MESSAGE_LENGTH = 666;

  protected String id;
  protected int revision;

  protected String topicName;
  protected String workerId;
  protected Date lockExpirationTime;
  protected Integer retries;
  protected String errorMessage;

  protected ByteArrayEntity errorDetailsByteArray;
  protected String errorDetailsByteArrayId;

  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();
  protected String executionId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String activityId;
  protected String activityInstanceId;
  protected String tenantId;
  protected long priority;

  protected ExecutionEntity execution;

  protected String businessKey;

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
  public String getTenantId() {
    return tenantId;
  }
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
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

  public boolean areRetriesLeft() {
    return retries == null || retries > 0;
  }

  @Override
  public long getPriority() {
    return priority;
  }

  public void setPriority(long priority) {
    this.priority = priority;
  }

  @Override
  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
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
    persistentState.put("tenantId", tenantId);
    persistentState.put("priority", priority);

    if(errorDetailsByteArrayId != null) {
      persistentState.put("errorDetailsByteArrayId", errorDetailsByteArrayId);
    }

    return persistentState;
  }

  public void insert() {
    Context.getCommandContext()
      .getExternalTaskManager()
      .insert(this);

    getExecution().addExternalTask(this);
  }

  /**
   * Method implementation relies on the command context object,
   * therefore should be invoked from the commands only
   *
   * @return error details persisted in byte array table
   */
  public String getErrorDetails() {
    ByteArrayEntity byteArray = getErrorByteArray();
    return ExceptionUtil.getExceptionStacktrace(byteArray);
  }

  public void setErrorMessage(String errorMessage) {
    if(errorMessage != null && errorMessage.length() > MAX_EXCEPTION_MESSAGE_LENGTH) {
      this.errorMessage = errorMessage.substring(0, MAX_EXCEPTION_MESSAGE_LENGTH);
    } else {
      this.errorMessage = errorMessage;
    }
  }

  protected void setErrorDetails(String exception) {
    EnsureUtil.ensureNotNull("exception", exception);

    byte[] exceptionBytes = toByteArray(exception);

    ByteArrayEntity byteArray = getErrorByteArray();

    if(byteArray == null) {
      byteArray = createExceptionByteArray(EXCEPTION_NAME,exceptionBytes);
      errorDetailsByteArrayId = byteArray.getId();
      errorDetailsByteArray = byteArray;
    }
    else {
      byteArray.setBytes(exceptionBytes);
    }
  }

  public String getErrorDetailsByteArrayId() {
    return errorDetailsByteArrayId;
  }

  protected ByteArrayEntity getErrorByteArray() {
    ensureErrorByteArrayInitialized();
    return errorDetailsByteArray;
  }

  protected void ensureErrorByteArrayInitialized() {
    if (errorDetailsByteArray == null && errorDetailsByteArrayId != null) {
      errorDetailsByteArray = Context
          .getCommandContext()
          .getDbEntityManager()
          .selectById(ByteArrayEntity.class, errorDetailsByteArrayId);
    }
  }

  public void delete() {
    deleteFromExecutionAndRuntimeTable();
    produceHistoricExternalTaskDeletedEvent();
  }

  protected void deleteFromExecutionAndRuntimeTable() {
    getExecution().removeExternalTask(this);

    CommandContext commandContext = Context.getCommandContext();

    commandContext
      .getExternalTaskManager()
      .delete(this);

    // Also delete the external tasks's error details byte array
    if (errorDetailsByteArrayId != null) {
      commandContext.getByteArrayManager().deleteByteArrayById(errorDetailsByteArrayId);
    }
  }

  public void complete(Map<String, Object> variables, Map<String, Object> localVariables) {
    ensureActive();

    ExecutionEntity associatedExecution = getExecution();

    if (variables != null) {
      associatedExecution.setVariables(variables);
    }

    if (localVariables != null) {
      associatedExecution.setVariablesLocal(localVariables);
    }

    deleteFromExecutionAndRuntimeTable();

    produceHistoricExternalTaskSuccessfulEvent();

    associatedExecution.signal(null, null);
  }

  /**
   * process failed state, make sure that binary entity is created for the errorMessage, shortError
   * message does not exceed limit, handle properly retry counts and incidents
   *
   * @param errorMessage - short error message text
   * @param errorDetails - full error details
   * @param retries - updated value of retries left
   * @param retryDuration - used for lockExpirationTime calculation
   */
  public void failed(String errorMessage, String errorDetails, int retries, long retryDuration) {
    ensureActive();

    this.setErrorMessage(errorMessage);
    if (errorDetails != null) {
      setErrorDetails(errorDetails);
    }
    this.lockExpirationTime = new Date(ClockUtil.getCurrentTime().getTime() + retryDuration);
    setRetriesAndManageIncidents(retries);
    produceHistoricExternalTaskFailedEvent();
  }

  public void bpmnError(String errorCode) {
    ensureActive();
    ActivityExecution activityExecution = getExecution();
    BpmnError bpmnError = new BpmnError(errorCode);
    try {
      ExternalTaskActivityBehavior behavior = ((ExternalTaskActivityBehavior) activityExecution.getActivity().getActivityBehavior());
      behavior.propagateBpmnError(bpmnError, activityExecution);
    } catch (Exception ex) {
      throw ProcessEngineLogger.CMD_LOGGER.exceptionBpmnErrorPropagationFailed(errorCode, ex);
    }
  }

  public void setRetriesAndManageIncidents(int retries) {

    if (areRetriesLeft() && retries <= 0) {
      createIncident();
    }
    else if (!areRetriesLeft() && retries > 0) {
      removeIncident();
    }

    setRetries(retries);
  }

  protected void createIncident() {
    IncidentHandler incidentHandler = Context
        .getProcessEngineConfiguration()
        .getIncidentHandler(Incident.EXTERNAL_TASK_HANDLER_TYPE);

    incidentHandler.handleIncident(createIncidentContext(), errorMessage);
  }

  protected void removeIncident() {
    IncidentHandler handler = Context
        .getProcessEngineConfiguration()
        .getIncidentHandler(Incident.EXTERNAL_TASK_HANDLER_TYPE);

    handler.resolveIncident(createIncidentContext());
  }

  protected IncidentContext createIncidentContext() {
    IncidentContext context = new IncidentContext();
    context.setProcessDefinitionId(processDefinitionId);
    context.setExecutionId(executionId);
    context.setActivityId(activityId);
    context.setTenantId(tenantId);
    context.setConfiguration(id);
    return context;
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

  @Override
  public String toString() {
    return "ExternalTaskEntity ["
        + "id=" + id
        + ", revision=" + revision
        + ", topicName=" + topicName
        + ", workerId=" + workerId
        + ", lockExpirationTime=" + lockExpirationTime
        + ", priority=" + priority
        + ", errorMessage=" + errorMessage
        + ", errorDetailsByteArray=" + errorDetailsByteArray
        + ", errorDetailsByteArrayId=" + errorDetailsByteArrayId
        + ", executionId=" + executionId + "]";
  }

  public void unlock() {
    workerId = null;
    lockExpirationTime = null;
  }

  public static ExternalTaskEntity createAndInsert(ExecutionEntity execution, String topic, long priority) {
    ExternalTaskEntity externalTask = new ExternalTaskEntity();

    externalTask.setTopicName(topic);
    externalTask.setExecutionId(execution.getId());
    externalTask.setProcessInstanceId(execution.getProcessInstanceId());
    externalTask.setProcessDefinitionId(execution.getProcessDefinitionId());
    externalTask.setActivityId(execution.getActivityId());
    externalTask.setActivityInstanceId(execution.getActivityInstanceId());
    externalTask.setTenantId(execution.getTenantId());
    externalTask.setPriority(priority);

    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) execution.getProcessDefinition();
    externalTask.setProcessDefinitionKey(processDefinition.getKey());

    externalTask.insert();
    externalTask.produceHistoricExternalTaskCreatedEvent();

    return externalTask;
  }

  protected void produceHistoricExternalTaskCreatedEvent() {
    CommandContext commandContext = Context.getCommandContext();
    commandContext.getHistoricExternalTaskLogManager().fireExternalTaskCreatedEvent(this);
  }

  protected void produceHistoricExternalTaskFailedEvent() {
    CommandContext commandContext = Context.getCommandContext();
    commandContext.getHistoricExternalTaskLogManager().fireExternalTaskFailedEvent(this);
  }

  protected void produceHistoricExternalTaskSuccessfulEvent() {
    CommandContext commandContext = Context.getCommandContext();
    commandContext.getHistoricExternalTaskLogManager().fireExternalTaskSuccessfulEvent(this);
  }

  protected void produceHistoricExternalTaskDeletedEvent() {
    CommandContext commandContext = Context.getCommandContext();
    commandContext.getHistoricExternalTaskLogManager().fireExternalTaskDeletedEvent(this);
  }

  public void extendLock(long newLockExpirationTime) {
    ensureActive();
    long newTime = ClockUtil.getCurrentTime().getTime() + newLockExpirationTime;
    this.lockExpirationTime = new Date(newTime);
  }

  @Override
  public Set<String> getReferencedEntityIds() {
    Set<String> referencedEntityIds = new HashSet<String>();
    return referencedEntityIds;
  }

  @Override
  public Map<String, Class> getReferencedEntitiesIdAndClass() {
    Map<String, Class> referenceIdAndClass = new HashMap<String, Class>();

    if (executionId != null) {
      referenceIdAndClass.put(executionId, ExecutionEntity.class);
    }
    if (errorDetailsByteArrayId != null) {
      referenceIdAndClass.put(errorDetailsByteArrayId, ByteArrayEntity.class);
    }

    return referenceIdAndClass;
  }
}
