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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.SuspendedEntityInteractionException;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.operation.CoreAtomicOperation;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableStore;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.event.CompensationEventHandler;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.util.FormPropertyStartContext;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.AtomicOperation;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionStartContext;
import org.camunda.bpm.engine.impl.pvm.runtime.OutgoingExecution;
import org.camunda.bpm.engine.impl.pvm.runtime.ProcessInstanceStartContext;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.FoxAtomicOperationDeleteCascadeFireActivityEnd;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;
import org.camunda.bpm.engine.impl.util.BitMaskUtil;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;



/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Falko Menge
 */
public class ExecutionEntity extends PvmExecutionImpl implements
      Execution,
      ProcessInstance,
      DbEntity,
      HasDbRevision,
      HasDbReferences {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(ExecutionEntity.class.getName());

  // Persistent refrenced entities state //////////////////////////////////////
  public static final int EVENT_SUBSCRIPTIONS_STATE_BIT = 1;
  public static final int TASKS_STATE_BIT = 2;
  public static final int JOBS_STATE_BIT = 3;
  public static final int INCIDENT_STATE_BIT = 4;
  public static final int VARIABLES_STATE_BIT = 5;
  public static final int SUB_PROCESS_INSTANCE_STATE_BIT = 6;

  // current position /////////////////////////////////////////////////////////

  /** the process instance.  this is the root of the execution tree.
   * the processInstance of a process instance is a self reference. */
  protected transient ExecutionEntity processInstance;

  /** the parent execution */
  protected transient ExecutionEntity parent;

  /** nested executions representing scopes or concurrent paths */
  protected transient List<ExecutionEntity> executions;

  /** super execution, not-null if this execution is part of a subprocess */
  protected transient ExecutionEntity superExecution;

  /** super case execution, not-null if this execution is part of a case execution */
  protected transient CaseExecutionEntity superCaseExecution;

  /** reference to a subprocessinstance, not-null if currently subprocess is started from this execution */
  protected transient ExecutionEntity subProcessInstance;

  protected boolean shouldQueryForSubprocessInstance = false;

  // associated entities /////////////////////////////////////////////////////

  // (we cache associated entities here to minimize db queries)
  protected transient List<EventSubscriptionEntity> eventSubscriptions;
  protected transient List<JobEntity> jobs;
  protected transient List<TaskEntity> tasks;
  protected transient List<IncidentEntity> incidents;
  protected int cachedEntityState;

  protected ExecutionEntityVariableStore variableStore = new ExecutionEntityVariableStore(this);

  // replaced by //////////////////////////////////////////////////////////////

  /** when execution structure is pruned during a takeAll, then
   * the original execution has to be resolved to the replaced execution.
   * @see {@link #takeAll(List, List)} {@link OutgoingExecution} */
  protected transient ExecutionEntity replacedBy;

  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();

  // Persistence //////////////////////////////////////////////////////////////

  protected int revision = 1;

  /**
   * persisted reference to the processDefinition.
   *
   * @see #processDefinition
   * @see #setProcessDefinition(ProcessDefinitionImpl)
   * @see #getProcessDefinition()
   */
  protected String processDefinitionId;

  /**
   * persisted reference to the current position in the diagram within the
   * {@link #processDefinition}.
   *
   * @see #activity
   * @see #getActivity()
   */
  protected String activityId;

  /**
   * The name of the current activity position
   */
  protected String activityName;

  /**
   * persisted reference to the process instance.
   *
   * @see #getProcessInstance()
   */
  protected String processInstanceId;

  /**
   * persisted reference to the parent of this execution.
   *
   * @see #getParent()
   */
  protected String parentId;

  /**
   * persisted reference to the super execution of this execution
   *
   * @See {@link #getSuperExecution()}
   * @see #setSuperExecution(ExecutionEntity)
   */
  protected String superExecutionId;

  /**
   * persisted reference to the super case execution of this execution
   *
   * @See {@link #getSuperCaseExecution()}
   * @see #setSuperCaseExecution(ExecutionEntity)
   */
  protected String superCaseExecutionId;

  protected boolean forcedUpdate;

  public ExecutionEntity() {

  }

  public ExecutionEntity(ActivityImpl activityImpl) {
    if (activityImpl != null) {
      this.processInstanceStartContext = new HistoryAwareStartContext(activityImpl);
    }
  }

  public ExecutionEntity createExecution() {
    return createExecution(false);
  }

  /** creates a new execution. properties processDefinition, processInstance and activity will be initialized. */
  public ExecutionEntity createExecution(boolean initializeExecutionStartContext) {
    // create the new child execution
    ExecutionEntity createdExecution = newExecution(null);

    // manage the bidirectional parent-child relation
    ensureExecutionsInitialized();
    executions.add(createdExecution);
    createdExecution.setParent(this);

    // initialize the new execution
    createdExecution.setProcessDefinition(getProcessDefinition());
    createdExecution.setProcessInstance(getProcessInstance());
    createdExecution.setActivity(getActivity());

    // make created execution start in same activity instance
    createdExecution.activityInstanceId = activityInstanceId;

    if (initializeExecutionStartContext) {
      createdExecution.setStartContext(new ExecutionStartContext());
    }

    if (log.isLoggable(Level.FINE)) {
      log.fine("Child execution "+createdExecution+" created with parent "+this);
    }

    return createdExecution;
  }

  public ExecutionEntity createSubProcessInstance(PvmProcessDefinition processDefinition) {
    return createSubProcessInstance(processDefinition, null);
  }

  @SuppressWarnings("unchecked")
  public ExecutionEntity createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey) {
    ExecutionEntity subProcessInstance = newExecution((ActivityImpl) processDefinition.getInitial());

    shouldQueryForSubprocessInstance = true;

    // manage bidirectional super-subprocess relation
    subProcessInstance.setSuperExecution(this);
    this.setSubProcessInstance(subProcessInstance);

    // Initialize the new execution
    subProcessInstance.setProcessDefinition((ProcessDefinitionImpl) processDefinition);
    subProcessInstance.setProcessInstance(subProcessInstance);

    subProcessInstance.setCaseInstanceId(getCaseInstanceId());

    // create event subscriptions for the current scope
    for (EventSubscriptionDeclaration declaration : EventSubscriptionDeclaration.getDeclarationsForScope(subProcessInstance.getScopeActivity())) {
      declaration.createSubscription(subProcessInstance);
    }

    // create timer jobs
    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) processDefinition.getProperty(BpmnParse.PROPERTYNAME_TIMER_DECLARATION);
    if (timerDeclarations != null) {
      for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
        timerDeclaration.createTimerInstance(subProcessInstance);
      }
    }

    if(businessKey != null) {
      subProcessInstance.setBusinessKey(businessKey);
    }

    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    int historyLevel = configuration.getHistoryLevel();
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {

      final HistoryEventProducer eventFactory = configuration.getHistoryEventProducer();
      final HistoryEventHandler eventHandler = configuration.getHistoryEventHandler();

      // publish start event for sub process instance
      HistoryEvent hpise = eventFactory.createProcessInstanceStartEvt(subProcessInstance);
      eventHandler.handleEvent(hpise);

      // publish update event for current activity instance (containing the id of the sub process)
      HistoryEvent haie = eventFactory.createActivityInstanceUpdateEvt(this, null);
      eventHandler.handleEvent(haie);
    }

    return subProcessInstance;
  }

  protected ExecutionEntity newExecution(ActivityImpl activity) {
    ExecutionEntity newExecution = new ExecutionEntity(activity);

    initializeAssociations(newExecution);

    Context
      .getCommandContext()
      .getExecutionManager()
      .insertExecution(newExecution);

    return newExecution;
  }


  // scopes ///////////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public void initialize() {
    log.fine("initializing "+this);

    ScopeImpl scope = getScopeActivity();
    ensureParentInitialized();

    List<VariableDeclaration> variableDeclarations = (List<VariableDeclaration>) scope.getProperty(BpmnParse.PROPERTYNAME_VARIABLE_DECLARATIONS);
    if (variableDeclarations!=null) {
      for (VariableDeclaration variableDeclaration : variableDeclarations) {
        variableDeclaration.initialize(this, parent);
      }
    }

    initializeAssociations(this);

    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) scope.getProperty(BpmnParse.PROPERTYNAME_TIMER_DECLARATION);
    if (timerDeclarations!=null) {
      for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
        timerDeclaration.createTimerInstance(this);
      }
    }

    // create event subscriptions for the current scope
    for (EventSubscriptionDeclaration declaration : EventSubscriptionDeclaration.getDeclarationsForScope(scope)) {
      declaration.createSubscription(this);
    }
  }



  protected void initializeAssociations(ExecutionEntity execution) {
    // initialize the lists of referenced objects (prevents db queries)
    execution.executions = new ArrayList<ExecutionEntity>();
    execution.variableStore.setVariableInstances(new HashMap<String, VariableInstanceEntity>());
    execution.eventSubscriptions = new ArrayList<EventSubscriptionEntity>();
    execution.jobs = new ArrayList<JobEntity>();
    execution.tasks = new ArrayList<TaskEntity>();
    execution.incidents = new ArrayList<IncidentEntity>();

    // Cached entity-state initialized to null, all bits are zero, indicating NO entities present
    execution.cachedEntityState = 0;
  }

   public void startWithFormProperties(Map<String, Object> properties) {
     if(isProcessInstanceExecution()) {
       ActivityImpl initial = processDefinition.getInitial();
       if(processInstanceStartContext != null) {
         initial = processInstanceStartContext.getInitial();
       }
       FormPropertyStartContext formPropertyStartContext = new FormPropertyStartContext(initial);
       formPropertyStartContext.setFormProperties(properties);
       processInstanceStartContext = formPropertyStartContext;
     }
     performOperation(PvmAtomicOperation.PROCESS_START);
   }

   public void destroy() {
     super.destroy();
     ensureParentInitialized();
     variableStore.removeVariablesWithoutFiringEvents();
   }

  public void cancelScope(String reason) {

    // remove all tasks associated with this execution.
    removeTasks(reason);

    super.cancelScope(reason);

  }

  // methods that translate to operations /////////////////////////////////////

  @SuppressWarnings("deprecation")
  public <T extends CoreExecution> void performOperation(CoreAtomicOperation<T> operation) {
    if (operation instanceof AtomicOperation) {
      performOperation((AtomicOperation) operation);
    } else {
      super.performOperation(operation);
    }
  }

  @SuppressWarnings("deprecation")
  public <T extends CoreExecution> void performOperationSync(CoreAtomicOperation<T> operation) {
    if (operation instanceof AtomicOperation) {
      performOperationSync((AtomicOperation) operation);
    } else {
      super.performOperationSync(operation);
    }
  }

  @SuppressWarnings("deprecation")
  public void performOperation(AtomicOperation executionOperation) {
    if(executionOperation.isAsync(this)) {
      scheduleAtomicOperationAsync(executionOperation);
    } else {
      performOperationSync(executionOperation);
    }
  }

  @SuppressWarnings("deprecation")
  public void performOperationSync(AtomicOperation executionOperation) {
    if (requiresUnsuspendedExecution(executionOperation)) {
      ensureNotSuspended();
    }

    Context
      .getCommandContext()
      .performOperation(executionOperation, this);
  }

  protected void ensureNotSuspended() {
    if (isSuspended()) {
      throw new SuspendedEntityInteractionException("Execution " + id + " is suspended.");
    }
  }

  @SuppressWarnings("deprecation")
  protected boolean requiresUnsuspendedExecution(
      AtomicOperation executionOperation) {
    if (executionOperation != PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_END
        && executionOperation != PvmAtomicOperation.TRANSITION_DESTROY_SCOPE
        && executionOperation != PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_TAKE
        && executionOperation != PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_END
        && executionOperation != PvmAtomicOperation.TRANSITION_CREATE_SCOPE
        && executionOperation != PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_START
        && executionOperation != PvmAtomicOperation.DELETE_CASCADE
        && executionOperation != PvmAtomicOperation.DELETE_CASCADE_FIRE_ACTIVITY_END) {
      return true;
    }

    return false;
  }

  @SuppressWarnings({"unchecked", "deprecation"})
  protected void scheduleAtomicOperationAsync(AtomicOperation executionOperation) {

    MessageJobDeclaration messageJobDeclaration = null;

    List<MessageJobDeclaration> messageJobDeclarations = (List<MessageJobDeclaration>) getActivity().getProperty(BpmnParse.PROPERTYNAME_MESSAGE_JOB_DECLARATION);
    if (messageJobDeclarations != null) {
      for (MessageJobDeclaration declaration : messageJobDeclarations) {
        if (declaration.isApplicableForOperation(executionOperation)) {
          messageJobDeclaration = declaration;
          break;
        }
      }
    }

    if(messageJobDeclaration != null) {
      MessageEntity message = messageJobDeclaration.createJobInstance(this);
      messageJobDeclaration.setJobHandlerConfiguration(message, this, executionOperation);

      Context
        .getCommandContext()
        .getJobManager()
        .send(message);

    } else {
      throw new ProcessEngineException("Asynchronous continuation requires a message job declaration.");

    }
  }

  public boolean isActive(String activityId) {
    return findExecution(activityId)!=null;
  }

  public void inactivate() {
    this.isActive = false;
  }

  // executions ///////////////////////////////////////////////////////////////

  /** ensures initialization and returns the non-null executions list */
  public List<ExecutionEntity> getExecutions() {
    ensureExecutionsInitialized();
    return executions;
  }

  protected void ensureExecutionsInitialized() {
    if (executions==null) {
      if(isExecutionTreePrefetchEnabled()) {
        ensureExecutionTreeInitialized();

      } else {
        this.executions = Context
          .getCommandContext()
          .getExecutionManager()
          .findChildExecutionsByParentExecutionId(id);
      }

    }
  }

  /**
   * @return true if execution tree prefetching is enabled
   */
  protected boolean isExecutionTreePrefetchEnabled() {
    return Context.getProcessEngineConfiguration()
      .isExecutionTreePrefetchEnabled();
  }

  public void setExecutions(List<ExecutionEntity> executions) {
    this.executions = executions;
  }

  // bussiness key ////////////////////////////////////////////////////////////

  public String getProcessBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }

  // process definition ///////////////////////////////////////////////////////

  /** ensures initialization and returns the process definition. */
  public ProcessDefinitionImpl getProcessDefinition() {
    ensureProcessDefinitionInitialized();
    return processDefinition;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  /** for setting the process definition, this setter must be used as subclasses can override */
  protected void ensureProcessDefinitionInitialized() {
    if ((processDefinition == null) && (processDefinitionId != null)) {
      ProcessDefinitionEntity deployedProcessDefinition = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedProcessDefinitionById(processDefinitionId);
      setProcessDefinition(deployedProcessDefinition);
    }
  }

  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
    this.processDefinitionId = processDefinition.getId();
  }

  // process instance /////////////////////////////////////////////////////////

  /** ensures initialization and returns the process instance. */
  public ExecutionEntity getProcessInstance() {
    ensureProcessInstanceInitialized();
    return processInstance;
  }

  protected void ensureProcessInstanceInitialized() {
    if ((processInstance == null) && (processInstanceId != null)) {

      if(isExecutionTreePrefetchEnabled()) {
        ensureExecutionTreeInitialized();

      } else {
        processInstance =  Context
          .getCommandContext()
          .getExecutionManager()
          .findExecutionById(processInstanceId);
      }

    }
  }

  public void setProcessInstance(PvmExecutionImpl processInstance) {
    this.processInstance = (ExecutionEntity) processInstance;
    if (processInstance != null) {
      this.processInstanceId = this.processInstance.getId();
    }
  }

  public boolean isProcessInstanceExecution() {
    return parentId == null;
  }

 // activity /////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the activity */
  public ActivityImpl getActivity() {
    ensureActivityInitialized();
    return super.getActivity();
  }

  public String getActivityId() {
    return activityId;
  }

  /** must be called before the activity member field or getActivity() is called */
  protected void ensureActivityInitialized() {
    if ((activity == null) && (activityId != null)) {
      setActivity(getProcessDefinition().findActivity(activityId));
    }
  }

  @Override
  public void setActivity(PvmActivity activity) {
    super.setActivity(activity);
    if (activity != null) {
      this.activityId = activity.getId();
      this.activityName = (String) activity.getProperty("name");
    } else {
      this.activityId = null;
      this.activityName = null;
    }

  }

  /**
   * generates an activity instance id
   */
  protected String generateActivityInstanceId(String activityId) {

    if(activityId.equals(processDefinitionId)) {
      return processInstanceId;

    } else {

      String nextId = Context.getProcessEngineConfiguration()
        .getIdGenerator()
        .getNextId();

      String compositeId = activityId+":"+nextId;
      if(compositeId.length()>64) {
        return String.valueOf(nextId);
      } else {
        return compositeId;
      }
    }
  }

 // parent ///////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the parent */
  public ExecutionEntity getParent() {
    ensureParentInitialized();
    return parent;
  }

  protected void ensureParentInitialized() {
    if (parent == null && parentId != null) {
      if(isExecutionTreePrefetchEnabled()) {
        ensureExecutionTreeInitialized();

      } else {
        parent = Context
          .getCommandContext()
          .getExecutionManager()
          .findExecutionById(parentId);
      }
    }
  }

  public void setParent(PvmExecutionImpl parent) {
    this.parent = (ExecutionEntity) parent;

    if (parent != null) {
      this.parentId = parent.getId();
    } else {
      this.parentId = null;
    }
  }

  // super- and subprocess executions /////////////////////////////////////////

  public String getSuperExecutionId() {
    return superExecutionId;
  }

  public ExecutionEntity getSuperExecution() {
    ensureSuperExecutionInitialized();
    return superExecution;
  }

  public void setSuperExecution(PvmExecutionImpl superExecution) {
    this.superExecution = (ExecutionEntity) superExecution;
    if (superExecution != null) {
      superExecution.setSubProcessInstance(null);
    }

    if (superExecution != null) {
      this.superExecutionId = superExecution.getId();
    } else {
      this.superExecutionId = null;
    }
  }

  protected void ensureSuperExecutionInitialized() {
    if (superExecution == null && superExecutionId != null) {
      superExecution = Context
        .getCommandContext()
        .getExecutionManager()
        .findExecutionById(superExecutionId);
    }
  }

  public ExecutionEntity getSubProcessInstance() {
    ensureSubProcessInstanceInitialized();
    return subProcessInstance;
  }

  public void setSubProcessInstance(PvmExecutionImpl subProcessInstance) {
    shouldQueryForSubprocessInstance = subProcessInstance != null;
    this.subProcessInstance = (ExecutionEntity) subProcessInstance;
  }

  protected void ensureSubProcessInstanceInitialized() {
    if (shouldQueryForSubprocessInstance && subProcessInstance == null) {
      subProcessInstance = Context
        .getCommandContext()
        .getExecutionManager()
        .findSubProcessInstanceBySuperExecutionId(id);
    }
  }

  // super case executions ///////////////////////////////////////////////////

  public String getSuperCaseExecutionId() {
    return superCaseExecutionId;
  }

  public void setSuperCaseExecutionId(String superCaseExecutionId) {
    this.superCaseExecutionId = superCaseExecutionId;
  }

  public CaseExecutionEntity getSuperCaseExecution() {
    ensureSuperCaseExecutionInitialized();
    return superCaseExecution;
  }

  public void setSuperCaseExecution(CmmnExecution superCaseExecution) {
    this.superCaseExecution = (CaseExecutionEntity) superCaseExecution;

    if (superCaseExecution != null) {
      this.superCaseExecutionId = superCaseExecution.getId();
      this.caseInstanceId = superCaseExecution.getCaseInstanceId();
    } else {
      this.superCaseExecutionId = null;
      this.caseInstanceId = null;
    }
  }

  protected void ensureSuperCaseExecutionInitialized() {
    if (superCaseExecution == null && superCaseExecutionId != null) {
      superCaseExecution = Context
        .getCommandContext()
        .getCaseExecutionManager()
        .findCaseExecutionById(superCaseExecutionId);
    }
  }

  // customized persistence behavior /////////////////////////////////////////

  public void remove() {
    super.remove();

    // delete all the variable instances
    variableStore.removeVariablesWithoutFiringEvents();

    // delete all the tasks
    removeTasks(null);

    // remove all jobs
    removeJobs();

    // remove all incidents
    removeIncidents();

    // remove all event subscriptions for this scope, if the scope has event subscriptions:
    removeEventSubscriptions();

    // finally delete this execution
    Context.getCommandContext()
      .getDbSqlSession()
      .delete(this);
  }

  public void interruptScope(String reason) {
    // remove Jobs
    removeJobs();

    // remove event subscriptions which are not compensate event subscriptions
    List<EventSubscriptionEntity> eventSubscriptions = getEventSubscriptions();
    for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
      if(!CompensationEventHandler.EVENT_HANDLER_TYPE.equals(eventSubscriptionEntity.getEventType())) {
        eventSubscriptionEntity.delete();
      }
    }
  }

  public void removeEventSubscriptions() {
    for (EventSubscriptionEntity eventSubscription : getEventSubscriptions()) {
      if (replacedBy != null) {
        eventSubscription.setExecution((ExecutionEntity) replacedBy);
      } else {
        eventSubscription.delete();
      }
    }
  }

  private void removeJobs() {
    for (Job job: getJobs()) {
      if (replacedBy!=null) {
        ((JobEntity)job).setExecution((ExecutionEntity) replacedBy);
      } else {
        ((JobEntity)job).delete();
      }
    }
  }

  private void removeIncidents() {
    for (IncidentEntity incident: getIncidents()) {
      if (replacedBy!=null) {
        incident.setExecution((ExecutionEntity) replacedBy);
      } else {
        incident.delete();
      }
    }
  }

  private void removeTasks(String reason) {
    if(reason == null) {
      reason = TaskEntity.DELETE_REASON_DELETED;
    }
    for (TaskEntity task : getTasks()) {
      if (replacedBy!=null) {
        if(task.getExecution() == null || task.getExecution() != replacedBy) {
          // All tasks should have been moved when "replacedBy" has been set. Just in case tasks where added,
          // wo do an additional check here and move it
          task.setExecution(replacedBy);
          this.replacedBy.addTask(task);
        }
      } else {
        task.delete(reason, false);
      }
    }
  }

  public ExecutionEntity getReplacedBy() {
    return replacedBy;
  }

  public void setReplacedBy(PvmExecutionImpl replacedBy) {
    this.replacedBy = (ExecutionEntity) replacedBy;

    CommandContext commandContext = Context.getCommandContext();
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();

    // update the related tasks
    for (TaskEntity task: getTasks()) {
      task.setExecutionId(replacedBy.getId());
      task.setExecution(this.replacedBy);

      // update the related local task variables
      List<VariableInstanceEntity> variables = commandContext
        .getVariableInstanceManager()
        .findVariableInstancesByTaskId(task.getId());

      for (VariableInstanceEntity variable : variables) {
        variable.setExecution(this.replacedBy);
      }

      this.replacedBy.addTask(task);
    }

    // All tasks have been moved to 'replacedBy', safe to clear the list
    this.tasks.clear();

    List<TaskEntity> tasks = dbSqlSession.findInCache(TaskEntity.class);
    for (TaskEntity task: tasks) {
      if (id.equals(task.getExecutionId())) {
        task.setExecutionId(replacedBy.getId());
      }
    }

    // update the related jobs
    List<JobEntity> jobs = getJobs();
    for (JobEntity job: jobs) {
      job.setExecution((ExecutionEntity) replacedBy);
    }

    // update the related event subscriptions
    List<EventSubscriptionEntity> eventSubscriptions = getEventSubscriptions();
    for (EventSubscriptionEntity subscriptionEntity: eventSubscriptions) {
      subscriptionEntity.setExecution((ExecutionEntity) replacedBy);
    }

    // update the related process variables
    variableStore.ensureVariableInstancesInitialized();
    for (CoreVariableInstance variable: variableStore.getVariableInstances().values()) {
      ((VariableInstanceEntity) variable).setExecutionId(replacedBy.getId());
    }

    // TODO: fire UPDATE activity instance events with new execution?
    super.setReplacedBy(replacedBy);
  }

  public void replace(PvmExecutionImpl execution) {
    ExecutionEntity replacedExecution = (ExecutionEntity) execution;

    CommandContext commandContext = Context.getCommandContext();

    // update the related tasks
    for (TaskEntity task: replacedExecution.getTasksInternal()) {
      task.setExecutionId(getId());
      task.setExecution(this);

      // update the related local task variables
      List<VariableInstanceEntity> variables = commandContext
        .getVariableInstanceManager()
        .findVariableInstancesByTaskId(task.getId());

      for (VariableInstanceEntity variable : variables) {
        variable.setExecution(this);
      }

      addTask(task);
    }
    replacedExecution.getTasksInternal().clear();

    super.replace(replacedExecution);
  }

  // variables ////////////////////////////////////////////////////////////////

  protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {
    variableInstance.setProcessInstanceId(processInstanceId);
    variableInstance.setExecutionId(id);
  }

  protected List<VariableInstanceEntity> loadVariableInstances() {
    return Context
      .getCommandContext()
      .getVariableInstanceManager()
      .findVariableInstancesByExecutionId(id);
  }

  protected boolean isAutoFireHistoryEvents() {
    // as long as the process instance is starting (ie. before activity instance of
    // the selected initial (start event) is created), the variable scope should not
    // automatic fire history events for variable updates.

    // firing the events is triggered by the processInstanceStart context after the initial activity
    // has been initialized. The effect is that the activity instance id of the historic variable instances
    // will be the activity instance id of the start event.

    return processInstanceStartContext == null && startContext == null;
  }

  public void fireHistoricVariableInstanceCreateEvents() {
    // this method is called by the start context and batch-fires create events for all variable instances
    Map<String, CoreVariableInstance> variableInstances = variableStore.getVariableInstances();
    if(variableInstances != null) {
      for (Entry<String, CoreVariableInstance> variable : variableInstances.entrySet()) {
        variableStore.fireHistoricVariableInstanceCreate((VariableInstanceEntity) variable.getValue(), this);
      }
    }
  }

  /**
   * Fetch all the executions inside the same process instance as list and then
   * reconstruct the complete execution tree.
   *
   * In many cases this is an optimization over fetching the execution tree
   * lazily. Usually we need all executions anyway and it is preferable to fetch
   * more data in a single query (maybe even too much data) then to run multiple
   * queries, each returning a fraction of the data.
   *
   * The most important consideration here is network roundtrip:  If the process
   * engine and database run on separate hosts, network roundtrip has to be added
   * to each query. Economizing on the number of queries economizes on network
   * roundtrip. The tradeoff here is network roundtrip vs. throughput: multiple
   * roundtrips carrying small chucks of data vs. a single roundtrip carrying
   * more data.
   *
   */
  protected void ensureExecutionTreeInitialized() {
    List<ExecutionEntity> executions = Context.getCommandContext()
      .getExecutionManager()
      .findChildExecutionsByProcessInstanceId(processInstanceId);

    ExecutionEntity processInstance = null;

    Map<String, ExecutionEntity> executionMap = new HashMap<String, ExecutionEntity>();
    for (ExecutionEntity execution : executions) {
      execution.executions = new ArrayList<ExecutionEntity>();
      executionMap.put(execution.getId(), execution);
      if(execution.isProcessInstanceExecution()) {
        processInstance = execution;
      }
    }

    for (ExecutionEntity execution : executions) {
      String parentId = execution.getParentId();
      ExecutionEntity parent = executionMap.get(parentId);
      if(!execution.isProcessInstanceExecution()) {
        execution.processInstance = processInstance;
        execution.parent = parent;
        parent.executions.add(execution);
      } else {
        execution.processInstance = execution;
      }
    }
  }

  // persistent state /////////////////////////////////////////////////////////

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("processDefinitionId", this.processDefinitionId);
    persistentState.put("businessKey", businessKey);
    persistentState.put("activityId", this.activityId);
    persistentState.put("activityInstanceId", this.activityInstanceId);
    persistentState.put("isActive", this.isActive);
    persistentState.put("isConcurrent", this.isConcurrent);
    persistentState.put("isScope", this.isScope);
    persistentState.put("isEventScope", this.isEventScope);
    persistentState.put("parentId", parentId);
    persistentState.put("superExecution", this.superExecutionId);
    persistentState.put("superCaseExecutionId", this.superCaseExecutionId);
    persistentState.put("caseInstanceId", this.caseInstanceId);
    if (forcedUpdate) {
      persistentState.put("forcedUpdate", Boolean.TRUE);
    }
    persistentState.put("suspensionState", this.suspensionState);
    persistentState.put("cachedEntityState", getCachedEntityState());
    return persistentState;
  }

  public void insert() {
    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(this);
  }

  public void deleteCascade2(String deleteReason) {
    this.deleteReason = deleteReason;
    this.deleteRoot = true;
    performOperation(new FoxAtomicOperationDeleteCascadeFireActivityEnd());
  }

  public int getRevisionNext() {
    return revision+1;
  }

  public void forceUpdate() {
    this.forcedUpdate = true;
  }

  // toString /////////////////////////////////////////////////////////////////

  public String toString() {
    if (isProcessInstanceExecution()) {
      return "ProcessInstance["+getToStringIdentity()+"]";
    } else {
      return (isConcurrent? "Concurrent" : "")+(isScope ? "Scope" : "")+"Execution["+getToStringIdentity()+"]";
    }
  }

  protected String getToStringIdentity() {
    return id;
  }

  // event subscription support //////////////////////////////////////////////

  public List<EventSubscriptionEntity> getEventSubscriptionsInternal() {
    ensureEventSubscriptionsInitialized();
    return eventSubscriptions;
  }

  public List<EventSubscriptionEntity> getEventSubscriptions() {
    return new ArrayList<EventSubscriptionEntity>(getEventSubscriptionsInternal());
  }

  public List<CompensateEventSubscriptionEntity> getCompensateEventSubscriptions() {
    List<EventSubscriptionEntity> eventSubscriptions = getEventSubscriptionsInternal();
    List<CompensateEventSubscriptionEntity> result = new ArrayList<CompensateEventSubscriptionEntity>(eventSubscriptions.size());
    for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
      if(eventSubscriptionEntity instanceof CompensateEventSubscriptionEntity) {
        result.add((CompensateEventSubscriptionEntity) eventSubscriptionEntity);
      }
    }
    return result;
  }

  public List<CompensateEventSubscriptionEntity> getCompensateEventSubscriptions(String activityId) {
    List<EventSubscriptionEntity> eventSubscriptions = getEventSubscriptionsInternal();
    List<CompensateEventSubscriptionEntity> result = new ArrayList<CompensateEventSubscriptionEntity>(eventSubscriptions.size());
    for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
      if(eventSubscriptionEntity instanceof CompensateEventSubscriptionEntity) {
        if(activityId.equals(eventSubscriptionEntity.getActivityId())) {
          result.add((CompensateEventSubscriptionEntity) eventSubscriptionEntity);
        }
      }
    }
    return result;
  }

  protected void ensureEventSubscriptionsInitialized() {
    if (eventSubscriptions == null) {

      eventSubscriptions = Context.getCommandContext()
        .getEventSubscriptionManager()
        .findEventSubscriptionsByExecution(id);
    }
  }

  public void addEventSubscription(EventSubscriptionEntity eventSubscriptionEntity) {
    getEventSubscriptionsInternal().add(eventSubscriptionEntity);

  }

  public void removeEventSubscription(EventSubscriptionEntity eventSubscriptionEntity) {
    getEventSubscriptionsInternal().remove(eventSubscriptionEntity);
  }

  // referenced job entities //////////////////////////////////////////////////

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void ensureJobsInitialized() {
    if(jobs == null) {
      jobs = (List)Context.getCommandContext()
        .getJobManager()
        .findJobsByExecutionId(id);
    }
  }

  protected List<JobEntity> getJobsInternal() {
    ensureJobsInitialized();
    return jobs;
  }

  public List<JobEntity> getJobs() {
    return new ArrayList<JobEntity>(getJobsInternal());
  }

  public void addJob(JobEntity jobEntity) {
    getJobsInternal().add(jobEntity);
  }

  public void removeJob(JobEntity job) {
    getJobsInternal().remove(job);
  }

  // referenced incidents entities //////////////////////////////////////////////

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void ensureIncidentsInitialized() {
    if(incidents == null) {
      incidents = (List)Context.getCommandContext()
        .getIncidentManager()
        .findIncidentsByExecution(id);
    }
  }

  protected List<IncidentEntity> getIncidentsInternal() {
    ensureIncidentsInitialized();
    return incidents;
  }

  public List<IncidentEntity> getIncidents() {
    return new ArrayList<IncidentEntity>(getIncidentsInternal());
  }

  public void addIncident(IncidentEntity incident) {
    getIncidentsInternal().add(incident);
  }

  public void removeIncident(IncidentEntity incident) {
    getIncidentsInternal().remove(incident);
  }

  public IncidentEntity getIncidentByCauseIncidentId(String causeIncidentId) {
    for (IncidentEntity incident : getIncidents()) {
      if (incident.getCauseIncidentId() != null &&
          incident.getCauseIncidentId().equals(causeIncidentId)) {
        return incident;
      }
    }
    return null;
  }

  // referenced task entities ///////////////////////////////////////////////////

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void ensureTasksInitialized() {
    if(tasks == null) {
      tasks = (List)Context.getCommandContext()
        .getTaskManager()
        .findTasksByExecutionId(id);
    }
  }

  protected List<TaskEntity> getTasksInternal() {
    ensureTasksInitialized();
    return tasks;
  }

  public List<TaskEntity> getTasks() {
    return new ArrayList<TaskEntity>(getTasksInternal());
  }

  public void addTask(TaskEntity taskEntity) {
    getTasksInternal().add(taskEntity);
  }

  public void removeTask(TaskEntity task) {
    getTasksInternal().remove(task);
  }

  // variables /////////////////////////////////////////////////////////

  protected CoreVariableStore getVariableStore() {
    return variableStore;
  }

  // getters and setters //////////////////////////////////////////////////////

  public void setCachedEntityState(int cachedEntityState) {
    this.cachedEntityState = cachedEntityState;

    // Check for flags that are down. These lists can be safely initialized as empty, preventing
    // additional queries that end up in an empty list anyway
    if(jobs == null && !BitMaskUtil.isBitOn(cachedEntityState, JOBS_STATE_BIT)) {
      jobs = new ArrayList<JobEntity>();
    }
    if(tasks == null && !BitMaskUtil.isBitOn(cachedEntityState, TASKS_STATE_BIT)) {
      tasks = new ArrayList<TaskEntity>();
    }
    if(eventSubscriptions == null && !BitMaskUtil.isBitOn(cachedEntityState, EVENT_SUBSCRIPTIONS_STATE_BIT)) {
      eventSubscriptions = new ArrayList<EventSubscriptionEntity>();
    }
    if(incidents == null && !BitMaskUtil.isBitOn(cachedEntityState, INCIDENT_STATE_BIT)) {
      incidents = new ArrayList<IncidentEntity>();
    }
    if(variableStore.getVariableInstancesWithoutInitialization() == null && !BitMaskUtil.isBitOn(cachedEntityState, VARIABLES_STATE_BIT)) {
      variableStore.setVariableInstances(new HashMap<String, VariableInstanceEntity>());
    }
    shouldQueryForSubprocessInstance = BitMaskUtil.isBitOn(cachedEntityState, SUB_PROCESS_INSTANCE_STATE_BIT);
  }

  public int getCachedEntityState() {
    cachedEntityState = 0;
    Map<String, VariableInstanceEntity> variableInstances = variableStore.getVariableInstancesWithoutInitialization();

    // Only mark a flag as false when the list is not-null and empty. If null, we can't be sure there are no entries in it since
    // the list hasn't been initialized/queried yet.
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, TASKS_STATE_BIT, (tasks == null || tasks.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, EVENT_SUBSCRIPTIONS_STATE_BIT, (eventSubscriptions == null || eventSubscriptions.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, JOBS_STATE_BIT, (jobs == null || jobs.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, INCIDENT_STATE_BIT, (incidents == null || incidents.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, VARIABLES_STATE_BIT, (variableInstances == null || variableInstances.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, SUB_PROCESS_INSTANCE_STATE_BIT, shouldQueryForSubprocessInstance);

    return cachedEntityState;
  }

  public int getCachedEntityStateRaw() {
    return cachedEntityState;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getParentId() {
    return parentId;
  }
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public boolean hasReferenceTo(DbEntity entity) {
    if(entity instanceof ExecutionEntity) {
      ExecutionEntity executionEntity = (ExecutionEntity) entity;
      String otherId = executionEntity.getId();

      // parentId
      if(parentId != null && parentId.equals(otherId)) {
        return true;
      }

      // superExecutionId
      if(superExecutionId != null && superExecutionId.equals(otherId)) {
        return true;
      }

    }

    return false;
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

  public ProcessInstanceStartContext getProcessInstanceStartContext() {
    if(isProcessInstanceExecution()) {
      if(processInstanceStartContext == null) {

        ActivityImpl activity = getActivity();
        processInstanceStartContext = new HistoryAwareStartContext(activity);

      }
    }
    return processInstanceStartContext;
  }

  public String getCurrentActivityId() {
    return activityId;
  }

  public String getCurrentActivityName() {
    return activityName;
  }

  public FlowElement getBpmnModelElementInstance() {
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance();
    if(bpmnModelInstance != null) {

      ModelElementInstance modelElementInstance = null;
      if(ExecutionListener.EVENTNAME_TAKE.equals(eventName)) {
        modelElementInstance = bpmnModelInstance.getModelElementById(transition.getId());
      } else {
        modelElementInstance = bpmnModelInstance.getModelElementById(activityId);
      }

      try {
        return (FlowElement) modelElementInstance;

      } catch(ClassCastException e) {
        ModelElementType elementType = modelElementInstance.getElementType();
        throw new ProcessEngineException("Cannot cast "+modelElementInstance+" to FlowElement. "
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

  public ProcessEngineServices getProcessEngineServices() {
    return Context.getProcessEngineConfiguration()
          .getProcessEngine();
  }

}
