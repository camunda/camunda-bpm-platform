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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProvider;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderProcessInstanceContext;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.operation.CoreAtomicOperation;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableCollectionProvider;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableInstanceFactory;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableInstanceLifecycleListener;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableListenerInvocationListener;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableStore;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableStore.VariablesProvider;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandling;
import org.camunda.bpm.engine.impl.interceptor.AtomicOperationInvocation;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.ActivityInstanceState;
import org.camunda.bpm.engine.impl.pvm.runtime.AtomicOperation;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;
import org.camunda.bpm.engine.impl.tree.ExecutionTopDownWalker;
import org.camunda.bpm.engine.impl.tree.TreeVisitor;
import org.camunda.bpm.engine.impl.util.BitMaskUtil;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Falko Menge
 */
public class ExecutionEntity extends PvmExecutionImpl implements Execution, ProcessInstance, DbEntity, HasDbRevision, HasDbReferences, VariablesProvider<VariableInstanceEntity> {

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

  @SuppressWarnings("unchecked")
  protected transient VariableStore<VariableInstanceEntity> variableStore =
      new VariableStore<>(this, new ExecutionEntityReferencer(this));


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
   * @see <code>setSuperExecution(ExecutionEntity)</code>
   */
  protected String superExecutionId;

  /**
   * persisted reference to the root process instance.
   *
   * @see #getRootProcessInstanceId()
   */
  protected String rootProcessInstanceId;

  /**
   * persisted reference to the super case execution of this execution
   *
   * @See {@link #getSuperCaseExecution()}
   * @see <code>setSuperCaseExecution(ExecutionEntity)</code>
   */
  protected String superCaseExecutionId;

  /**
   * Contains observers which are observe the execution.
   * @since 7.6
   */
  protected transient List<ExecutionObserver> executionObservers = new ArrayList<>();

  protected transient List<VariableInstanceLifecycleListener<VariableInstanceEntity>> registeredVariableListeners
    = new ArrayList<>();

  public ExecutionEntity() {
  }

  /**
   * creates a new execution. properties processDefinition, processInstance and
   * activity will be initialized.
   */
  @Override
  public ExecutionEntity createExecution() {
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

    // inherit the tenant id from parent execution
    if(tenantId != null) {
      createdExecution.setTenantId(tenantId);
    }

    // with the fix of CAM-9249 we presume that the parent and the child have the same startContext
    createdExecution.setStartContext(scopeInstantiationContext);

    createdExecution.skipCustomListeners = this.skipCustomListeners;
    createdExecution.skipIoMapping = this.skipIoMapping;

    LOG.createChildExecution(createdExecution, this);

    return createdExecution;
  }

  // sub process instance
  // /////////////////////////////////////////////////////////////

  @Override
  public ExecutionEntity createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey, String caseInstanceId) {
    shouldQueryForSubprocessInstance = true;

    ExecutionEntity subProcessInstance = (ExecutionEntity) super.createSubProcessInstance(processDefinition, businessKey, caseInstanceId);

    // inherit the tenant-id from the process definition
    String tenantId = ((ProcessDefinitionEntity) processDefinition).getTenantId();
    if (tenantId != null) {
      subProcessInstance.setTenantId(tenantId);
    }
    else {
      // if process definition has no tenant id, inherit this process instance's tenant id
      subProcessInstance.setTenantId(this.tenantId);
    }

    fireHistoricActivityInstanceUpdate();

    return subProcessInstance;
  }

  protected static ExecutionEntity createNewExecution() {
    ExecutionEntity newExecution = new ExecutionEntity();
    initializeAssociations(newExecution);
    newExecution.insert();

    return newExecution;
  }

  @Override
  protected PvmExecutionImpl newExecution() {
    return createNewExecution();
  }

  // sub case instance ////////////////////////////////////////////////////////

  @Override
  public CaseExecutionEntity createSubCaseInstance(CmmnCaseDefinition caseDefinition) {
    return createSubCaseInstance(caseDefinition, null);
  }

  @Override
  public CaseExecutionEntity createSubCaseInstance(CmmnCaseDefinition caseDefinition, String businessKey) {
    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) caseDefinition.createCaseInstance(businessKey);

    // inherit the tenant-id from the case definition
    String tenantId = ((CaseDefinitionEntity) caseDefinition).getTenantId();
    if (tenantId != null) {
      subCaseInstance.setTenantId(tenantId);
    }
    else {
      // if case definition has no tenant id, inherit this process instance's tenant id
      subCaseInstance.setTenantId(this.tenantId);
    }

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
      // publish update event for current activity instance (containing the id
      // of the sub process/case)
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createActivityInstanceUpdateEvt(ExecutionEntity.this);
        }
      });
    }
  }

  // scopes ///////////////////////////////////////////////////////////////////

  @Override
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
    for (EventSubscriptionDeclaration declaration : EventSubscriptionDeclaration.getDeclarationsForScope(scope).values()) {
      if(!declaration.isStartEvent()) {
        declaration.createSubscriptionForExecution(this);
      }
    }
  }

  @Override
  public void initializeTimerDeclarations() {
    LOG.initializeTimerDeclaration(this);
    ScopeImpl scope = getScopeActivity();
    createTimerInstances(TimerDeclarationImpl.getDeclarationsForScope(scope).values());
    for (Map<String, TimerDeclarationImpl> timerDeclarations : TimerDeclarationImpl.getTimeoutListenerDeclarationsForScope(scope).values()) {
      createTimerInstances(timerDeclarations.values());
    }
  }

  protected void createTimerInstances(Collection<TimerDeclarationImpl> timerDeclarations) {
    for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
      timerDeclaration.createTimerInstance(this);
    }
  }

  protected static void initializeAssociations(ExecutionEntity execution) {
    // initialize the lists of referenced objects (prevents db queries)
    execution.executions = new ArrayList<>();
    execution.variableStore.setVariablesProvider(VariableCollectionProvider.<VariableInstanceEntity>emptyVariables());
    execution.variableStore.forceInitialization();
    execution.eventSubscriptions = new ArrayList<>();
    execution.jobs = new ArrayList<>();
    execution.tasks = new ArrayList<>();
    execution.externalTasks = new ArrayList<>();
    execution.incidents = new ArrayList<>();

    // Cached entity-state initialized to null, all bits are zero, indicating NO
    // entities present
    execution.cachedEntityState = 0;
  }

  @Override
  public void start(Map<String, Object> variables, VariableMap formProperties) {
    if (getSuperExecution() == null) {
      setRootProcessInstanceId(processInstanceId);
    } else {
      ExecutionEntity superExecution = getSuperExecution();
      setRootProcessInstanceId(superExecution.getRootProcessInstanceId());
    }

    // determine tenant Id if null
    provideTenantId(variables, formProperties);
    super.start(variables, formProperties);
  }

  @Override
  public void startWithoutExecuting(Map<String, Object> variables) {
    setRootProcessInstanceId(getProcessInstanceId());
    provideTenantId(variables, null);
    super.startWithoutExecuting(variables);
  }

  protected void provideTenantId(Map<String, Object> variables, VariableMap properties) {
    if (tenantId == null) {
      TenantIdProvider tenantIdProvider = Context.getProcessEngineConfiguration().getTenantIdProvider();

      if (tenantIdProvider != null) {
        VariableMap variableMap = Variables.fromMap(variables);
        if(properties != null && !properties.isEmpty()) {
          variableMap.putAll(properties);
        }

        ProcessDefinition processDefinition = getProcessDefinition();

        TenantIdProviderProcessInstanceContext ctx;
        if (superExecutionId != null) {
          ctx = new TenantIdProviderProcessInstanceContext(processDefinition, variableMap, getSuperExecution());
        } else if (superCaseExecutionId != null) {
          ctx = new TenantIdProviderProcessInstanceContext(processDefinition, variableMap, getSuperCaseExecution());
        } else {
          ctx = new TenantIdProviderProcessInstanceContext(processDefinition, variableMap);
        }

        tenantId = tenantIdProvider.provideTenantIdForProcessInstance(ctx);
      }
    }
  }

  @Override
  public void fireHistoricProcessStartEvent() {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    HistoryLevel historyLevel = configuration.getHistoryLevel();
    // TODO: This smells bad, as the rest of the history is done via the
    // ParseListener
    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.PROCESS_INSTANCE_START, processInstance)) {

      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createProcessInstanceStartEvt(processInstance);
        }
      });
    }
  }

  /**
   * Method used for destroying a scope in a way that the execution can be
   * removed afterwards.
   */
  @Override
  public void destroy(boolean alwaysSkipIoMappings) {
    ensureParentInitialized();

    // execute Output Mappings (if they exist).
    ensureActivityInitialized();
    if (activity != null && activity.getIoMapping() != null && !skipIoMapping && !alwaysSkipIoMappings) {
      activity.getIoMapping().executeOutputParameters(this);
    }

    clearExecution();

    super.destroy(alwaysSkipIoMappings);

    removeEventSubscriptionsExceptCompensation();
  }

  public void removeAllTasks() {
    // delete all the tasks
    removeTasks(null);

    // delete external tasks
    removeExternalTasks();
  }

  protected void clearExecution() {
    //call the onRemove method of the execution observers
    //so they can do some clean up before
    for (ExecutionObserver observer : executionObservers) {
      observer.onClear(this);
    }

    // delete all the tasks and external tasks
    removeAllTasks();

    // delete all the variable instances
    removeVariablesLocalInternal();

    // remove all jobs
    removeJobs();

    // remove all incidents
    removeIncidents();
  }

  public void removeVariablesLocalInternal() {
    for (VariableInstanceEntity variableInstance : variableStore.getVariables()) {
      invokeVariableLifecycleListenersDelete(
          variableInstance,
          this,
          Collections.singletonList(getVariablePersistenceListener()));
      removeVariableInternal(variableInstance);
    }
  }

  @Override
  public void interrupt(String reason, boolean skipCustomListeners, boolean skipIoMappings, boolean externallyTerminated) {

    // remove Jobs
    if (preserveScope) {
      removeActivityJobs(reason);
    } else {
      removeJobs();
      removeEventSubscriptionsExceptCompensation();
    }

    removeTasks(reason);

    super.interrupt(reason, skipCustomListeners, skipIoMappings, externallyTerminated);
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

  @Override
  @SuppressWarnings("deprecation")
  public <T extends CoreExecution> void performOperation(CoreAtomicOperation<T> operation) {
    if (operation instanceof AtomicOperation) {
      performOperation((AtomicOperation) operation);
    } else {
      super.performOperation(operation);
    }
  }

  @Override
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
    boolean async = !isIgnoreAsync() && executionOperation.isAsync(this);

    if (!async && requiresUnsuspendedExecution(executionOperation)) {
      ensureNotSuspended();
    }

    Context
      .getCommandInvocationContext()
      .performOperation(executionOperation, this, async);
  }

  @SuppressWarnings("deprecation")
  public void performOperationSync(AtomicOperation executionOperation) {
    if (requiresUnsuspendedExecution(executionOperation)) {
      ensureNotSuspended();
    }

    Context.getCommandInvocationContext().performOperation(executionOperation, this);
  }

  protected void ensureNotSuspended() {
    if (isSuspended()) {
      throw LOG.suspendedEntityException("Execution", id);
    }
  }

  @SuppressWarnings("deprecation")
  protected boolean requiresUnsuspendedExecution(AtomicOperation executionOperation) {
    if (executionOperation != PvmAtomicOperation.TRANSITION_DESTROY_SCOPE
        && executionOperation != PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_TAKE && executionOperation != PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_END
        && executionOperation != PvmAtomicOperation.TRANSITION_CREATE_SCOPE && executionOperation != PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_START
        && executionOperation != PvmAtomicOperation.DELETE_CASCADE && executionOperation != PvmAtomicOperation.DELETE_CASCADE_FIRE_ACTIVITY_END) {
      return true;
    }

    return false;
  }

  @SuppressWarnings({"unchecked"})
  public void scheduleAtomicOperationAsync(AtomicOperationInvocation executionOperationInvocation) {

    MessageJobDeclaration messageJobDeclaration = null;

    List<MessageJobDeclaration> messageJobDeclarations = (List<MessageJobDeclaration>) getActivity()
        .getProperty(BpmnParse.PROPERTYNAME_MESSAGE_JOB_DECLARATION);
    if (messageJobDeclarations != null) {
      for (MessageJobDeclaration declaration : messageJobDeclarations) {
        if (declaration.isApplicableForOperation(executionOperationInvocation.getOperation())) {
          messageJobDeclaration = declaration;
          break;
        }
      }
    }

    if (messageJobDeclaration != null) {
      MessageEntity message = messageJobDeclaration.createJobInstance(executionOperationInvocation);
      Context.getCommandContext().getJobManager().send(message);

    } else {
      throw LOG.requiredAsyncContinuationException(getActivity().getId());
    }
  }

  @Override
  public boolean isActive(String activityId) {
    return findExecution(activityId) != null;
  }

  @Override
  public void inactivate() {
    this.isActive = false;
  }

  // executions ///////////////////////////////////////////////////////////////

  public void addExecutionObserver(ExecutionObserver observer) {
    executionObservers.add(observer);
  }

  public void removeExecutionObserver(ExecutionObserver observer) {
    executionObservers.remove(observer);
  }

  @Override
  public List<ExecutionEntity> getExecutions() {
    ensureExecutionsInitialized();
    return executions;
  }

  @Override
  public List<ExecutionEntity> getExecutionsAsCopy() {
    return new ArrayList<>(getExecutions());
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

  @Override
  public String getProcessBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }

  // process definition ///////////////////////////////////////////////////////

  /** ensures initialization and returns the process definition. */
  @Override
  public ProcessDefinitionEntity getProcessDefinition() {
    ensureProcessDefinitionInitialized();
    return (ProcessDefinitionEntity) processDefinition;
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

  @Override
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
    if (processDefinition != null) {
      this.processDefinitionId = processDefinition.getId();
    }
    else {
      this.processDefinitionId = null;
    }

  }

  // process instance /////////////////////////////////////////////////////////

  /** ensures initialization and returns the process instance. */
  @Override
  public ExecutionEntity getProcessInstance() {
    ensureProcessInstanceInitialized();
    return processInstance;
  }

  protected void ensureProcessInstanceInitialized() {
    if ((processInstance == null) && (processInstanceId != null)) {

      if (id.equals(processInstanceId)) {
        processInstance = this;
      } else {
        if (isExecutionTreePrefetchEnabled()) {
          ensureExecutionTreeInitialized();

        } else {
          processInstance = Context.getCommandContext().getExecutionManager().findExecutionById(processInstanceId);
        }
      }
    }
  }

  @Override
  public void setProcessInstance(PvmExecutionImpl processInstance) {
    this.processInstance = (ExecutionEntity) processInstance;
    if (processInstance != null) {
      this.processInstanceId = this.processInstance.getId();
    }
  }

  @Override
  public boolean isProcessInstanceExecution() {
    return parentId == null;
  }

  public boolean isProcessInstanceStarting() {
    // the process instance can only be starting if it is currently in main-memory already
    // we never have to access the database
    return processInstance != null && processInstance.isStarting;
  }

  // activity /////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the activity */
  @Override
  public ActivityImpl getActivity() {
    ensureActivityInitialized();
    return super.getActivity();
  }

  @Override
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
  @Override
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
  @Override
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

  @Override
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

  @Override
  public ExecutionEntity getSuperExecution() {
    ensureSuperExecutionInitialized();
    return superExecution;
  }

  @Override
  public void setSuperExecution(PvmExecutionImpl superExecution) {
    if (this.superExecutionId != null) {
      ensureSuperExecutionInitialized();
      this.superExecution.setSubProcessInstance(null);
    }

    this.superExecution = (ExecutionEntity) superExecution;

    if (superExecution != null) {
      this.superExecutionId = superExecution.getId();
      this.superExecution.setSubProcessInstance(this);
    } else {
      this.superExecutionId = null;
    }
  }

  protected void ensureSuperExecutionInitialized() {
    if (superExecution == null && superExecutionId != null) {
      superExecution = Context.getCommandContext().getExecutionManager().findExecutionById(superExecutionId);
    }
  }

  @Override
  public ExecutionEntity getSubProcessInstance() {
    ensureSubProcessInstanceInitialized();
    return subProcessInstance;
  }

  @Override
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

  @Override
  public CaseExecutionEntity getSuperCaseExecution() {
    ensureSuperCaseExecutionInitialized();
    return superCaseExecution;
  }

  @Override
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

  @Override
  public CaseExecutionEntity getSubCaseInstance() {
    ensureSubCaseInstanceInitialized();
    return subCaseInstance;

  }

  @Override
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

  @Override
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
      if (!EventType.COMPENSATE.name().equals(eventSubscriptionEntity.getEventType())) {
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
        IncidentContext incidentContext = createIncidentContext(incident.getConfiguration());
        IncidentHandling.removeIncidents(incident.getIncidentType(), incidentContext, false);
      }
    }

    for (IncidentEntity incident : getIncidents()) {
      // if the handler doesn't take care of it,
      // make sure the incident is deleted nevertheless
      incident.delete();
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

  @Override
  public ExecutionEntity getReplacedBy() {
    return (ExecutionEntity) replacedBy;
  }

  @Override
  public ExecutionEntity resolveReplacedBy() {
    return (ExecutionEntity) super.resolveReplacedBy();
  }

  @Override
  public void replace(PvmExecutionImpl execution) {
    ExecutionEntity replacedExecution = (ExecutionEntity) execution;

    setListenerIndex(replacedExecution.getListenerIndex());
    replacedExecution.setListenerIndex(0);

    // update the related tasks
    replacedExecution.moveTasksTo(this);

    replacedExecution.moveExternalTasksTo(this);

    // update those jobs that are directly related to the argument execution's
    // current activity
    replacedExecution.moveActivityLocalJobsTo(this);

    if (!replacedExecution.isEnded()) {
      // on compaction, move all variables
      if (replacedExecution.getParent() == this) {
        replacedExecution.moveVariablesTo(this);
      }
      // on expansion, move only concurrent local variables
      else {
        replacedExecution.moveConcurrentLocalVariablesTo(this);
      }
    }

    // note: this method not move any event subscriptions since concurrent
    // executions
    // do not have event subscriptions (and either one of the executions
    // involved in this
    // operation is concurrent)

    super.replace(replacedExecution);
  }

  @Override
  public void onConcurrentExpand(PvmExecutionImpl scopeExecution) {
    ExecutionEntity scopeExecutionEntity = (ExecutionEntity) scopeExecution;
    scopeExecutionEntity.moveConcurrentLocalVariablesTo(this);
    super.onConcurrentExpand(scopeExecutionEntity);
  }

  protected void moveTasksTo(ExecutionEntity other) {
    // update the related tasks
    for (TaskEntity task : getTasksInternal()) {
      task.setExecution(other);

      // update the related local task variables
      Collection<VariableInstanceEntity> variables = task.getVariablesInternal();

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
    List<VariableInstanceEntity> variables = variableStore.getVariables();
    variableStore.removeVariables();

    for (VariableInstanceEntity variable : variables) {
      moveVariableTo(variable, other);
    }
  }

  protected void moveVariableTo(VariableInstanceEntity variable, ExecutionEntity other) {
    if (other.variableStore.containsKey(variable.getName())) {
      CoreVariableInstance existingInstance = other.variableStore.getVariable(variable.getName());
      existingInstance.setValue(variable.getTypedValue(false));
      invokeVariableLifecycleListenersUpdate(existingInstance, this);
      invokeVariableLifecycleListenersDelete(
          variable,
          this,
          Collections.singletonList(getVariablePersistenceListener()));
    }
    else {
      other.variableStore.addVariable(variable);
    }
  }

  protected void moveConcurrentLocalVariablesTo(ExecutionEntity other) {
    List<VariableInstanceEntity> variables = variableStore.getVariables();

    for (VariableInstanceEntity variable : variables) {
      if (variable.isConcurrentLocal()) {
        moveVariableTo(variable, other);
      }
    }
  }

  // variables ////////////////////////////////////////////////////////////////

  public void addVariableListener(VariableInstanceLifecycleListener<VariableInstanceEntity> listener) {
    registeredVariableListeners.add(listener);
  }

  public void removeVariableListener(VariableInstanceLifecycleListener<VariableInstanceEntity> listener) {
    registeredVariableListeners.remove(listener);
  }

  @Override
  public Collection<VariableInstanceEntity> provideVariables() {
    return Context.getCommandContext().getVariableInstanceManager().findVariableInstancesByExecutionId(id);
  }

  public Collection<VariableInstanceEntity> provideVariables(Collection<String> variableNames) {
    return Context.getCommandContext().getVariableInstanceManager().findVariableInstancesByExecutionIdAndVariableNames(id, variableNames);
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
      .findExecutionsByProcessInstanceId(processInstanceId);

    ExecutionEntity processInstance = isProcessInstanceExecution() ? this : null;

    if(processInstance == null) {
      for (ExecutionEntity execution : executions) {
        if (execution.isProcessInstanceExecution()) {
          processInstance = execution;
        }
      }
    }

    processInstance.restoreProcessInstance(executions, null, null, null, null, null, null);
  }

  /**
   * Restores a complete process instance tree including referenced entities.
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
   * @param jobs
   * @param tasks
   * @param incidents
   */
  public void restoreProcessInstance(Collection<ExecutionEntity> executions,
      Collection<EventSubscriptionEntity> eventSubscriptions,
      Collection<VariableInstanceEntity> variables,
      Collection<TaskEntity> tasks,
      Collection<JobEntity> jobs,
      Collection<IncidentEntity> incidents,
      Collection<ExternalTaskEntity> externalTasks) {

    EnsureUtil.ensureNotEmpty(NullValueException.class,
        String.format("Cannot restore state of process instance %s", processInstanceId),
        "list of executions", executions);

    if(!isProcessInstanceExecution()) {
      throw LOG.restoreProcessInstanceException(this);
    }

    // index executions by id
    Map<String, ExecutionEntity> executionsMap = new HashMap<>();
    for (ExecutionEntity execution : executions) {
      executionsMap.put(execution.getId(), execution);
    }

    Map<String, List<VariableInstanceEntity>> variablesByScope = new HashMap<>();
    if(variables != null) {
      for (VariableInstanceEntity variable : variables) {
        CollectionUtil.addToMapOfLists(variablesByScope, variable.getVariableScopeId(), variable);
      }
    }

    // restore execution tree
    for (ExecutionEntity execution : executions) {
      if (execution.executions == null) {
        execution.executions = new ArrayList<>();
      }
      if(execution.eventSubscriptions == null && eventSubscriptions != null) {
        execution.eventSubscriptions = new ArrayList<>();
      }
      if(variables != null) {
        execution.variableStore.setVariablesProvider(
            new VariableCollectionProvider<>(variablesByScope.get(execution.id)));
      }
      String parentId = execution.getParentId();
      ExecutionEntity parent = executionsMap.get(parentId);
      if (!execution.isProcessInstanceExecution()) {
        if (parent == null) {
          throw LOG.resolveParentOfExecutionFailedException(parentId, execution.getId());
        }
        execution.processInstance = this;
        execution.parent = parent;
        if (parent.executions == null) {
          parent.executions = new ArrayList<>();
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

    if (jobs != null) {
      for (JobEntity job : jobs) {
        ExecutionEntity execution = executionsMap.get(job.getExecutionId());
        job.setExecution(execution);
      }
    }

    if (tasks != null) {
      for (TaskEntity task : tasks) {
        ExecutionEntity execution = executionsMap.get(task.getExecutionId());
        task.setExecution(execution);
        execution.addTask(task);

        if(variables != null) {
          task.variableStore.setVariablesProvider(new VariableCollectionProvider<>(variablesByScope.get(task.id)));
        }
      }
    }


    if (incidents != null) {
      for (IncidentEntity incident : incidents) {
        ExecutionEntity execution = executionsMap.get(incident.getExecutionId());
        incident.setExecution(execution);
      }
    }

    if (externalTasks != null) {
      for (ExternalTaskEntity externalTask : externalTasks) {
        ExecutionEntity execution = executionsMap.get(externalTask.getExecutionId());
        externalTask.setExecution(execution);
        execution.addExternalTask(externalTask);
      }
    }
  }


  // persistent state /////////////////////////////////////////////////////////

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<>();
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

  public int getRevisionNext() {
    return revision + 1;
  }

  public void forceUpdate() {
    Context.getCommandContext().getDbEntityManager().forceUpdate(this);
  }

  // toString /////////////////////////////////////////////////////////////////

  @Override
  public String toString() {
    if (isProcessInstanceExecution()) {
      return "ProcessInstance[" + getToStringIdentity() + "]";
    } else {
      return (isConcurrent ? "Concurrent" : "") + (isScope ? "Scope" : "") + "Execution[" + getToStringIdentity() + "]";
    }
  }

  @Override
  protected String getToStringIdentity() {
    return id;
  }

  // event subscription support //////////////////////////////////////////////

  public List<EventSubscriptionEntity> getEventSubscriptionsInternal() {
    ensureEventSubscriptionsInitialized();
    return eventSubscriptions;
  }

  public List<EventSubscriptionEntity> getEventSubscriptions() {
    return new ArrayList<>(getEventSubscriptionsInternal());
  }

  public List<EventSubscriptionEntity> getCompensateEventSubscriptions() {
    List<EventSubscriptionEntity> eventSubscriptions = getEventSubscriptionsInternal();
    List<EventSubscriptionEntity> result = new ArrayList<>(eventSubscriptions.size());
    for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
      if (eventSubscriptionEntity.isSubscriptionForEventType(EventType.COMPENSATE)) {
        result.add(eventSubscriptionEntity);
      }
    }
    return result;
  }

  public List<EventSubscriptionEntity> getCompensateEventSubscriptions(String activityId) {
    List<EventSubscriptionEntity> eventSubscriptions = getEventSubscriptionsInternal();
    List<EventSubscriptionEntity> result = new ArrayList<>(eventSubscriptions.size());
    for (EventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
      if (eventSubscriptionEntity.isSubscriptionForEventType(EventType.COMPENSATE)
              && activityId.equals(eventSubscriptionEntity.getActivityId())) {
          result.add(eventSubscriptionEntity);
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
    return new ArrayList<>(getJobsInternal());
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
    return new ArrayList<>(getIncidentsInternal());
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

  protected void ensureTasksInitialized() {
    if (tasks == null) {
      tasks = Context.getCommandContext().getTaskManager().findTasksByExecutionId(id);
    }
  }

  protected List<TaskEntity> getTasksInternal() {
    ensureTasksInitialized();
    return tasks;
  }

  public List<TaskEntity> getTasks() {
    return new ArrayList<>(getTasksInternal());
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
      externalTasks = Context.getCommandContext().getExternalTaskManager().findExternalTasksByExecutionId(id);
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
    return new ArrayList<>(getExternalTasksInternal());
  }

  // variables /////////////////////////////////////////////////////////

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected VariableStore<CoreVariableInstance> getVariableStore() {
    return (VariableStore) variableStore;
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected VariableInstanceFactory<CoreVariableInstance> getVariableInstanceFactory() {
    return (VariableInstanceFactory) VariableInstanceEntityFactory.INSTANCE;
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected List<VariableInstanceLifecycleListener<CoreVariableInstance>> getVariableInstanceLifecycleListeners() {

    List<VariableInstanceLifecycleListener<CoreVariableInstance>> listeners = new ArrayList<>();

    listeners.add(getVariablePersistenceListener());
    listeners.add((VariableInstanceLifecycleListener) new VariableInstanceConcurrentLocalInitializer(this));
    listeners.add((VariableInstanceLifecycleListener) VariableInstanceSequenceCounterListener.INSTANCE);

    listeners.add((VariableInstanceLifecycleListener) VariableInstanceHistoryListener.INSTANCE);

    listeners.add((VariableInstanceLifecycleListener) new VariableListenerInvocationListener(this));

    listeners.addAll((List) registeredVariableListeners);

    return listeners;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public VariableInstanceLifecycleListener<CoreVariableInstance> getVariablePersistenceListener() {
    return (VariableInstanceLifecycleListener) VariableInstanceEntityPersistenceListener.INSTANCE;
  }

  public Collection<VariableInstanceEntity> getVariablesInternal() {
    return variableStore.getVariables();
  }

  public void removeVariableInternal(VariableInstanceEntity variable) {
    if (variableStore.containsValue(variable)) {
      variableStore.removeVariable(variable.getName());
    }
  }

  public void addVariableInternal(VariableInstanceEntity variable) {
    if (variableStore.containsKey(variable.getName())) {
      VariableInstanceEntity existingVariable = variableStore.getVariable(variable.getName());
      existingVariable.setValue(variable.getTypedValue());
      variable.delete();
    }
    else {
      variableStore.addVariable(variable);
    }
  }

  public void handleConditionalEventOnVariableChange(VariableEvent variableEvent) {
    List<EventSubscriptionEntity> subScriptions = getEventSubscriptions();
    for (EventSubscriptionEntity subscription : subScriptions) {
      if (EventType.CONDITONAL.name().equals(subscription.getEventType())) {
        subscription.processEventSync(variableEvent);
      }
    }
  }

  @Override
  public void dispatchEvent(VariableEvent variableEvent) {
    final List<ExecutionEntity> execs = new ArrayList<>();
    new ExecutionTopDownWalker(this).addPreVisitor(new TreeVisitor<ExecutionEntity>() {
      @Override
      public void visit(ExecutionEntity obj) {
        if (!obj.getEventSubscriptions().isEmpty() &&

          (obj.isInState(ActivityInstanceState.DEFAULT) || (!obj.getActivity().isScope()))) { // state is default or tree is compacted
          execs.add(obj);
        }
      }
    }).walkUntil();
    for (ExecutionEntity execution : execs) {
      execution.handleConditionalEventOnVariableChange(variableEvent);
    }
  }



  // getters and setters //////////////////////////////////////////////////////

  public void setCachedEntityState(int cachedEntityState) {
    this.cachedEntityState = cachedEntityState;

    // Check for flags that are down. These lists can be safely initialized as
    // empty, preventing
    // additional queries that end up in an empty list anyway
    if (jobs == null && !BitMaskUtil.isBitOn(cachedEntityState, JOBS_STATE_BIT)) {
      jobs = new ArrayList<>();
    }
    if (tasks == null && !BitMaskUtil.isBitOn(cachedEntityState, TASKS_STATE_BIT)) {
      tasks = new ArrayList<>();
    }
    if (eventSubscriptions == null && !BitMaskUtil.isBitOn(cachedEntityState, EVENT_SUBSCRIPTIONS_STATE_BIT)) {
      eventSubscriptions = new ArrayList<>();
    }
    if (incidents == null && !BitMaskUtil.isBitOn(cachedEntityState, INCIDENT_STATE_BIT)) {
      incidents = new ArrayList<>();
    }
    if (!variableStore.isInitialized() && !BitMaskUtil.isBitOn(cachedEntityState, VARIABLES_STATE_BIT)) {
      variableStore.setVariablesProvider(VariableCollectionProvider.<VariableInstanceEntity>emptyVariables());
      variableStore.forceInitialization();
    }
    if (externalTasks == null && !BitMaskUtil.isBitOn(cachedEntityState, EXTERNAL_TASKS_BIT)) {
      externalTasks = new ArrayList<>();
    }
    shouldQueryForSubprocessInstance = BitMaskUtil.isBitOn(cachedEntityState, SUB_PROCESS_INSTANCE_STATE_BIT);
    shouldQueryForSubCaseInstance = BitMaskUtil.isBitOn(cachedEntityState, SUB_CASE_INSTANCE_STATE_BIT);
  }

  public int getCachedEntityState() {
    cachedEntityState = 0;

    // Only mark a flag as false when the list is not-null and empty. If null,
    // we can't be sure there are no entries in it since
    // the list hasn't been initialized/queried yet.
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, TASKS_STATE_BIT, (tasks == null || tasks.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, EVENT_SUBSCRIPTIONS_STATE_BIT, (eventSubscriptions == null || eventSubscriptions.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, JOBS_STATE_BIT, (jobs == null || jobs.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, INCIDENT_STATE_BIT, (incidents == null || incidents.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, VARIABLES_STATE_BIT, (!variableStore.isInitialized() || !variableStore.isEmpty()));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, SUB_PROCESS_INSTANCE_STATE_BIT, shouldQueryForSubprocessInstance);
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, SUB_CASE_INSTANCE_STATE_BIT, shouldQueryForSubCaseInstance);
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, EXTERNAL_TASKS_BIT, (externalTasks == null || externalTasks.size() > 0));

    return cachedEntityState;
  }

  public int getCachedEntityStateRaw() {
    return cachedEntityState;
  }

  public String getRootProcessInstanceId() {
    if (isProcessInstanceExecution()) {
      return rootProcessInstanceId;
    } else {
      ExecutionEntity processInstance = getProcessInstance();
      return processInstance.rootProcessInstanceId;
    }
  }

  public String getRootProcessInstanceIdRaw() {
    return rootProcessInstanceId;
  }

  public void setRootProcessInstanceId(String rootProcessInstanceId) {
    this.rootProcessInstanceId = rootProcessInstanceId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;

    if (id.equals(processInstanceId)) {
      this.processInstance = this;
    }
  }

  @Override
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

  @Override
  public Set<String> getReferencedEntityIds() {
    Set<String> referenceIds = new HashSet<>();

    if (superExecutionId != null) {
      referenceIds.add(superExecutionId);
    }
    if (parentId != null) {
      referenceIds.add(parentId);
    }

    return referenceIds;
  }

  @Override
  public Map<String, Class> getReferencedEntitiesIdAndClass() {
    Map<String, Class> referenceIdAndClass = new HashMap<>();

    if (superExecutionId != null) {
      referenceIdAndClass.put(this.superExecutionId, ExecutionEntity.class);
    }
    if (parentId != null) {
      referenceIdAndClass.put(this.parentId, ExecutionEntity.class);
    }
    if (processInstanceId != null) {
      referenceIdAndClass.put(this.processInstanceId, ExecutionEntity.class);
    }
    if (processDefinitionId != null) {
      referenceIdAndClass.put(this.processDefinitionId, ProcessDefinitionEntity.class);
    }

    return referenceIdAndClass;
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

  @Override
  public String getCurrentActivityId() {
    return activityId;
  }

  @Override
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

  @Override
  public ProcessEngine getProcessEngine() {
    return Context.getProcessEngineConfiguration().getProcessEngine();
  }

  public String getProcessDefinitionTenantId() {
    return getProcessDefinition().getTenantId();
  }
}
