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
package org.camunda.bpm.engine.impl.pvm.runtime;

import static org.camunda.bpm.engine.impl.bpmn.helper.CompensationUtil.SIGNAL_COMPENSATION_DONE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.PvmExecution;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.ModificationObserverBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ActivityStartBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.FoxAtomicOperationDeleteCascadeFireActivityEnd;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;
import org.camunda.bpm.engine.impl.tree.ActivityAwareScopeExecutionCollector;
import org.camunda.bpm.engine.impl.tree.ExecutionWalker;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.ScopeCollector;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 *
 */
public abstract class PvmExecutionImpl extends CoreExecution implements ActivityExecution, PvmProcessInstance {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(PvmExecutionImpl.class.getName());

  protected transient ProcessDefinitionImpl processDefinition;

  protected transient ExecutionStartContext startContext;

  // current position /////////////////////////////////////////////////////////

  /** current activity */
  protected transient ActivityImpl activity;

  /** the activity which is to be started next */
  protected transient PvmActivity nextActivity;

  /** the transition that is currently being taken */
  protected transient TransitionImpl transition;

  /** A list of outgoing transitions from the current activity
   * that are going to be taken */
  protected transient List<PvmTransition> transitionsToTake = null;

  /** the unique id of the current activity instance */
  protected String activityInstanceId;

  /** the id of a case associated with this execution */
  protected String caseInstanceId;

  protected PvmExecutionImpl replacedBy;

  // cascade deletion ////////////////////////////////////////////////////////

  protected boolean deleteRoot;
  protected String deleteReason;

  //state/type of execution //////////////////////////////////////////////////

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

  /** transient; used for process instance modification to preserve a scope from getting deleted */
  protected boolean preserveScope = false;

  /** marks the current activity instance */
  protected int activityInstanceState = ActivityInstanceState.DEFAULT.getStateCode();

  // sequence counter ////////////////////////////////////////////////////////
  protected long sequenceCounter = 0;

  public PvmExecutionImpl() {
  }

  // API ////////////////////////////////////////////////

  /** creates a new execution. properties processDefinition, processInstance and activity will be initialized. */
  @Override
  public PvmExecutionImpl createExecution() {
    return createExecution(false);
  }

  @Override
  public abstract PvmExecutionImpl createExecution(boolean initStartContext);

  // sub process instance

  @Override
  public PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition) {
    return createSubProcessInstance(processDefinition, null);
  }

  @Override
  public PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey) {
    PvmExecutionImpl processInstance = getProcessInstance();

    String caseInstanceId = null;
    if (processInstance != null) {
      caseInstanceId = processInstance.getCaseInstanceId();
    }

    return createSubProcessInstance(processDefinition, businessKey, caseInstanceId);
  }

  @Override
  public PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey, String caseInstanceId) {
    PvmExecutionImpl subProcessInstance = newExecution();

    // manage bidirectional super-subprocess relation
    subProcessInstance.setSuperExecution(this);
    this.setSubProcessInstance(subProcessInstance);

    // Initialize the new execution
    subProcessInstance.setProcessDefinition((ProcessDefinitionImpl) processDefinition);
    subProcessInstance.setProcessInstance(subProcessInstance);
    subProcessInstance.setActivity(processDefinition.getInitial());

    if(businessKey != null) {
      subProcessInstance.setBusinessKey(businessKey);
    }

    if(caseInstanceId != null) {
      subProcessInstance.setCaseInstanceId(caseInstanceId);
    }

    return subProcessInstance;
  }

  protected abstract PvmExecutionImpl newExecution();

  // sub case instance

  @Override
  public abstract CmmnExecution createSubCaseInstance(CmmnCaseDefinition caseDefinition);

  @Override
  public abstract CmmnExecution createSubCaseInstance(CmmnCaseDefinition caseDefinition, String businessKey);

  public abstract void initialize();

  public abstract void initializeTimerDeclarations();

  public void executeIoMapping() {
    // execute Input Mappings (if they exist).
    ScopeImpl currentScope = getScopeActivity();
    if (currentScope != currentScope.getProcessDefinition()) {
      ActivityImpl currentActivity = (ActivityImpl) currentScope;

      if (currentActivity != null && currentActivity.getIoMapping() != null && !skipIoMapping) {
        currentActivity.getIoMapping().executeInputParameters(this);
      }
    }

  }

  @Override
  public void start() {
    start(null);
  }

  @Override
  public void start(Map<String, Object> variables) {
    startContext = new ProcessInstanceStartContext(getActivity());

    initialize();
    initializeTimerDeclarations();

    if(variables != null) {
      setVariables(variables);
    }

    fireHistoricProcessStartEvent();

    performOperation(PvmAtomicOperation.PROCESS_START);
  }

  /**
   * perform starting behavior but don't execute the initial activity
   */
  public void startWithoutExecuting() {
    initialize();
    initializeTimerDeclarations();
    fireHistoricProcessStartEvent();
    performOperation(PvmAtomicOperation.FIRE_PROCESS_START);
  }

  public abstract void fireHistoricProcessStartEvent();

  @Override
  public void destroy() {
    log.fine("destroying "+this);

    setScope(false);
  }

  protected void removeEventScopes() {
    List<PvmExecutionImpl> childExecutions = new ArrayList<PvmExecutionImpl>(getEventScopeExecutions());
    for (PvmExecutionImpl childExecution : childExecutions) {
      log.fine("removing eventScope "+childExecution);
      childExecution.destroy();
      childExecution.remove();
    }
  }

  public void clearScope(String reason, boolean skipCustomListeners, boolean skipIoMappings) {
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMapping = skipIoMappings;

    if (getSubProcessInstance() != null) {
      getSubProcessInstance().deleteCascade(reason, skipCustomListeners, skipIoMappings);
    }

    // remove all child executions and sub process instances:
    List<PvmExecutionImpl> executions = new ArrayList<PvmExecutionImpl>(getNonEventScopeExecutions());
    for (PvmExecutionImpl childExecution : executions) {
      if (childExecution.getSubProcessInstance()!=null) {
        childExecution.getSubProcessInstance().deleteCascade(reason, skipCustomListeners, skipIoMappings);
      }
      childExecution.deleteCascade(reason, skipCustomListeners, skipIoMappings);
    }

    // fire activity end on active activity
    PvmActivity activity = getActivity();
    if(isActive && activity != null) {
      // set activity instance state to cancel
      setCanceled(true);
      performOperation(PvmAtomicOperation.FIRE_ACTIVITY_END);
      // set activity instance state back to 'default'
      // -> execution will be reused for executing more activities and we want the state to
      // be default initially.
      activityInstanceState = ActivityInstanceState.DEFAULT.getStateCode();
    }
  }

  /**
   * Interrupts an execution
   */
  @Override
  public void interrupt(String reason) {
    interrupt(reason, false, false);
  }

  public void interrupt(String reason, boolean skipCustomListeners, boolean skipIoMappings) {
    if(log.isLoggable(Level.FINE)) {
      log.fine("Interrupting execution "+this);
    }
    clearScope(reason, skipCustomListeners, skipIoMappings);
  }

  /**
   * Ends an execution. Invokes end listeners for the current activity and notifies the flow scope execution
   * of this happening which may result in the flow scope ending.
   *
   * @param completeScope true if ending the execution contributes to completing the BPMN 2.0 scope
   */
  @Override
  public void end(boolean completeScope) {

    setCompleteScope(completeScope);

    isActive = false;
    isEnded = true;

    if (hasReplacedParent()) {
      getParent().replacedBy = null;
    }

    performOperation(PvmAtomicOperation.ACTIVITY_NOTIFY_LISTENER_END);
  }

  @Override
  public void endCompensation() {
    remove();
    performOperation(PvmAtomicOperation.FIRE_ACTIVITY_END);

    PvmExecutionImpl parent = getParent();

    if(parent.getActivity() == null){
      parent.setActivity((PvmActivity) getActivity().getFlowScope());
    }

    parent.signal(SIGNAL_COMPENSATION_DONE, null);
  }

  /**
   * <p>Precondition: execution is already ended but this has not been propagated yet.</p>
   *
   * <p>Propagates the ending of this execution to the flowscope execution; currently only supports
   * the process instance execution</p>
   */
  public void propagateEnd() {
    if (!isEnded()) {
      throw new ProcessEngineException(toString() + " must have ended before ending can be propagated");
    }

    if (isProcessInstanceExecution()) {
      performOperation(PvmAtomicOperation.PROCESS_END);
    }
    else {
      // not supported yet
    }
  }

  @Override
  public void remove() {
    PvmExecutionImpl parent = getParent();
    if (parent!=null) {
      parent.getExecutions().remove(this);

      // if the sequence counter is greater than the
      // sequence counter of the parent, then set
      // the greater sequence counter on the parent.
      long parentSequenceCounter = parent.getSequenceCounter();
      long mySequenceCounter = getSequenceCounter();
      if (mySequenceCounter > parentSequenceCounter) {
        parent.setSequenceCounter(mySequenceCounter);
      }

      // propagate skipping configuration upwards, if it was not initially set on
      // the root execution
      parent.skipCustomListeners |= this.skipCustomListeners;
      parent.skipIoMapping |= this.skipIoMapping;

    }

    isActive = false;
    isEnded = true;

    if (hasReplacedParent()) {
      getParent().replacedBy = null;
    }

    removeEventScopes();
  }

  public PvmExecutionImpl createConcurrentExecution() {
    if (!isScope()) {
      throw new ProcessEngineException("Cannot create concurrent execution for " + this);
    }

    // The following covers the three cases in which a concurrent execution may be created
    // (this execution is the root in each scenario).
    //
    // Note: this should only consider non-event-scope executions. Event-scope executions
    // are not relevant for the tree structure and should remain under their original parent.
    //
    //
    // (1) A compacted tree:
    //
    // Before:               After:
    //       -------               -------
    //       |  e1  |              |  e1 |
    //       -------               -------
    //                             /     \
    //                         -------  -------
    //                         |  e2 |  |  e3 |
    //                         -------  -------
    //
    // e2 replaces e1; e3 is the new root for the activity stack to instantiate
    //
    //
    // (2) A single child that is a scope execution
    // Before:               After:
    //       -------               -------
    //       |  e1 |               |  e1 |
    //       -------               -------
    //          |                  /     \
    //       -------           -------  -------
    //       |  e2 |           |  e3 |  |  e4 |
    //       -------           -------  -------
    //                            |
    //                         -------
    //                         |  e2 |
    //                         -------
    //
    //
    // e3 is created and is concurrent;
    // e4 is the new root for the activity stack to instantiate
    //
    // (3) Existing concurrent execution(s)
    // Before:               After:
    //       -------                    ---------
    //       |  e1 |                    |   e1  |
    //       -------                    ---------
    //       /     \                   /    |    \
    //  -------    -------      -------  -------  -------
    //  |  e2 | .. |  eX |      |  e2 |..|  eX |  | eX+1|
    //  -------    -------      -------  -------  -------
    //
    // eX+1 is concurrent and the new root for the activity stack to instantiate
    List<? extends PvmExecutionImpl> children = this.getNonEventScopeExecutions();

    if (children.isEmpty()) {
      // (1)
      PvmExecutionImpl replacingExecution = this.createExecution();
      replacingExecution.setConcurrent(true);
      replacingExecution.setScope(false);
      replacingExecution.replace(this);
      this.inactivate();
      this.setActivity(null);

    }
    else if (children.size() == 1) {
      // (2)
      PvmExecutionImpl child = children.get(0);

      PvmExecutionImpl concurrentReplacingExecution = this.createExecution();
      concurrentReplacingExecution.setConcurrent(true);
      concurrentReplacingExecution.setScope(false);
      child.setParent(concurrentReplacingExecution);
      this.leaveActivityInstance();
      this.setActivity(null);
    }

    // (1), (2), and (3)
    PvmExecutionImpl concurrentExecution = this.createExecution();
    concurrentExecution.setConcurrent(true);
    concurrentExecution.setScope(false);

    return concurrentExecution;
  }

  @Override
  public boolean tryPruneLastConcurrentChild() {

    if (getNonEventScopeExecutions().size() == 1) {
      PvmExecutionImpl lastConcurrent = getNonEventScopeExecutions().get(0);
      if (lastConcurrent.isConcurrent()) {
        if (!lastConcurrent.isScope()) {
          setActivity(lastConcurrent.getActivity());
          setTransition(lastConcurrent.getTransition());
          this.replace(lastConcurrent);

          // Move children of lastConcurrent one level up
          if (lastConcurrent.hasChildren()) {
            for (PvmExecutionImpl childExecution : lastConcurrent.getExecutionsAsCopy()) {
              childExecution.setParent(this);
            }
          }

          // Copy execution-local variables of lastConcurrent
          setVariablesLocal(lastConcurrent.getVariablesLocal());

          // Make sure parent execution is re-activated when the last concurrent
          // child execution is active
          if (!isActive() && lastConcurrent.isActive()) {
            setActive(true);
          }

          lastConcurrent.remove();
        } else {
          // legacy behavior
          LegacyBehavior.pruneConcurrentScope(lastConcurrent);
        }
        return true;
      }
    }

    return false;

  }

  @Override
  public void deleteCascade(String deleteReason) {
    deleteCascade(deleteReason, false);
  }

  public void deleteCascade(String deleteReason, boolean skipCustomListeners) {
    deleteCascade(deleteReason, skipCustomListeners, false);
  }

  public void deleteCascade(String deleteReason, boolean skipCustomListeners, boolean skipIoMappings) {
    this.deleteReason = deleteReason;
    this.deleteRoot = true;
    this.isEnded = true;
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMapping = skipIoMappings;
    performOperation(PvmAtomicOperation.DELETE_CASCADE);
  }

  public void deleteCascade2(String deleteReason) {
    this.deleteReason = deleteReason;
    this.deleteRoot = true;
    performOperation(new FoxAtomicOperationDeleteCascadeFireActivityEnd());
  }

  public void executeEventHandlerActivity(ActivityImpl eventHandlerActivity) {

    // the target scope
    ScopeImpl flowScope = eventHandlerActivity.getFlowScope();

    // the event scope (the current activity)
    ScopeImpl eventScope = eventHandlerActivity.getEventScope();

    if(eventHandlerActivity.getActivityStartBehavior() == ActivityStartBehavior.CONCURRENT_IN_FLOW_SCOPE
        && flowScope != eventScope) {
      // the current scope is the event scope of the activity
      findExecutionForScope(eventScope, flowScope)
        .executeActivity(eventHandlerActivity);
    }
    else {
      executeActivity(eventHandlerActivity);
    }
  }

  // tree compaction & expansion ///////////////////////////////////////////

  /**
   * <p>Returns an execution that has replaced this execution for executing activities in their shared scope.</p>
   * <p>Invariant: this execution and getReplacedBy() execute in the same scope.</p>
   */
  public abstract PvmExecutionImpl getReplacedBy();

  public boolean hasReplacedParent() {
    return getParent() != null && getParent().getReplacedBy() == this;
  }

  public boolean isReplacedByParent() {
    return getReplacedBy() != null && getReplacedBy() == this.getParent();
  }

  /**
   * <p>Replace an execution by this execution. The replaced execution has a pointer ({@link #getReplacedBy()}) to this execution.
   * This pointer is maintained until the replaced execution is removed or this execution is removed/ended.</p>
   *
   * <p>This is used for two cases: Execution tree expansion and execution tree compaction</p>
   * <ul>
   *   <li><b>expansion</b>: Before:
   *     <pre>
   *       -------
   *       |  e1 |  scope
   *       -------
   *     </pre>
   *     After:
   *     <pre>
   *       -------
   *       |  e1 |  scope
   *       -------
   *          |
   *       -------
   *       |  e2 |  cc (no scope)
   *       -------
   *     </pre>
   *     e2 replaces e1: it should receive all entities associated with the activity currently executed
   *       by e1; these are tasks, (local) variables, jobs (specific for the activity, not the scope)
   *   </li>
   *   <li><b>compaction</b>: Before:
   *     <pre>
   *       -------
   *       |  e1 |  scope
   *       -------
   *          |
   *       -------
   *       |  e2 |  cc (no scope)
   *       -------
   *     </pre>
   *     After:
   *     <pre>
   *       -------
   *       |  e1 |  scope
   *       -------
   *     </pre>
   *     e1 replaces e2: it should receive all entities associated with the activity currently executed
   *       by e2; these are tasks, (all) variables, all jobs
   *   </li>
   * </ul>
   *
   * @see #createConcurrentExecution()
   * @see #tryPruneLastConcurrentChild()
   */
  public void replace(PvmExecutionImpl execution) {
    // activity instance id handling
    this.activityInstanceId = execution.getActivityInstanceId();
    this.isActive = execution.isActive;

    this.replacedBy = null;
    execution.replacedBy = this;

    execution.leaveActivityInstance();
  }

  // methods that translate to operations /////////////////////////////////////

  @Override
  public void signal(String signalName, Object signalData) {
    if (getActivity() == null) {
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

  public void take() {
    if (this.transition == null) {
      throw new PvmException(toString() + ": no transition to take specified");
    }
    if (transition==null) {
      throw new PvmException("transition is null");
    }
    TransitionImpl transitionImpl = transition;
    setActivity(transitionImpl.getSource());
    // while executing the transition, the activityInstance is 'null'
    // (we are not executing an activity)
    setActivityInstanceId(null);
    setActive(true);
    performOperation(PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_TAKE);
  }

  /**
   * Execute an activity which is not contained in normal flow (having no incoming sequence flows).
   * Cannot be called for activities contained in normal flow.
   *<p>
   * First, the ActivityStartBehavior is evaluated.
   * In case the start behavior is not {@link ActivityStartBehavior#DEFAULT}, the corresponding start
   * behavior is executed before executing the activity.
   *<p>
   * For a given activity, the execution on which this method must be called depends on the type of the start behavior:
   * <ul>
   * <li>CONCURRENT_IN_FLOW_SCOPE: scope execution for {@link PvmActivity#getFlowScope()}</li>
   * <li>INTERRUPT_EVENT_SCOPE: scope execution for {@link PvmActivity#getEventScope()}</li>
   * <li>CANCEL_EVENT_SCOPE: scope execution for {@link PvmActivity#getEventScope()}</li>
   * </ul>
   *
   * @param the activity to start
   */
  @Override
  public void executeActivity(PvmActivity activity) {
    if(!activity.getIncomingTransitions().isEmpty()) {
      throw new ProcessEngineException("Activity is contained in normal flow and cannot be executed using executeActivity().");
    }

    ActivityStartBehavior activityStartBehavior = activity.getActivityStartBehavior();
    if(!isScope() && ActivityStartBehavior.DEFAULT != activityStartBehavior) {
      throw new ProcessEngineException("Activity '"+activity+"' with start behavior '"+activityStartBehavior+"'"
            + "cannot be executed by non-scope execution.");
    }

    PvmActivity activityImpl = activity;
    setActive(true);
    switch (activityStartBehavior) {
    case CONCURRENT_IN_FLOW_SCOPE:
      this.nextActivity = activityImpl;
      performOperation(PvmAtomicOperation.ACTIVITY_START_CONCURRENT);
      break;

    case CANCEL_EVENT_SCOPE:
      this.nextActivity = activityImpl;
      performOperation(PvmAtomicOperation.ACTIVITY_START_CANCEL_SCOPE);
      break;

    case INTERRUPT_EVENT_SCOPE:
      this.nextActivity = activityImpl;
      performOperation(PvmAtomicOperation.ACTIVITY_START_INTERRUPT_SCOPE);
      break;

    default:
      setActivity(activityImpl);
      setActivityInstanceId(null);
      performOperation(PvmAtomicOperation.ACTIVITY_START_CREATE_SCOPE);
      break;
    }
  }

  /**
   * Instantiates the given activity stack under this execution.
   * Sets the variables for the execution responsible to execute the most deeply nested
   * activity.
   *
   * @param activityStack The most deeply nested activity is the last element in the list
   */
  public void executeActivitiesConcurrent(List<PvmActivity> activityStack, PvmActivity targetActivity,
      PvmTransition targetTransition, Map<String, Object> variables, Map<String, Object> localVariables,
      boolean skipCustomListeners, boolean skipIoMappings) {


    PvmExecutionImpl propagatingExecution = createConcurrentExecution();

    ScopeImpl flowScope = null;
    if (!activityStack.isEmpty()) {
      flowScope = activityStack.get(0).getFlowScope();
    } else if (targetActivity != null) {
      flowScope = targetActivity.getFlowScope();
    } else if (targetTransition != null) {
      flowScope = targetTransition.getSource().getFlowScope();
    }

    if (flowScope.getActivityBehavior() instanceof ModificationObserverBehavior) {
      ModificationObserverBehavior flowScopeBehavior = (ModificationObserverBehavior) flowScope.getActivityBehavior();
      flowScopeBehavior.concurrentExecutionCreated(propagatingExecution.getParent(), propagatingExecution);
    }

    propagatingExecution.executeActivities(activityStack, targetActivity, targetTransition, variables, localVariables,
        skipCustomListeners, skipIoMappings);
  }

  /**
   * Instantiates the given activity stack. Uses this execution to execute the
   * highest activity in the stack.
   * Sets the variables for the execution responsible to execute the most deeply nested
   * activity.
   *
   * @param activityStack The most deeply nested activity is the last element in the list
   */
  public void executeActivities(List<PvmActivity> activityStack, PvmActivity targetActivity,
      PvmTransition targetTransition, Map<String, Object> variables, Map<String, Object> localVariables,
      boolean skipCustomListeners, boolean skipIoMappings) {


    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMapping = skipIoMappings;
    this.activityInstanceId = null;
    this.isEnded = false;

    if (!activityStack.isEmpty()) {
      ExecutionStartContext executionStartContext = new ExecutionStartContext(false);

      InstantiationStack instantiationStack = new InstantiationStack(activityStack, targetActivity, targetTransition);
      executionStartContext.setInstantiationStack(instantiationStack);
      executionStartContext.setVariables(variables);
      executionStartContext.setVariablesLocal(localVariables);
      setStartContext(executionStartContext);

      performOperation(PvmAtomicOperation.ACTIVITY_INIT_STACK);

    }
    else if (targetActivity != null) {
      setVariables(variables);
      setVariablesLocal(localVariables);
      setActivity(targetActivity);
      performOperation(PvmAtomicOperation.ACTIVITY_START_CREATE_SCOPE);

    }
    else if (targetTransition != null) {
      setVariables(variables);
      setVariablesLocal(localVariables);
      setActivity(targetTransition.getSource());
      setTransition(targetTransition);
      performOperation(PvmAtomicOperation.TRANSITION_START_NOTIFY_LISTENER_TAKE);
    }
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<ActivityExecution> findInactiveConcurrentExecutions(PvmActivity activity) {
    List<PvmExecutionImpl> inactiveConcurrentExecutionsInActivity = new ArrayList<PvmExecutionImpl>();
    List<PvmExecutionImpl> otherConcurrentExecutions = new ArrayList<PvmExecutionImpl>();
    if (isConcurrent()) {
      List< ? extends PvmExecutionImpl> concurrentExecutions = getParent().getAllChildExecutions();
      for (PvmExecutionImpl concurrentExecution: concurrentExecutions) {
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
    return (List) inactiveConcurrentExecutionsInActivity;
  }

  protected List<PvmExecutionImpl> getAllChildExecutions() {
    List<PvmExecutionImpl> childExecutions = new ArrayList<PvmExecutionImpl>();
    for (PvmExecutionImpl childExecution : getExecutions()) {
      childExecutions.add(childExecution);
      childExecutions.addAll(childExecution.getAllChildExecutions());
    }
    return childExecutions;
  }

  @Override
  public void leaveActivityViaTransition(PvmTransition outgoingTransition) {
    leaveActivityViaTransitions(Arrays.asList(outgoingTransition), Collections.<ActivityExecution>emptyList());
  }

  @Override
  public void leaveActivityViaTransitions(List<PvmTransition> _transitions, List<? extends ActivityExecution> _recyclableExecutions) {
    List<? extends ActivityExecution> recyclableExecutions = Collections.emptyList();
    if (_recyclableExecutions != null) {
      recyclableExecutions = new ArrayList<ActivityExecution>(_recyclableExecutions);
    }

    recyclableExecutions.remove(this);
    for (ActivityExecution execution : recyclableExecutions) {
      execution.end(_transitions.isEmpty());
    }

    PvmExecutionImpl propagatingExecution = this;
    if (getReplacedBy() != null) {
      propagatingExecution = getReplacedBy();
    }

    propagatingExecution.isActive = true;
    propagatingExecution.isEnded = false;

    if (_transitions.isEmpty()) {
      propagatingExecution.end(!propagatingExecution.isConcurrent());
    }
    else {
      propagatingExecution.setTransitionsToTake(_transitions);
      propagatingExecution.performOperation(PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_END);
    }
  }

  public boolean isActive(String activityId) {
    return findExecution(activityId)!=null;
  }

  @Override
  public void inactivate() {
    this.isActive = false;
  }

  // executions ///////////////////////////////////////////////////////////////

  @Override
  public abstract List<? extends PvmExecutionImpl> getExecutions();

  public abstract List<? extends PvmExecutionImpl> getExecutionsAsCopy();

  public List<? extends PvmExecutionImpl> getNonEventScopeExecutions() {
    List<? extends PvmExecutionImpl> children = getExecutions();
    List<PvmExecutionImpl> result = new ArrayList<PvmExecutionImpl>();

    for (PvmExecutionImpl child : children) {
      if (!child.isEventScope()) {
        result.add(child);
      }
    }

    return result;
  }

  public List<? extends PvmExecutionImpl> getEventScopeExecutions() {
    List<? extends PvmExecutionImpl> children = getExecutions();
    List<PvmExecutionImpl> result = new ArrayList<PvmExecutionImpl>();

    for (PvmExecutionImpl child : children) {
      if (child.isEventScope()) {
        result.add(child);
      }
    }

    return result;
  }

  @Override
  public PvmExecutionImpl findExecution(String activityId) {
    if ( (getActivity()!=null)
         && (getActivity().getId().equals(activityId))
       ) {
      return this;
    }
    for (PvmExecutionImpl nestedExecution : getExecutions()) {
      PvmExecutionImpl result = nestedExecution.findExecution(activityId);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public List<PvmExecution> findExecutions(String activityId) {
    List<PvmExecution> matchingExecutions = new ArrayList<PvmExecution>();
    collectExecutions(activityId, matchingExecutions);

    return matchingExecutions;
  }

  protected void collectExecutions(String activityId, List<PvmExecution> executions) {
    if ( (getActivity()!=null)
        && (getActivity().getId().equals(activityId))
      ) {
      executions.add(this);
    }

    for (PvmExecutionImpl nestedExecution : getExecutions()) {
      nestedExecution.collectExecutions(activityId, executions);
    }
  }

  @Override
  public List<String> findActiveActivityIds() {
    List<String> activeActivityIds = new ArrayList<String>();
    collectActiveActivityIds(activeActivityIds);
    return activeActivityIds;
  }

  protected void collectActiveActivityIds(List<String> activeActivityIds) {
    ActivityImpl activity = getActivity();
    if (isActive && activity!=null) {
      activeActivityIds.add(activity.getId());
    }

    for (PvmExecutionImpl execution: getExecutions()) {
      execution.collectActiveActivityIds(activeActivityIds);
    }
  }

  // business key /////////////////////////////////////////

  @Override
  public String getProcessBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }

  // process definition ///////////////////////////////////////////////////////

  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }

  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }

  // process instance /////////////////////////////////////////////////////////

  /** ensures initialization and returns the process instance. */
  @Override
  public abstract PvmExecutionImpl getProcessInstance();

  public abstract void setProcessInstance(PvmExecutionImpl pvmExecutionImpl);

  // case instance id /////////////////////////////////////////////////////////

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  // activity /////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the activity */
  @Override
  public ActivityImpl getActivity() {
    return activity;
  }

  public String getActivityId() {
    ActivityImpl activity = getActivity();
    if(activity != null) {
      return activity.getId();
    } else {
      return null;
    }
  }

  @Override
  public String getCurrentActivityName() {
    ActivityImpl activity = getActivity();
    if(activity != null) {
      return activity.getName();
    } else {
      return null;
    }
  }

  @Override
  public String getCurrentActivityId() {
    return getActivityId();
  }

  @Override
  public void setActivity(PvmActivity activity) {
    this.activity = (ActivityImpl) activity;
  }

  @Override
  public void enterActivityInstance() {
    ActivityImpl activity = getActivity();

    activityInstanceId = generateActivityInstanceId(activity.getId());

    if(log.isLoggable(Level.FINE)) {
      log.fine("[ENTER] "+this + ": "+activityInstanceId+", parent: "+getParentActivityInstanceId());
    }

    // <LEGACY>: in general, io mappings may only exist when the activity is scope
    // however, for multi instance activities, the inner activity does not become a scope
    // due to the presence of an io mapping. In that case, it is ok to execute the io mapping
    // anyway because the multi-instance body already ensures variable isolation
    executeIoMapping();

    if (activity.isScope()) {
      initializeTimerDeclarations();
    }

  }

  protected abstract String generateActivityInstanceId(String activityId);

  @Override
  public void leaveActivityInstance() {
    if(activityInstanceId != null) {

      if(log.isLoggable(Level.FINE)) {
        log.fine("[LEAVE] "+ this + ": "+activityInstanceId );
      }

    }
    activityInstanceId = getParentActivityInstanceId();

    activityInstanceState = ActivityInstanceState.DEFAULT.getStateCode();
  }

  @Override
  public String getParentActivityInstanceId() {
    if(isProcessInstanceExecution()) {
      return getId();

    } else {
      return getParent().getActivityInstanceId();
    }
  }

  /**
   * Returns the activity instance id of the of the scope the current execution belongs to
   *
   * Limitation: this does not return the correct activity instance id in case of a
   *   compensation handler execution that is the direct child of a compensation throwing execution
   *   (background: in this case, multiple parent executions have to be skipped to
   *   find the correct scope execution)
   */
  public String getScopeActivityInstanceId() {
    PvmExecutionImpl scopeExecution = isScope ? this : getParent();

    PvmActivity scopeActivity = scopeExecution.getActivity();
    if (scopeActivity != null && scopeActivity.isScope()
        && scopeExecution.getActivityInstanceId() != null
        && !CompensationBehavior.isCompensationThrowing(scopeExecution)
        && !CompensationBehavior.executesDefaultCompensationHandler(scopeExecution)) {
      // take the execution's activity instance id if
      //   * it is a leaf (scopeActivity != null)
      //   * it executes a scope activity (scopeActivity != null)
      //   * it actually executes the activity (activityInstanceId != null)
      //   * it cannot have child executions (i.e. no compensation throwing event)
      return scopeExecution.getActivityInstanceId();
    }
    else {
      return scopeExecution.getParentActivityInstanceId();
    }
  }

  public void forceUpdateActivityInstance() {
    activityInstanceId = generateActivityInstanceId(getActivity().getId());
  }

  @Override
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  @Override
  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  // parent ///////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the parent */
  @Override
  public abstract PvmExecutionImpl getParent();

  @Override
  public String getParentId() {
    PvmExecutionImpl parent = getParent();
    if(parent != null) {
      return parent.getId();
    } else {
      return null;
    }
  }

  public boolean hasChildren() {
    return !getExecutions().isEmpty();
  }

  /**
   * Sets the execution's parent and updates the old and new parents' set of
   * child executions
   */
  @SuppressWarnings("unchecked")
  public void setParent(PvmExecutionImpl parent) {
    PvmExecutionImpl currentParent = getParent();

    setParentExecution(parent);

    if (currentParent != null) {
      currentParent.getExecutions().remove(this);
    }

    if (parent != null) {
      ((List<PvmExecutionImpl>) parent.getExecutions()).add(this);
    }
  }

  /**
   * Use #setParent to also update the child execution sets
   */
  public abstract void setParentExecution(PvmExecutionImpl parent);

  // super- and subprocess executions /////////////////////////////////////////

  @Override
  public abstract PvmExecutionImpl getSuperExecution();

  public abstract void setSuperExecution(PvmExecutionImpl superExecution);

  public abstract PvmExecutionImpl getSubProcessInstance();

  public abstract void setSubProcessInstance(PvmExecutionImpl subProcessInstance);

  // super case execution /////////////////////////////////////////////////////

  public abstract CmmnExecution getSuperCaseExecution();

  public abstract void setSuperCaseExecution(CmmnExecution superCaseExecution);

  // sub case execution ///////////////////////////////////////////////////////

  public abstract CmmnExecution getSubCaseInstance();

  public abstract void setSubCaseInstance(CmmnExecution subCaseInstance);

  // scopes ///////////////////////////////////////////////////////////////////

  protected ScopeImpl getScopeActivity() {
    ScopeImpl scope = null;
    // this if condition is important during process instance startup
    // where the activity of the process instance execution may not be aligned
    // with the execution tree
    if (isProcessInstanceExecution()) {
      scope = getProcessDefinition();
    } else {
      scope = getActivity();
    }
    return scope;
  }

  @Override
  public boolean isScope() {
    return isScope;
  }

  @Override
  public void setScope(boolean isScope) {
    this.isScope = isScope;
  }


  /**
   * For a given target flow scope, this method returns the corresponding scope execution.
   *
   * Precondition: the execution is active and executing an activity.
   * Can be invoked for scope and non scope executions.
   *
   * @param targetFlowScope scope activity or process definition for which the scope execution should be found
   * @return the scope execution for the provided targetFlowScope
   */
  @Override
  public PvmExecutionImpl findExecutionForFlowScope(PvmScope targetFlowScope) {
    // if this execution is not a scope execution, use the parent
    final PvmExecutionImpl scopeExecution = isScope() ? this : getParent();

    ScopeImpl currentActivity = getActivity();
    EnsureUtil.ensureNotNull("activity of current execution", currentActivity);

    // if this is a scope execution currently executing a non scope activity
    currentActivity = currentActivity.isScope() ? currentActivity : currentActivity.getFlowScope();

    return scopeExecution.findExecutionForScope(currentActivity, (ScopeImpl) targetFlowScope);
  }


  public PvmExecutionImpl findExecutionForScope(ScopeImpl currentScope, ScopeImpl targetScope) {

    if(!targetScope.isScope()) {
      throw new ProcessEngineException("Target scope must be a scope.");
    }

    Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping = createActivityExecutionMapping(currentScope);
    PvmExecutionImpl scopeExecution = activityExecutionMapping.get(targetScope);
    if(scopeExecution == null){
      // the target scope is scope but no corresponding execution was found
      // => legacy behavior
      scopeExecution = LegacyBehavior.getScopeExecution(targetScope, activityExecutionMapping);
    }
    return scopeExecution;
  }

  @Override
  public Map<ScopeImpl, PvmExecutionImpl> createActivityExecutionMapping() {
    ScopeImpl currentActivity = getActivity();
    EnsureUtil.ensureNotNull("activity of current execution", currentActivity);

    if (!currentActivity.isScope() || activityInstanceId == null || (currentActivity.isScope() && !isScope())) {
      // if
      // - this is a scope execution currently executing a non scope activity
      // - or it is not scope but the current activity is (e.g. can happen during activity end, when the actual
      //   scope execution has been removed and the concurrent parent has been set to the scope activity)
      // - or it is asyncBefore/asyncAfter

      currentActivity = currentActivity.getFlowScope();
    }

    PvmExecutionImpl scopeExecution = getFlowScopeExecution();
    return scopeExecution.createActivityExecutionMapping(currentActivity);
  }

  protected PvmExecutionImpl getFlowScopeExecution() {
    if (!isScope || CompensationBehavior.executesNonScopeCompensationHandler(this)) {
      // recursion is necessary since there may be more than one concurrent execution in the presence of compensating executions
      // that compensate non-scope activities contained in the same flow scope as the throwing compensation event
      return getParent().getFlowScopeExecution();
    }
    else {
      return this;
    }
  }

  public Map<ScopeImpl, PvmExecutionImpl> createActivityExecutionMapping(ScopeImpl currentScope) {
    if(!isScope()) {
      throw new ProcessEngineException("Execution must be a scope execution");
    }
    if(!currentScope.isScope()) {
      throw new ProcessEngineException("Current scope must be a scope.");
    }

    ActivityAwareScopeExecutionCollector scopeExecutionCollector = new ActivityAwareScopeExecutionCollector(currentScope);
    new ExecutionWalker(this)
      .addPreVisitor(scopeExecutionCollector)
      .walkUntil();
    List<PvmExecutionImpl> scopeExecutions = scopeExecutionCollector.getExecutions();

    ScopeCollector scopeCollector = new ScopeCollector();
    new FlowScopeWalker(currentScope)
      .addPreVisitor(scopeCollector)
      .walkUntil();

    List<ScopeImpl> scopes = scopeCollector.getScopes();
    if(scopes.size() == scopeExecutions.size()) {
      // the trees are in sync
      Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping = new HashMap<ScopeImpl, PvmExecutionImpl>();
      for(int i = 0; i<scopes.size(); i++) {
        activityExecutionMapping.put(scopes.get(i), scopeExecutions.get(i));
      }
      return activityExecutionMapping;
    }
    else {
      // Wounderful! The trees are out of sync. This is due to legacy behavior
      return LegacyBehavior.createActivityExecutionMapping(scopeExecutions, scopes);
    }
  }

  // toString /////////////////////////////////////////////////////////////////

  @Override
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

  // variables ////////////////////////////////////////////

  @Override
  public String getVariableScopeKey() {
    return "execution";
  }

  @Override
  public AbstractVariableScope getParentVariableScope() {
    return getParent();
  }

  // sequence counter ///////////////////////////////////////////////////////////

  public long getSequenceCounter() {
    return sequenceCounter;
  }

  public void setSequenceCounter(long sequenceCounter) {
    this.sequenceCounter = sequenceCounter;
  }

  public void incrementSequenceCounter() {
    sequenceCounter++;
  }

  // Getter / Setters ///////////////////////////////////

  public String getDeleteReason() {
    return deleteReason;
  }

  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }

  public boolean isDeleteRoot() {
    return deleteRoot;
  }

  @Override
  public TransitionImpl getTransition() {
    return transition;
  }

  public List<PvmTransition> getTransitionsToTake() {
    return transitionsToTake;
  }

  public void setTransitionsToTake(List<PvmTransition> transitionsToTake) {
    this.transitionsToTake = transitionsToTake;
  }

  @Override
  public String getCurrentTransitionId() {
    TransitionImpl transition = getTransition();
    if(transition != null) {
      return transition.getId();
    } else {
      return null;
    }
  }

  public void setTransition(PvmTransition transition) {
    this.transition = (TransitionImpl) transition;
  }

  @Override
  public boolean isConcurrent() {
    return isConcurrent;
  }

  @Override
  public void setConcurrent(boolean isConcurrent) {
    this.isConcurrent = isConcurrent;
  }

  @Override
  public boolean isActive() {
    return isActive;
  }

  @Override
  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  public void setEnded(boolean isEnded) {
    this.isEnded = isEnded;
  }

  @Override
  public boolean isEnded() {
    return isEnded;
  }

  @Override
  public boolean isCanceled() {
    return ActivityInstanceState.CANCELED.getStateCode() == activityInstanceState;
  }

  public void setCanceled(boolean canceled) {
    if (canceled) {
      activityInstanceState = ActivityInstanceState.CANCELED.getStateCode();
    }
  }

  @Override
  public boolean isCompleteScope() {
    return ActivityInstanceState.SCOPE_COMPLETE.getStateCode() == activityInstanceState;
  }

  public void setCompleteScope(boolean completeScope) {
    if (completeScope) {
      activityInstanceState = ActivityInstanceState.SCOPE_COMPLETE.getStateCode();
    }
  }

  public void setPreserveScope(boolean preserveScope) {
    this.preserveScope = preserveScope;
  }

  public boolean isPreserveScope() {
    return preserveScope;
  }

  public int getActivityInstanceState() {
    return activityInstanceState;
  }

  public boolean isEventScope() {
    return isEventScope;
  }

  public void setEventScope(boolean isEventScope) {
    this.isEventScope = isEventScope;
  }

  public ExecutionStartContext getExecutionStartContext() {
    return startContext;
  }

  public void disposeProcessInstanceStartContext() {
    startContext = null;
  }

  public void disposeExecutionStartContext() {
    startContext = null;
  }

  @Override
  public PvmActivity getNextActivity() {
    return nextActivity;
  }

  @Override
  public boolean isProcessInstanceExecution() {
    return getParent() == null;
  }

  public ProcessInstanceStartContext getProcessInstanceStartContext() {
    if (startContext != null && startContext instanceof ProcessInstanceStartContext) {
      return (ProcessInstanceStartContext) startContext;
    }
    return null;
  }

  public boolean hasProcessInstanceStartContext() {
    return startContext != null && startContext instanceof ProcessInstanceStartContext;
  }

  public void setStartContext(ExecutionStartContext startContext) {
    this.startContext = startContext;
  }

  public void setNextActivity(PvmActivity nextActivity) {
    this.nextActivity = nextActivity;
  }

  public PvmExecutionImpl getParentScopeExecution(boolean considerSuperExecution) {
    if(isProcessInstanceExecution()) {
      if(considerSuperExecution && getSuperExecution() != null) {
        PvmExecutionImpl superExecution = getSuperExecution();
        if(superExecution.isScope()) {
          return superExecution;
        }
        else {
          return superExecution.getParent();
        }
      }
      else {
        return null;
      }
    }
    else {
      PvmExecutionImpl parent = getParent();
      if(parent.isScope()) {
        return parent;
      }
      else {
        return parent.getParent();
      }
    }
  }
}
