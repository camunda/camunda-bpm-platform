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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.FoxAtomicOperationDeleteCascadeFireActivityEnd;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;

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
  protected transient ActivityImpl nextActivity;

  /** current sequence flow. is null when there is no transition being taken. */
  protected transient TransitionImpl transition = null;

  /** transition that will be taken.  is null when there is no transition being taken. */
  protected transient TransitionImpl transitionBeingTaken = null;

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

  /** marks the current activity instance */
  protected int activityInstanceState = ActivityInstanceState.DEFAULT.getStateCode();

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

  public void cancelScope(String reason) {
    if(log.isLoggable(Level.FINE)) {
      log.fine("performing cancel scope behavior for execution "+this);
    }

    if (getSubProcessInstance() != null) {
      getSubProcessInstance().deleteCascade(reason);
    }

    // remove all child executions and sub process instances:
    List<PvmExecutionImpl> executions = new ArrayList<PvmExecutionImpl>(getExecutions());
    for (PvmExecutionImpl childExecution : executions) {
      if (childExecution.getSubProcessInstance()!=null) {
        childExecution.getSubProcessInstance().deleteCascade(reason);
      }
      childExecution.deleteCascade(reason);
    }

    // set activity instance state to cancel
    setCanceled(true);

    // fire activity end on active activity
    ActivityImpl activity = getActivity();
    if(isActive && activity != null) {
      performOperation(PvmAtomicOperation.FIRE_ACTIVITY_END);
    }

    // set activity instance state back to 'default'
    // -> execution will be reused for executing more activities and we want the state to
    // be default initially.
    activityInstanceState = ActivityInstanceState.DEFAULT.getStateCode();
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
    }

    isActive = false;
    isEnded = true;

    removeEventScopes();
  }

  public void deleteCascade(String deleteReason) {
    deleteCascade(deleteReason, false);
  }

  public void deleteCascade(String deleteReason, boolean skipCustomListeners) {
    this.deleteReason = deleteReason;
    this.deleteRoot = true;
    this.isEnded = true;
    this.skipCustomListeners = skipCustomListeners;
    performOperation(PvmAtomicOperation.DELETE_CASCADE);
  }

  public void deleteCascade2(String deleteReason) {
    this.deleteReason = deleteReason;
    this.deleteRoot = true;
    performOperation(new FoxAtomicOperationDeleteCascadeFireActivityEnd());
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

  public void take(PvmTransition transition) {
    if (this.transition!=null) {
      throw new PvmException("already taking a transition");
    }
    if (transition==null) {
      throw new PvmException("transition is null");
    }
    TransitionImpl transitionImpl = (TransitionImpl) transition;
    setActivity(transitionImpl.getSource());
    setTransition(transitionImpl);
    performOperation(PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_END);
  }

  public void executeActivity(PvmActivity activity) {
    ActivityImpl activityImpl = (ActivityImpl) activity;
    if(activity.isConcurrent()) {
      this.nextActivity = activityImpl;
      performOperation(PvmAtomicOperation.ACTIVITY_START_CONCURRENT);

    } else if(activity.isCancelScope()) {
      this.nextActivity = activityImpl;
      performOperation(PvmAtomicOperation.ACTIVITY_START_CANCEL_SCOPE);

    } else {
      setActivity(activityImpl);
      performOperation(PvmAtomicOperation.ACTIVITY_START);
    }
  }

  /**
   * Instantiates the given activity stack under this execution.
   * Sets the variables for the execution responsible to execute the most deeply nested
   * activity.
   *
   * @param activityStack The most deeply nested activity is the last element in the list
   */
  public void executeActivitiesConcurrent(List<PvmActivity> activityStack,
      Map<String, Object> variables, Map<String, Object> localVariables) {

    if (activityStack.isEmpty()) {
      return;
    }

    // The following covers the three cases in which a concurrent execution may be created
    // (this execution is the root in each scenario):
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

    List<? extends PvmExecutionImpl> children = getExecutions();
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
      children.remove(child);
    }

    // (1), (2), and (3)
    PvmExecutionImpl propagatingExecution = createExecution();
    propagatingExecution.setConcurrent(true);
    propagatingExecution.setScope(false);

    propagatingExecution.executeActivities(activityStack, variables, localVariables);

  }

  /**
   * Instantiates the given activity stack. Uses this execution to execute the
   * highest activity in the stack.
   * Sets the variables for the execution responsible to execute the most deeply nested
   * activity.
   *
   * @param activityStack The most deeply nested activity is the last element in the list
   */
  public void executeActivities(List<PvmActivity> activityStack,
      Map<String, Object> variables, Map<String, Object> localVariables) {

    ExecutionStartContext executionStartContext = new ExecutionStartContext();
    executionStartContext.setActivityStack(activityStack);
    executionStartContext.setVariables(variables);
    executionStartContext.setVariablesLocal(localVariables);
    setStartContext(executionStartContext);

    if (activityStack.size() > 1) {
      performOperation(PvmAtomicOperation.ACTIVITY_INIT_STACK);

    } else {
      setVariables(variables);
      setVariablesLocal(localVariables);
      setActivity(activityStack.get(0));
      performOperation(PvmAtomicOperation.ACTIVITY_START_CREATE_SCOPE);

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

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void takeAll(List<PvmTransition> _transitions, List<? extends ActivityExecution> _recyclableExecutions) {
    ArrayList<TransitionImpl> transitions = new ArrayList<TransitionImpl>((List)_transitions);
    ArrayList<PvmExecutionImpl> recyclableExecutions = (_recyclableExecutions!=null ? new ArrayList<PvmExecutionImpl>((List)_recyclableExecutions) : new ArrayList<PvmExecutionImpl>());

    if (recyclableExecutions.size()>1) {
      for (PvmExecutionImpl recyclableExecution: recyclableExecutions) {
        if (recyclableExecution.isScope()) {
          throw new PvmException("joining scope executions is not allowed");
        }
      }
    }

    PvmExecutionImpl concurrentRoot = ((isConcurrent && !isScope) ? getParent() : this);
    List<PvmExecutionImpl> concurrentActiveExecutions = new ArrayList<PvmExecutionImpl>();
    List<PvmExecutionImpl> concurrentInActiveExecutions = new ArrayList<PvmExecutionImpl>();
    for (PvmExecutionImpl execution: concurrentRoot.getExecutions()) {
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

      List<PvmExecutionImpl> recyclableExecutionImpls = recyclableExecutions;
      recyclableExecutions.remove(concurrentRoot);
      for (PvmExecutionImpl prunedExecution: recyclableExecutionImpls) {
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
        TransitionImpl outgoingTransition = transitions.remove(0);

        PvmExecutionImpl outgoingExecution = null;
        if (recyclableExecutions.isEmpty()) {
          outgoingExecution = concurrentRoot.createExecution();
          log.fine("new "+outgoingExecution+" with parent "
                  + outgoingExecution.getParent()+" created to take transition "+outgoingTransition);
        } else {
          outgoingExecution = recyclableExecutions.remove(0);
          log.fine("recycled "+outgoingExecution+" to take transition "+outgoingTransition);
        }

        outgoingExecution.setActive(true);
        outgoingExecution.setScope(false);
        outgoingExecution.setConcurrent(true);
        outgoingExecution.setTransitionBeingTaken(outgoingTransition);
        outgoingExecutions.add(new OutgoingExecution(outgoingExecution, outgoingTransition, true));
      }

      concurrentRoot.setActivityInstanceId(concurrentRoot.getParentActivityInstanceId());

      boolean isConcurrentEnd = outgoingExecutions.isEmpty();

      // prune the executions that are not recycled
      for (PvmExecutionImpl prunedExecution: recyclableExecutions) {
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
      PvmExecutionImpl parent = getParent();
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

  public String getCurrentTransitionId() {
    TransitionImpl transition = getTransition();
    if(transition != null) {
      return transition.getId();
    } else {
      return null;
    }
  }

  public TransitionImpl getTransitionBeingTaken() {
    return transitionBeingTaken;
  }

  public void setTransition(TransitionImpl transition) {
    this.transition = transition;
  }

  public void setTransitionBeingTaken(TransitionImpl transitionBeingTaken) {
    this.transitionBeingTaken = transitionBeingTaken;
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

  public ActivityImpl getNextActivity() {
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

}
