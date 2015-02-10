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
import org.camunda.bpm.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ActivityStartBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.FoxAtomicOperationDeleteCascadeFireActivityEnd;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;
import org.camunda.bpm.engine.impl.tree.ExecutionWalker;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.ScopeCollector;
import org.camunda.bpm.engine.impl.tree.ScopeExecutionCollector;
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

  protected transient ProcessInstanceStartContext processInstanceStartContext;

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

  public PvmExecutionImpl(ActivityImpl initialActivity) {
    processInstanceStartContext = new ProcessInstanceStartContext(initialActivity);
  }

  // API ////////////////////////////////////////////////

  /** creates a new execution. properties processDefinition, processInstance and activity will be initialized. */
  public PvmExecutionImpl createExecution() {
    return createExecution(false);
  }

  public abstract PvmExecutionImpl createExecution(boolean initStartContext);

  // sub process instance

  public PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition) {
    return createSubProcessInstance(processDefinition, null);
  }

  public abstract PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey);

  public abstract PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey, String caseInstanceId);

  // sub case instance

  public abstract CmmnExecution createSubCaseInstance(CmmnCaseDefinition caseDefinition);

  public abstract CmmnExecution createSubCaseInstance(CmmnCaseDefinition caseDefinition, String businessKey);

  public abstract void initialize();

  public void executeIoMapping() {
    // execute Input Mappings (if they exist).
    ActivityImpl currentActivity = getActivity();
    if (currentActivity != null && currentActivity.getIoMapping() != null && !skipIoMapping) {
      currentActivity.getIoMapping().executeInputParameters(this);
    }
  }

  public void start() {
    start(null);
  }

  public void start(Map<String, Object> variables) {
    if(variables != null) {
      setVariables(variables);
    }

    performOperation(PvmAtomicOperation.PROCESS_START);
  }

  public void destroy() {
    log.fine("destroying "+this);

    setScope(false);
  }

  protected void removeEventScopes() {
    List<PvmExecutionImpl> childExecutions = new ArrayList<PvmExecutionImpl>(getExecutions());
    for (PvmExecutionImpl childExecution : childExecutions) {
      if(childExecution.isEventScope()) {
        log.fine("removing eventScope "+childExecution);
        childExecution.destroy();
        childExecution.remove();
      }
    }
  }

  public void clearScope(String reason, boolean skipCustomListeners, boolean skipIoMappings) {
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMapping = skipIoMappings;

    if (getSubProcessInstance() != null) {
      getSubProcessInstance().deleteCascade(reason, skipCustomListeners, skipIoMappings);
    }

    // remove all child executions and sub process instances:
    List<PvmExecutionImpl> executions = new ArrayList<PvmExecutionImpl>(getExecutions());
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
  public void interrupt(String reason) {
    interrupt(reason, false, false);
  }

  public void interrupt(String reason, boolean skipCustomListeners, boolean skipIoMappings) {
    if(log.isLoggable(Level.FINE)) {
      log.fine("Interrupting execution "+this);
    }
    clearScope(reason, skipCustomListeners, skipIoMappings);
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
    performOperation(PvmAtomicOperation.ACTIVITY_NOTIFY_LISTENER_END);
  }

  public void endCompensation() {
    remove();
    performOperation(PvmAtomicOperation.FIRE_ACTIVITY_END);
    getParent().signal("compensationDone", null);
  }

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
    // (2b: LEGACY behavior)
    // Before:               After:
    //       -------               -------
    //       |  e1 |               |  e1 |
    //       -------               -------
    //          |                  /     \
    //       -------           -------  -------
    //       |  e2 |           |  e2 |  |  e3 |
    //       -------           -------  -------
    //
    // e2 remains under e1 but is now scope AND concurrent
    // e3 is a new, non-scope activity
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
      this.setActivity(null);

    }
    else if (children.size() == 1) {
      // (2)
      PvmExecutionImpl child = children.get(0);

      if(LegacyBehavior.get().isConcurrentScopeExecutionEnabled()) {
        // 2b) legacy behavior
        LegacyBehavior.get().createConcurrentScope(child);
      } else {
        PvmExecutionImpl concurrentReplacingExecution = this.createExecution();
        concurrentReplacingExecution.setConcurrent(true);
        concurrentReplacingExecution.setScope(false);
        child.setParent(concurrentReplacingExecution);
        ((List<PvmExecutionImpl>) concurrentReplacingExecution.getExecutions()).add(child);
        this.getExecutions().remove(child);
      }
    }

    // (1), (2), and (3)
    PvmExecutionImpl concurrentExecution = this.createExecution();
    concurrentExecution.setConcurrent(true);
    concurrentExecution.setScope(false);

    return concurrentExecution;
  }

  public boolean tryPruneLastConcurrentChild() {

    if (getNonEventScopeExecutions().size() == 1) {
      PvmExecutionImpl lastConcurrent = getNonEventScopeExecutions().get(0);
      if (lastConcurrent.isConcurrent()) {
        if (!lastConcurrent.isScope()) {
          setActivity(lastConcurrent.getActivity());
          setTransition(lastConcurrent.getTransition());
          lastConcurrent.setReplacedBy(this);

          // Move children of lastConcurrent one level up
          if (lastConcurrent.getExecutions().size() > 0) {
            getExecutions().clear();
            for (PvmExecutionImpl childExecution : lastConcurrent.getExecutions()) {
              ((List) getExecutions()).add(childExecution);
              childExecution.setParent(this);
            }
            lastConcurrent.getExecutions().clear();
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
          LegacyBehavior.get().pruneConcurrentScope(lastConcurrent);
        }
        return true;
      }
    }

    return false;

  }

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
    flowScope = LegacyBehavior.get().normalizeSecondNonScope(flowScope);

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

  public abstract PvmExecutionImpl getReplacedBy();

  public void setReplacedBy(PvmExecutionImpl replacedBy) {
    replacedBy.setActivityInstanceId(activityInstanceId);
  }

  public void replace(PvmExecutionImpl execution) {
    // activity instance id handling
    this.activityInstanceId = execution.getActivityInstanceId();

    execution.leaveActivityInstance();
  }

  // methods that translate to operations /////////////////////////////////////

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
    TransitionImpl transitionImpl = (TransitionImpl) transition;
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
  public void executeActivity(PvmActivity activity) {
    if(!activity.getIncomingTransitions().isEmpty()) {
      throw new ProcessEngineException("Activity is contained in normal flow and cannot be executed using executeActivity().");
    }

    ActivityStartBehavior activityStartBehavior = activity.getActivityStartBehavior();
    if(!isScope() && ActivityStartBehavior.DEFAULT != activityStartBehavior) {
      throw new ProcessEngineException("Activity '"+activity+"' with start behavior '"+activityStartBehavior+"'"
            + "cannot be executed by non-scope execution.");
    }

    PvmActivity activityImpl = (PvmActivity) activity;
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
  @SuppressWarnings("unchecked")
  public void executeActivitiesConcurrent(List<PvmActivity> activityStack, PvmActivity targetActivity,
      PvmTransition targetTransition, Map<String, Object> variables, Map<String, Object> localVariables,
      boolean skipCustomListeners, boolean skipIoMappings) {

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
    //
    // (3) A single child that is a scope execution
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
    List<? extends PvmExecutionImpl> children = getNonEventScopeExecutions();

    if (children.isEmpty()) {
      // (1)
      PvmExecutionImpl replacingExecution = createExecution();
      replacingExecution.setConcurrent(true);
      replacingExecution.setScope(false);
      replacingExecution.replace(this);
      setActivity(null);

    }
    else if (children.size() == 1) {
      // (2)
      PvmExecutionImpl child = children.get(0);

      PvmExecutionImpl concurrentReplacingExecution = createExecution();
      concurrentReplacingExecution.setConcurrent(true);
      concurrentReplacingExecution.setScope(false);
      child.setParent(concurrentReplacingExecution);
      ((List<PvmExecutionImpl>) concurrentReplacingExecution.getExecutions()).add(child);
      getExecutions().remove(child);
      leaveActivityInstance();
    }

    // (1), (2), and (3)
    PvmExecutionImpl propagatingExecution = createExecution();
    propagatingExecution.setConcurrent(true);
    propagatingExecution.setScope(false);

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
      setTransition((TransitionImpl) targetTransition);
      performOperation(PvmAtomicOperation.TRANSITION_START_NOTIFY_LISTENER_TAKE);
    }
  }

  @SuppressWarnings("unchecked")
  public void removeFromParentScope() {
    PvmExecutionImpl parent = getParent();

    if (isScope()) {
      destroy();
    }
    remove();

    if (parent.isConcurrent()) {
      parent.remove();
      parent = parent.getParent();
    }

    // consolidate the parent scope's execution tree
    if (parent.getExecutions().size() == 1) {
      PvmExecutionImpl concurrentChild = parent.getExecutions().get(0);
      parent.replace(concurrentChild);
      parent.setActivity(concurrentChild.getActivity());
      parent.setActive(concurrentChild.isActive());

      if (!concurrentChild.getExecutions().isEmpty()) {
        // a concurrent execution has exactly one child
        PvmExecutionImpl childScopeExecution = concurrentChild.getExecutions().get(0);
        childScopeExecution.setParent(parent);
        ((List<PvmExecutionImpl>) parent.getExecutions()).add(childScopeExecution);
      }

      concurrentChild.remove();
    }
  }

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

  public void leaveActivityViaTransition(PvmTransition outgoingTransition) {
    leaveActivityViaTransitions(Arrays.asList(outgoingTransition), Collections.<ActivityExecution>emptyList());
  }

  public void leaveActivityViaTransitions(List<PvmTransition> _transitions, List<? extends ActivityExecution> _recyclableExecutions) {
    List<? extends ActivityExecution> recyclableExecutions = Collections.emptyList();
    if (_recyclableExecutions != null) {
      recyclableExecutions = new ArrayList<ActivityExecution>(_recyclableExecutions);
    }

    recyclableExecutions.remove(this);
    for (ActivityExecution execution : recyclableExecutions) {
      execution.end(_transitions.isEmpty());
    }

    // ending a recyclable execution might have replaced this execution as well
    PvmExecutionImpl propagatingExecution = this;
    if (getReplacedBy() != null) {
      propagatingExecution = getReplacedBy();
    }

    if (_transitions.isEmpty()) {
      propagatingExecution.end(!propagatingExecution.isConcurrent());
    }
    else {
      propagatingExecution.setTransitionsToTake(_transitions);
      propagatingExecution.performOperation(PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_END);
    }
  }

  protected boolean hasConcurrentSiblings(PvmExecutionImpl concurrentRoot) {
    if(concurrentRoot.isProcessInstanceExecution()) {
      return false;
    } else {
      List<? extends PvmExecutionImpl> executions = concurrentRoot.getParent().getExecutions();
      for (PvmExecutionImpl executionImpl : executions) {
        if(executionImpl != concurrentRoot
            && !executionImpl.isEventScope()) {
          return true;
        }
      }
      return false;
    }
  }

  protected boolean allExecutionsInSameActivity(List<PvmExecutionImpl> executions) {
    if (executions.size() > 1) {
      String activityId = executions.get(0).getActivityId();
      for (PvmExecutionImpl execution : executions) {
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

  public boolean isActive(String activityId) {
    return findExecution(activityId)!=null;
  }

  public void inactivate() {
    this.isActive = false;
  }

  // executions ///////////////////////////////////////////////////////////////

  public abstract List<? extends PvmExecutionImpl> getExecutions();

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

  public String getCurrentActivityName() {
    ActivityImpl activity = getActivity();
    if(activity != null) {
      return activity.getName();
    } else {
      return null;
    }
  }

  public String getCurrentActivityId() {
    return getActivityId();
  }

  public void setActivity(PvmActivity activity) {
    this.activity = (ActivityImpl) activity;
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

  protected abstract String generateActivityInstanceId(String activityId);

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
    if(isProcessInstanceExecution()) {
      return getId();

    } else {
      ActivityImpl currentActivity = getActivity();
      if (activityInstanceId == null || (currentActivity != null && !currentActivity.isScope())) {
        PvmExecutionImpl parent = getParent();
        return parent.getActivityInstanceId();
      }
      else {
        PvmExecutionImpl parentScopeExecution = getParentScopeExecution(false);
        return parentScopeExecution.getActivityInstanceId();
      }
    }
  }

  public void forceUpdateActivityInstance() {
    activityInstanceId = generateActivityInstanceId(getActivity().getId());
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  // parent ///////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the parent */
  public abstract PvmExecutionImpl getParent();

  public String getParentId() {
    PvmExecutionImpl parent = getParent();
    if(parent != null) {
      return parent.getId();
    } else {
      return null;
    }
  }

  public abstract void setParent(PvmExecutionImpl parent);

  // super- and subprocess executions /////////////////////////////////////////

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
    if (isProcessInstanceExecution()) {
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


  /**
   * For a given target flow scope, this method returns the corresponding scope execution.
   *
   * Precondition: the execution is active and executing an activity.
   * Can be invoked for scope and non scope executions.
   *
   * @param targetFlowScope scope activity or process definition for which the scope execution should be found
   * @return the scope execution for the provided targetFlowScope
   */
  public PvmExecutionImpl findExecutionForFlowScope(PvmScope targetFlowScope) {
    // if this execution is not a scope execution, use the parent
    final PvmExecutionImpl scopeExecution = isScope() ? this : getParent();

    ScopeImpl currentActivity = getActivity();
    EnsureUtil.ensureNotNull("activity of current execution", currentActivity);

    // if this is a scope execution currently executing a non scope activity
    currentActivity = currentActivity.isScope() ? currentActivity : currentActivity.getFlowScope();
    currentActivity = LegacyBehavior.get().normalizeSecondNonScope(currentActivity);

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
      scopeExecution = LegacyBehavior.get().getScopeExecution(targetScope, activityExecutionMapping);
    }
    return scopeExecution;
  }

  public Map<ScopeImpl, PvmExecutionImpl> createActivityExecutionMapping() {
    ScopeImpl currentActivity = getActivity();
    EnsureUtil.ensureNotNull("activity of current execution", currentActivity);

    // if this is a scope execution currently executing a non scope activity
    currentActivity = currentActivity.isScope() ? currentActivity : currentActivity.getFlowScope();
    currentActivity = LegacyBehavior.get().normalizeSecondNonScope(currentActivity);

    PvmExecutionImpl scopeExecution = isScope() ? this : getParent();

    return scopeExecution.createActivityExecutionMapping(currentActivity);
  }

  public Map<ScopeImpl, PvmExecutionImpl> createActivityExecutionMapping(ScopeImpl currentScope) {
    if(!isScope()) {
      throw new ProcessEngineException("Execution must be a scope execution");
    }
    if(!currentScope.isScope()) {
      throw new ProcessEngineException("Current scope must be a scope.");
    }

    ScopeExecutionCollector scopeExecutionCollector = new ScopeExecutionCollector();
    new ExecutionWalker((PvmExecutionImpl) this)
      .addPreCollector(scopeExecutionCollector)
      .walkUntil();
    List<PvmExecutionImpl> scopeExecutions = scopeExecutionCollector.getExecutions();

    ScopeCollector scopeCollector = new ScopeCollector();
    new FlowScopeWalker((ScopeImpl) currentScope)
      .addPreCollector(scopeCollector)
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
      return LegacyBehavior.get().createActivityExecutionMapping(scopeExecutions, scopes);
    }
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

  // variables ////////////////////////////////////////////

  @Override
  public String getVariableScopeKey() {
    return "execution";
  }

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

  public TransitionImpl getTransition() {
    return transition;
  }

  public List<PvmTransition> getTransitionsToTake() {
    return transitionsToTake;
  }

  public void setTransitionsToTake(List<PvmTransition> transitionsToTake) {
    this.transitionsToTake = transitionsToTake;
  }

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
    processInstanceStartContext = null;
  }

  public void disposeExecutionStartContext() {
    startContext = null;
  }

  public PvmActivity getNextActivity() {
    return nextActivity;
  }

  public boolean isProcessInstanceExecution() {
    return getParent() == null;
  }

  public ProcessInstanceStartContext getProcessInstanceStartContext() {
    return processInstanceStartContext;
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
