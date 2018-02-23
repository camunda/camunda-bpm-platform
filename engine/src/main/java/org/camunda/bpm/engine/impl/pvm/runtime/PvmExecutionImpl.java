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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.incident.DefaultIncidentHandler;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.incident.IncidentHandler;
import org.camunda.bpm.engine.impl.persistence.entity.DelayedVariableEvent;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.impl.pvm.*;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ModificationObserverBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.*;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.FoxAtomicOperationDeleteCascadeFireActivityEnd;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;
import org.camunda.bpm.engine.impl.tree.*;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.runtime.Incident;

import java.util.*;

import static org.camunda.bpm.engine.impl.bpmn.helper.CompensationUtil.SIGNAL_COMPENSATION_DONE;
import static org.camunda.bpm.engine.impl.pvm.runtime.ActivityInstanceState.ENDING;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 */
public abstract class PvmExecutionImpl extends CoreExecution implements ActivityExecution, PvmProcessInstance {

  private static final long serialVersionUID = 1L;

  private static final PvmLogger LOG = ProcessEngineLogger.PVM_LOGGER;

  protected transient ProcessDefinitionImpl processDefinition;

  protected transient ExecutionStartContext startContext;

  // current position /////////////////////////////////////////////////////////

  /**
   * current activity
   */
  protected transient ActivityImpl activity;

  /**
   * the activity which is to be started next
   */
  protected transient PvmActivity nextActivity;

  /**
   * the transition that is currently being taken
   */
  protected transient TransitionImpl transition;

  /**
   * A list of outgoing transitions from the current activity
   * that are going to be taken
   */
  protected transient List<PvmTransition> transitionsToTake = null;

  /**
   * the unique id of the current activity instance
   */
  protected String activityInstanceId;

  /**
   * the id of a case associated with this execution
   */
  protected String caseInstanceId;

  protected PvmExecutionImpl replacedBy;

  // cascade deletion ////////////////////////////////////////////////////////

  protected boolean deleteRoot;
  protected String deleteReason;
  protected boolean externallyTerminated;

  //state/type of execution //////////////////////////////////////////////////

  /**
   * indicates if this execution represents an active path of execution.
   * Executions are made inactive in the following situations:
   * <ul>
   * <li>an execution enters a nested scope</li>
   * <li>an execution is split up into multiple concurrent executions, then the parent is made inactive.</li>
   * <li>an execution has arrived in a parallel gateway or join and that join has not yet activated/fired.</li>
   * <li>an execution is ended.</li>
   * </ul>
   */
  protected boolean isActive = true;
  protected boolean isScope = true;
  protected boolean isConcurrent = false;
  protected boolean isEnded = false;
  protected boolean isEventScope = false;

  /**
   * transient; used for process instance modification to preserve a scope from getting deleted
   */
  protected boolean preserveScope = false;

  /**
   * marks the current activity instance
   */
  protected int activityInstanceState = ActivityInstanceState.DEFAULT.getStateCode();

  // sequence counter ////////////////////////////////////////////////////////
  protected long sequenceCounter = 0;

  public PvmExecutionImpl() {
  }

  // API ////////////////////////////////////////////////

  /**
   * creates a new execution. properties processDefinition, processInstance and activity will be initialized.
   */
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

    if (businessKey != null) {
      subProcessInstance.setBusinessKey(businessKey);
    }

    if (caseInstanceId != null) {
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

    if (variables != null) {
      setVariables(variables);
    }

    fireHistoricProcessStartEvent();

    performOperation(PvmAtomicOperation.PROCESS_START);
  }

  /**
   * perform starting behavior but don't execute the initial activity
   *
   * @param variables the variables which are used for the start
   */
  public void startWithoutExecuting(Map<String, Object> variables) {
    initialize();
    initializeTimerDeclarations();
    fireHistoricProcessStartEvent();
    performOperation(PvmAtomicOperation.FIRE_PROCESS_START);

    setActivity(null);
    setActivityInstanceId(getId());

    // set variables
    setVariables(variables);
  }

  public abstract void fireHistoricProcessStartEvent();

  @Override
  public void destroy() {
    LOG.destroying(this);
    setScope(false);
  }

  public void removeAllTasks() {
  }

  protected void removeEventScopes() {
    List<PvmExecutionImpl> childExecutions = new ArrayList<PvmExecutionImpl>(getEventScopeExecutions());
    for (PvmExecutionImpl childExecution : childExecutions) {
      LOG.removingEventScope(childExecution);
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
      if (childExecution.getSubProcessInstance() != null) {
        childExecution.getSubProcessInstance().deleteCascade(reason, skipCustomListeners, skipIoMappings);
      }
      childExecution.deleteCascade(reason, skipCustomListeners, skipIoMappings);
    }

    // fire activity end on active activity
    PvmActivity activity = getActivity();
    if (isActive && activity != null) {
      // set activity instance state to cancel
      if (activityInstanceState != ENDING.getStateCode()) {
        setCanceled(true);
        performOperation(PvmAtomicOperation.FIRE_ACTIVITY_END);
      }
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
    LOG.interruptingExecution(reason, skipCustomListeners);

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
    performOperation(PvmAtomicOperation.FIRE_ACTIVITY_END);
    remove();

    PvmExecutionImpl parent = getParent();

    if (parent.getActivity() == null) {
      parent.setActivity((PvmActivity) getActivity().getFlowScope());
    }

    parent.signal(SIGNAL_COMPENSATION_DONE, null);
  }

  /**
   * <p>Precondition: execution is already ended but this has not been propagated yet.</p>
   * <p>
   * <p>Propagates the ending of this execution to the flowscope execution; currently only supports
   * the process instance execution</p>
   */
  public void propagateEnd() {
    if (!isEnded()) {
      throw new ProcessEngineException(toString() + " must have ended before ending can be propagated");
    }

    if (isProcessInstanceExecution()) {
      performOperation(PvmAtomicOperation.PROCESS_END);
    } else {
      // not supported yet
    }
  }

  @Override
  public void remove() {
    PvmExecutionImpl parent = getParent();
    if (parent != null) {
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

    // whenever we change the set of child executions we have to force an update
    // on the scope executions to avoid concurrent modifications (e.g. tree compaction)
    // that go unnoticed
    forceUpdate();

    if (children.isEmpty()) {
      // (1)
      PvmExecutionImpl replacingExecution = this.createExecution();
      replacingExecution.setConcurrent(true);
      replacingExecution.setScope(false);
      replacingExecution.replace(this);
      this.inactivate();
      this.setActivity(null);

    } else if (children.size() == 1) {
      // (2)
      PvmExecutionImpl child = children.get(0);

      PvmExecutionImpl concurrentReplacingExecution = this.createExecution();
      concurrentReplacingExecution.setConcurrent(true);
      concurrentReplacingExecution.setScope(false);
      concurrentReplacingExecution.setActive(false);
      concurrentReplacingExecution.onConcurrentExpand(this);
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
    deleteCascade(deleteReason, skipCustomListeners, skipIoMappings, false, false);
  }

  public void deleteCascade(String deleteReason, boolean skipCustomListeners, boolean skipIoMappings, boolean externallyTerminated, boolean skipSubprocesses) {
    this.deleteReason = deleteReason;
    setDeleteRoot(true);
    this.isEnded = true;
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMapping = skipIoMappings;
    this.externallyTerminated = externallyTerminated;
    this.skipSubprocesses = skipSubprocesses;
    performOperation(PvmAtomicOperation.DELETE_CASCADE);
  }

  public void deleteCascade2(String deleteReason) {
    this.deleteReason = deleteReason;
    setDeleteRoot(true);
    performOperation(new FoxAtomicOperationDeleteCascadeFireActivityEnd());
  }

  public void executeEventHandlerActivity(ActivityImpl eventHandlerActivity) {

    // the target scope
    ScopeImpl flowScope = eventHandlerActivity.getFlowScope();

    // the event scope (the current activity)
    ScopeImpl eventScope = eventHandlerActivity.getEventScope();

    if (eventHandlerActivity.getActivityStartBehavior() == ActivityStartBehavior.CONCURRENT_IN_FLOW_SCOPE
      && flowScope != eventScope) {
      // the current scope is the event scope of the activity
      findExecutionForScope(eventScope, flowScope)
        .executeActivity(eventHandlerActivity);
    } else {
      executeActivity(eventHandlerActivity);
    }
  }

  // tree compaction & expansion ///////////////////////////////////////////

  /**
   * <p>Returns an execution that has replaced this execution for executing activities in their shared scope.</p>
   * <p>Invariant: this execution and getReplacedBy() execute in the same scope.</p>
   */
  public abstract PvmExecutionImpl getReplacedBy();

  /**
   * Instead of {@link #getReplacedBy()}, which returns the execution that this execution was directly replaced with,
   * this resolves the chain of replacements (i.e. in the case the replacedBy execution itself was replaced again)
   */
  public PvmExecutionImpl resolveReplacedBy() {
    // follow the links of execution replacement;
    // note: this can be at most two hops:
    // case 1:
    //   this execution is a scope execution
    //     => tree may have expanded meanwhile
    //     => scope execution references replacing execution directly (one hop)
    //
    // case 2:
    //   this execution is a concurrent execution
    //     => tree may have compacted meanwhile
    //     => concurrent execution references scope execution directly (one hop)
    //
    // case 3:
    //   this execution is a concurrent execution
    //     => tree may have compacted/expanded/compacted/../expanded any number of times
    //     => the concurrent execution has been removed and therefore references the scope execution (first hop)
    //     => the scope execution may have been replaced itself again with another concurrent execution (second hop)
    //   note that the scope execution may have a long "history" of replacements, but only the last replacement is relevant here
    PvmExecutionImpl replacingExecution = getReplacedBy();

    if (replacingExecution != null) {
      PvmExecutionImpl secondHopReplacingExecution = replacingExecution.getReplacedBy();
      if (secondHopReplacingExecution != null) {
        replacingExecution = secondHopReplacingExecution;
      }
    }

    return replacingExecution;
  }

  public boolean hasReplacedParent() {
    return getParent() != null && getParent().getReplacedBy() == this;
  }

  public boolean isReplacedByParent() {
    return getReplacedBy() != null && getReplacedBy() == this.getParent();
  }

  /**
   * <p>Replace an execution by this execution. The replaced execution has a pointer ({@link #getReplacedBy()}) to this execution.
   * This pointer is maintained until the replaced execution is removed or this execution is removed/ended.</p>
   * <p>
   * <p>This is used for two cases: Execution tree expansion and execution tree compaction</p>
   * <ul>
   * <li><b>expansion</b>: Before:
   * <pre>
   *       -------
   *       |  e1 |  scope
   *       -------
   *     </pre>
   * After:
   * <pre>
   *       -------
   *       |  e1 |  scope
   *       -------
   *          |
   *       -------
   *       |  e2 |  cc (no scope)
   *       -------
   *     </pre>
   * e2 replaces e1: it should receive all entities associated with the activity currently executed
   * by e1; these are tasks, (local) variables, jobs (specific for the activity, not the scope)
   * </li>
   * <li><b>compaction</b>: Before:
   * <pre>
   *       -------
   *       |  e1 |  scope
   *       -------
   *          |
   *       -------
   *       |  e2 |  cc (no scope)
   *       -------
   *     </pre>
   * After:
   * <pre>
   *       -------
   *       |  e1 |  scope
   *       -------
   *     </pre>
   * e1 replaces e2: it should receive all entities associated with the activity currently executed
   * by e2; these are tasks, (all) variables, all jobs
   * </li>
   * </ul>
   *
   * @see #createConcurrentExecution()
   * @see #tryPruneLastConcurrentChild()
   */
  public void replace(PvmExecutionImpl execution) {
    // activity instance id handling
    this.activityInstanceId = execution.getActivityInstanceId();
    this.isActive = execution.isActive;
    this.deleteRoot = execution.deleteRoot;

    this.replacedBy = null;
    execution.replacedBy = this;

    this.transitionsToTake = execution.transitionsToTake;

    execution.leaveActivityInstance();
  }

  /**
   * Callback on tree expansion when this execution is used as the concurrent execution
   * where the argument's children become a subordinate to. Note that this case is not the inverse
   * of replace because replace has the semantics that the replacing execution can be used to continue
   * execution of this execution's activity instance.
   */
  public void onConcurrentExpand(PvmExecutionImpl scopeExecution) {
    // by default, do nothing
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
      throw new PvmException("couldn't process signal '" + signalName + "' on activity '" + activity.getId() + "': " + e.getMessage(), e);
    }
  }

  public void take() {
    if (this.transition == null) {
      throw new PvmException(toString() + ": no transition to take specified");
    }
    if (transition == null) {
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
   * <p>
   * First, the ActivityStartBehavior is evaluated.
   * In case the start behavior is not {@link ActivityStartBehavior#DEFAULT}, the corresponding start
   * behavior is executed before executing the activity.
   * <p>
   * For a given activity, the execution on which this method must be called depends on the type of the start behavior:
   * <ul>
   * <li>CONCURRENT_IN_FLOW_SCOPE: scope execution for {@link PvmActivity#getFlowScope()}</li>
   * <li>INTERRUPT_EVENT_SCOPE: scope execution for {@link PvmActivity#getEventScope()}</li>
   * <li>CANCEL_EVENT_SCOPE: scope execution for {@link PvmActivity#getEventScope()}</li>
   * </ul>
   *
   * @param activity the activity to start
   */
  @Override
  public void executeActivity(PvmActivity activity) {
    if (!activity.getIncomingTransitions().isEmpty()) {
      throw new ProcessEngineException("Activity is contained in normal flow and cannot be executed using executeActivity().");
    }

    ActivityStartBehavior activityStartBehavior = activity.getActivityStartBehavior();
    if (!isScope() && ActivityStartBehavior.DEFAULT != activityStartBehavior) {
      throw new ProcessEngineException("Activity '" + activity + "' with start behavior '" + activityStartBehavior + "'"
        + "cannot be executed by non-scope execution.");
    }

    PvmActivity activityImpl = activity;
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


    ScopeImpl flowScope = null;
    if (!activityStack.isEmpty()) {
      flowScope = activityStack.get(0).getFlowScope();
    } else if (targetActivity != null) {
      flowScope = targetActivity.getFlowScope();
    } else if (targetTransition != null) {
      flowScope = targetTransition.getSource().getFlowScope();
    }

    PvmExecutionImpl propagatingExecution = null;
    if (flowScope.getActivityBehavior() instanceof ModificationObserverBehavior) {
      ModificationObserverBehavior flowScopeBehavior = (ModificationObserverBehavior) flowScope.getActivityBehavior();
      propagatingExecution = (PvmExecutionImpl) flowScopeBehavior.createInnerInstance(this);
    } else {
      propagatingExecution = createConcurrentExecution();
    }

    propagatingExecution.executeActivities(activityStack, targetActivity, targetTransition, variables, localVariables,
      skipCustomListeners, skipIoMappings);
  }

  /**
   * Instantiates the given set of activities and returns the execution for the bottom-most activity
   */
  public Map<PvmActivity, PvmExecutionImpl> instantiateScopes(List<PvmActivity> activityStack,
                                                              boolean skipCustomListeners,
                                                              boolean skipIoMappings) {

    if (activityStack.isEmpty()) {
      return Collections.emptyMap();
    }

    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMapping = skipIoMappings;

    ExecutionStartContext executionStartContext = new ExecutionStartContext(false);

    InstantiationStack instantiationStack = new InstantiationStack(new LinkedList<PvmActivity>(activityStack));
    executionStartContext.setInstantiationStack(instantiationStack);
    setStartContext(executionStartContext);

    performOperation(PvmAtomicOperation.ACTIVITY_INIT_STACK_AND_RETURN);

    Map<PvmActivity, PvmExecutionImpl> createdExecutions = new HashMap<PvmActivity, PvmExecutionImpl>();

    PvmExecutionImpl currentExecution = this;
    for (PvmActivity instantiatedActivity : activityStack) {
      // there must exactly one child execution
      currentExecution = currentExecution.getNonEventScopeExecutions().get(0);
      if (currentExecution.isConcurrent()) {
        // there may be a non-scope execution that we have to skip (e.g. multi-instance)
        currentExecution = currentExecution.getNonEventScopeExecutions().get(0);
      }

      createdExecutions.put(instantiatedActivity, currentExecution);
    }

    return createdExecutions;
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

    } else if (targetActivity != null) {
      setVariables(variables);
      setVariablesLocal(localVariables);
      setActivity(targetActivity);
      performOperation(PvmAtomicOperation.ACTIVITY_START_CREATE_SCOPE);

    } else if (targetTransition != null) {
      setVariables(variables);
      setVariablesLocal(localVariables);
      setActivity(targetTransition.getSource());
      setTransition(targetTransition);
      performOperation(PvmAtomicOperation.TRANSITION_START_NOTIFY_LISTENER_TAKE);
    }
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public List<ActivityExecution> findInactiveConcurrentExecutions(PvmActivity activity) {
    List<PvmExecutionImpl> inactiveConcurrentExecutionsInActivity = new ArrayList<PvmExecutionImpl>();
    if (isConcurrent()) {
      return getParent().findInactiveChildExecutions(activity);
    } else if (!isActive()) {
      inactiveConcurrentExecutionsInActivity.add(this);
    }

    return (List) inactiveConcurrentExecutionsInActivity;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public List<ActivityExecution> findInactiveChildExecutions(PvmActivity activity) {
    List<PvmExecutionImpl> inactiveConcurrentExecutionsInActivity = new ArrayList<PvmExecutionImpl>();
    List<? extends PvmExecutionImpl> concurrentExecutions = getAllChildExecutions();
    for (PvmExecutionImpl concurrentExecution : concurrentExecutions) {
      if (concurrentExecution.getActivity() == activity && !concurrentExecution.isActive()) {
        inactiveConcurrentExecutionsInActivity.add(concurrentExecution);
      }
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

    // if recyclable executions size is greater
    // than 1, then the executions are joined and
    // the activity is left with 'this' execution,
    // if it is not not the last concurrent execution.
    // therefore it is necessary to remove the local
    // variables (event if it is the last concurrent
    // execution).
    if (recyclableExecutions.size() > 1) {
      removeVariablesLocalInternal();
    }

    // mark all recyclable executions as ended
    // if the list of recyclable executions also
    // contains 'this' execution, then 'this' execution
    // is also marked as ended. (if 'this' execution is
    // pruned, then the local variables are not copied
    // to the parent execution)
    // this is a workaround to not delete all recyclable
    // executions and create a new execution which leaves
    // the activity.
    for (ActivityExecution execution : recyclableExecutions) {
      execution.setEnded(true);
    }

    // remove 'this' from recyclable executions to
    // leave the activity with 'this' execution
    // (when 'this' execution is the last concurrent
    // execution, then 'this' execution will be pruned,
    // and the activity is left with the scope
    // execution)
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
    } else {
      propagatingExecution.setTransitionsToTake(_transitions);
      propagatingExecution.performOperation(PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_END);
    }
  }

  protected abstract void removeVariablesLocalInternal();

  public boolean isActive(String activityId) {
    return findExecution(activityId) != null;
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
    if ((getActivity() != null)
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
    if ((getActivity() != null)
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
    if (isActive && activity != null) {
      activeActivityIds.add(activity.getId());
    }

    for (PvmExecutionImpl execution : getExecutions()) {
      execution.collectActiveActivityIds(activeActivityIds);
    }
  }

  // business key /////////////////////////////////////////

  @Override
  public String getProcessBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }

  @Override
  public String getBusinessKey() {
    if (this.isProcessInstanceExecution()) {
      return businessKey;
    } else return getProcessBusinessKey();
  }

  // process definition ///////////////////////////////////////////////////////

  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }

  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }

  // process instance /////////////////////////////////////////////////////////

  /**
   * ensures initialization and returns the process instance.
   */
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

  /**
   * ensures initialization and returns the activity
   */
  @Override
  public ActivityImpl getActivity() {
    return activity;
  }

  public String getActivityId() {
    ActivityImpl activity = getActivity();
    if (activity != null) {
      return activity.getId();
    } else {
      return null;
    }
  }

  @Override
  public String getCurrentActivityName() {
    ActivityImpl activity = getActivity();
    if (activity != null) {
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

    LOG.debugEnterActivityInstance(this, getParentActivityInstanceId());

    // <LEGACY>: in general, io mappings may only exist when the activity is scope
    // however, for multi instance activities, the inner activity does not become a scope
    // due to the presence of an io mapping. In that case, it is ok to execute the io mapping
    // anyway because the multi-instance body already ensures variable isolation
    executeIoMapping();

    if (activity.isScope()) {
      initializeTimerDeclarations();
    }

  }

  public void activityInstanceStarting() {
    this.activityInstanceState = ActivityInstanceState.STARTING.getStateCode();
  }


  public void activityInstanceStarted() {
    this.activityInstanceState = ActivityInstanceState.DEFAULT.getStateCode();
  }

  public void activityInstanceDone() {
    this.activityInstanceState = ENDING.getStateCode();
  }

  protected abstract String generateActivityInstanceId(String activityId);

  @Override
  public void leaveActivityInstance() {
    if (activityInstanceId != null) {
      LOG.debugLeavesActivityInstance(this, activityInstanceId);
    }
    activityInstanceId = getParentActivityInstanceId();

    activityInstanceState = ActivityInstanceState.DEFAULT.getStateCode();
  }

  @Override
  public String getParentActivityInstanceId() {
    if (isProcessInstanceExecution()) {
      return getId();

    } else {
      return getParent().getActivityInstanceId();
    }
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

  /**
   * ensures initialization and returns the parent
   */
  @Override
  public abstract PvmExecutionImpl getParent();

  @Override
  public String getParentId() {
    PvmExecutionImpl parent = getParent();
    if (parent != null) {
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
   * <p>
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

    if (!targetScope.isScope()) {
      throw new ProcessEngineException("Target scope must be a scope.");
    }

    Map<ScopeImpl, PvmExecutionImpl> activityExecutionMapping = createActivityExecutionMapping(currentScope);
    PvmExecutionImpl scopeExecution = activityExecutionMapping.get(targetScope);
    if (scopeExecution == null) {
      // the target scope is scope but no corresponding execution was found
      // => legacy behavior
      scopeExecution = LegacyBehavior.getScopeExecution(targetScope, activityExecutionMapping);
    }
    return scopeExecution;
  }

  public Map<ScopeImpl, PvmExecutionImpl> createActivityExecutionMapping(ScopeImpl currentScope) {
    if (!isScope()) {
      throw new ProcessEngineException("Execution must be a scope execution");
    }
    if (!currentScope.isScope()) {
      throw new ProcessEngineException("Current scope must be a scope.");
    }

    // A single path in the execution tree from a leaf (no child executions) to the root
    // may in fact contain multiple executions that correspond to leaves in the activity instance hierarchy.
    //
    // This is because compensation throwing executions have child executions. In that case, the
    // flow scope hierarchy is not aligned with the scope execution hierarchy: There is a scope
    // execution for a compensation-throwing event that is an ancestor of this execution,
    // while these events are not ancestor scopes of currentScope.
    //
    // The strategy to deal with this situation is as follows:
    // 1. Determine all executions that correspond to leaf activity instances
    // 2. Order the leaf executions in top-to-bottom fashion
    // 3. Iteratively build the activity execution mapping based on the leaves in top-to-bottom order
    //    3.1. For the first leaf, create the activity execution mapping regularly
    //    3.2. For every following leaf, rebuild the mapping but reuse any scopes and scope executions
    //         that are part of the mapping created in the previous iteration
    //
    // This process ensures that the resulting mapping does not contain scopes that are not ancestors
    // of currentScope and that it does not contain scope executions for such scopes.
    // For any execution hierarchy that does not involve compensation, the number of iterations in step 3
    // should be 1, i.e. there are no other leaf activity instance executions in the hierarchy.

    // 1. Find leaf activity instance executions
    LeafActivityInstanceExecutionCollector leafCollector = new LeafActivityInstanceExecutionCollector();
    new ExecutionWalker(this).addPreVisitor(leafCollector).walkUntil();

    List<PvmExecutionImpl> leaves = leafCollector.getLeaves();
    leaves.remove(this);

    // 2. Order them from top to bottom
    Collections.reverse(leaves);

    // 3. Iteratively extend the mapping for every additional leaf
    Map<ScopeImpl, PvmExecutionImpl> mapping = new HashMap<ScopeImpl, PvmExecutionImpl>();
    for (PvmExecutionImpl leaf : leaves) {
      ScopeImpl leafFlowScope = leaf.getFlowScope();
      PvmExecutionImpl leafFlowScopeExecution = leaf.getFlowScopeExecution();

      mapping = leafFlowScopeExecution.createActivityExecutionMapping(leafFlowScope, mapping);
    }

    // finally extend the mapping for the current execution
    // (note that the current execution need not be a leaf itself)
    mapping = this.createActivityExecutionMapping(currentScope, mapping);

    return mapping;
  }

  @Override
  public Map<ScopeImpl, PvmExecutionImpl> createActivityExecutionMapping() {
    ScopeImpl currentActivity = getActivity();
    EnsureUtil.ensureNotNull("activity of current execution", currentActivity);

    ScopeImpl flowScope = getFlowScope();
    PvmExecutionImpl flowScopeExecution = getFlowScopeExecution();

    return flowScopeExecution.createActivityExecutionMapping(flowScope);
  }

  protected PvmExecutionImpl getFlowScopeExecution() {
    if (!isScope || CompensationBehavior.executesNonScopeCompensationHandler(this)) {
      // LEGACY: a correct implementation should also skip a compensation-throwing parent scope execution
      // (since compensation throwing activities are scopes), but this cannot be done for backwards compatibility
      // where a compensation throwing activity was no scope (and we would wrongly skip an execution in that case)
      return getParent().getFlowScopeExecution();

    } else {
      return this;
    }
  }

  protected ScopeImpl getFlowScope() {
    ActivityImpl activity = getActivity();

    if (!activity.isScope() || activityInstanceId == null
      || (activity.isScope() && !isScope() && activity.getActivityBehavior() instanceof CompositeActivityBehavior)) {
      // if
      // - this is a scope execution currently executing a non scope activity
      // - or it is not scope but the current activity is (e.g. can happen during activity end, when the actual
      //   scope execution has been removed and the concurrent parent has been set to the scope activity)
      // - or it is asyncBefore/asyncAfter

      return activity.getFlowScope();
    } else {
      return activity;
    }
  }

  /**
   * Creates an extended mapping based on this execution and the given existing mapping.
   * Any entry <code>mapping</code> in mapping that corresponds to an ancestor scope of
   * <code>currentScope</code> is reused.
   */
  protected Map<ScopeImpl, PvmExecutionImpl> createActivityExecutionMapping(ScopeImpl currentScope,
                                                                            final Map<ScopeImpl, PvmExecutionImpl> mapping) {
    if (!isScope()) {
      throw new ProcessEngineException("Execution must be a scope execution");
    }
    if (!currentScope.isScope()) {
      throw new ProcessEngineException("Current scope must be a scope.");
    }

    // collect all ancestor scope executions unless one is encountered that is already in "mapping"
    ScopeExecutionCollector scopeExecutionCollector = new ScopeExecutionCollector();
    new ExecutionWalker(this)
      .addPreVisitor(scopeExecutionCollector)
      .walkWhile(new ReferenceWalker.WalkCondition<PvmExecutionImpl>() {
        public boolean isFulfilled(PvmExecutionImpl element) {
          return element == null || mapping.containsValue(element);
        }
      });
    final List<PvmExecutionImpl> scopeExecutions = scopeExecutionCollector.getScopeExecutions();

    // collect all ancestor scopes unless one is encountered that is already in "mapping"
    ScopeCollector scopeCollector = new ScopeCollector();
    new FlowScopeWalker(currentScope)
      .addPreVisitor(scopeCollector)
      .walkWhile(new ReferenceWalker.WalkCondition<ScopeImpl>() {
        public boolean isFulfilled(ScopeImpl element) {
          return element == null || mapping.containsKey(element);
        }
      });

    final List<ScopeImpl> scopes = scopeCollector.getScopes();

    // add all ancestor scopes and scopeExecutions that are already in "mapping"
    // and correspond to ancestors of the topmost previously collected scope
    ScopeImpl topMostScope = scopes.get(scopes.size() - 1);
    new FlowScopeWalker(topMostScope.getFlowScope())
      .addPreVisitor(new TreeVisitor<ScopeImpl>() {
        public void visit(ScopeImpl obj) {
          scopes.add(obj);
          PvmExecutionImpl priorMappingExecution = mapping.get(obj);

          if (priorMappingExecution != null && !scopeExecutions.contains(priorMappingExecution)) {
            scopeExecutions.add(priorMappingExecution);
          }
        }
      })
      .walkWhile();

    if (scopes.size() == scopeExecutions.size()) {
      // the trees are in sync
      Map<ScopeImpl, PvmExecutionImpl> result = new HashMap<ScopeImpl, PvmExecutionImpl>();
      for (int i = 0; i < scopes.size(); i++) {
        result.put(scopes.get(i), scopeExecutions.get(i));
      }
      return result;
    } else {
      // Wounderful! The trees are out of sync. This is due to legacy behavior
      return LegacyBehavior.createActivityExecutionMapping(scopeExecutions, scopes);
    }
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

  /**
   * {@inheritDoc}
   */
  public void setVariable(String variableName, Object value, String targetActivityId) {
    String activityId = getActivityId();
    if (activityId != null && activityId.equals(targetActivityId)) {
      setVariableLocal(variableName, value);
    } else {
      PvmExecutionImpl executionForFlowScope = findExecutionForFlowScope(targetActivityId);
      if (executionForFlowScope != null) {
        executionForFlowScope.setVariableLocal(variableName, value);
      }
    }
  }

  /**
   * @param targetScopeId - destination scope to be found in current execution tree
   * @return execution with activity id corresponding to targetScopeId
   */
  protected PvmExecutionImpl findExecutionForFlowScope(final String targetScopeId) {
    EnsureUtil.ensureNotNull("target scope id", targetScopeId);

    ScopeImpl currentActivity = getActivity();
    EnsureUtil.ensureNotNull("activity of current execution", currentActivity);

    FlowScopeWalker walker = new FlowScopeWalker(currentActivity);
    ScopeImpl targetFlowScope = walker.walkUntil(new ReferenceWalker.WalkCondition<ScopeImpl>() {

      @Override
      public boolean isFulfilled(ScopeImpl scope) {
        return scope == null || scope.getId().equals(targetScopeId);
      }

    });

    if (targetFlowScope == null) {
      throw LOG.scopeNotFoundException(targetScopeId, this.getId());
    }

    return findExecutionForFlowScope(targetFlowScope);
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


  public boolean isExternallyTerminated() {
    return externallyTerminated;
  }

  public void setExternallyTerminated(boolean externallyTerminated) {
    this.externallyTerminated = externallyTerminated;
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

  public void setDeleteRoot(boolean deleteRoot) {
    if (getReplacedBy() != null) {
      getReplacedBy().setDeleteRoot(deleteRoot);
    }
    this.deleteRoot = deleteRoot;
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
    if (transition != null) {
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
    if (completeScope && !isCanceled()) {
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

  public boolean isInState(ActivityInstanceState state) {
    return activityInstanceState == state.getStateCode();
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
    if (isProcessInstanceExecution()) {
      if (considerSuperExecution && getSuperExecution() != null) {
        PvmExecutionImpl superExecution = getSuperExecution();
        if (superExecution.isScope()) {
          return superExecution;
        } else {
          return superExecution.getParent();
        }
      } else {
        return null;
      }
    } else {
      PvmExecutionImpl parent = getParent();
      if (parent.isScope()) {
        return parent;
      } else {
        return parent.getParent();
      }
    }
  }

  /**
   * Contains the delayed variable events, which will be dispatched on a save point.
   */
  protected transient List<DelayedVariableEvent> delayedEvents = new ArrayList<DelayedVariableEvent>();

  /**
   * Delays a given variable event with the given target scope.
   *
   * @param targetScope   the target scope of the variable event
   * @param variableEvent the variable event which should be delayed
   */
  public void delayEvent(PvmExecutionImpl targetScope, VariableEvent variableEvent) {
    DelayedVariableEvent delayedVariableEvent = new DelayedVariableEvent(targetScope, variableEvent);
    delayEvent(delayedVariableEvent);
  }

  /**
   * Delays and stores the given DelayedVariableEvent on the process instance.
   *
   * @param delayedVariableEvent the DelayedVariableEvent which should be store on the process instance
   */
  public void delayEvent(DelayedVariableEvent delayedVariableEvent) {

    //if process definition has no conditional events the variable events does not have to be delayed
    Boolean hasConditionalEvents = this.getProcessDefinition().getProperties().get(BpmnProperties.HAS_CONDITIONAL_EVENTS);
    if (hasConditionalEvents == null || !hasConditionalEvents.equals(Boolean.TRUE)) {
      return;
    }

    if (isProcessInstanceExecution()) {
      delayedEvents.add(delayedVariableEvent);
    } else {
      getProcessInstance().delayEvent(delayedVariableEvent);
    }
  }

  /**
   * The current delayed variable events.
   *
   * @return a list of DelayedVariableEvent objects
   */
  public List<DelayedVariableEvent> getDelayedEvents() {
    if (isProcessInstanceExecution()) {
      return delayedEvents;
    }
    return getProcessInstance().getDelayedEvents();
  }

  /**
   * Cleares the current delayed variable events.
   */
  public void clearDelayedEvents() {
    if (isProcessInstanceExecution()) {
      delayedEvents.clear();
    } else {
      getProcessInstance().clearDelayedEvents();
    }
  }


  /**
   * Dispatches the current delayed variable events and performs the given atomic operation
   * if the current state was not changed.
   *
   * @param atomicOperation the atomic operation which should be executed
   */
  public void dispatchDelayedEventsAndPerformOperation(final PvmAtomicOperation atomicOperation) {
    dispatchDelayedEventsAndPerformOperation(new Callback<PvmExecutionImpl, Void>() {
      @Override
      public Void callback(PvmExecutionImpl param) {
        param.performOperation(atomicOperation);
        return null;
      }
    });
  }

  /**
   * Dispatches the current delayed variable events and performs the given atomic operation
   * if the current state was not changed.
   *
   * @param continuation the atomic operation continuation which should be executed
   */
  public void dispatchDelayedEventsAndPerformOperation(final Callback<PvmExecutionImpl, Void> continuation) {
    PvmExecutionImpl execution = this;

    if (execution.getDelayedEvents().isEmpty()) {
      continueExecutionIfNotCanceled(continuation, execution);
      return;
    }

    continueIfExecutionDoesNotAffectNextOperation(new Callback<PvmExecutionImpl, Void>() {
      @Override
      public Void callback(PvmExecutionImpl execution) {
        dispatchScopeEvents(execution);
        return null;
      }
    }, new Callback<PvmExecutionImpl, Void>(){
      @Override
      public Void callback(PvmExecutionImpl execution) {
        continueExecutionIfNotCanceled(continuation, execution);
        return null;
      }
    }, execution);
  }

  /**
   * Executes the given depending operations with the given execution.
   * The execution state will be checked with the help of the activity instance id and activity id of the execution before and after
   * the dispatching callback call. If the id's are not changed the
   * continuation callback is called.
   *
   * @param dispatching         the callback to dispatch the variable events
   * @param continuation        the callback to continue with the next atomic operation
   * @param execution           the execution which is used for the execution
   */
  public void continueIfExecutionDoesNotAffectNextOperation(Callback<PvmExecutionImpl, Void> dispatching,
                                                            Callback<PvmExecutionImpl, Void> continuation,
                                                            PvmExecutionImpl execution) {

    String lastActivityId = execution.getActivityId();
    String lastActivityInstanceId = getActivityInstanceId(execution);

    dispatching.callback(execution);

    execution = execution.getReplacedBy() != null ? execution.getReplacedBy() : execution;
    String currentActivityInstanceId = getActivityInstanceId(execution);
    String currentActivityId = execution.getActivityId();

    //if execution was canceled or was changed during the dispatch we should not execute the next operation
    //since another atomic operation was executed during the dispatching
    if (!execution.isCanceled() && isOnSameActivity(lastActivityInstanceId, lastActivityId, currentActivityInstanceId, currentActivityId)) {
      continuation.callback(execution);
    }
  }

  protected void continueExecutionIfNotCanceled(Callback<PvmExecutionImpl, Void> continuation, PvmExecutionImpl execution) {
    if (continuation != null && !execution.isCanceled()) {
      continuation.callback(execution);
    }
  }

  /**
   * Dispatches the current delayed variable events on the scope of the given execution.
   *
   * @param execution the execution on which scope the delayed variable should be dispatched
   */
  protected void dispatchScopeEvents(PvmExecutionImpl execution) {
    PvmExecutionImpl scopeExecution = execution.isScope() ? execution : execution.getParent();

    List<DelayedVariableEvent> delayedEvents = new ArrayList<DelayedVariableEvent>(scopeExecution.getDelayedEvents());
    scopeExecution.clearDelayedEvents();

    Map<PvmExecutionImpl, String> activityInstanceIds = new HashMap<PvmExecutionImpl, String>();
    Map<PvmExecutionImpl, String> activityIds = new HashMap<PvmExecutionImpl, String>();
    initActivityIds(delayedEvents, activityInstanceIds, activityIds);

    //For each delayed variable event we have to check if the delayed event can be dispatched,
    //the check will be done with the help of the activity id and activity instance id.
    //That means it will be checked if the dispatching changed the execution tree in a way that we can't dispatch the
    //the other delayed variable events. We have to check the target scope with the last activity id and activity instance id
    //and also the replace pointer if it exist. Because on concurrency the replace pointer will be set on which we have
    //to check the latest state.
    for (DelayedVariableEvent event : delayedEvents) {
      PvmExecutionImpl targetScope = event.getTargetScope();
      PvmExecutionImpl replaced = targetScope.getReplacedBy() != null ? targetScope.getReplacedBy() : targetScope;
      dispatchOnSameActivity(targetScope, replaced, activityIds, activityInstanceIds, event);
    }
  }

  /**
   * Initializes the given maps with the target scopes and current activity id's and activity instance id's.
   *
   * @param delayedEvents       the delayed events which contains the information about the target scope
   * @param activityInstanceIds the map which maps target scope to activity instance id
   * @param activityIds         the map which maps target scope to activity id
   */
  protected void initActivityIds(List<DelayedVariableEvent> delayedEvents,
                                 Map<PvmExecutionImpl, String> activityInstanceIds,
                                 Map<PvmExecutionImpl, String> activityIds) {

    for (DelayedVariableEvent event : delayedEvents) {
      PvmExecutionImpl targetScope = event.getTargetScope();

      String targetScopeActivityInstanceId = getActivityInstanceId(targetScope);
      activityInstanceIds.put(targetScope, targetScopeActivityInstanceId);
      activityIds.put(targetScope, targetScope.getActivityId());
    }
  }

  /**
   * Dispatches the delayed variable event, if the target scope and replaced by scope (if target scope was replaced) have the
   * same activity Id's and activity instance id's.
   *
   * @param targetScope          the target scope on which the event should be dispatched
   * @param replacedBy           the replaced by pointer which should have the same state
   * @param activityIds          the map which maps scope to activity id
   * @param activityInstanceIds  the map which maps scope to activity instance id
   * @param delayedVariableEvent the delayed variable event which should be dispatched
   */
  private void dispatchOnSameActivity(PvmExecutionImpl targetScope, PvmExecutionImpl replacedBy,
                                      Map<PvmExecutionImpl, String> activityIds,
                                      Map<PvmExecutionImpl, String> activityInstanceIds,
                                      DelayedVariableEvent delayedVariableEvent) {
    //check if the target scope has the same activity id and activity instance id
    //since the dispatching was started
    String currentActivityInstanceId = getActivityInstanceId(targetScope);
    String currentActivityId = targetScope.getActivityId();

    final String lastActivityInstanceId = activityInstanceIds.get(targetScope);
    final String lastActivityId = activityIds.get(targetScope);

    boolean onSameAct = isOnSameActivity(lastActivityInstanceId, lastActivityId, currentActivityInstanceId, currentActivityId);

    //If not we have to check the replace pointer,
    //which was set if a concurrent execution was created during the dispatching.
    if (targetScope != replacedBy && !onSameAct) {
      currentActivityInstanceId = getActivityInstanceId(replacedBy);
      currentActivityId = replacedBy.getActivityId();
      onSameAct = isOnSameActivity(lastActivityInstanceId, lastActivityId, currentActivityInstanceId, currentActivityId);
    }

    //dispatching
    if (onSameAct && isOnDispatchableState(targetScope)) {
      targetScope.dispatchEvent(delayedVariableEvent.getEvent());
    }
  }

  /**
   * Checks if the given execution is on a dispatchable state.
   * That means if the current activity is not a leaf in the activity tree OR
   * it is a leaf but not a scope OR it is a leaf, a scope
   * and the execution is in state DEFAULT, which means not in state
   * Starting, Execute or Ending. For this states it is
   * prohibited to trigger conditional events, otherwise unexpected behavior can appear.
   *
   * @return true if the execution is on a dispatchable state, false otherwise
   */
  private boolean isOnDispatchableState(PvmExecutionImpl targetScope) {
    ActivityImpl targetActivity = targetScope.getActivity();
    return
      //if not leaf, activity id is null -> dispatchable
      targetScope.getActivityId() == null ||
        // if leaf and not scope -> dispatchable
        !targetActivity.isScope() ||
        // if leaf, scope and state in default -> dispatchable
        (targetScope.isInState(ActivityInstanceState.DEFAULT));
  }


  /**
   * Compares the given activity instance id's and activity id's to check if the execution is on the same
   * activity as before an operation was executed. The activity instance id's can be null on transitions.
   * In this case the activity Id's have to be equal, otherwise the execution changed.
   *
   * @param lastActivityInstanceId    the last activity instance id
   * @param lastActivityId            the last activity id
   * @param currentActivityInstanceId the current activity instance id
   * @param currentActivityId         the current activity id
   * @return true if the execution is on the same activity, otherwise false
   */
  private boolean isOnSameActivity(String lastActivityInstanceId, String lastActivityId,
                                   String currentActivityInstanceId, String currentActivityId) {
    return
      //activityInstanceId's can be null on transitions, so the activityId must be equal
      ((lastActivityInstanceId == null && lastActivityInstanceId == currentActivityInstanceId && lastActivityId.equals(currentActivityId))
        //if activityInstanceId's are not null they must be equal -> otherwise execution changed
        || (lastActivityInstanceId != null && lastActivityInstanceId.equals(currentActivityInstanceId)
        && (lastActivityId == null || lastActivityId.equals(currentActivityId))));

  }

  /**
   * Returns the activity instance id for the given execution.
   *
   * @param targetScope the execution for which the activity instance id should be returned
   * @return the activity instance id
   */
  private String getActivityInstanceId(PvmExecutionImpl targetScope) {
    if (targetScope.isConcurrent()) {
      return targetScope.getActivityInstanceId();
    } else {
      ActivityImpl targetActivity = targetScope.getActivity();
      if ((targetActivity != null && targetActivity.getActivities().isEmpty())) {
        return targetScope.getActivityInstanceId();
      } else {
        return targetScope.getParentActivityInstanceId();
      }
    }
  }

  /**
   * Returns the newest incident in this execution
   *
   * @param incidentType the type of new incident
   * @param configuration configuration of the incident
   * @return new incident
   */
  @Override
  public Incident createIncident(String incidentType, String configuration) {
    return createIncident(incidentType, configuration, null);
  }

  public Incident createIncident(String incidentType, String configuration, String message) {
    IncidentContext incidentContext = new IncidentContext();

    incidentContext.setTenantId(this.getTenantId());
    incidentContext.setProcessDefinitionId(this.getProcessDefinitionId());
    incidentContext.setExecutionId(this.getId());
    incidentContext.setActivityId(this.getActivityId());
    incidentContext.setConfiguration(configuration);

    IncidentHandler incidentHandler = findIncidentHandler(incidentType);

    if (incidentHandler == null) {
      incidentHandler = new DefaultIncidentHandler(incidentType);
    }
    return incidentHandler.handleIncident(incidentContext, message);
  }


  /**
   * Resolves an incident with given id.
   *
   * @param incidentId
   */
  @Override
  public void resolveIncident(final String incidentId) {
    IncidentEntity incident = (IncidentEntity) Context
        .getCommandContext()
        .getIncidentManager()
        .findIncidentById(incidentId);

    IncidentHandler incidentHandler = findIncidentHandler(incident.getIncidentType());

    if (incidentHandler == null) {
      incidentHandler = new DefaultIncidentHandler(incident.getIncidentType());
    }
    IncidentContext incidentContext = new IncidentContext(incident);
    incidentHandler.resolveIncident(incidentContext);
  }

  public IncidentHandler findIncidentHandler(String incidentType) {
    Map<String, IncidentHandler> incidentHandlers = Context.getProcessEngineConfiguration().getIncidentHandlers();
    return incidentHandlers.get(incidentType);
  }
}
