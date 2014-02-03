/* Licensed under the Apache License, ersion 2.0 (the "License");
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.persistence.entity.ActivityInstanceState;
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
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Falko Menge
 */
public class ExecutionImpl implements
        Serializable,
        ActivityExecution,
        ExecutionListenerExecution,
        PvmExecution,
        InterpretableExecution {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(ExecutionImpl.class.getName());

  private static AtomicInteger idGenerator = new AtomicInteger();

  // current position /////////////////////////////////////////////////////////

  protected ProcessDefinitionImpl processDefinition;

  /** current activity */
  protected ActivityImpl activity;

  protected PvmActivity nextActivity;

  /** the Id of the current activity instance */
  protected String activityInstanceId;

  /** current transition.  is null when there is no transition being taken. */
  protected TransitionImpl transition = null;

  /** transition that will be taken.  is null when there is no transition being taken. */
  protected TransitionImpl transitionBeingTaken = null;

  /** the process instance.  this is the root of the execution tree.
   * the processInstance of a process instance is a self reference. */
  protected ExecutionImpl processInstance;

  /** the parent execution */
  protected ExecutionImpl parent;

  /** nested executions representing scopes or concurrent paths */
  protected List<ExecutionImpl> executions;

  /** super execution, not-null if this execution is part of a subprocess */
  protected ExecutionImpl superExecution;

  /** reference to a subprocessinstance, not-null if currently subprocess is started from this execution */
  protected ExecutionImpl subProcessInstance;

  /** only available until the process instance is started */
  protected ProcessInstanceStartContext processInstanceStartContext;

  /** the business key */
  protected String businessKey;
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

  protected int activityInstanceState = ActivityInstanceState.DEFAULT.getStateCode();

  protected Map<String, Object> variables = null;

  // events ///////////////////////////////////////////////////////////////////

  protected String eventName;
  protected PvmProcessElement eventSource;
  protected int executionListenerIndex = 0;

  // cascade deletion ////////////////////////////////////////////////////////

  protected boolean deleteRoot;
  protected String deleteReason;

  // replaced by //////////////////////////////////////////////////////////////

  /** when execution structure is pruned during a takeAll, then
   * the original execution has to be resolved to the replaced execution.
   * @see {@link #takeAll(List, List)} {@link OutgoingExecution} */
  protected ExecutionImpl replacedBy;

  // atomic operations ////////////////////////////////////////////////////////

  /** next operation.  process execution is in fact runtime interpretation of the process model.
   * each operation is a logical unit of interpretation of the process.  so sequentially processing
   * the operations drives the interpretation or execution of a process.
   * @see AtomicOperation
   * @see #performOperation(AtomicOperation) */
  protected AtomicOperation nextOperation;
  protected boolean isOperating = false;

  /* Default constructor for ibatis/jpa/etc. */
  public ExecutionImpl() {
  }

  public ExecutionImpl(ActivityImpl initial) {
    processInstanceStartContext = new ProcessInstanceStartContext(initial);
  }

  // lifecycle methods ////////////////////////////////////////////////////////

  /** creates a new execution. properties processDefinition, processInstance and activity will be initialized. */
  public ExecutionImpl createExecution() {
    // create the new child execution
    ExecutionImpl createdExecution = newExecution();

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

    return createdExecution;
  }

  /** instantiates a new execution.  can be overridden by subclasses */
  protected ExecutionImpl newExecution() {
    return new ExecutionImpl();
  }

  public PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition) {
    return createSubProcessInstance(processDefinition, null);
  }

  public PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey) {
    ExecutionImpl subProcessInstance = newExecution();

    // manage bidirectional super-subprocess relation
    subProcessInstance.setSuperExecution(this);
    this.setSubProcessInstance(subProcessInstance);

    // Initialize the new execution
    subProcessInstance.setProcessDefinition((ProcessDefinitionImpl) processDefinition);
    subProcessInstance.setProcessInstance(subProcessInstance);

    if(businessKey != null) {
      subProcessInstance.setBusinessKey(businessKey);
    }

    return subProcessInstance;
  }

  public void initialize() {
  }

  public void destroy() {
    setScope(false);
  }

  public void remove() {
    isEnded = true;
    isActive = false;
    ensureParentInitialized();
    if (parent!=null) {
      parent.ensureExecutionsInitialized();
      parent.executions.remove(this);
    }

    // remove event scopes:
    List<InterpretableExecution> childExecutions = new ArrayList<InterpretableExecution>(getExecutions());
    for (InterpretableExecution childExecution : childExecutions) {
      if(childExecution.isEventScope()) {
        log.fine("removing eventScope "+childExecution);
        childExecution.destroy();
        childExecution.remove();
      }
    }
  }

  public void cancelScope(String reason) {

    if(log.isLoggable(Level.FINE)) {
      log.fine("performing destroy scope behavior for execution "+this);
    }

    // remove all child executions and sub process instances:
    List<InterpretableExecution> executions = new ArrayList<InterpretableExecution>(getExecutions());
    for (InterpretableExecution childExecution : executions) {
      if (childExecution.getSubProcessInstance()!=null) {
        childExecution.getSubProcessInstance().deleteCascade(reason);
      }
      childExecution.deleteCascade(reason);
    }

  }

  public void interruptScope(String reason) {
  }

  // parent ///////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the parent */
  public ExecutionImpl getParent() {
    ensureParentInitialized();
    return parent;
  }

  public String getParentId() {
    ensureActivityInitialized();
    if(parent != null) {
      return parent.getId();
    }
    return null;
  }

  /** all updates need to go through this setter as subclasses can override this method */
  public void setParent(InterpretableExecution parent) {
    this.parent = (ExecutionImpl) parent;
  }

  /** must be called before memberfield parent is used.
   * can be used by subclasses to provide parent member field initialization. */
  protected void ensureParentInitialized() {
  }

  // executions ///////////////////////////////////////////////////////////////

  /** ensures initialization and returns the non-null executions list */
  public List<ExecutionImpl> getExecutions() {
    ensureExecutionsInitialized();
    return executions;
  }

  public ExecutionImpl getSuperExecution() {
    ensureSuperExecutionInitialized();
    return superExecution;
  }

  public void setSuperExecution(ExecutionImpl superExecution) {
    this.superExecution = superExecution;
    if (superExecution != null) {
      superExecution.setSubProcessInstance(null);
    }
  }

  // Meant to be overridden by persistent subclasseses
  protected void ensureSuperExecutionInitialized() {
  }

  public ExecutionImpl getSubProcessInstance() {
    ensureSubProcessInstanceInitialized();
    return subProcessInstance;
  }

  public void setSubProcessInstance(InterpretableExecution subProcessInstance) {
    this.subProcessInstance = (ExecutionImpl) subProcessInstance;
  }

  // Meant to be overridden by persistent subclasses
  protected void ensureSubProcessInstanceInitialized() {
  }

  public void deleteCascade(String deleteReason) {
    this.deleteReason = deleteReason;
    this.deleteRoot = true;
    performOperation(AtomicOperation.DELETE_CASCADE);
  }

  public void deleteCascade2(String deleteReason) {
    this.deleteReason = deleteReason;
    this.deleteRoot = true;
    performOperation(new FoxAtomicOperationDeleteCascadeFireActivityEnd());
  }

  /** removes an execution. if there are nested executions, those will be ended recursively.
   * if there is a parent, this method removes the bidirectional relation
   * between parent and this execution. */
  public void end(boolean completeScope) {
    setCompleteScope(completeScope);

    isActive = false;
    isEnded = true;
    performOperation(AtomicOperation.ACTIVITY_END);
  }

  /** searches for an execution positioned in the given activity */
  public ExecutionImpl findExecution(String activityId) {
    if ( (getActivity()!=null)
         && (getActivity().getId().equals(activityId))
       ) {
      return this;
    }
    for (ExecutionImpl nestedExecution : getExecutions()) {
      ExecutionImpl result = nestedExecution.findExecution(activityId);
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
    for (ExecutionImpl execution: executions) {
      execution.collectActiveActivityIds(activeActivityIds);
    }
  }

  /** must be called before memberfield executions is used.
   * can be used by subclasses to provide executions member field initialization. */
  protected void ensureExecutionsInitialized() {
    if (executions==null) {
      executions = new ArrayList<ExecutionImpl>();
    }
  }

  // process definition ///////////////////////////////////////////////////////

  /** ensures initialization and returns the process definition. */
  public ProcessDefinitionImpl getProcessDefinition() {
    ensureProcessDefinitionInitialized();
    return processDefinition;
  }

  public String getProcessDefinitionId() {
    return getProcessDefinition().getId();
  }

  /** for setting the process definition, this setter must be used as subclasses can override */

  /** must be called before memberfield processDefinition is used.
   * can be used by subclasses to provide processDefinition member field initialization. */
  protected void ensureProcessDefinitionInitialized() {
  }

  // process instance /////////////////////////////////////////////////////////

  /** ensures initialization and returns the process instance. */
  public ExecutionImpl getProcessInstance() {
    ensureProcessInstanceInitialized();
    return processInstance;
  }

  public String getProcessInstanceId() {
    return getProcessInstance().getId();
  }

  public String getBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getProcessBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }

  /** for setting the process instance, this setter must be used as subclasses can override */
  public void setProcessInstance(InterpretableExecution processInstance) {
    this.processInstance = (ExecutionImpl) processInstance;
  }

  /** must be called before memberfield processInstance is used.
   * can be used by subclasses to provide processInstance member field initialization. */
  protected void ensureProcessInstanceInitialized() {
  }

  // activity /////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the activity */
  public ActivityImpl getActivity() {
    ensureActivityInitialized();
    return activity;
  }

  /** sets the current activity.  can be overridden by subclasses.  doesn't
   * require initialization. */
  public void setActivity(PvmActivity activity) {
    this.activity = (ActivityImpl) activity;
  }

  /** must be called before the activity member field or getActivity() is called */
  protected void ensureActivityInitialized() {
  }

  public void enterActivityInstance() {

    activity = getActivity();
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
      return String.valueOf(System.identityHashCode(getProcessInstance()));
    } else {
      ExecutionImpl parent = getParent();
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
    int nextId = idGenerator.incrementAndGet();
    String compositeId = activityId+":"+nextId;
    if(compositeId.length()>64) {
      return String.valueOf(nextId);
    } else {
      return compositeId;
    }
  }

  // scopes ///////////////////////////////////////////////////////////////////

  protected void ensureScopeInitialized() {
  }

  public boolean isScope() {
    return isScope;
  }
  public void setScope(boolean isScope) {
    this.isScope = isScope;
  }

  // process instance start implementation ////////////////////////////////////

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
    if(isProcessInstance()) {
      if(processInstanceStartContext == null) {
        processInstanceStartContext = new ProcessInstanceStartContext(processDefinition.getInitial());
      }
    }

    if(variables != null) {
      setVariables(variables);
    }

    if(businessKey != null) {
      setBusinessKey(businessKey);
    }

    performOperation(AtomicOperation.PROCESS_START);
  }

  // methods that translate to operations /////////////////////////////////////

  public void signal(String signalName, Object signalData) {
    ensureActivityInitialized();
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
    setTransition((TransitionImpl) transition);
    performOperation(AtomicOperation.TRANSITION_NOTIFY_LISTENER_END);
  }

  public void executeActivity(PvmActivity activity) {
    if(activity.isConcurrent()) {
      this.nextActivity = activity;
      performOperation(AtomicOperation.ACTIVITY_START_CONCURRENT);
    } else {
      setActivity((ActivityImpl) activity);
      performOperation(AtomicOperation.ACTIVITY_START);
    }
  }

  public List<ActivityExecution> findInactiveConcurrentExecutions(PvmActivity activity) {
    List<ActivityExecution> inactiveConcurrentExecutionsInActivity = new ArrayList<ActivityExecution>();
    List<ActivityExecution> otherConcurrentExecutions = new ArrayList<ActivityExecution>();
    if (isConcurrent()) {
      List< ? extends ActivityExecution> concurrentExecutions = getParent().getExecutions();
      for (ActivityExecution concurrentExecution: concurrentExecutions) {
        if (concurrentExecution.getActivity()==activity) {
          if (concurrentExecution.isActive()) {
            throw new PvmException("didn't expect active execution in "+activity+". bug?");
          }
          inactiveConcurrentExecutionsInActivity.add(concurrentExecution);
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

  @SuppressWarnings("unchecked")
  public void takeAll(List<PvmTransition> transitions, List<ActivityExecution> recyclableExecutions) {
    transitions = new ArrayList<PvmTransition>(transitions);
    recyclableExecutions = (recyclableExecutions!=null ? new ArrayList<ActivityExecution>(recyclableExecutions) : new ArrayList<ActivityExecution>());

    if (recyclableExecutions.size()>1) {
      for (ActivityExecution recyclableExecution: recyclableExecutions) {
        if (((ExecutionImpl)recyclableExecution).isScope()) {
          throw new PvmException("joining scope executions is not allowed");
        }
      }
    }

    ExecutionImpl concurrentRoot = ((isConcurrent && !isScope) ? getParent() : this);
    List<ExecutionImpl> concurrentActiveExecutions = new ArrayList<ExecutionImpl>();
    List<ExecutionImpl> concurrentInActiveExecutions = new ArrayList<ExecutionImpl>();
    for (ExecutionImpl execution: concurrentRoot.getExecutions()) {
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

      List<ExecutionImpl> recyclableExecutionImpls = (List) recyclableExecutions;
      recyclableExecutions.remove(concurrentRoot);
      for (ExecutionImpl prunedExecution: recyclableExecutionImpls) {
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

        ExecutionImpl outgoingExecution = null;
        if (recyclableExecutions.isEmpty()) {
          outgoingExecution = concurrentRoot.createExecution();
          log.fine("new "+outgoingExecution+" with parent "
                  + outgoingExecution.getParent()+" created to take transition "+outgoingTransition);
        } else {
          outgoingExecution = (ExecutionImpl) recyclableExecutions.remove(0);
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

  protected boolean allExecutionsInSameActivity(List<ExecutionImpl> executions) {
    if (executions.size() > 1) {
      String activityId = executions.get(0).getActivity().getId();
      for (ExecutionImpl execution : executions) {
        String otherActivityId = execution.getActivity().getId();
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
    performOperationSync(executionOperation);
  }

  public void performOperationSync(AtomicOperation executionOperation) {
    this.nextOperation = executionOperation;
    if (!isOperating) {
      isOperating = true;
      while (nextOperation!=null) {
        AtomicOperation currentOperation = this.nextOperation;
        this.nextOperation = null;
        if (log.isLoggable(Level.FINEST)) {
          log.finest("AtomicOperation: " + currentOperation + " on " + this);
        }
        currentOperation.execute(this);
      }
      isOperating = false;
    }
  }

  protected boolean hasConcurrentSiblings(ExecutionImpl concurrentRoot) {
    if(concurrentRoot.isProcessInstance()) {
      return false;
    } else {
      List<ExecutionImpl> executions = concurrentRoot.getParent().getExecutions();
      for (ExecutionImpl executionImpl : executions) {
        if(executionImpl != concurrentRoot
            && !executionImpl.isEventScope()) {
          return true;
        }
      }
      return false;
    }
  }

  public boolean isActive(String activityId) {
    return findExecution(activityId)!=null;
  }

  // variables ////////////////////////////////////////////////////////////////

  public Object getVariable(String variableName) {
    ensureVariablesInitialized();

    // If value is found in this scope, return it
    if (variables.containsKey(variableName)) {
      return variables.get(variableName);
    }

    // If value not found in this scope, check the parent scope
    ensureParentInitialized();
    if (parent != null) {
      return parent.getVariable(variableName);
    }

    // Variable is nowhere to be found
    return null;
  }

  public Map<String, Object> getVariables() {
    Map<String, Object> collectedVariables = new HashMap<String, Object>();
    collectVariables(collectedVariables);
    return collectedVariables;
  }

  protected void collectVariables(Map<String, Object> collectedVariables) {
    ensureParentInitialized();
    if (parent!=null) {
      parent.collectVariables(collectedVariables);
    }
    ensureVariablesInitialized();
    for (String variableName: variables.keySet()) {
      collectedVariables.put(variableName, variables.get(variableName));
    }
  }

  public void setVariables(Map<String, ? extends Object> variables) {
    ensureVariablesInitialized();
    if (variables!=null) {
      for (String variableName: variables.keySet()) {
        setVariable(variableName, variables.get(variableName));
      }
    }
  }

  public void setVariable(String variableName, Object value) {
    ensureVariablesInitialized();
    if (variables.containsKey(variableName)) {
      setVariableLocally(variableName, value);
    } else {
      ensureParentInitialized();
      if (parent!=null) {
        parent.setVariable(variableName, value);
      } else {
        setVariableLocally(variableName, value);
      }
    }
  }

  public void setVariableLocally(String variableName, Object value) {
    log.fine("setting variable '"+variableName+"' to value '"+value+"' on "+this);
    variables.put(variableName, value);
  }

  public boolean hasVariable(String variableName) {
    ensureVariablesInitialized();
    if (variables.containsKey(variableName)) {
      return true;
    }
    ensureParentInitialized();
    if (parent!=null) {
      return parent.hasVariable(variableName);
    }
    return false;
  }

  protected void ensureVariablesInitialized() {
    if (variables==null) {
      variables = new HashMap<String, Object>();
    }
  }

  // toString /////////////////////////////////////////////////////////////////

  public String toString() {
    if (isProcessInstance()) {
      return "ProcessInstance["+getToStringIdentity()+"]";
    } else {
      return (isEventScope? "EventScope":"")+(isConcurrent? "Concurrent" : "")+(isScope() ? "Scope" : "")+"Execution["+getToStringIdentity()+"]";
    }
  }

  protected String getToStringIdentity() {
    return Integer.toString(System.identityHashCode(this));
  }

  // customized getters and setters ///////////////////////////////////////////

  public boolean isProcessInstance() {
    ensureParentInitialized();
    return parent==null;
  }

  public void inactivate() {
    this.isActive = false;
  }

  // allow for subclasses to expose a real id /////////////////////////////////

  public String getId() {
    return null;
  }

  // getters and setters //////////////////////////////////////////////////////

  public TransitionImpl getTransition() {
    return transition;
  }
  public void setTransition(TransitionImpl transition) {
    this.transition = transition;
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

  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
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
  public ExecutionImpl getReplacedBy() {
    return replacedBy;
  }
  public void setReplacedBy(InterpretableExecution replacedBy) {
    this.replacedBy = (ExecutionImpl) replacedBy;
    // set execution to this activity instance
    replacedBy.setActivityInstanceId(this.activityInstanceId);
  }

  public void replace(InterpretableExecution execution) {
    this.activityInstanceId = execution.getActivityInstanceId();
    execution.leaveActivityInstance();
  }

  public void setExecutions(List<ExecutionImpl> executions) {
    this.executions = executions;
  }
  public boolean isDeleteRoot() {
    return deleteRoot;
  }

  public String getCurrentActivityId() {
    String currentActivityId = null;
    if (this.activity != null) {
      currentActivityId = activity.getId();
    }
    return currentActivityId;
  }

  public String getCurrentActivityName() {
    String currentActivityName = null;
    if (this.activity != null) {
      currentActivityName = (String) activity.getProperty("name");
    }
    return currentActivityName;
  }

  public Object getVariableLocal(String variableName) {
    return null;
  }

  public Set<String> getVariableNames() {
    return null;
  }

  public Set<String> getVariableNamesLocal() {
    return null;
  }

  public Map<String, Object> getVariablesLocal() {
    return null;
  }

  public boolean hasVariableLocal(String variableName) {
    return false;
  }

  public boolean hasVariables() {
    return false;
  }

  public boolean hasVariablesLocal() {
    return false;
  }

  public void removeVariable(String variableName) {
  }

  public void removeVariableLocal(String variableName) {
  }

  public void removeVariables(Collection<String> variableNames) {
  }

  public void removeVariablesLocal(Collection<String> variableNames) {
  }

  public void removeVariables() {
  }

  public void removeVariablesLocal() {
  }

  public void deleteVariablesLocal() {
  }

  public void setVariableLocal(String variableName, Object value) {
  }

  public void setVariablesLocal(Map<String, ? extends Object> variables) {
  }

  public boolean isEventScope() {
    return isEventScope;
  }

  public void setEventScope(boolean isEventScope) {
    this.isEventScope = isEventScope;
  }

  public ProcessInstanceStartContext getProcessInstanceStartContext() {
    return processInstanceStartContext;
  }

  public void disposeProcessInstanceStartContext() {
    processInstanceStartContext = null;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public PvmActivity getNextActivity() {
    return nextActivity;
  }

  public void setTransitionBeingTaken(TransitionImpl transitionBeingTaken) {
    this.transitionBeingTaken = transitionBeingTaken;
  }

  public String getCurrentTransitionId() {
    if(transition != null) {
      return transition.getId();
    } else {
      return null;
    }
  }

}
