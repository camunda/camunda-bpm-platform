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
import org.camunda.bpm.engine.SuspendedEntityInteractionException;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.db.HasRevision;
import org.camunda.bpm.engine.impl.db.PersistentObject;
import org.camunda.bpm.engine.impl.event.CompensationEventHandler;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.util.FormPropertyStartContext;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.PvmExecution;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessElement;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.ExecutionListenerExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.AtomicOperation;
import org.camunda.bpm.engine.impl.pvm.runtime.FoxAtomicOperationDeleteCascadeFireActivityEnd;
import org.camunda.bpm.engine.impl.pvm.runtime.InterpretableExecution;
import org.camunda.bpm.engine.impl.pvm.runtime.OutgoingExecution;
import org.camunda.bpm.engine.impl.pvm.runtime.ProcessInstanceStartContext;
import org.camunda.bpm.engine.impl.util.BitMaskUtil;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;



/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Falko Menge
 */
public class ExecutionEntity extends VariableScopeImpl implements ActivityExecution, ExecutionListenerExecution, Execution, PvmExecution, ProcessInstance, InterpretableExecution, PersistentObject, HasRevision {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(ExecutionEntity.class.getName());

  // Persistent refrenced entities state //////////////////////////////////////
  protected static final int EVENT_SUBSCRIPTIONS_STATE_BIT = 1;
  protected static final int TASKS_STATE_BIT = 2;
  protected static final int JOBS_STATE_BIT = 3;
  protected static final int INCIDENT_STATE_BIT = 4;

  // current position /////////////////////////////////////////////////////////

  protected ProcessDefinitionImpl processDefinition;

  /** current activity */
  protected ActivityImpl activity;

  /** current transition.  is null when there is no transition being taken. */
  protected TransitionImpl transition = null;

  /** transition that will be taken.  is null when there is no transition being taken. */
  protected TransitionImpl transitionBeingTaken = null;

  /** the process instance.  this is the root of the execution tree.
   * the processInstance of a process instance is a self reference. */
  protected ExecutionEntity processInstance;

  /** the parent execution */
  protected ExecutionEntity parent;

  /** nested executions representing scopes or concurrent paths */
  protected List<ExecutionEntity> executions;

  /** super execution, not-null if this execution is part of a subprocess */
  protected ExecutionEntity superExecution;

  /** reference to a subprocessinstance, not-null if currently subprocess is started from this execution */
  protected ExecutionEntity subProcessInstance;

  /** the unique id of the current activity instance */
  protected String activityInstanceId;

  protected ProcessInstanceStartContext processInstanceStartContext;

  // state/type of execution //////////////////////////////////////////////////

  /** indicates if this execution represents an active path of execution.
   * Executions are made inactive in the following situations:
   * <ul>
   *   <li>an execution enters a nested scope</li>
   *   <li>an execution is split up into multiple concurrent executions, then the parent is made inactive.</li>
   *   <li>an execution has arrived in a parallel gateway or join and that join has not yet activated/fired.</li>
   *   <li>an execution is ended.</li>
   * </ul>*/
  protected boolean isActive = true;
  protected boolean isScope = true;
  protected boolean isConcurrent = false;
  protected boolean isEnded = false;
  protected boolean isEventScope = false;

  /** marks the current activity instance */
  protected int activityInstanceState = ActivityInstanceState.DEFAULT.getStateCode();

  // events ///////////////////////////////////////////////////////////////////

  protected String eventName;
  protected PvmProcessElement eventSource;
  protected int executionListenerIndex = 0;

  // associated entities /////////////////////////////////////////////////////

  // (we cache associated entities here to minimize db queries)
  protected List<EventSubscriptionEntity> eventSubscriptions;
  protected List<JobEntity> jobs;
  protected List<TaskEntity> tasks;
  protected List<IncidentEntity> incidents;
  protected int cachedEntityState;

  // cascade deletion ////////////////////////////////////////////////////////

  protected boolean deleteRoot;
  protected String deleteReason;

  // replaced by //////////////////////////////////////////////////////////////

  /** when execution structure is pruned during a takeAll, then
   * the original execution has to be resolved to the replaced execution.
   * @see {@link #takeAll(List, List)} {@link OutgoingExecution} */
  protected ExecutionEntity replacedBy;

  // atomic operations ////////////////////////////////////////////////////////

  /** next operation.  process execution is in fact runtime interpretation of the process model.
   * each operation is a logical unit of interpretation of the process.  so sequentially processing
   * the operations drives the interpretation or execution of a process.
   * @see AtomicOperation
   * @see #performOperation(AtomicOperation) */
  protected AtomicOperation nextOperation;
  protected boolean isOperating = false;

  protected int revision = 1;
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();

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
   * @see #setActivity(ActivityImpl)
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
   * persisted reference to the business key.
   */
  protected String businessKey;

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

  protected boolean forcedUpdate;

  /** an activity which is to be started next */
  protected PvmActivity nextActivity;

  public ExecutionEntity() {

  }

  public ExecutionEntity(ActivityImpl activityImpl) {
    this.processInstanceStartContext = new HistoryAwareStartContext(activityImpl);
  }

  /** creates a new execution. properties processDefinition, processInstance and activity will be initialized. */
  public ExecutionEntity createExecution() {
    // create the new child execution
    ExecutionEntity createdExecution = newExecution();

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

    if (log.isLoggable(Level.FINE)) {
      log.fine("Child execution "+createdExecution+" created with parent "+this);
    }

    return createdExecution;
  }

  public PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition) {
    return createSubProcessInstance(processDefinition, null);
  }

  public PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey) {
    ExecutionEntity subProcessInstance = newExecution();

    // manage bidirectional super-subprocess relation
    subProcessInstance.setSuperExecution(this);
    this.setSubProcessInstance(subProcessInstance);

    // Initialize the new execution
    subProcessInstance.setProcessDefinition((ProcessDefinitionImpl) processDefinition);
    subProcessInstance.setProcessInstance(subProcessInstance);

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

  protected ExecutionEntity newExecution() {
    ExecutionEntity newExecution = new ExecutionEntity();
    newExecution.executions = new ArrayList<ExecutionEntity>();

    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(newExecution);

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

    // initialize the lists of referenced objects (prevents db queries)
    variableInstances = new HashMap<String, VariableInstanceEntity>();
    eventSubscriptions = new ArrayList<EventSubscriptionEntity>();
    jobs = new ArrayList<JobEntity>();
    tasks = new ArrayList<TaskEntity>();
    incidents = new ArrayList<IncidentEntity>();

    // Cached entity-state initialized to null, all bits are zore, indicating NO entities present
    cachedEntityState = 0;

    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) scope.getProperty(BpmnParse.PROPERTYNAME_TIMER_DECLARATION);
    if (timerDeclarations!=null) {
      for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
        TimerEntity timer = timerDeclaration.createJobInstance(this);
        Context
          .getCommandContext()
          .getJobManager()
          .schedule(timer);
      }
    }

    // create event subscriptions for the current scope
    for (EventSubscriptionDeclaration declaration : EventSubscriptionDeclaration.getDeclarationsForScope(scope)) {
      declaration.createSubscription(this);
    }
  }

  public void start() {
    start(null, null);
  }

  public void start(Map<String, Object> variables) {
    start(null, variables);
  }

  public void start(String businessKey) {
    start(businessKey, null);
  }

  public void start(String businessKey, Map<String, Object> variables) {

    if(variables != null) {
      setVariables(variables);
    }

    if(businessKey != null) {
      setBusinessKey(businessKey);
    }

    performOperation(AtomicOperation.PROCESS_START);
  }

   public void startWithFormProperties(Map<String, Object> properties) {
     if(isProcessInstance()) {
       ActivityImpl initial = processDefinition.getInitial();
       if(processInstanceStartContext != null) {
         initial = processInstanceStartContext.getInitial();
       }
       FormPropertyStartContext formPropertyStartContext = new FormPropertyStartContext(initial);
       formPropertyStartContext.setFormProperties(properties);
       processInstanceStartContext = formPropertyStartContext;
     }
     performOperation(AtomicOperation.PROCESS_START);
   }

  public void destroy() {
    log.fine("destroying "+this);

    ensureParentInitialized();
    deleteVariablesInstanceForLeavingScope();

    setScope(false);
  }

  public void cancelScope(String reason) {

    if(log.isLoggable(Level.FINE)) {
      log.fine("performing cancel scope behavior for execution "+this);
    }

    if (getSubProcessInstance() != null) {
      getSubProcessInstance().deleteCascade(reason);
    }

    // remove all child executions and sub process instances:
    List<InterpretableExecution> executions = new ArrayList<InterpretableExecution>(getExecutions());
    for (InterpretableExecution childExecution : executions) {
      if (childExecution.getSubProcessInstance()!=null) {
        childExecution.getSubProcessInstance().deleteCascade(reason);
      }
      childExecution.deleteCascade(reason);
    }

    removeTasks(reason);
  }

  /** removes an execution. if there are nested executions, those will be ended recursively.
   * if there is a parent, this method removes the bidirectional relation
   * between parent and this execution.
   *
   * @param completeScope true if ending the execution contributes to completing the BPMN 2.0 scope
   */
  public void end(boolean completeScope) {

    setCompleteScope(completeScope);

    isActive = false;
    isEnded = true;
    performOperation(AtomicOperation.ACTIVITY_END);
  }

  // methods that translate to operations /////////////////////////////////////

  public void signal(String signalName, Object signalData) {
    ensureActivityInitialized();

    if (activity == null) {
      throw new PvmException("cannot signal execution " + this.id + ": it has no current activity");
    }

    SignallableActivityBehavior activityBehavior = (SignallableActivityBehavior) activity.getActivityBehavior();
    try {
      activityBehavior.signal(this, signalName, signalData);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new PvmException("couldn't process signal '"+signalName+"' on activity '"+activity.getId()+"': "+e.getMessage(), e);
    }
  }

  public void take(PvmTransition transition) {
    if (this.transition!=null) {
      throw new PvmException("already taking a transition");
    }
    if (transition==null) {
      throw new PvmException("transition is null");
    }
    setActivity((ActivityImpl)transition.getSource());
    setTransition((TransitionImpl) transition);
    performOperation(AtomicOperation.TRANSITION_NOTIFY_LISTENER_END);
  }

  public void executeActivity(PvmActivity activity) {
    if(activity.isConcurrent()) {
      this.nextActivity = activity;
      performOperation(AtomicOperation.ACTIVITY_START_CONCURRENT);
    } else if(activity.isCancelScope()) {
      this.nextActivity = activity;
      performOperation(AtomicOperation.ACTIVITY_START_CANCEL_SCOPE);
    } else {
      setActivity((ActivityImpl) activity);
      performOperation(AtomicOperation.ACTIVITY_START);
    }
  }

  public List<ActivityExecution> findInactiveConcurrentExecutions(PvmActivity activity) {
    List<ActivityExecution> inactiveConcurrentExecutionsInActivity = new ArrayList<ActivityExecution>();
    List<ActivityExecution> otherConcurrentExecutions = new ArrayList<ActivityExecution>();
    if (isConcurrent()) {
      List< ? extends ActivityExecution> concurrentExecutions = getParent().getAllChildExecutions();
      for (ActivityExecution concurrentExecution: concurrentExecutions) {
        if (concurrentExecution.getActivity()==activity) {
          if (!concurrentExecution.isActive()) {
            inactiveConcurrentExecutionsInActivity.add(concurrentExecution);
          }
        } else {
          otherConcurrentExecutions.add(concurrentExecution);
        }
      }
    } else {
      if (!isActive()) {
        inactiveConcurrentExecutionsInActivity.add(this);
      } else {
        otherConcurrentExecutions.add(this);
      }
    }
    if (log.isLoggable(Level.FINE)) {
      log.fine("inactive concurrent executions in '"+activity+"': "+inactiveConcurrentExecutionsInActivity);
      log.fine("other concurrent executions: "+otherConcurrentExecutions);
    }
    return inactiveConcurrentExecutionsInActivity;
  }

  protected List<ExecutionEntity> getAllChildExecutions() {
    List<ExecutionEntity> childExecutions = new ArrayList<ExecutionEntity>();
    for (ExecutionEntity childExecution : getExecutions()) {
      childExecutions.add(childExecution);
      childExecutions.addAll(childExecution.getAllChildExecutions());
    }
    return childExecutions;
  }

  @SuppressWarnings("unchecked")
  public void takeAll(List<PvmTransition> transitions, List<ActivityExecution> recyclableExecutions) {
    transitions = new ArrayList<PvmTransition>(transitions);
    recyclableExecutions = (recyclableExecutions!=null ? new ArrayList<ActivityExecution>(recyclableExecutions) : new ArrayList<ActivityExecution>());

    if (recyclableExecutions.size()>1) {
      for (ActivityExecution recyclableExecution: recyclableExecutions) {
        if (((ActivityExecution)recyclableExecution).isScope()) {
          throw new PvmException("joining scope executions is not allowed");
        }
      }
    }

    ExecutionEntity concurrentRoot = ((isConcurrent && !isScope) ? getParent() : this);
    List<ExecutionEntity> concurrentActiveExecutions = new ArrayList<ExecutionEntity>();
    List<ExecutionEntity> concurrentInActiveExecutions = new ArrayList<ExecutionEntity>();
    for (ExecutionEntity execution: concurrentRoot.getExecutions()) {
      if (execution.isActive()) {
        concurrentActiveExecutions.add(execution);
      } else {
        concurrentInActiveExecutions.add(execution);
      }
    }

    if (log.isLoggable(Level.FINE)) {
      log.fine("transitions to take concurrent: " + transitions);
      log.fine("active concurrent executions: " + concurrentActiveExecutions);
    }

    if ( (transitions.size()==1)
         && (concurrentActiveExecutions.isEmpty())
         && allExecutionsInSameActivity(concurrentInActiveExecutions)
       ) {

      List<ExecutionEntity> recyclableExecutionImpls = (List) recyclableExecutions;
      recyclableExecutions.remove(concurrentRoot);
      for (ActivityExecution prunedExecution: recyclableExecutionImpls) {
        // End the pruned executions if necessary.
        // Some recyclable executions are inactivated (joined executions)
        // Others are already ended (end activities)
        if (!prunedExecution.isEnded()) {
          log.fine("pruning execution " + prunedExecution);
          prunedExecution.end(false);
        }
      }

      log.fine("activating the concurrent root "+concurrentRoot+" as the single path of execution going forward");
      concurrentRoot.setActive(true);
      concurrentRoot.setActivity(activity);
      concurrentRoot.setConcurrent(hasConcurrentSiblings(concurrentRoot));
      concurrentRoot.take(transitions.get(0));

    } else {

      List<OutgoingExecution> outgoingExecutions = new ArrayList<OutgoingExecution>();

      recyclableExecutions.remove(concurrentRoot);

      log.fine("recyclable executions for reuse: " + recyclableExecutions);

      // first create the concurrent executions
      while (!transitions.isEmpty()) {
        PvmTransition outgoingTransition = transitions.remove(0);

        ExecutionEntity outgoingExecution = null;
        if (recyclableExecutions.isEmpty()) {
          outgoingExecution = concurrentRoot.createExecution();
          log.fine("new "+outgoingExecution+" with parent "
                  + outgoingExecution.getParent()+" created to take transition "+outgoingTransition);
        } else {
          outgoingExecution = (ExecutionEntity) recyclableExecutions.remove(0);
          log.fine("recycled "+outgoingExecution+" to take transition "+outgoingTransition);
        }

        outgoingExecution.setActive(true);
        outgoingExecution.setScope(false);
        outgoingExecution.setConcurrent(true);
        outgoingExecution.setTransitionBeingTaken((TransitionImpl) outgoingTransition);
        outgoingExecutions.add(new OutgoingExecution(outgoingExecution, outgoingTransition, true));
      }

      concurrentRoot.setActivityInstanceId(concurrentRoot.getParentActivityInstanceId());

      boolean isConcurrentEnd = outgoingExecutions.isEmpty();

      // prune the executions that are not recycled
      for (ActivityExecution prunedExecution: recyclableExecutions) {
        log.fine("pruning execution "+prunedExecution);
        prunedExecution.end(isConcurrentEnd);
      }

      // then launch all the concurrent executions
      for (OutgoingExecution outgoingExecution: outgoingExecutions) {
        outgoingExecution.take();
      }

      // if no outgoing executions, the concurrent root execution ends
      if (isConcurrentEnd) {
        concurrentRoot.end(true);
      }
    }
  }

  protected boolean hasConcurrentSiblings(ExecutionEntity concurrentRoot) {
    if(concurrentRoot.isProcessInstance()) {
      return false;
    } else {
      List<ExecutionEntity> executions = concurrentRoot.getParent().getExecutions();
      for (ExecutionEntity executionImpl : executions) {
        if(executionImpl != concurrentRoot
            && !executionImpl.isEventScope()) {
          return true;
        }
      }
      return false;
    }
  }

  protected boolean allExecutionsInSameActivity(List<ExecutionEntity> executions) {
    if (executions.size() > 1) {
      String activityId = executions.get(0).getActivityId();
      for (ExecutionEntity execution : executions) {
        String otherActivityId = execution.getActivityId();
        if (!execution.isEnded) {
          if ( (activityId == null && otherActivityId != null)
                  || (activityId != null && otherActivityId == null)
                  || (activityId != null && otherActivityId!= null && !otherActivityId.equals(activityId))) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public void performOperation(AtomicOperation executionOperation) {
    if(executionOperation.isAsync(this)) {
      scheduleAtomicOperationAsync(executionOperation);
    } else {
      performOperationSync(executionOperation);
    }
  }

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

  protected boolean requiresUnsuspendedExecution(
      AtomicOperation executionOperation) {
    if (executionOperation != AtomicOperation.TRANSITION_NOTIFY_LISTENER_END
        && executionOperation != AtomicOperation.TRANSITION_DESTROY_SCOPE
        && executionOperation != AtomicOperation.TRANSITION_NOTIFY_LISTENER_TAKE
        && executionOperation != AtomicOperation.TRANSITION_NOTIFY_LISTENER_END
        && executionOperation != AtomicOperation.TRANSITION_CREATE_SCOPE
        && executionOperation != AtomicOperation.TRANSITION_NOTIFY_LISTENER_START
        && executionOperation != AtomicOperation.DELETE_CASCADE
        && executionOperation != AtomicOperation.DELETE_CASCADE_FIRE_ACTIVITY_END) {
      return true;
    }

    return false;
  }

  protected void scheduleAtomicOperationAsync(AtomicOperation executionOperation) {

    final MessageJobDeclaration messageJobDeclaration = (MessageJobDeclaration) getActivity().getProperty(BpmnParse.PROPERTYNAME_MESSAGE_JOB_DECLARATION);

    if(messageJobDeclaration != null) {
      MessageEntity message = messageJobDeclaration.createJobInstance(this);
      message.setJobHandlerConfiguration(executionOperation.getCanonicalName());

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

  @SuppressWarnings("unchecked")
  protected void ensureExecutionsInitialized() {
    if (executions==null) {
      this.executions = (List) Context
        .getCommandContext()
        .getExecutionManager()
        .findChildExecutionsByParentExecutionId(id);
    }
  }

  public void setExecutions(List<ExecutionEntity> executions) {
    this.executions = executions;
  }

  /** searches for an execution positioned in the given activity */
  public ExecutionEntity findExecution(String activityId) {
    if ( (getActivity()!=null)
         && (getActivity().getId().equals(activityId))
       ) {
      return this;
    }
    for (ExecutionEntity nestedExecution : getExecutions()) {
      ExecutionEntity result = nestedExecution.findExecution(activityId);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public List<String> findActiveActivityIds() {
    List<String> activeActivityIds = new ArrayList<String>();
    collectActiveActivityIds(activeActivityIds);
    return activeActivityIds;
  }

  protected void collectActiveActivityIds(List<String> activeActivityIds) {
    ensureActivityInitialized();
    if (isActive && activity!=null) {
      activeActivityIds.add(activity.getId());
    }
    ensureExecutionsInitialized();
    for (ExecutionEntity execution: executions) {
      execution.collectActiveActivityIds(activeActivityIds);
    }
  }


  // bussiness key ////////////////////////////////////////////////////////////

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

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
      processInstance =  Context
        .getCommandContext()
        .getExecutionManager()
        .findExecutionById(processInstanceId);
    }
  }

  public void setProcessInstance(InterpretableExecution processInstance) {
    this.processInstance = (ExecutionEntity) processInstance;
    if (processInstance != null) {
      this.processInstanceId = this.processInstance.getId();
    }
  }

  public boolean isProcessInstance() {
    return parentId == null;
  }

 // activity /////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the activity */
  public ActivityImpl getActivity() {
    ensureActivityInitialized();
    return activity;
  }

  /** must be called before the activity member field or getActivity() is called */
  protected void ensureActivityInitialized() {
    if ((activity == null) && (activityId != null)) {
      activity = getProcessDefinition().findActivity(activityId);
    }
  }

  public void setActivity(PvmActivity activity) {
    this.activity = (ActivityImpl) activity;
    if (activity != null) {
      this.activityId = activity.getId();
      this.activityName = (String) activity.getProperty("name");
    } else {
      this.activityId = null;
      this.activityName = null;
    }

  }

  public void enterActivityInstance() {

    ActivityImpl activity = getActivity();

    // special treatment for starting process instance
    if(activity == null && processInstanceStartContext!= null) {
      activity = processInstanceStartContext.getInitial();
    }

    activityInstanceId = generateActivityInstanceId(activity.getId());

    if(log.isLoggable(Level.FINE)) {
      log.fine("[ENTER] "+this + ": "+activityInstanceId+", parent: "+getParentActivityInstanceId());
    }

  }

  public void leaveActivityInstance() {

    if(activityInstanceId != null) {

      if(log.isLoggable(Level.FINE)) {
        log.fine("[LEAVE] "+ this + ": "+activityInstanceId );
      }

      activityInstanceId = getParentActivityInstanceId();
    }

    activityInstanceState = ActivityInstanceState.DEFAULT.getStateCode();
  }

  public String getParentActivityInstanceId() {
    if(isProcessInstance()) {
      return id;

    } else {
      ExecutionEntity parent = getParent();
      ActivityImpl activity = getActivity();
      ActivityImpl parentActivity = parent.getActivity();
      if (parent.isScope() && !isConcurrent() || parent.isConcurrent
           && activity != parentActivity
          ) {
        return parent.getActivityInstanceId();
      } else {
        return parent.getParentActivityInstanceId();
      }

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

  public void forceUpdateActivityInstance() {
    activityInstanceId = generateActivityInstanceId(getActivity().getActivityId());
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }


 // parent ///////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the parent */
  public ExecutionEntity getParent() {
    ensureParentInitialized();
    return parent;
  }

  protected void ensureParentInitialized() {
    if (parent == null && parentId != null) {
      parent = Context
        .getCommandContext()
        .getExecutionManager()
        .findExecutionById(parentId);
    }
  }

  public void setParent(InterpretableExecution parent) {
    this.parent = (ExecutionEntity) parent;

    if (parent != null) {
      this.parentId = ((ActivityExecution)parent).getId();
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

  public void setSuperExecution(ExecutionEntity superExecution) {
    this.superExecution = superExecution;
    if (superExecution != null) {
      superExecution.setSubProcessInstance(null);
    }

    if (superExecution != null) {
      this.superExecutionId = ((ActivityExecution)superExecution).getId();
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

  public void setSubProcessInstance(InterpretableExecution subProcessInstance) {
    this.subProcessInstance = (ExecutionEntity) subProcessInstance;
  }

  protected void ensureSubProcessInstanceInitialized() {
    if (subProcessInstance == null) {
      subProcessInstance = Context
        .getCommandContext()
        .getExecutionManager()
        .findSubProcessInstanceBySuperExecutionId(id);
    }
  }

  // scopes ///////////////////////////////////////////////////////////////////

  protected ScopeImpl getScopeActivity() {
    ScopeImpl scope = null;
    if (isProcessInstance()) {
      scope = getProcessDefinition();
    } else {
      scope = getActivity();
    }
    return scope;
  }

  public boolean isScope() {
    return isScope;
  }

  public void setScope(boolean isScope) {
    this.isScope = isScope;
  }

  // customized persistence behavior /////////////////////////////////////////

  public void remove() {
    ensureParentInitialized();
    if (parent!=null) {
      parent.ensureExecutionsInitialized();
      parent.executions.remove(this);
    }

    isEnded = true;

    // delete all the variable instances
    ensureVariableInstancesInitialized();
    deleteVariablesInstanceForLeavingScope();

    // delete all the tasks
    removeTasks(null);

    // remove all jobs
    removeJobs();

    // remove all incidents
    removeIncidents();

    // remove all event subscriptions for this scope, if the scope has event subscriptions:
    removeEventSubscriptions();

    // remove event scopes:
    removeEventScopes();

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

  private void removeEventScopes() {
    List<InterpretableExecution> childExecutions = new ArrayList<InterpretableExecution>(getExecutions());
    for (InterpretableExecution childExecution : childExecutions) {
      if(childExecution.isEventScope()) {
        log.fine("removing eventScope "+childExecution);
        childExecution.destroy();
        childExecution.remove();
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
        Context.getCommandContext()
          .getTaskManager()
          .deleteTask(task, reason, false);
      }
    }
  }

  public ExecutionEntity getReplacedBy() {
    return replacedBy;
  }

  @SuppressWarnings("unchecked")
  public void setReplacedBy(InterpretableExecution replacedBy) {
    this.replacedBy = (ExecutionEntity) replacedBy;

    CommandContext commandContext = Context.getCommandContext();
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();

    // update the related tasks
    for (TaskEntity task: getTasks()) {
      task.setExecutionId(replacedBy.getId());
      task.setExecution(this.replacedBy);

      // update the related local task variables
      List<VariableInstanceEntity> variables = (List) commandContext
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
    List<VariableInstanceEntity> variables = (List) commandContext
      .getVariableInstanceManager()
      .findVariableInstancesByExecutionId(id);

    for (VariableInstanceEntity variable: variables) {
      variable.setExecutionId(replacedBy.getId());
    }
    variables = dbSqlSession.findInCache(VariableInstanceEntity.class);
    for (VariableInstanceEntity variable: variables) {
      if (id.equals(variable.getExecutionId())) {
        variable.setExecutionId(replacedBy.getId());
      }
    }

    // TODO: fire UPDATE activity instance events with new execution?

    // set replaced by activity to our activity id
    replacedBy.setActivityInstanceId(activityInstanceId);
  }

  @SuppressWarnings("unchecked")
  public void replace(InterpretableExecution execution) {
    ExecutionEntity replacedExecution = (ExecutionEntity) execution;

    CommandContext commandContext = Context.getCommandContext();

    // update the related tasks
    for (TaskEntity task: replacedExecution.getTasksInternal()) {
      task.setExecutionId(getId());
      task.setExecution(this);

      // update the related local task variables
      List<VariableInstanceEntity> variables = (List) commandContext
        .getVariableInstanceManager()
        .findVariableInstancesByTaskId(task.getId());

      for (VariableInstanceEntity variable : variables) {
        variable.setExecution(this);
      }

      addTask(task);
    }
    replacedExecution.getTasksInternal().clear();

    // activity instance id handling
    this.activityInstanceId = execution.getActivityInstanceId();
    execution.leaveActivityInstance();
  }

  // variables ////////////////////////////////////////////////////////////////

  @Override
  protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {
    variableInstance.setProcessInstanceId(processInstanceId);
    variableInstance.setExecutionId(id);
  }

  @Override
  protected List<VariableInstanceEntity> loadVariableInstances() {
    return Context
      .getCommandContext()
      .getVariableInstanceManager()
      .findVariableInstancesByExecutionId(id);
  }

  @Override
  protected VariableScopeImpl getParentVariableScope() {
    return getParent();
  }

  /** used to calculate the sourceActivityExecution */
  protected ExecutionEntity getSourceActivityExecution() {
    return (activityId!=null ? this : null);
  }

  protected boolean isAutoFireHistoryEvents() {
    // as long as the process instance is starting (ie. before activity instance of
    // the selected initial (start event) is created), the variable scope should not
    // automatic fire history events for variable updates.

    // firing the events is triggered by the processInstanceStart context after the initial activity
    // has been initialized. The effect is that the activity instance id of the historic variable instances
    // will be the activity instance id of the start event.

    return processInstanceStartContext == null;
  }

  public void fireHistoricVariableInstanceCreateEvents() {
    // this method is called by the start context and batch-fires create events for all variable instances
    if(variableInstances != null) {
      for (Entry<String, VariableInstanceEntity> variable : variableInstances.entrySet()) {
        fireHistoricVariableInstanceCreate(variable.getValue(), this);
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

  public void deleteCascade(String deleteReason) {
    this.deleteReason = deleteReason;
    this.deleteRoot = true;
    this.isEnded = true;
    performOperation(AtomicOperation.DELETE_CASCADE);
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
    if (isProcessInstance()) {
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
  }

  public int getCachedEntityState() {
    cachedEntityState = 0;

    // Only mark a flag as false when the list is not-null and empty. If null, we can't be sure there are no entries in it since
    // the list hasn't been initialized/queried yet.
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, TASKS_STATE_BIT, (tasks == null || tasks.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, EVENT_SUBSCRIPTIONS_STATE_BIT, (eventSubscriptions == null || eventSubscriptions.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, JOBS_STATE_BIT, (jobs == null || jobs.size() > 0));
    cachedEntityState = BitMaskUtil.setBit(cachedEntityState, INCIDENT_STATE_BIT, (incidents == null || incidents.size() > 0));

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
  public String getActivityId() {
    return activityId;
  }

  public TransitionImpl getTransition() {
    return transition;
  }
  public void setTransition(TransitionImpl transition) {
    this.transition = transition;
  }
  public TransitionImpl getTransitionBeingTaken() {
    return transitionBeingTaken;
  }
  public void setTransitionBeingTaken(TransitionImpl transitionBeingTaken) {
    this.transitionBeingTaken = transitionBeingTaken;
  }
  public Integer getExecutionListenerIndex() {
    return executionListenerIndex;
  }
  public void setExecutionListenerIndex(Integer executionListenerIndex) {
    this.executionListenerIndex = executionListenerIndex;
  }
  public boolean isConcurrent() {
    return isConcurrent;
  }
  public void setConcurrent(boolean isConcurrent) {
    this.isConcurrent = isConcurrent;
  }
  public boolean isActive() {
    return isActive;
  }
  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }
  public boolean isEnded() {
    return isEnded;
  }

  public boolean isCanceled() {
    return ActivityInstanceState.CANCELED.getStateCode() == activityInstanceState;
  }

  public void setCanceled(boolean canceled) {
    if (canceled) {
      activityInstanceState = ActivityInstanceState.CANCELED.getStateCode();
    }
  }

  public boolean isCompleteScope() {
    return ActivityInstanceState.SCOPE_COMPLETE.getStateCode() == activityInstanceState;
  }

  public void setCompleteScope(boolean completeScope) {
    if (completeScope) {
      activityInstanceState = ActivityInstanceState.SCOPE_COMPLETE.getStateCode();
    }
  }

  public int getActivityInstanceState() {
    return activityInstanceState;
  }

  public String getEventName() {
    return eventName;
  }
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  public PvmProcessElement getEventSource() {
    return eventSource;
  }
  public void setEventSource(PvmProcessElement eventSource) {
    this.eventSource = eventSource;
  }
  public String getDeleteReason() {
    return deleteReason;
  }
  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }
  public boolean isDeleteRoot() {
    return deleteRoot;
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

  public boolean isEventScope() {
    return isEventScope;
  }

  public void setEventScope(boolean isEventScope) {
    this.isEventScope = isEventScope;
  }

  public ProcessInstanceStartContext getProcessInstanceStartContext() {
    if(isProcessInstance()) {
      if(processInstanceStartContext == null) {
        processInstanceStartContext = new ProcessInstanceStartContext(processDefinition.getInitial());
      }
    }
    return processInstanceStartContext;
  }

  public void disposeProcessInstanceStartContext() {
    processInstanceStartContext = null;
  }

  public String getCurrentActivityId() {
    return activityId;
  }

  public String getCurrentActivityName() {
    return activityName;
  }

  public String getCurrentTransitionId() {
    if(transition != null) {
      return transition.getId();
    } else {
      return null;
    }
  }

  /**
   * @return the nextActivity
   */
  public PvmActivity getNextActivity() {
    return nextActivity;
  }

}
