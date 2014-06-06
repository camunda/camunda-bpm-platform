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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.SuspendedEntityInteractionException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableScope;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableStore;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.db.HasRevision;
import org.camunda.bpm.engine.impl.db.PersistentObject;
import org.camunda.bpm.engine.impl.delegate.TaskListenerInvocation;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextCloseListener;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.variable.AbstractVariableStore;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

import java.io.Serializable;
import java.util.*;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 */
public class TaskEntity extends CoreVariableScope implements Task, DelegateTask, Serializable, PersistentObject, HasRevision, CommandContextCloseListener {

  public static final String DELETE_REASON_COMPLETED = "completed";
  public static final String DELETE_REASON_DELETED = "deleted";

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected String owner;
  protected String assignee;
  protected DelegationState delegationState;

  protected String parentTaskId;

  protected String name;
  protected String description;
  protected int priority = Task.PRIORITY_NORMAL;
  protected Date createTime; // The time when the task has been created
  protected Date dueDate;
  protected Date followUpDate;
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();

  protected boolean isIdentityLinksInitialized = false;
  protected transient List<IdentityLinkEntity> taskIdentityLinkEntities = new ArrayList<IdentityLinkEntity>();

  protected String executionId;
  protected transient ExecutionEntity execution;

  protected String processInstanceId;
  protected transient ExecutionEntity processInstance;

  protected String processDefinitionId;

  protected transient TaskDefinition taskDefinition;
  protected String taskDefinitionKey;

  protected boolean isDeleted;
  protected String deleteReason;

  protected String eventName;

  protected AbstractVariableStore variableStore;

  /**
   * contains all changed properties of this entity
   */
  private transient Map<String, PropertyChange> propertyChanges = new HashMap<String, PropertyChange>();

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

  public TaskEntity() {
    this(null);
  }

  public TaskEntity(String taskId) {
    this.id = taskId;
    this.variableStore = createVariableStore();
  }

  /** creates and initializes a new persistent task. */
  public static TaskEntity createAndInsert(ActivityExecution execution) {
    TaskEntity task = create();
    task.insert((ExecutionEntity) execution);
    return task;
  }

  public void insert(ExecutionEntity execution) {
    ensureParentTaskActive();

    CommandContext commandContext = Context.getCommandContext();
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
    dbSqlSession.insert(this);

    if(execution != null) {
      execution.addTask(this);
    }
  }

  public void update() {
    setAssignee(this.getAssignee());

    CommandContext commandContext = Context.getCommandContext();
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
    dbSqlSession.update(this);

    commandContext.registerCommandContextCloseListener(this);
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
    ensureTaskActive();

    fireEvent(TaskListener.EVENTNAME_COMPLETE);

    Context
      .getCommandContext()
      .getTaskManager()
      .deleteTask(this, TaskEntity.DELETE_REASON_COMPLETED, false);

    if (executionId!=null) {
      ExecutionEntity execution = getExecution();
      execution.removeTask(this);
      execution.signal(null, null);
    }
  }

  public void delete(String deleteReason, boolean cascade) {
    this.deleteReason = deleteReason;
    fireEvent(TaskListener.EVENTNAME_DELETE);

    Context
      .getCommandContext()
      .getTaskManager()
      .deleteTask(this, deleteReason, cascade);

    if (executionId != null) {
      ExecutionEntity execution = getExecution();
      execution.removeTask(this);
    }
  }

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

    persistentState.put("suspensionState", this.suspensionState);

    return persistentState;
  }

  public int getRevisionNext() {
    return revision+1;
  }

  protected void ensureParentTaskActive() {
    if (parentTaskId != null) {
      TaskEntity parentTask = Context
          .getCommandContext()
          .getTaskManager()
          .findTaskById(parentTaskId);

      if (parentTask.suspensionState == SuspensionState.SUSPENDED.getStateCode()) {
        throw new SuspendedEntityInteractionException("parent task " + id + " is suspended");
      }
    }
  }

  protected void ensureTaskActive() {
    if (suspensionState == SuspensionState.SUSPENDED.getStateCode()) {
      throw new SuspendedEntityInteractionException("task " + id + " is suspended");
    }
  }

  public UserTask getBpmnModelElementInstance() {
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance();
    if(bpmnModelInstance != null) {
      ModelElementInstance modelElementInstance = bpmnModelInstance.getModelElementById(taskDefinitionKey);
      try {
        return (UserTask) modelElementInstance;
      } catch(ClassCastException e) {
        ModelElementType elementType = modelElementInstance.getElementType();
        throw new ProcessEngineException("Cannot cast "+modelElementInstance+" to UserTask. "
            + "Is of type "+elementType.getTypeName() + " Namespace "
            + elementType.getTypeNamespace(), e);
      }
    } else {
      return null;
    }
  }

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

  protected AbstractVariableStore createVariableStore() {
    return new TaskEntityVariableStore(this);
  }

  protected CoreVariableStore getVariableStore() {
    return variableStore;
  }

  protected CoreVariableScope getParentVariableScope() {
    if (getExecution()!=null) {
      return execution;
    }
    return null;
  }

  protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {
    variableInstance.setTaskId(id);
    variableInstance.setExecutionId(executionId);
    variableInstance.setProcessInstanceId(processInstanceId);
  }

  protected List<VariableInstanceEntity> loadVariableInstances() {
    return Context
      .getCommandContext()
      .getVariableInstanceManager()
      .findVariableInstancesByTaskId(id);
  }

  // execution ////////////////////////////////////////////////////////////////

  public ExecutionEntity getExecution() {
    if ( (execution==null) && (executionId!=null) ) {
      this.execution = Context
        .getCommandContext()
        .getExecutionManager()
        .findExecutionById(executionId);
    }
    return execution;
  }

  public void setExecution(DelegateExecution execution) {
    if (execution!=null) {

      this.execution = (ExecutionEntity) execution;
      this.executionId = this.execution.getId();
      this.processInstanceId = this.execution.getProcessInstanceId();
      this.processDefinitionId = this.execution.getProcessDefinitionId();

    } else {
      this.execution = null;
      this.executionId = null;
      this.processInstanceId = null;
      this.processDefinitionId = null;
    }
  }

  // task assignment //////////////////////////////////////////////////////////

  public IdentityLinkEntity addIdentityLink(String userId, String groupId, String type) {
    ensureTaskActive();

    IdentityLinkEntity identityLinkEntity = IdentityLinkEntity.createAndInsert();
    getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setTask(this);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(type);
    return identityLinkEntity;
  }

  public void deleteIdentityLink(String userId, String groupId, String type) {
    ensureTaskActive();

    List<IdentityLinkEntity> identityLinks = Context
      .getCommandContext()
      .getIdentityLinkManager()
      .findIdentityLinkByTaskUserGroupAndType(id, userId, groupId, type);

    for (IdentityLinkEntity identityLink: identityLinks) {
      Context
        .getCommandContext()
        .getDbSqlSession()
        .delete(identityLink);
    }
  }

  public Set<IdentityLink> getCandidates() {
    Set<IdentityLink> potentialOwners = new HashSet<IdentityLink>();
    for (IdentityLinkEntity identityLinkEntity : getIdentityLinks()) {
      if (IdentityLinkType.CANDIDATE.equals(identityLinkEntity.getType())) {
        potentialOwners.add(identityLinkEntity);
      }
    }
    return potentialOwners;
  }

  public void addCandidateUser(String userId) {
    addIdentityLink(userId, null, IdentityLinkType.CANDIDATE);
  }

  public void addCandidateUsers(Collection<String> candidateUsers) {
    for (String candidateUser : candidateUsers) {
      addCandidateUser(candidateUser);
    }
  }

  public void addCandidateGroup(String groupId) {
    addIdentityLink(null, groupId, IdentityLinkType.CANDIDATE);
  }

  public void addCandidateGroups(Collection<String> candidateGroups) {
    for (String candidateGroup : candidateGroups) {
      addCandidateGroup(candidateGroup);
    }
  }

  public void addGroupIdentityLink(String groupId, String identityLinkType) {
    addIdentityLink(null, groupId, identityLinkType);
  }

  public void addUserIdentityLink(String userId, String identityLinkType) {
    addIdentityLink(userId, null, identityLinkType);
  }

  public void deleteCandidateGroup(String groupId) {
    deleteGroupIdentityLink(groupId, IdentityLinkType.CANDIDATE);
  }

  public void deleteCandidateUser(String userId) {
    deleteUserIdentityLink(userId, IdentityLinkType.CANDIDATE);
  }

  public void deleteGroupIdentityLink(String groupId, String identityLinkType) {
    if (groupId!=null) {
      deleteIdentityLink(null, groupId, identityLinkType);
    }
  }

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
    if (getExecution()!=null) {
      execution.setVariables(parameters);
    }
  }

  public String toString() {
    return "Task["+id+"]";
  }

  // special setters //////////////////////////////////////////////////////////

  public void setName(String taskName) {
    registerCommandContextCloseListener();
    propertyChanged(NAME, this.name, taskName);
    this.name = taskName;
  }

  /* plain setter for persistence */
  public void setNameWithoutCascade(String taskName) {
    this.name = taskName;
  }

  public void setDescription(String description) {
    registerCommandContextCloseListener();
    propertyChanged(DESCRIPTION, this.description, description);
    this.description = description;
  }

  /* plain setter for persistence */
  public void setDescriptionWithoutCascade(String description) {
    this.description = description;
  }

  public void setAssignee(String assignee) {
    ensureTaskActive();
    registerCommandContextCloseListener();

    if (assignee==null && this.assignee==null) {
      return;
    }
//    if (assignee!=null && assignee.equals(this.assignee)) {
//      return;
//    }
    propertyChanged(ASSIGNEE, this.assignee, assignee);
    this.assignee = assignee;

    CommandContext commandContext = Context.getCommandContext();
    // if there is no command context, then it means that the user is calling the
    // setAssignee outside a service method.  E.g. while creating a new task.
    if (commandContext!=null) {
      fireEvent(TaskListener.EVENTNAME_ASSIGNMENT);
    }
  }

  /* plain setter for persistence */
  public void setAssigneeWithoutCascade(String assignee) {
    this.assignee = assignee;
  }

  public void setOwner(String owner) {
    ensureTaskActive();
    registerCommandContextCloseListener();

    if (owner==null && this.owner==null) {
      return;
    }
//    if (owner!=null && owner.equals(this.owner)) {
//      return;
//    }
    propertyChanged(OWNER, this.owner, owner);
    this.owner = owner;

  }

  /* plain setter for persistence */
  public void setOwnerWithoutCascade(String owner) {
    this.owner = owner;
  }

  public void setDueDate(Date dueDate) {
    registerCommandContextCloseListener();
    propertyChanged(DUE_DATE, this.dueDate, dueDate);
    this.dueDate = dueDate;
  }

  public void setDueDateWithoutCascade(Date dueDate) {
    this.dueDate = dueDate;
  }

  public void setPriority(int priority) {
    registerCommandContextCloseListener();
    propertyChanged(PRIORITY, this.priority, priority);
    this.priority = priority;
  }

  public void setPriorityWithoutCascade(int priority) {
    this.priority = priority;
  }

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
    TaskDefinition taskDefinition = getTaskDefinition();
    if (taskDefinition != null) {
      List<TaskListener> taskEventListeners = getTaskDefinition().getTaskListener(taskEventName);
      if (taskEventListeners != null) {
        for (TaskListener taskListener : taskEventListeners) {
          ExecutionEntity execution = getExecution();
          if (execution != null) {
            setEventName(taskEventName);
          }
          try {
            Context.getProcessEngineConfiguration()
              .getDelegateInterceptor()
              .handleInvocation(new TaskListenerInvocation(taskListener, (DelegateTask)this, execution));
          }catch (Exception e) {
            throw new ProcessEngineException("Exception while invoking TaskListener: "+e.getMessage(), e);
          }
        }
      }
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

  // modified getters and setters /////////////////////////////////////////////

  public void setTaskDefinition(TaskDefinition taskDefinition) {
    this.taskDefinition = taskDefinition;
    this.taskDefinitionKey = taskDefinition.getKey();
  }

  public TaskDefinition getTaskDefinition() {
    if (taskDefinition==null && taskDefinitionKey!=null) {
      ProcessDefinitionEntity processDefinition = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedProcessDefinitionById(processDefinitionId);
      taskDefinition = processDefinition.getTaskDefinitions().get(taskDefinitionKey);
    }
    return taskDefinition;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public int getPriority() {
    return priority;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getAssignee() {
    return assignee;
  }

  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }

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
  public void setExecution(ExecutionEntity execution) {
    this.execution = execution;
  }
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  public String getOwner() {
    return owner;
  }
  public DelegationState getDelegationState() {
    return delegationState;
  }
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

  public String getDeleteReason() {
    return deleteReason;
  }

  public void setDeleted(boolean isDeleted) {
    propertyChanged(DELETE, this.isDeleted, isDeleted);
    this.isDeleted = isDeleted;
  }
  public String getParentTaskId() {
    return parentTaskId;
  }
  public Map<String, VariableInstanceEntity> getVariableInstances() {
    variableStore.ensureVariableInstancesInitialized();
    return variableStore.getVariableInstances();
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

  public Date getFollowUpDate() {
    return followUpDate;
  }

  public void setFollowUpDate(Date followUpDate) {
    registerCommandContextCloseListener();
    propertyChanged(FOLLOW_UP_DATE, this.followUpDate, followUpDate);
    this.followUpDate = followUpDate;
  }

  public void setFollowUpDateWithoutCascade(Date followUpDate) {
    this.followUpDate = followUpDate;
  }

  public void onCommandContextClose(CommandContext commandContext) {
    if(commandContext.getDbSqlSession().isUpdated(this)) {
      commandContext.getHistoricTaskInstanceManager().updateHistoricTaskInstance(this);
    }
  }
  protected void registerCommandContextCloseListener() {
    CommandContext commandContext = Context.getCommandContext();
    if (commandContext!=null) {
      commandContext.registerCommandContextCloseListener(this);
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
    }
    propertyChanges.clear();
  }

  public ProcessEngineServices getProcessEngineServices() {
    return Context.getProcessEngineConfiguration()
          .getProcessEngine();
  }

}
