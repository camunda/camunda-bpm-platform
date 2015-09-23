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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.operation.CoreAtomicOperation;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.scope.CoreVariableStore;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.event.CompensationEventHandler;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.util.FormPropertyStartContext;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.AtomicOperation;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionStartContext;
import org.camunda.bpm.engine.impl.pvm.runtime.ProcessInstanceStartContext;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.FoxAtomicOperationDeleteCascadeFireActivityEnd;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;
import org.camunda.bpm.engine.impl.util.BitMaskUtil;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Falko Menge
 */
public class ExecutionEntity extends PvmExecutionImpl implements Execution, ProcessInstance, DbEntity, HasDbRevision, HasDbReferences {

  private static final long serialVersionUID = 1L;

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  // Persistent refrenced entities state //////////////////////////////////////
  public static final int EVENT_SUBSCRIPTIONS_STATE_BIT = 1;
  public static final int TASKS_STATE_BIT = 2;
  public static final int JOBS_STATE_BIT = 3;
  public static final int INCIDENT_STATE_BIT = 4;
  public static final int VARIABLES_STATE_BIT = 5;
  public static final int SUB_PROCESS_INSTANCE_STATE_BIT = 6;
  public static final int SUB_CASE_INSTANCE_STATE_BIT = 7;
  public static final int EXTERNAL_TASKS_BIT = 8;

  // current position /////////////////////////////////////////////////////////

  /**
   * the process instance. this is the root of the execution tree. the
   * processInstance of a process instance is a self reference.
   */
  protected transient ExecutionEntity processInstance;

  /** the parent execution */
  protected transient ExecutionEntity parent;

  /** nested executions representing scopes or concurrent paths */
  protected transient List<ExecutionEntity> executions;

  /** super execution, not-null if this execution is part of a subprocess */
  protected transient ExecutionEntity superExecution;

  /**
   * super case execution, not-null if this execution is part of a case
   * execution
   */
  protected transient CaseExecutionEntity superCaseExecution;

  /**
   * reference to a subprocessinstance, not-null if currently subprocess is
   * started from this execution
   */
  protected transient ExecutionEntity subProcessInstance;

  /**
   * reference to a subcaseinstance, not-null if currently subcase is started
   * from this execution
   */
  protected transient CaseExecutionEntity subCaseInstance;

  protected boolean shouldQueryForSubprocessInstance = false;

  protected boolean shouldQueryForSubCaseInstance = false;

  // associated entities /////////////////////////////////////////////////////

  // (we cache associated entities here to minimize db queries)
  protected transient List<EventSubscriptionEntity> eventSubscriptions;
  protected transient List<JobEntity> jobs;
  protected transient List<TaskEntity> tasks;
  protected transient List<ExternalTaskEntity> externalTasks;
  protected transient List<IncidentEntity> incidents;
  protected int cachedEntityState;

  protected transient ExecutionEntityVariableStore variableStore = new ExecutionEntityVariableStore(this);

  // replaced by //////////////////////////////////////////////////////////////

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

  public ExecutionEntity() {

  }

  public ExecutionEntity createExecution() {
    return createExecution(false);
  }

  /**
   * creates a new execution. properties processDefinition, processInstance and
   * activity will be initialized.
   */
  public ExecutionEntity createExecution(boolean initializeExecutionStartContext) {
    // create the new child execution
    ExecutionEntity createdExecution = createNewExecution();

    // initialize sequence counter
    createdExecution.setSequenceCounter(getSequenceCounter());

    // manage the bidirectional parent-child relation
    createdExecution.setParent(this);

    // initialize the new execution
    createdExecution.setProcessDefinition(getProcessDefinition());
    createdExecution.setProcessInstance(getProcessInstance());
    createdExecution.setActivity(getActivity());
    createdExecution.setSuspensionState(getSuspensionState());

    // make created execution start in same activity instance
    createdExecution.activityInstanceId = activityInstanceId;

    if (initializeExecutionStartContext) {
      createdExecution.setStartContext(new ExecutionStartContext());
    } else if (startContext != null) {
      createdExecution.setStartContext(startContext);
    }

    createdExecution.skipCustomListeners = this.skipCustomListeners;
    createdExecution.skipIoMapping = this.skipIoMapping;

    LOG.createChildExecution(createdExecution, this);

    return createdExecution;
  }

  // sub process instance
  // /////////////////////////////////////////////////////////////

  public ExecutionEntity createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey, String caseInstanceId) {
    shouldQueryForSubprocessInstance = true;

    ExecutionEntity subProcessInstance = (ExecutionEntity) super.createSubProcessInstance(processDefinition, businessKey, caseInstanceId);

    fireHistoricActivityInstanceUpdate();

    return subProcessInstance;
  }

  protected static ExecutionEntity createNewExecution() {
    ExecutionEntity newExecution = new ExecutionEntity();
    initializeAssociations(newExecution);
    newExecution.insert();

    return newExecution;
  }

  protected PvmExecutionImpl newExecution() {
    return createNewExecution();
  }

  // sub case instance ////////////////////////////////////////////////////////

  public CaseExecutionEntity createSubCaseInstance(CmmnCaseDefinition caseDefinition) {
    return createSubCaseInstance(caseDefinition, null);
  }

  public CaseExecutionEntity createSubCaseInstance(CmmnCaseDefinition caseDefinition, String businessKey) {
    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) caseDefinition.createCaseInstance(businessKey);

    // manage bidirectional super-process-sub-case-instances relation
    subCaseInstance.setSuperExecution(this);
    setSubCaseInstance(subCaseInstance);

    fireHistoricActivityInstanceUpdate();

    return subCaseInstance;
  }

  // helper ///////////////////////////////////////////////////////////////////

  public void fireHistoricActivityInstanceUpdate() {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    HistoryLevel historyLevel = configuration.getHistoryLevel();
    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.ACTIVITY_INSTANCE_UPDATE, this)) {

      final HistoryEventProducer eventFactory = configuration.getHistoryEventProducer();
      final HistoryEventHandler eventHandler = configuration.getHistoryEventHandler();

      // publish update event for current activity instance (containing the id
      // of the sub process/case)
      HistoryEvent haie = eventFactory.createActivityInstanceUpdateEvt(this, null);
      eventHandler.handleEvent(haie);
    }
  }

  // scopes ///////////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public void initialize() {
    LOG.initializeExecution(this);

    ScopeImpl scope = getScopeActivity();
    ensureParentInitialized();

    List<VariableDeclaration> variableDeclarations = (List<VariableDeclaration>) scope.getProperty(BpmnParse.PROPERTYNAME_VARIABLE_DECLARATIONS);
    if (variableDeclarations != null) {
      for (VariableDeclaration variableDeclaration : variableDeclarations) {
        variableDeclaration.initialize(this, parent);
      }
    }

    if (isProcessInstanceExecution()) {
      String initiatorVariableName = (String) processDefinition.getProperty(BpmnParse.PROPERTYNAME_INITIATOR_VARIABLE_NAME);
      if (initiatorVariableName != null) {
        String authenticatedUserId = Context.getCommandContext().getAuthenticatedUserId();
        setVariable(initiatorVariableName, authenticatedUserId);
      }
    }

    // create event subscriptions for the current scope
    for (EventSubscriptionDeclaration declaration : EventSubscriptionDeclaration.getDeclarationsForScope(scope)) {
      declaration.createSubscription(this);
    }
  }

  @SuppressWarnings("unchecked")
  public void initializeTimerDeclarations() {
    LOG.initializeTimerDeclaration(this);
    ScopeImpl scope = getScopeActivity();
    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) scope.getProperty(BpmnParse.PROPERTYNAME_TIMER_DECLARATION);
    if (timerDeclarations != null) {
      for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
        timerDeclaration.createTimerInstance(this);
      }
    }
  }

  protected static void initializeAssociations(ExecutionEntity execution) {
    // initialize the lists of referenced objects (prevents db queries)
    execution.executions = new ArrayList<ExecutionEntity>();
    execution.variableStore.setVariableInstances(new HashMap<String, VariableInstanceEntity>());
    execution.eventSubscriptions = new ArrayList<EventSubscriptionEntity>();
    execution.jobs = new ArrayList<JobEntity>();
    execution.tasks = new ArrayList<TaskEntity>();
    execution.externalTasks = new ArrayList<ExternalTaskEntity>();
    execution.incidents = new ArrayList<IncidentEntity>();

    // Cached entity-state initialized to null, all bits are zero, indicating NO
    // entities present
    execution.cachedEntityState = 0;
  }

  public void startWithFormProperties(VariableMap properties) {
    if (isProcessInstanceExecution()) {
      ActivityImpl initial = processDefinition.getInitial();
      ProcessInstanceStartContext processInstanceStartContext = getProcessInstanceStartContext();
      if (processInstanceStartContext != null) {
        initial = processInstanceStartContext.getInitial();
      }
      FormPropertyStartContext formPropertyStartContext = new FormPropertyStartContext(initial);
      formPropertyStartContext.setFormProperties(properties);
      startContext = formPropertyStartContext;

      initialize();
      initializeTimerDeclarations();
      fireHistoricProcessStartEvent();
    }
    performOperation(PvmAtomicOperation.PROCESS_START);
  }

  public void fireHistoricProcessStartEvent() {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    HistoryLevel historyLevel = configuration.getHistoryLevel();
    // TODO: This smells bad, as the rest of the history is done via the
    // ParseListener
    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.PROCESS_INSTANCE_START, processInstance)) {

      final HistoryEventProducer eventFactory = configuration.getHistoryEventProducer();
      final HistoryEventHandler eventHandler = configuration.getHistoryEventHandler();

      // publish event for historic process instance start
      HistoryEvent pise = eventFactory.createProcessInstanceStartEvt(processInstance);
      eventHandler.handleEvent(pise);
    }
  }

  /**
   * Method used for destroying a scope in a way that the execution can be
   * removed afterwards.
   */
  public void destroy() {
    super.destroy();
    ensureParentInitialized();

    // execute Output Mappings (if they exist).
    ensureActivityInitialized();
    if (activity != null && activity.getIoMapping() != null && !skipIoMapping) {
      activity.getIoMapping().executeOutputParameters(this);
    }

    clearExecution();
    removeEventSubscriptionsExceptCompensation();
  }

  protected void clearExecution() {
    // delete all the variable instances
    variableStore.removeVariablesWithoutFiringEvents();

    // delete all the tasks
    removeTasks(null);

    // delete external tasks
    removeExternalTasks();

    // remove all jobs
    removeJobs();

    // remove all incidents
    removeIncidents();
  }

  public void interrupt(String reason, boolean skipCustomListeners, boolean skipIoMappings) {

    // remove Jobs
    if (preserveScope) {
      removeActivityJobs(reason);
    } else {
      removeJobs();
      removeEventSubscriptionsExceptCompensation();
    }

    removeTasks(reason);

    super.interrupt(reason, skipCustomListeners, skipIoMappings);
  }

  protected void removeActivityJobs(String reason) {
    if (activityId != null) {
      for (JobEntity job : getJobs()) {
        if (activityId.equals(job.getActivityId())) {
          job.delete();
          removeJob(job);
        }
      }

    }

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
    boolean async = executionOperation.isAsync(this);

    if (!async && requiresUnsuspendedExecution(executionOperation)) {
      ensureNotSuspended();
    }

    Context
      .getCommandContext()
      .performOperation(executionOperation, this, async);
  }

  @SuppressWarnings("deprecation")
  public void performOperationSync(AtomicOperation executionOperation) {
    if (requiresUnsuspendedExecution(executionOperation)) {
      ensureNotSuspended();
    }

    Context.getCommandContext().performOperation(executionOperation, this);
  }

  protected void ensureNotSuspended() {
    if (isSuspended()) {
      throw LOG.suspendedEntityException("Execution", id);
    }
  }

  @SuppressWarnings("deprecation")
  protected boolean requiresUnsuspendedExecution(AtomicOperation executionOperation) {
    if (executionOperation != PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_END && executionOperation != PvmAtomicOperation.TRANSITION_DESTROY_SCOPE
        && executionOperation != PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_TAKE && executionOperation != PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_END
        && executionOperation != PvmAtomicOperation.TRANSITION_CREATE_SCOPE && executionOperation != PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_START
        && executionOperation != PvmAtomicOperation.DELETE_CASCADE && executionOperation != PvmAtomicOperation.DELETE_CASCADE_FIRE_ACTIVITY_END) {
      return true;
    }

    return false;
  }

  @SuppressWarnings({"unchecked", "deprecation"})
  public void scheduleAtomicOperationAsync(AtomicOperation executionOperation) {

    MessageJobDeclaration messageJobDeclaration = null;

    List<MessageJobDeclaration> messageJobDeclarations = (List<MessageJobDeclaration>) getActivity()
        .getProperty(BpmnParse.PROPERTYNAME_MESSAGE_JOB_DECLARATION);
    if (messageJobDeclarations != null) {
      for (MessageJobDeclaration declaration : messageJobDeclarations) {
        if (declaration.isApplicableForOperation(executionOperation)) {
          messageJobDeclaration = declaration;
          break;
        }
      }
    }

    if (messageJobDeclaration != null) {
      MessageEntity message = messageJobDeclaration.createJobInstance(this);
      messageJobDeclaration.setJobHandlerConfiguration(message, this, executionOperation);

      Context.getCommandContext().getJobManager().send(message);

    } else {
      throw LOG.requiredAsyncContinuationException(getActivity().getId());
    }
  }

  public boolean isActive(String activityId) {
    return findExecution(activityId) != null;
  }

  public void inactivate() {
    this.isActive = false;
  }

  // executions ///////////////////////////////////////////////////////////////

  public List<ExecutionEntity> getExecutions() {
    ensureExecutionsInitialized();
    return executions;
  }

  public List<ExecutionEntity> getExecutionsAsCopy() {
    return new ArrayList<ExecutionEntity>(getExecutions());
  }

  protected void ensureExecutionsInitialized() {
    if (executions == null) {
      if (isExecutionTreePrefetchEnabled()) {
        ensureExecutionTreeInitialized();

      } else {
        this.executions = Context.getCommandContext().getExecutionManager().findChildExecutionsByParentExecutionId(id);
      }

    }
  }

  /**
   * @return true if execution tree prefetching is enabled
   */
  protected boolean isExecutionTreePrefetchEnabled() {
    return Context.getProcessEngineConfiguration().isExecutionTreePrefetchEnabled();
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

  /**
   * for setting the process definition, this setter must be used as subclasses
   * can override
   */
  protected void ensureProcessDefinitionInitialized() {
    if ((processDefinition == null) && (processDefinitionId != null)) {
      ProcessDefinitionEntity deployedProcessDefinition = Context.getProcessEngineConfiguration().getDeploymentCache()
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

      if (isExecutionTreePrefetchEnabled()) {
        ensureExecutionTreeInitialized();

      } else {
        processInstance = Context.getCommandContext().getExecutionManager().findExecutionById(processInstanceId);
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

    if (activityId.equals(processDefinitionId)) {
      return processInstanceId;

    } else {

      String nextId = Context.getProcessEngineConfiguration().getIdGenerator().getNextId();

      String compositeId = activityId + ":" + nextId;
      if (compositeId.length() > 64) {
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
      if (isExecutionTreePrefetchEnabled()) {
        ensureExecutionTreeInitialized();

      } else {
        parent = Context.getCommandContext().getExecutionManager().findExecutionById(parentId);
      }
    }
  }

  public void setParentExecution(PvmExecutionImpl parent) {
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
      superExecution = Context.getCommandContext().getExecutionManager().findExecutionById(superExecutionId);
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
      subProcessInstance = Context.getCommandContext().getExecutionManager().findSubProcessInstanceBySuperExecutionId(id);
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
      superCaseExecution = Context.getCommandContext().getCaseExecutionManager().findCaseExecutionById(superCaseExecutionId);
    }
  }

  // sub case execution //////////////////////////////////////////////////////

  public CaseExecutionEntity getSubCaseInstance() {
    ensureSubCaseInstanceInitialized();
    return subCaseInstance;

  }

  public void setSubCaseInstance(CmmnExecution subCaseInstance) {
    shouldQueryForSubCaseInstance = subCaseInstance != null;
    this.subCaseInstance = (CaseExecutionEntity) subCaseInstance;
  }

  protected void ensureSubCaseInstanceInitialized() {
    if (shouldQueryForSubCaseInstance && subCaseInstance == null) {
      subCaseInstance = Context.getCommandContext().getCaseExecutionManager().findSubCaseInstanceBySuperExecutionId(id);
    }
  }

  // customized persistence behavior /////////////////////////////////////////

  public void remove() {
    super.remove();

    // removes jobs, incidents and tasks, and
    // clears the variable store
    clearExecution();

    // remove all event subscriptions for this scope, if the scope has event
    // subscriptions:
    removeEventSubscriptions();

    // finally delete this execution
    Context.getCommandContext().getExecutionManager().deleteExecution(this);
  }

  protected void removeEventSubscriptionsExceptCompensation() {
    // remove event subscriptions which are not compensate event subscriptions
    List<EventSubscriptionEntity> eventSubscriptions = getEventSubscriptions();
    for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
      if (!CompensationEventHandler.EVENT_HANDLER_TYPE.equals(eventSubscriptionEntity.getEventType())) {
        eventSubscriptionEntity.delete();
      }
    }
  }

  public void removeEventSubscriptions() {
    for (EventSubscriptionEntity eventSubscription : getEventSubscriptions()) {
      if (getReplacedBy() != null) {
        eventSubscription.setExecution(getReplacedBy());
      } else {
        eventSubscription.delete();
      }
    }
  }

  private void removeJobs() {
    for (Job job : getJobs()) {
      if (isReplacedByParent()) {
        ((JobEntity) job).setExecution(getReplacedBy());
      } else {
        ((JobEntity) job).delete();
      }
    }
  }

  private void removeIncidents() {
    for (IncidentEntity incident : getIncidents()) {
      if (isReplacedByParent()) {
        incident.setExecution(getReplacedBy());
      } else {
        incident.delete();
      }
    }
  }

  protected void removeTasks(String reason) {
    if (reason == null) {
      reason = TaskEntity.DELETE_REASON_DELETED;
    }
    for (TaskEntity task : getTasks()) {
      if (isReplacedByParent()) {
        if (task.getExecution() == null || task.getExecution() != replacedBy) {
          // All tasks should have been moved when "replacedBy" has been set.
          // Just in case tasks where added,
          // wo do an additional check here and move it
          task.setExecution(replacedBy);
          this.getReplacedBy().addTask(task);
        }
      } else {
        task.delete(reason, false, skipCustomListeners);
      }
    }
  }

  protected void removeExternalTasks() {
    for (ExternalTaskEntity externalTask : getExternalTasks()) {
      externalTask.delete();
    }
  }

  public ExecutionEntity getReplacedBy() {
    return (ExecutionEntity) replacedBy;
  }

  public void replace(PvmExecutionImpl execution) {
    ExecutionEntity replacedExecution = (ExecutionEntity) execution;

    // update the related tasks
    replacedExecution.moveTasksTo(this);

    replacedExecution.moveExternalTasksTo(this);

    // update those jobs that are directly related to the argument execution's
    // current activity
    replacedExecution.moveActivityLocalJobsTo(this);

    // only move variables when compacting the tree, but not when expanding
    // this behavior should be changed when fixing CAM-3941
    if (replacedExecution.getParent() == this) {
      // update the related process variables
      replacedExecution.moveVariablesTo(this);
    }

    // note: this method not move any event subscriptions since concurrent
    // executions
    // do not have event subscriptions (and either one of the executions
    // involved in this
    // operation is concurrent)

    super.replace(replacedExecution);
  }

  protected void moveTasksTo(ExecutionEntity other) {
    CommandContext commandContext = Context.getCommandContext();

    // update the related tasks
    for (TaskEntity task : getTasksInternal()) {
      task.setExecutionId(other.getId());
      task.setExecution(other);

      // update the related local task variables
      List<VariableInstanceEntity> variables = commandContext.getVariableInstanceManager().findVariableInstancesByTaskId(task.getId());

      for (VariableInstanceEntity variable : variables) {
        variable.setExecution(other);
      }

      other.addTask(task);
    }
    getTasksInternal().clear();
  }

  protected void moveExternalTasksTo(ExecutionEntity other) {
    for (ExternalTaskEntity externalTask : getExternalTasksInternal()) {
      externalTask.setExecutionId(other.getId());
      externalTask.setExecution(other);

      other.addExternalTask(externalTask);
    }

    getExternalTasksInternal().clear();
  }

  protected void moveActivityLocalJobsTo(ExecutionEntity other) {
    if (activityId != null) {
      for (JobEntity job : getJobs()) {

        if (activityId.equals(job.getActivityId())) {
          removeJob(job);
          job.setExecution(other);
        }
      }
    }
  }

  protected void moveVariablesTo(ExecutionEntity other) {
    variableStore.ensureVariableInstancesInitialized();
    for (CoreVariableInstance variable : variableStore.getVariableInstances().values()) {
      ((VariableInstanceEntity) variable).setExecutionId(other.getId());
    }
  }

  // variables ////////////////////////////////////////////////////////////////

  protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {
    variableInstance.setProcessInstanceId(processInstanceId);
    variableInstance.setExecutionId(id);

    variableInstance.setConcurrentLocal(!isScope || isExecutingScopeLeafActivity());
  }

  protected boolean isExecutingScopeLeafActivity() {
    return isActive && getActivity() != null && getActivity().isScope() && activityInstanceId != null
        && !(getActivity().getActivityBehavior() instanceof CompositeActivityBehavior);
  }

  protected List<VariableInstanceEntity> loadVariableInstances() {
    return Context.getCommandContext().getVariableInstanceManager().findVariableInstancesByExecutionId(id);
  }

  protected boolean isAutoFireHistoryEvents() {
    // as long as the process instance is starting (ie. before activity instance
    // of
    // the selected initial (start event) is created), the variable scope should
    // not
    // automatic fire history events for variable updates.

    // firing the events is triggered by the processInstanceStart context after
    // the initial activity
    // has been initialized. The effect is that the activity instance id of the
    // historic variable instances
    // will be the activity instance id of the start event.

    return startContext == null || (startContext != null && !startContext.isDelayFireHistoricVariableEvents());
  }

  public void fireHistoricVariableInstanceCreateEvents() {
    // this method is called by the start context and batch-fires create events
    // for all variable instances
    Map<String, CoreVariableInstance> variableInstances = variableStore.getVariableInstances();
    if (variableInstances != null) {
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
   * The most important consideration here is network roundtrip: If the process
   * engine and database run on separate hosts, network roundtrip has to be
   * added to each query. Economizing on the number of queries economizes on
   * network roundtrip. The tradeoff here is network roundtrip vs. throughput:
   * multiple roundtrips carrying small chucks of data vs. a single roundtrip
   * carrying more data.
   *
   */
  protected void ensureExecutionTreeInitialized() {
    List<ExecutionEntity> executions = Context.getCommandContext()
      .getExecutionManager()
      .findChildExecutionsByProcessInstanceId(processInstanceId);

    ExecutionEntity processInstance = isProcessInstanceExecution() ? this : null;

    if(processInstance == null) {
      for (ExecutionEntity execution : executions) {
        if (execution.isProcessInstanceExecution()) {
          processInstance = execution;
        }
      }
    }

    processInstance.restoreProcessInstance(executions, null, null);
  }

  /**
   * Restores a complete process instance tree including referenced entities.
   * Note: currently only the restoring of variables and event subscriptions is supported.
   *
   * @param executions
   *   the list of all executions that are part of this process instance.
   *   Cannot be null, must include the process instance execution itself.
   * @param eventSubscriptions
   *   the list of all event subscriptions that are linked to executions which is part of this process instance
   *   If null, event subscriptions are not initialized and lazy loaded on demand
   * @param variables
   *   the list of all variables that are linked to executions which are part of this process instance
   *   If null, variables are not initialized and are lazy loaded on demand
   */
  public void restoreProcessInstance(Collection<ExecutionEntity> executions,
      Collection<EventSubscriptionEntity> eventSubscriptions,
      Collection<VariableInstanceEntity> variables) {

    if(!isProcessInstanceExecution()) {
      throw LOG.restoreProcessInstanceException(this);
    }

    // index executions by id
    Map<String, ExecutionEntity> executionsMap = new HashMap<String, ExecutionEntity>();
    for (ExecutionEntity execution : executions) {
      executionsMap.put(execution.getId(), execution);
    }

    // restore execution tree
    for (ExecutionEntity execution : executions) {
      if (execution.executions == null) {
        execution.executions = new ArrayList<ExecutionEntity>();
      }
      if(execution.eventSubscriptions == null && eventSubscriptions != null) {
        execution.eventSubscriptions = new ArrayList<EventSubscriptionEntity>();
      }
      if(variableStore.getVariableInstancesDirect() == null && variables != null) {
        variableStore.setVariableInstances(new HashMap<String, VariableInstanceEntity>());
      }
      String parentId = execution.getParentId();
      ExecutionEntity parent = executionsMap.get(parentId);
      if (!execution.isProcessInstanceExecution()) {
        execution.processInstance = this;
        execution.parent = parent;
        if (parent.executions == null) {
          parent.executions = new ArrayList<ExecutionEntity>();
        }
        parent.executions.add(execution);
      } else {
        execution.processInstance = execution;
      }
    }

    if(eventSubscriptions != null) {
      // add event subscriptions to the right executions in the tree
      for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
        ExecutionEntity executionEntity = executionsMap.get(eventSubscription.getExecutionId());
        if (executionEntity != null) {
          executionEntity.addEventSubscription(eventSubscription);
        }
        else {
          throw LOG.executionNotFoundException(eventSubscription.getExecutionId());
        }
      }
    }

    if(variables != null) {
      for (VariableInstanceEntity variable : variables) {
        ExecutionEntity executionEntity = executionsMap.get(variable.getExecutionId());
        executionEntity.getVariableStore().getVariableInstances().put(variable.getName(), variable);
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
    persistentState.put("suspensionState", this.suspensionState);
    persistentState.put("cachedEntityState", getCachedEntityState());
    persistentState.put("sequenceCounter", getSequenceCounter());
    return persistentState;
  }

  public void insert() {
    Context.getCommandContext().getExecutionManager().insertExecution(this);
  }

  public void deleteCascade2(String deleteReason) {
    this.deleteReason = deleteReason;
    this.deleteRoot = true;
    performOperation(new FoxAtomicOperationDeleteCascadeFireActivityEnd());
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  public void forceUpdate() {
    Context.getCommandContext().getDbEntityManager().forceUpdate(this);
  }

  // toString /////////////////////////////////////////////////////////////////

  public String toString() {
    if (isProcessInstanceExecution()) {
      return "ProcessInstance[" + getToStringIdentity() + "]";
    } else {
      return (isConcurrent ? "Concurrent" : "") + (isScope ? "Scope" : "") + "Execution[" + getToStringIdentity() + "]";
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
      if (eventSubscriptionEntity instanceof CompensateEventSubscriptionEntity) {
        result.add((CompensateEventSubscriptionEntity) eventSubscriptionEntity);
      }
    }
    return result;
  }

  public List<CompensateEventSubscriptionEntity> getCompensateEventSubscriptions(String activityId) {
    List<EventSubscriptionEntity> eventSubscriptions = getEventSubscriptionsInternal();
    List<CompensateEventSubscriptionEntity> result = new ArrayList<CompensateEventSubscriptionEntity>(eventSubscriptions.size());
    for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
      if (eventSubscriptionEntity instanceof CompensateEventSubscriptionEntity) {
        if (activityId.equals(eventSubscriptionEntity.getActivityId())) {
          result.add((CompensateEventSubscriptionEntity) eventSubscriptionEntity);
        }
      }
    }
    return result;
  }

  protected void ensureEventSubscriptionsInitialized() {
    if (eventSubscriptions == null) {

      eventSubscriptions = Context.getCommandContext().getEventSubscriptionManager().findEventSubscriptionsByExecution(id);
    }
  }

  public void addEventSubscription(EventSubscriptionEntity eventSubscriptionEntity) {
    List<EventSubscriptionEntity> eventSubscriptionsInternal = getEventSubscriptionsInternal();
    if (!eventSubscriptionsInternal.contains(eventSubscriptionEntity)) {
      eventSubscriptionsInternal.add(eventSubscriptionEntity);
    }
  }

  public void removeEventSubscription(EventSubscriptionEntity eventSubscriptionEntity) {
    getEventSubscriptionsInternal().remove(eventSubscriptionEntity);
  }

  // referenced job entities //////////////////////////////////////////////////

  protected void ensureJobsInitialized() {
    if (jobs == null) {
      jobs = Context.getCommandContext().getJobManager().findJobsByExecutionId(id);
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
    List<JobEntity> jobsInternal = getJobsInternal();
    if (!jobsInternal.contains(jobEntity)) {
      jobsInternal.add(jobEntity);
    }
  }

  public void removeJob(JobEntity job) {
    getJobsInternal().remove(job);
  }

  // referenced incidents entities
  // //////////////////////////////////////////////

  protected void ensureIncidentsInitialized() {
    if (incidents == null) {
      incidents = Context.getCommandContext().getIncidentManager().findIncidentsByExecution(id);
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
    List<IncidentEntity> incidentsInternal = getIncidentsInternal();
    if (!incidentsInternal.contains(incident)) {
      incidentsInternal.add(incident);
    }
  }

  public void removeIncident(IncidentEntity incident) {
    getIncidentsInternal().remove(incident);
  }

  public IncidentEntity getIncidentByCauseIncidentId(String causeIncidentId) {
    for (IncidentEntity incident : getIncidents()) {
      if (incident.getCauseIncidentId() != null && incident.getCauseIncidentId().equals(causeIncidentId)) {
        return incident;
      }
    }
    return null;
  }

  // referenced task entities
  // ///////////////////////////////////////////////////

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void ensureTasksInitialized() {
    if (tasks == null) {
      tasks = (List) Context.getCommandContext().getTaskManager().findTasksByExecutionId(id);
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
    List<TaskEntity> tasksInternal = getTasksInternal();
    if (!tasksInternal.contains(taskEntity)) {
      tasksInternal.add(taskEntity);
    }
  }

  public void removeTask(TaskEntity task) {
    getTasksInternal().remove(task);
  }

  // external tasks

  protected void ensureExternalTasksInitialized() {
    if (externalTasks == null) {
      externalTasks = (List) Context.getCommandContext().getExternalTaskManager().findExternalTasksByExecutionId(id);
    }
  }

  protected List<ExternalTaskEntity> getExternalTasksInternal() {
    ensureExternalTasksInitialized();
    return externalTasks;
  }

  public void addExternalTask(ExternalTaskEntity externalTask) {
    getExternalTasksInternal().add(externalTask);
  }

  public void removeExternalTask(ExternalTaskEntity externalTask) {
    getExternalTasksInternal().remove(externalTask);
  }

  public List<ExternalTaskEntity> getExternalTasks() {
    return new ArrayList<ExternalTaskEntity>(getExternalTasksInternal());
  }

  // variables /////////////////////////////////////////////////////////

  protected CoreVariableStore getVariableStore() {
    return variableStore;
  }

  // getters and setters //////////////////////////////////////////////////////

  public void setCachedEntityState(int cachedEntityState) {
    this.cachedEntityState = cachedEntityState;

    // Check for flags that are down. These lists can be safely initialized as
    // empty, preventing
    // additional queries that end up in an empty list anyway
    if (jobs == null && !BitMaskUtil.isBitOn(cachedEntityState, JOBS_STATE_BIT)) {
      jobs = new ArrayList<JobEntity>();
    }
    if (tasks == null && !BitMaskUtil.isBitOn(cachedEntityState, TASKS_STATE_BIT)) {
      tasks = new ArrayList<TaskEntity>();
    }
    if (eventSubscriptions == null && !BitMaskUtil.isBitOn(cachedEntityState, EVENT_SUBSCRIPTIONS_STATE_BIT)) {
      eventSubscriptions = new ArrayList<EventSubscriptionEntity>();
    }
    if (incidents == null && !BitMaskUtil.isBitOn(cachedEntityState, INCIDENT_STATE_BIT)) {
      incidents = new ArrayList<IncidentEntity>();
    }
    if (variableStore.getVariableInstancesWithoutInitialization() == null && !BitMaskUtil.isBitOn(cachedEntityState, VARIABLES_STATE_BIT)) {
      variableStore.setVariableInstances(new HashMap<String, VariableInstanceEntity>());
    }
    if (externalTasks == null && !BitMaskUtil.isBitOn(cachedEntityState, EXTERNAL_TASKS_BIT)) {
      externalTasks = new ArrayList<ExternalTaskEntity>();
    }
    shouldQueryForSubprocessInstance = BitMaskUtil.isBitOn(cachedEntityState, SUB_PROCESS_INSTANCE_STATE_BIT);
    shouldQueryForSubCaseInstance = BitMaskUtil.isBitOn(cachedEntityState, SUB_CASE_INSTANCE_STATE_BIT);
  }

  public int getCachedEntityState() {
    cachedEntityState = 0;
    Map<String, VariableInstanceEntity> variableInstances = variableStore.getVariableInstancesWithoutInitialization();

    // Only mark a flag as false when the list is not-null and empty. If null,
    // we can't be sure there are no entries in it since
    // the list hasn't been initialized/queried yet.
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, TASKS_STATE_BIT, (tasks == null || tasks.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, EVENT_SUBSCRIPTIONS_STATE_BIT, (eventSubscriptions == null || eventSubscriptions.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, JOBS_STATE_BIT, (jobs == null || jobs.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, INCIDENT_STATE_BIT, (incidents == null || incidents.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, VARIABLES_STATE_BIT, (variableInstances == null || variableInstances.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, SUB_PROCESS_INSTANCE_STATE_BIT, shouldQueryForSubprocessInstance);
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, SUB_CASE_INSTANCE_STATE_BIT, shouldQueryForSubCaseInstance);
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, EXTERNAL_TASKS_BIT, (externalTasks == null || externalTasks.size() > 0));

    return cachedEntityState;
  }

  public int getCachedEntityStateRaw() {
    return cachedEntityState;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
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

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public void setSuperExecutionId(String superExecutionId) {
    this.superExecutionId = superExecutionId;
  }

  public boolean hasReferenceTo(DbEntity entity) {
    if (entity instanceof ExecutionEntity) {
      ExecutionEntity executionEntity = (ExecutionEntity) entity;
      String otherId = executionEntity.getId();

      // parentId
      if (parentId != null && parentId.equals(otherId)) {
        return true;
      }

      // superExecutionId
      if (superExecutionId != null && superExecutionId.equals(otherId)) {
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
    if (isProcessInstanceExecution()) {
      if (startContext == null) {

        ActivityImpl activity = getActivity();
        startContext = new ProcessInstanceStartContext(activity);

      }
    }
    return super.getProcessInstanceStartContext();
  }

  public String getCurrentActivityId() {
    return activityId;
  }

  public String getCurrentActivityName() {
    return activityName;
  }

  public FlowElement getBpmnModelElementInstance() {
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance();
    if (bpmnModelInstance != null) {

      ModelElementInstance modelElementInstance = null;
      if (ExecutionListener.EVENTNAME_TAKE.equals(eventName)) {
        modelElementInstance = bpmnModelInstance.getModelElementById(transition.getId());
      } else {
        modelElementInstance = bpmnModelInstance.getModelElementById(activityId);
      }

      try {
        return (FlowElement) modelElementInstance;

      } catch (ClassCastException e) {
        ModelElementType elementType = modelElementInstance.getElementType();
        throw LOG.castModelInstanceException(modelElementInstance, "FlowElement", elementType.getTypeName(),
          elementType.getTypeNamespace(), e);
      }

    } else {
      return null;
    }
  }

  public BpmnModelInstance getBpmnModelInstance() {
    if (processDefinitionId != null) {
      return Context.getProcessEngineConfiguration().getDeploymentCache().findBpmnModelInstanceForProcessDefinition(processDefinitionId);

    } else {
      return null;

    }
  }

  public ProcessEngineServices getProcessEngineServices() {
    return Context.getProcessEngineConfiguration().getProcessEngine();
  }


}
