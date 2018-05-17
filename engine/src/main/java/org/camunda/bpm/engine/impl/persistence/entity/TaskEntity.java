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

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.auth.ResourceAuthorizationProvider;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableInstanceFactory;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableInstanceLifecycleListener;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableListenerInvocationListener;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableStore;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableStore.VariablesProvider;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.task.delegate.TaskListenerInvocation;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.camunda.bpm.engine.delegate.TaskListener.EVENTNAME_DELETE;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 * @author Deivarayan Azhagappan
 */
public class TaskEntity extends AbstractVariableScope implements Task, DelegateTask, Serializable, DbEntity, HasDbRevision, HasDbReferences, CommandContextListener, VariablesProvider<VariableInstanceEntity> {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  public static final String DELETE_REASON_COMPLETED = "completed";
  public static final String DELETE_REASON_DELETED = "deleted";

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected String owner;
  protected String assignee;
  protected DelegationState delegationState;

  protected String parentTaskId;
  protected transient TaskEntity parentTask;

  protected String name;
  protected String description;
  protected int priority = Task.PRIORITY_NORMAL;
  protected Date createTime; // The time when the task has been created
  protected Date dueDate;
  protected Date followUpDate;
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();
  protected String tenantId;

  protected boolean isIdentityLinksInitialized = false;
  protected transient List<IdentityLinkEntity> taskIdentityLinkEntities = new ArrayList<IdentityLinkEntity>();

  // execution
  protected String executionId;
  protected transient ExecutionEntity execution;

  protected String processInstanceId;
  protected transient ExecutionEntity processInstance;

  protected String processDefinitionId;

  // caseExecution
  protected String caseExecutionId;
  protected transient CaseExecutionEntity caseExecution;

  protected String caseInstanceId;
  protected String caseDefinitionId;

  // taskDefinition
  protected transient TaskDefinition taskDefinition;
  protected String taskDefinitionKey;

  protected boolean isDeleted;
  protected String deleteReason;

  protected String eventName;
  protected boolean isFormKeyInitialized = false;
  protected String formKey;

  @SuppressWarnings({ "unchecked" })
  protected transient VariableStore<VariableInstanceEntity> variableStore
    = new VariableStore<VariableInstanceEntity>(this, new TaskEntityReferencer(this));


  protected transient boolean skipCustomListeners = false;

  /**
   * contains all changed properties of this entity
   */
  protected transient Map<String, PropertyChange> propertyChanges = new HashMap<String, PropertyChange>();

  protected transient List<PropertyChange> identityLinkChanges = new ArrayList<PropertyChange>();

  // name references of tracked properties
  public static final String ASSIGNEE = "assignee";
  public static final String DELEGATION = "delegation";
  public static final String DELETE = "delete";
  public static final String DESCRIPTION = "description";
  public static final String DUE_DATE = "dueDate";
  public static final String FOLLOW_UP_DATE = "followUpDate";
  public static final String NAME = "name";
  public static final String OWNER = "owner";
  public static final String PARENT_TASK = "parentTask";
  public static final String PRIORITY = "priority";
  public static final String CASE_INSTANCE_ID = "caseInstanceId";

  public TaskEntity() {
    this(null);
  }

  public TaskEntity(String taskId) {
    this.id = taskId;
  }

  /** creates and initializes a new persistent task. */
  public static TaskEntity createAndInsert(VariableScope execution) {
    TaskEntity task = create();

    if (execution instanceof ExecutionEntity) {
      ExecutionEntity executionEntity = (ExecutionEntity) execution;
      task.setExecution(executionEntity);
      task.skipCustomListeners = executionEntity.isSkipCustomListeners();
      task.insert(executionEntity);
      return task;

    }
    else if (execution instanceof CaseExecutionEntity) {
      task.setCaseExecution((DelegateCaseExecution) execution);
    }

    task.insert(null);
    return task;
  }

  public void insert(ExecutionEntity execution) {
    ensureParentTaskActive();
    propagateExecutionTenantId(execution);
    propagateParentTaskTenantId();

    CommandContext commandContext = Context.getCommandContext();
    TaskManager taskManager = commandContext.getTaskManager();
    taskManager.insertTask(this);

    if(execution != null) {
      execution.addTask(this);
    }
  }

  protected void propagateExecutionTenantId(ExecutionEntity execution) {
    if(execution != null) {
      setTenantId(execution.getTenantId());
    }
  }

  protected void propagateParentTaskTenantId() {
    if (parentTaskId != null) {

      final TaskEntity parentTask = Context
          .getCommandContext()
          .getTaskManager()
          .findTaskById(parentTaskId);

      if(tenantId != null && !tenantIdIsSame(parentTask)) {
        throw LOG.cannotSetDifferentTenantIdOnSubtask(parentTaskId, parentTask.getTenantId(), tenantId);
      }

      setTenantId(parentTask.getTenantId());
    }
  }

  public void update() {
    ensureTenantIdNotChanged();

    registerCommandContextCloseListener();

    CommandContext commandContext = Context.getCommandContext();
    DbEntityManager dbEntityManger = commandContext.getDbEntityManager();

    dbEntityManger.merge(this);
  }

  protected void ensureTenantIdNotChanged() {
    final TaskEntity persistentTask = Context.getCommandContext().getTaskManager()
      .findTaskById(id);

    if(persistentTask != null) {

      boolean changed = !tenantIdIsSame(persistentTask);

      if(changed) {
        throw LOG.cannotChangeTenantIdOfTask(id, persistentTask.tenantId, tenantId);
      }
    }
  }

  protected boolean tenantIdIsSame(final TaskEntity otherTask) {
    final String otherTenantId = otherTask.getTenantId();

    if(otherTenantId == null) {
      return tenantId == null;
    }
    else {
      return otherTenantId.equals(tenantId);
    }
  }

  /** new task.  Embedded state and create time will be initialized.
   * But this task still will have to be persisted with
   * TransactionContext
   *     .getCurrent()
   *     .getPersistenceSession()
   *     .insert(task);
   */
  public static TaskEntity create() {
    TaskEntity task = new TaskEntity();
    task.isIdentityLinksInitialized = true;
    task.setCreateTime(ClockUtil.getCurrentTime());
    return task;
  }

  public void complete() {

    if (TaskListener.EVENTNAME_COMPLETE.equals(this.eventName) || TaskListener.EVENTNAME_DELETE.equals(this.eventName)) {
      throw LOG.invokeTaskListenerException(new IllegalStateException("invalid task state"));
    }
    // if the task is associated with a case
    // execution then call complete on the
    // associated case execution. The case
    // execution handles the completion of
    // the task.
    if (caseExecutionId != null) {
      getCaseExecution().manualComplete();
      return;
    }

    // in the other case:

    // ensure the the Task is not suspended
    ensureTaskActive();

    // trigger TaskListener.complete event
    fireEvent(TaskListener.EVENTNAME_COMPLETE);

    // delete the task
    Context
      .getCommandContext()
      .getTaskManager()
      .deleteTask(this, TaskEntity.DELETE_REASON_COMPLETED, false, skipCustomListeners);

    // if the task is associated with a
    // execution (and not a case execution)
    // then call signal an the associated
    // execution.
    if (executionId!=null) {
      ExecutionEntity execution = getExecution();
      execution.removeTask(this);
      execution.signal(null, null);
    }
  }

  public void caseExecutionCompleted() {
    // ensure the the Task is not suspended
    ensureTaskActive();

    // trigger TaskListener.complete event
    fireEvent(TaskListener.EVENTNAME_COMPLETE);

    // delete the task
    Context
      .getCommandContext()
      .getTaskManager()
      .deleteTask(this, TaskEntity.DELETE_REASON_COMPLETED, false, false);
  }

  public void delete(String deleteReason, boolean cascade) {
    this.deleteReason = deleteReason;
    fireEvent(EVENTNAME_DELETE);

    Context
      .getCommandContext()
      .getTaskManager()
      .deleteTask(this, deleteReason, cascade, skipCustomListeners);

    if (executionId != null) {
      ExecutionEntity execution = getExecution();
      execution.removeTask(this);
    }
  }

  public void delete(String deleteReason, boolean cascade, boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
    delete(deleteReason, cascade);
  }

  @Override
  public void delegate(String userId) {
    setDelegationState(DelegationState.PENDING);
    if (getOwner() == null) {
      setOwner(getAssignee());
    }
    setAssignee(userId);
  }

  public void resolve() {
    setDelegationState(DelegationState.RESOLVED);
    setAssignee(this.owner);
  }

  @Override
  public Object getPersistentState() {
    Map<String, Object> persistentState = new  HashMap<String, Object>();
    persistentState.put("assignee", this.assignee);
    persistentState.put("owner", this.owner);
    persistentState.put("name", this.name);
    persistentState.put("priority", this.priority);
    if (executionId != null) {
      persistentState.put("executionId", this.executionId);
    }
    if (processDefinitionId != null) {
      persistentState.put("processDefinitionId", this.processDefinitionId);
    }
    if (caseExecutionId != null) {
      persistentState.put("caseExecutionId", this.caseExecutionId);
    }
    if (caseInstanceId != null) {
      persistentState.put("caseInstanceId", this.caseInstanceId);
    }
    if (caseDefinitionId != null) {
      persistentState.put("caseDefinitionId", this.caseDefinitionId);
    }
    if (createTime != null) {
      persistentState.put("createTime", this.createTime);
    }
    if(description != null) {
      persistentState.put("description", this.description);
    }
    if(dueDate != null) {
      persistentState.put("dueDate", this.dueDate);
    }
    if(followUpDate != null) {
      persistentState.put("followUpDate", this.followUpDate);
    }
    if (parentTaskId != null) {
      persistentState.put("parentTaskId", this.parentTaskId);
    }
    if (delegationState != null) {
      persistentState.put("delegationState", this.delegationState);
    }
    if (tenantId != null) {
      persistentState.put("tenantId", this.tenantId);
    }

    persistentState.put("suspensionState", this.suspensionState);

    return persistentState;
  }

  @Override
  public int getRevisionNext() {
    return revision+1;
  }

  protected void ensureParentTaskActive() {
    if (parentTaskId != null) {
      TaskEntity parentTask = Context
          .getCommandContext()
          .getTaskManager()
          .findTaskById(parentTaskId);

      ensureNotNull(NullValueException.class, "Parent task with id '"+parentTaskId+"' does not exist", "parentTask", parentTask);

      if (parentTask.suspensionState == SuspensionState.SUSPENDED.getStateCode()) {
        throw LOG.suspendedEntityException("parent task", id);
      }
    }
  }

  protected void ensureTaskActive() {
    if (suspensionState == SuspensionState.SUSPENDED.getStateCode()) {
      throw LOG.suspendedEntityException("task", id);
    }
  }

  @Override
  public UserTask getBpmnModelElementInstance() {
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance();
    if(bpmnModelInstance != null) {
      ModelElementInstance modelElementInstance = bpmnModelInstance.getModelElementById(taskDefinitionKey);
      try {
        return (UserTask) modelElementInstance;
      } catch(ClassCastException e) {
        ModelElementType elementType = modelElementInstance.getElementType();
        throw LOG.castModelInstanceException(modelElementInstance, "UserTask", elementType.getTypeName(),
          elementType.getTypeNamespace(), e);
      }
    } else {
      return null;
    }
  }

  @Override
  public BpmnModelInstance getBpmnModelInstance() {
    if(processDefinitionId != null) {
      return Context.getProcessEngineConfiguration()
        .getDeploymentCache()
        .findBpmnModelInstanceForProcessDefinition(processDefinitionId);

    } else {
      return null;

    }
  }

  // variables ////////////////////////////////////////////////////////////////

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected VariableStore<CoreVariableInstance> getVariableStore() {
    return (VariableStore) variableStore;
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected VariableInstanceFactory<CoreVariableInstance> getVariableInstanceFactory() {
    return (VariableInstanceFactory) VariableInstanceEntityFactory.INSTANCE;
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected List<VariableInstanceLifecycleListener<CoreVariableInstance>> getVariableInstanceLifecycleListeners() {
    return Arrays.<VariableInstanceLifecycleListener<CoreVariableInstance>>asList(
        (VariableInstanceLifecycleListener) VariableInstanceEntityPersistenceListener.INSTANCE,
        (VariableInstanceLifecycleListener) VariableInstanceSequenceCounterListener.INSTANCE,
        (VariableInstanceLifecycleListener) VariableInstanceHistoryListener.INSTANCE
      );
  }

  @Override
  public void dispatchEvent(VariableEvent variableEvent) {
    if (execution != null && variableEvent.getVariableInstance().getTaskId() == null) {
      execution.handleConditionalEventOnVariableChange(variableEvent);
    }
  }

  @Override
  public Collection<VariableInstanceEntity> provideVariables() {
    return Context
        .getCommandContext()
        .getVariableInstanceManager()
        .findVariableInstancesByTaskId(id);
  }

  public Collection<VariableInstanceEntity> provideVariables(Collection<String> variableNames) {
    return Context
        .getCommandContext()
        .getVariableInstanceManager()
        .findVariableInstancesByTaskIdAndVariableNames(id, variableNames);
  }

  @Override
  public AbstractVariableScope getParentVariableScope() {
    if (getExecution()!=null) {
      return execution;
    }
    if (getCaseExecution()!=null) {
      return caseExecution;
    }
    if (getParentTask() != null) {
      return parentTask;
    }
    return null;
  }

  @Override
  public String getVariableScopeKey() {
    return "task";
  }

  // execution ////////////////////////////////////////////////////////////////

  public TaskEntity getParentTask() {
    if ( parentTask == null && parentTaskId != null) {
      this.parentTask = Context.getCommandContext()
                                .getTaskManager()
                                .findTaskById(parentTaskId);
    }
    return parentTask;
  }

  @Override
  public ExecutionEntity getExecution() {
    if ( (execution==null) && (executionId!=null) ) {
      this.execution = Context
        .getCommandContext()
        .getExecutionManager()
        .findExecutionById(executionId);
    }
    return execution;
  }

  public void setExecution(PvmExecutionImpl execution) {
    if (execution!=null) {

      this.execution = (ExecutionEntity) execution;
      this.executionId = this.execution.getId();
      this.processInstanceId = this.execution.getProcessInstanceId();
      this.processDefinitionId = this.execution.getProcessDefinitionId();

      // get the process instance
      ExecutionEntity instance = this.execution.getProcessInstance();
      if (instance != null) {
        // set case instance id on this task
        this.caseInstanceId = instance.getCaseInstanceId();
      }

    } else {
      this.execution = null;
      this.executionId = null;
      this.processInstanceId = null;
      this.processDefinitionId = null;
      this.caseInstanceId = null;
    }
  }

  // case execution ////////////////////////////////////////////////////////////////

  @Override
  public CaseExecutionEntity getCaseExecution() {
    ensureCaseExecutionInitialized();
    return caseExecution;
  }

  protected void ensureCaseExecutionInitialized() {
    if ((caseExecution==null) && (caseExecutionId!=null) ) {
      caseExecution = Context
        .getCommandContext()
        .getCaseExecutionManager()
        .findCaseExecutionById(caseExecutionId);
    }
  }

  public void setCaseExecution(DelegateCaseExecution caseExecution) {
    if (caseExecution!=null) {

      this.caseExecution = (CaseExecutionEntity) caseExecution;
      this.caseExecutionId = this.caseExecution.getId();
      this.caseInstanceId = this.caseExecution.getCaseInstanceId();
      this.caseDefinitionId = this.caseExecution.getCaseDefinitionId();
      this.tenantId = this.caseExecution.getTenantId();

    } else {
      this.caseExecution = null;
      this.caseExecutionId = null;
      this.caseInstanceId = null;
      this.caseDefinitionId = null;
      this.tenantId = null;

    }
  }

  @Override
  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public void setCaseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
  }

  @Override
  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  @Override
  public void setCaseInstanceId(String caseInstanceId) {
    registerCommandContextCloseListener();
    propertyChanged(CASE_INSTANCE_ID, this.caseInstanceId, caseInstanceId);
    this.caseInstanceId = caseInstanceId;
  }

  /* plain setter for persistence */
  public void setCaseInstanceIdWithoutCascade(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  public CaseDefinitionEntity getCaseDefinition() {
    if (caseDefinitionId != null) {
      return Context
          .getProcessEngineConfiguration()
          .getDeploymentCache()
          .findDeployedCaseDefinitionById(caseDefinitionId);
    }
    return null;
  }

  @Override
  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public void setCaseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
  }

  // task assignment //////////////////////////////////////////////////////////

  public IdentityLinkEntity addIdentityLink(String userId, String groupId, String type) {
    ensureTaskActive();

    IdentityLinkEntity identityLink = newIdentityLink(userId, groupId, type);
    identityLink.insert();
    getIdentityLinks().add(identityLink);

    fireAddIdentityLinkAuthorizationProvider(type, userId, groupId);
    return identityLink;
  }

  public void fireIdentityLinkHistoryEvents(String userId, String groupId, String type, HistoryEventTypes historyEventType) {
    IdentityLinkEntity identityLinkEntity = newIdentityLink(userId, groupId, type);
    identityLinkEntity.fireHistoricIdentityLinkEvent(historyEventType);
  }

  public IdentityLinkEntity newIdentityLink(String userId, String groupId, String type) {
    IdentityLinkEntity identityLinkEntity = new IdentityLinkEntity();
    identityLinkEntity.setTask(this);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(type);
    identityLinkEntity.setTenantId(getTenantId());
    return identityLinkEntity;
  }

  public void deleteIdentityLink(String userId, String groupId, String type) {
    ensureTaskActive();

    List<IdentityLinkEntity> identityLinks = Context
      .getCommandContext()
      .getIdentityLinkManager()
      .findIdentityLinkByTaskUserGroupAndType(id, userId, groupId, type);

    for (IdentityLinkEntity identityLink: identityLinks) {
      fireDeleteIdentityLinkAuthorizationProvider(type, userId, groupId);
      identityLink.delete();
    }
  }

  public void deleteIdentityLinks(boolean withHistory) {
    List<IdentityLinkEntity> identityLinkEntities = getIdentityLinks();
    for (IdentityLinkEntity identityLinkEntity : identityLinkEntities) {
      fireDeleteIdentityLinkAuthorizationProvider(identityLinkEntity.getType(),
        identityLinkEntity.getUserId(), identityLinkEntity.getGroupId());
      identityLinkEntity.delete(withHistory);
    }
    isIdentityLinksInitialized = false;
  }

  @Override
  public Set<IdentityLink> getCandidates() {
    Set<IdentityLink> potentialOwners = new HashSet<IdentityLink>();
    for (IdentityLinkEntity identityLinkEntity : getIdentityLinks()) {
      if (IdentityLinkType.CANDIDATE.equals(identityLinkEntity.getType())) {
        potentialOwners.add(identityLinkEntity);
      }
    }
    return potentialOwners;
  }

  @Override
  public void addCandidateUser(String userId) {
    addIdentityLink(userId, null, IdentityLinkType.CANDIDATE);
  }

  @Override
  public void addCandidateUsers(Collection<String> candidateUsers) {
    for (String candidateUser : candidateUsers) {
      addCandidateUser(candidateUser);
    }
  }

  @Override
  public void addCandidateGroup(String groupId) {
    addIdentityLink(null, groupId, IdentityLinkType.CANDIDATE);
  }

  @Override
  public void addCandidateGroups(Collection<String> candidateGroups) {
    for (String candidateGroup : candidateGroups) {
      addCandidateGroup(candidateGroup);
    }
  }

  @Override
  public void addGroupIdentityLink(String groupId, String identityLinkType) {
    addIdentityLink(null, groupId, identityLinkType);
  }

  @Override
  public void addUserIdentityLink(String userId, String identityLinkType) {
    addIdentityLink(userId, null, identityLinkType);
  }

  @Override
  public void deleteCandidateGroup(String groupId) {
    deleteGroupIdentityLink(groupId, IdentityLinkType.CANDIDATE);
  }

  @Override
  public void deleteCandidateUser(String userId) {
    deleteUserIdentityLink(userId, IdentityLinkType.CANDIDATE);
  }

  @Override
  public void deleteGroupIdentityLink(String groupId, String identityLinkType) {
    if (groupId!=null) {
      deleteIdentityLink(null, groupId, identityLinkType);
    }
  }

  @Override
  public void deleteUserIdentityLink(String userId, String identityLinkType) {
    if (userId!=null) {
      deleteIdentityLink(userId, null, identityLinkType);
    }
  }

  public List<IdentityLinkEntity> getIdentityLinks() {
    if (!isIdentityLinksInitialized) {
      taskIdentityLinkEntities = Context
        .getCommandContext()
        .getIdentityLinkManager()
        .findIdentityLinksByTaskId(id);
      isIdentityLinksInitialized = true;
    }

    return taskIdentityLinkEntities;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getActivityInstanceVariables() {
    if (execution!=null) {
      return execution.getVariables();
    }
    return Collections.EMPTY_MAP;
  }

  public void setExecutionVariables(Map<String, Object> parameters) {
    AbstractVariableScope scope = getParentVariableScope();
    if (scope != null) {
      scope.setVariables(parameters);
    }
  }

  @Override
  public String toString() {
    return "Task["+id+"]";
  }

  // special setters //////////////////////////////////////////////////////////

  @Override
  public void setName(String taskName) {
    registerCommandContextCloseListener();
    propertyChanged(NAME, this.name, taskName);
    this.name = taskName;
  }

  /* plain setter for persistence */
  public void setNameWithoutCascade(String taskName) {
    this.name = taskName;
  }

  @Override
  public void setDescription(String description) {
    registerCommandContextCloseListener();
    propertyChanged(DESCRIPTION, this.description, description);
    this.description = description;
  }

  /* plain setter for persistence */
  public void setDescriptionWithoutCascade(String description) {
    this.description = description;
  }

  @Override
  public void setAssignee(String assignee) {
    ensureTaskActive();
    registerCommandContextCloseListener();

    String oldAssignee = this.assignee;
    if (assignee==null && oldAssignee==null) {
      return;
    }

    addIdentityLinkChanges(IdentityLinkType.ASSIGNEE, oldAssignee, assignee);
    propertyChanged(ASSIGNEE, oldAssignee, assignee);
    this.assignee = assignee;

    CommandContext commandContext = Context.getCommandContext();
    // if there is no command context, then it means that the user is calling the
    // setAssignee outside a service method.  E.g. while creating a new task.
    if (commandContext != null) {
      fireEvent(TaskListener.EVENTNAME_ASSIGNMENT);
      if (commandContext.getDbEntityManager().contains(this)) {
        fireAssigneeAuthorizationProvider(oldAssignee, assignee);
        fireHistoricIdentityLinks();
      }
    }
  }

  /* plain setter for persistence */
  public void setAssigneeWithoutCascade(String assignee) {
    this.assignee = assignee;
  }

  @Override
  public void setOwner(String owner) {
    ensureTaskActive();
    registerCommandContextCloseListener();

    String oldOwner = this.owner;
    if (owner==null && oldOwner==null) {
      return;
    }

    addIdentityLinkChanges(IdentityLinkType.OWNER, oldOwner, owner);
    propertyChanged(OWNER, oldOwner, owner);
    this.owner = owner;

    CommandContext commandContext = Context.getCommandContext();
    // if there is no command context, then it means that the user is calling the
    // setOwner outside a service method.  E.g. while creating a new task.
    if (commandContext != null && commandContext.getDbEntityManager().contains(this)) {
      fireOwnerAuthorizationProvider(oldOwner, owner);
      this.fireHistoricIdentityLinks();
    }

  }

  /* plain setter for persistence */
  public void setOwnerWithoutCascade(String owner) {
    this.owner = owner;
  }

  @Override
  public void setDueDate(Date dueDate) {
    registerCommandContextCloseListener();
    propertyChanged(DUE_DATE, this.dueDate, dueDate);
    this.dueDate = dueDate;
  }

  public void setDueDateWithoutCascade(Date dueDate) {
    this.dueDate = dueDate;
  }

  @Override
  public void setPriority(int priority) {
    registerCommandContextCloseListener();
    propertyChanged(PRIORITY, this.priority, priority);
    this.priority = priority;
  }

  public void setPriorityWithoutCascade(int priority) {
    this.priority = priority;
  }

  @Override
  public void setParentTaskId(String parentTaskId) {
    registerCommandContextCloseListener();
    propertyChanged(PARENT_TASK, this.parentTaskId, parentTaskId);
    this.parentTaskId = parentTaskId;
  }

  public void setParentTaskIdWithoutCascade(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }

  public void setTaskDefinitionKeyWithoutCascade(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }

  public void fireEvent(String taskEventName) {

    List<TaskListener> taskEventListeners = getListenersForEvent(taskEventName);

    if (taskEventListeners != null) {
      for (TaskListener taskListener : taskEventListeners) {
        CoreExecution execution = getExecution();
        if (execution == null) {
          execution = getCaseExecution();
        }

        if (execution != null) {
          setEventName(taskEventName);
        }
        try {
          TaskListenerInvocation listenerInvocation = new TaskListenerInvocation(taskListener, this, execution);
          Context.getProcessEngineConfiguration()
            .getDelegateInterceptor()
            .handleInvocation(listenerInvocation);
        } catch (Exception e) {
          throw LOG.invokeTaskListenerException(e);
        }
      }
    }
  }

  protected List<TaskListener> getListenersForEvent(String event) {
    TaskDefinition resolvedTaskDefinition = getTaskDefinition();
    if (resolvedTaskDefinition != null) {
      if (skipCustomListeners) {
        return resolvedTaskDefinition.getBuiltinTaskListeners(event);
      }
      else {
        return resolvedTaskDefinition.getTaskListeners(event);
      }

    }
    else {
      return null;
    }
  }

  /**
   * Tracks a property change. Therefore the original and new value are stored in a map.
   * It tracks multiple changes and if a property finally is changed back to the original
   * value, then the change is removed.
   *
   * @param propertyName
   * @param orgValue
   * @param newValue
   */
  protected void propertyChanged(String propertyName, Object orgValue, Object newValue) {
    if (propertyChanges.containsKey(propertyName)) { // update an existing change to save the original value
      Object oldOrgValue = propertyChanges.get(propertyName).getOrgValue();
      if ((oldOrgValue == null && newValue == null) // change back to null
          || (oldOrgValue != null && oldOrgValue.equals(newValue))) { // remove this change
        propertyChanges.remove(propertyName);
      } else {
        propertyChanges.get(propertyName).setNewValue(newValue);
      }
    } else { // save this change
      if ((orgValue == null && newValue != null) // null to value
          || (orgValue != null && newValue == null) // value to null
          || (orgValue != null && !orgValue.equals(newValue))) // value change
        propertyChanges.put(propertyName, new PropertyChange(propertyName, orgValue, newValue));
    }
  }

  // authorizations ///////////////////////////////////////////////////////////

  public void fireAuthorizationProvider() {
    PropertyChange assigneePropertyChange = propertyChanges.get(ASSIGNEE);
    if (assigneePropertyChange != null) {
      String oldAssignee = assigneePropertyChange.getOrgValueString();
      String newAssignee = assigneePropertyChange.getNewValueString();
      fireAssigneeAuthorizationProvider(oldAssignee, newAssignee);
    }

    PropertyChange ownerPropertyChange = propertyChanges.get(OWNER);
    if (ownerPropertyChange != null) {
      String oldOwner = ownerPropertyChange.getOrgValueString();
      String newOwner = ownerPropertyChange.getNewValueString();
      fireOwnerAuthorizationProvider(oldOwner, newOwner);
    }
  }

  public void fireEvents() {
    PropertyChange assigneePropertyChange = propertyChanges.get(ASSIGNEE);
    if (assigneePropertyChange != null) {
      fireEvent(TaskListener.EVENTNAME_ASSIGNMENT);
    }
  }

  protected void fireAssigneeAuthorizationProvider(String oldAssignee, String newAssignee) {
    fireAuthorizationProvider(ASSIGNEE, oldAssignee, newAssignee);
  }

  protected void fireOwnerAuthorizationProvider(String oldOwner, String newOwner) {
    fireAuthorizationProvider(OWNER, oldOwner, newOwner);
  }

  protected void fireAuthorizationProvider(String property, String oldValue, String newValue) {
    if (isAuthorizationEnabled() && caseExecutionId == null) {
      ResourceAuthorizationProvider provider = getResourceAuthorizationProvider();

      AuthorizationEntity[] authorizations = null;
      if (ASSIGNEE.equals(property)) {
        authorizations = provider.newTaskAssignee(this, oldValue, newValue);
      }
      else if (OWNER.equals(property)) {
        authorizations = provider.newTaskOwner(this, oldValue, newValue);
      }

      saveAuthorizations(authorizations);
    }
  }

  protected void fireAddIdentityLinkAuthorizationProvider(String type, String userId, String groupId) {
    if (isAuthorizationEnabled() && caseExecutionId == null) {
      ResourceAuthorizationProvider provider = getResourceAuthorizationProvider();

      AuthorizationEntity[] authorizations = null;
      if (userId != null) {
        authorizations = provider.newTaskUserIdentityLink(this, userId, type);
      }
      else if (groupId != null) {
        authorizations = provider.newTaskGroupIdentityLink(this, groupId, type);
      }

      saveAuthorizations(authorizations);
    }
  }

  protected void fireDeleteIdentityLinkAuthorizationProvider(String type, String userId, String groupId) {
    if (isAuthorizationEnabled() && caseExecutionId == null) {
      ResourceAuthorizationProvider provider = getResourceAuthorizationProvider();

      AuthorizationEntity[] authorizations = null;
      if (userId != null) {
        authorizations = provider.deleteTaskUserIdentityLink(this, userId, type);
      }
      else if (groupId != null) {
        authorizations = provider.deleteTaskGroupIdentityLink(this, groupId, type);
      }

      deleteAuthorizations(authorizations);
    }
  }

  protected ResourceAuthorizationProvider getResourceAuthorizationProvider() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    return processEngineConfiguration.getResourceAuthorizationProvider();
  }

  protected void saveAuthorizations(AuthorizationEntity[] authorizations) {
    CommandContext commandContext = Context.getCommandContext();
    TaskManager taskManager = commandContext.getTaskManager();
    taskManager.saveDefaultAuthorizations(authorizations);
  }

  protected void deleteAuthorizations(AuthorizationEntity[] authorizations) {
    CommandContext commandContext = Context.getCommandContext();
    TaskManager taskManager = commandContext.getTaskManager();
    taskManager.deleteDefaultAuthorizations(authorizations);
  }

  protected boolean isAuthorizationEnabled() {
    return Context.getProcessEngineConfiguration().isAuthorizationEnabled();
  }

  // modified getters and setters /////////////////////////////////////////////

  public void setTaskDefinition(TaskDefinition taskDefinition) {
    this.taskDefinition = taskDefinition;
    this.taskDefinitionKey = taskDefinition.getKey();
  }

  public TaskDefinition getTaskDefinition() {
    if (taskDefinition==null && taskDefinitionKey!=null) {

      Map<String, TaskDefinition> taskDefinitions = null;
      if (processDefinitionId != null) {
        ProcessDefinitionEntity processDefinition = Context
            .getProcessEngineConfiguration()
            .getDeploymentCache()
            .findDeployedProcessDefinitionById(processDefinitionId);

        taskDefinitions = processDefinition.getTaskDefinitions();

      } else {
        CaseDefinitionEntity caseDefinition = Context
            .getProcessEngineConfiguration()
            .getDeploymentCache()
            .findDeployedCaseDefinitionById(caseDefinitionId);

        taskDefinitions = caseDefinition.getTaskDefinitions();
      }

      taskDefinition = taskDefinitions.get(taskDefinitionKey);
    }
    return taskDefinition;
  }

  // getters and setters //////////////////////////////////////////////////////

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public int getRevision() {
    return revision;
  }

  @Override
  public void setRevision(int revision) {
    this.revision = revision;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Date getDueDate() {
    return dueDate;
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  @Override
  public String getExecutionId() {
    return executionId;
  }

  @Override
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public ProcessDefinitionEntity getProcessDefinition() {
    if (processDefinitionId != null) {
      return Context
          .getProcessEngineConfiguration()
          .getDeploymentCache()
          .findDeployedProcessDefinitionById(processDefinitionId);
    }
    return null;
  }

  @Override
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void initializeFormKey() {
    isFormKeyInitialized = true;
    if(taskDefinitionKey != null) {
      TaskDefinition taskDefinition = getTaskDefinition();
      if(taskDefinition != null) {
        Expression formKey = taskDefinition.getFormKey();
        if(formKey != null) {
          this.formKey = (String) formKey.getValue(this);
        }
      }
    }
  }

  @Override
  public String getFormKey() {
    if(!isFormKeyInitialized) {
      throw LOG.uninitializedFormKeyException();
    }
    return formKey;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @Override
  public String getAssignee() {
    return assignee;
  }

  @Override
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public void setTaskDefinitionKey(String taskDefinitionKey) {
    if ((taskDefinitionKey == null && this.taskDefinitionKey != null)
     || (taskDefinitionKey != null && !taskDefinitionKey.equals(this.taskDefinitionKey))) {
      this.taskDefinition = null;
      this.formKey = null;
      this.isFormKeyInitialized = false;
    }

    this.taskDefinitionKey = taskDefinitionKey;
  }

  @Override
  public String getEventName() {
    return eventName;
  }
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public ExecutionEntity getProcessInstance() {
    return processInstance;
  }
  public void setProcessInstance(ExecutionEntity processInstance) {
    this.processInstance = processInstance;
  }
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  @Override
  public String getOwner() {
    return owner;
  }
  @Override
  public DelegationState getDelegationState() {
    return delegationState;
  }
  @Override
  public void setDelegationState(DelegationState delegationState) {
    propertyChanged(DELEGATION, this.delegationState, delegationState);
    this.delegationState = delegationState;
  }

  public void setDelegationStateWithoutCascade(DelegationState delegationState) {
    this.delegationState = delegationState;
  }

  public String getDelegationStateString() {
    return (delegationState!=null ? delegationState.toString() : null);
  }

  /**
   * Setter for mybatis mapper.
   *
   * @param delegationState  the delegation state as string
   */
  public void setDelegationStateString(String delegationState) {
    if (delegationState == null) {
      setDelegationStateWithoutCascade(null);
    } else {
      setDelegationStateWithoutCascade(DelegationState.valueOf(delegationState));
    }
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  @Override
  public String getDeleteReason() {
    return deleteReason;
  }

  public void setDeleted(boolean isDeleted) {
    propertyChanged(DELETE, this.isDeleted, isDeleted);
    this.isDeleted = isDeleted;
  }
  @Override
  public String getParentTaskId() {
    return parentTaskId;
  }

  public int getSuspensionState() {
    return suspensionState;
  }
  public void setSuspensionState(int suspensionState) {
    this.suspensionState = suspensionState;
  }
  @Override
  public boolean isSuspended() {
    return suspensionState == SuspensionState.SUSPENDED.getStateCode();
  }

  @Override
  public Date getFollowUpDate() {
    return followUpDate;
  }

  @Override
  public String getTenantId() {
    return tenantId;
  }

  @Override
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public void setFollowUpDate(Date followUpDate) {
    registerCommandContextCloseListener();
    propertyChanged(FOLLOW_UP_DATE, this.followUpDate, followUpDate);
    this.followUpDate = followUpDate;
  }

  public void setFollowUpDateWithoutCascade(Date followUpDate) {
    this.followUpDate = followUpDate;
  }

  public Collection<VariableInstanceEntity> getVariablesInternal() {
    return variableStore.getVariables();
  }

  @Override
  public void onCommandContextClose(CommandContext commandContext) {
    if(commandContext.getDbEntityManager().isDirty(this)) {
      commandContext.getHistoricTaskInstanceManager().updateHistoricTaskInstance(this);
    }
  }

  @Override
  public void onCommandFailed(CommandContext commandContext, Throwable t) {
    // ignore
  }

  protected void registerCommandContextCloseListener() {
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext.registerCommandContextListener(this);
    }
  }

  public Map<String, PropertyChange> getPropertyChanges() {
    return propertyChanges;
  }

  public void createHistoricTaskDetails(String operation) {
    final CommandContext commandContext = Context.getCommandContext();
    if (commandContext != null) {
      List<PropertyChange> values = new ArrayList<PropertyChange>(propertyChanges.values());
      commandContext.getOperationLogManager().logTaskOperations(operation, this, values);
      fireHistoricIdentityLinks();
      propertyChanges.clear();
    }
  }

  public void fireHistoricIdentityLinks() {
    for (PropertyChange propertyChange : identityLinkChanges) {
      String oldValue = propertyChange.getOrgValueString();
      String propertyName = propertyChange.getPropertyName();
      if (oldValue != null) {
        fireIdentityLinkHistoryEvents(oldValue, null, propertyName, HistoryEventTypes.IDENTITY_LINK_DELETE);
      }
      String newValue = propertyChange.getNewValueString();
      if (newValue != null) {
        fireIdentityLinkHistoryEvents(newValue, null, propertyName, HistoryEventTypes.IDENTITY_LINK_ADD);
      }
    }
    identityLinkChanges.clear();
  }

  @Override
  public ProcessEngineServices getProcessEngineServices() {
    return Context.getProcessEngineConfiguration()
          .getProcessEngine();
  }

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
    TaskEntity other = (TaskEntity) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  public void executeMetrics(String metricsName) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if(processEngineConfiguration.isMetricsEnabled()) {
      processEngineConfiguration.getMetricsRegistry().markOccurrence(Metrics.ACTIVTY_INSTANCE_START);
    }
  }

  public void addIdentityLinkChanges(String type, String oldProperty, String newProperty) {
    identityLinkChanges.add(new PropertyChange(type, oldProperty, newProperty));
  }

  @Override
  public void setVariablesLocal(Map<String, ?> variables) {
    super.setVariablesLocal(variables);
    Context.getCommandContext().getDbEntityManager().forceUpdate(this);
  }

  @Override
  public Set<String> getReferencedEntityIds() {
    Set<String> referencedEntityIds = new HashSet<String>();
    return referencedEntityIds;
  }

  @Override
  public Map<String, Class> getReferencedEntitiesIdAndClass() {
    Map<String, Class> referenceIdAndClass = new HashMap<String, Class>();

    if (processDefinitionId != null) {
      referenceIdAndClass.put(processDefinitionId, ProcessDefinitionEntity.class);
    }
    if (processInstanceId != null) {
      referenceIdAndClass.put(processInstanceId, ExecutionEntity.class);
    }
    if (executionId != null) {
      referenceIdAndClass.put(executionId, ExecutionEntity.class);
    }
    if (caseDefinitionId != null) {
      referenceIdAndClass.put(caseDefinitionId, CaseDefinitionEntity.class);
    }
    if (caseExecutionId != null) {
      referenceIdAndClass.put(caseExecutionId, CaseExecutionEntity.class);
    }

    return referenceIdAndClass;
  }
}
