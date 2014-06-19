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
package org.camunda.bpm.engine.impl.cmmn.execution;

import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ACTIVE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.AVAILABLE;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.CLOSED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.COMPLETED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.DISABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.ENABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.FAILED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.SUSPENDED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATED;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_DELETE_CASCADE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_NOTIFY_LISTENER_COMPLETE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_NOTIFY_LISTENER_CREATE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_NOTIFY_LISTENER_DISABLE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_NOTIFY_LISTENER_ENABLE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_NOTIFY_LISTENER_MANUAL_START;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_NOTIFY_LISTENER_RE_ENABLE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_NOTIFY_LISTENER_START;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_INSTANCE_NOTIFY_LISTENER_CREATE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableScope;

/**
 * @author Roman Smirnov
 *
 */
public abstract class CmmnExecution extends CoreExecution implements CmmnCaseInstance {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(CmmnExecution.class.getName());

  protected transient CmmnCaseDefinition caseDefinition;

  // current position //////////////////////////////////////

  /** current activity */
  protected transient CmmnActivity activity;

  /** the activity which is to be started next */
  protected transient CmmnActivity nextActivity;

  protected int previousState;

  protected int currentState;

  public CmmnExecution() {
  }

  // plan items ///////////////////////////////////////////////////////////////

  public abstract List<? extends CmmnExecution> getCaseExecutions();

  public CmmnExecution findCaseExecution(String activityId) {
    if ((getActivity()!=null) && (getActivity().getId().equals(activityId))) {
     return this;
   }
   for (CmmnExecution nestedExecution : getCaseExecutions()) {
     CmmnExecution result = nestedExecution.findCaseExecution(activityId);
     if (result != null) {
       return result;
     }
   }
   return null;
  }

  // business key ////////////////////////////////////////////////////////////

  public String getCaseBusinessKey() {
    return getCaseInstance().getBusinessKey();
  }

  // case definition ///////////////////////////////////////////////////////

  public CmmnCaseDefinition getCaseDefinition() {
    return caseDefinition;
  }

  public void setCaseDefinition(CmmnCaseDefinition caseDefinition) {
    this.caseDefinition = caseDefinition;
  }

  // case instance /////////////////////////////////////////////////////////

  /** ensures initialization and returns the process instance. */
  public abstract CmmnExecution getCaseInstance();

  public abstract void setCaseInstance(CmmnExecution caseInstance);

  public boolean isCaseInstanceExecution() {
    return getParent() == null;
  }

  // case instance id /////////////////////////////////////////////////////////

  /** ensures initialization and returns the process instance. */
  public String getCaseInstanceId() {
    return getCaseInstance().getId();
  }

  // parent ///////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the parent */
  public abstract CmmnExecution getParent();

  public abstract void setParent(CmmnExecution parent);

  // activity /////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the activity */
  public CmmnActivity getActivity() {
    return activity;
  }

  public void setActivity(CmmnActivity activity) {
    this.activity = activity;
  }

  // next activity /////////////////////////////////////////////////////////////

  public CmmnActivity getNextActivity() {
    return nextActivity;
  }

  public void setNextActivity(CmmnActivity nextActivity) {
    this.nextActivity = nextActivity;
  }

  // variables ////////////////////////////////////////////

  public CoreVariableScope getParentVariableScope() {
    return getParent();
  }

  //delete/remove /////////////////////////////////////////////////////

  public void deleteCascade() {
   performOperation(CASE_EXECUTION_DELETE_CASCADE);
  }

  public void remove() {
   CmmnExecution parent = getParent();
   if (parent!=null) {
     parent.getCaseExecutions().remove(this);
   }
  }

  // state /////////////////////////////////////////////////////

  public int getCurrentState() {
    return currentState;
  }

  public void setCurrentState(CaseExecutionState currentState) {
    previousState = this.currentState;
    this.currentState = currentState.getStateCode();
  }

  public void setCurrentState(int currentState) {
    this.currentState = currentState;
  }

  public boolean isAvailable() {
    return currentState == AVAILABLE.getStateCode();
  }

  public boolean isEnabled() {
    return currentState == ENABLED.getStateCode();
  }

  public boolean isDisabled() {
    return currentState == DISABLED.getStateCode();
  }

  public boolean isActive() {
    return currentState == ACTIVE.getStateCode();
  }

  public boolean isCompleted() {
    return currentState == COMPLETED.getStateCode();
  }

  public boolean isSuspended() {
    return currentState == SUSPENDED.getStateCode();
  }

  public boolean isTerminated() {
    return currentState == TERMINATED.getStateCode();
  }

  public boolean isFailed() {
    return currentState == FAILED.getStateCode();
  }

  public boolean isClosed() {
    return currentState == CLOSED.getStateCode();
  }

  // previous state /////////////////////////////////////////////

  public int getPreviousState() {
    return previousState;
  }

  public void setPreviousState(int previousState) {
    this.previousState = currentState;
  }

  // state transition ///////////////////////////////////////////

  public void create() {
    create(null, null);
  }

  public void create(Map<String, Object> variables) {
    create(null, variables);
  }

  public void create(String businessKey) {
    create(businessKey, null);
  }

  public void create(String businessKey, Map<String, Object> variables) {
    if(variables != null) {
      setVariables(variables);
    }

    if(businessKey != null) {
      setBusinessKey(businessKey);
    }

    // the case instance is "ACTIVE" after creation
    setCurrentState(ACTIVE);

    performOperation(CASE_INSTANCE_NOTIFY_LISTENER_CREATE);
  }

  public void createChildExecutions(List<CmmnActivity> activities) {
    // this execution must be in the active state
    if (!isActive()) {
      String message = "Cannot create child case executions on case execution '"
          +id+"' as parent because the case execution is not active.";
      throw new ProcessEngineException(message);
    }

    List<CmmnExecution> children = new ArrayList<CmmnExecution>();

    // first create new child case executions
    for (CmmnActivity currentActivity : activities) {
      CmmnExecution child = createCaseExecution(currentActivity);
      children.add(child);
    }

    // then notify create listener for each created
    // child case execution
    for (CmmnExecution child : children) {
      if (isActive()) {
        // only do this when the this case execution is
        // still active
        child.performOperation(CASE_EXECUTION_NOTIFY_LISTENER_CREATE);
      } else {
        // if this case execution is not active anymore,
        // then stop notifying create listener and executing
        // of each child case execution
        break;
      }
    }
  }

  protected abstract CmmnExecution createCaseExecution(CmmnActivity activity);

  protected abstract CmmnExecution newCaseExecution();

  public void enable() {
    if (isCaseInstanceExecution()) {
      String message = "Cannot perform transition on case execution '"+id+"': it is not possible to enable a case instance.";
      throw new ProcessEngineException(message);
    }

    transition(AVAILABLE, ENABLED, CASE_EXECUTION_NOTIFY_LISTENER_ENABLE);
  }

  public void disable() {
    if (isCaseInstanceExecution()) {
      String message = "Cannot perform transition on case execution '"+id+"': it is not possible to disable a case instance.";
      throw new ProcessEngineException(message);
    }

    transition(ENABLED, DISABLED, CASE_EXECUTION_NOTIFY_LISTENER_DISABLE);
  }

  public void reenable() {
    if (isCaseInstanceExecution()) {
      String message = "Cannot perform transition on case execution '"+id+"': it is not possible to re-enable a case instance.";
      throw new ProcessEngineException(message);
    }

    transition(DISABLED, ENABLED, CASE_EXECUTION_NOTIFY_LISTENER_RE_ENABLE);
  }

  public void manualStart() {
    if (isCaseInstanceExecution()) {
      String message = "Cannot perform transition on case execution '"+id+"': it is not possible to start a case instance manually.";
      throw new ProcessEngineException(message);
    }

    transition(ENABLED, ACTIVE, CASE_EXECUTION_NOTIFY_LISTENER_MANUAL_START);
  }

  public void start() {
    if (isCaseInstanceExecution()) {
      String message = "Cannot perform transition on case execution '"+id+"': it is not possible to start a case instance.";
      throw new ProcessEngineException(message);
    }
    transition(AVAILABLE, ACTIVE, CASE_EXECUTION_NOTIFY_LISTENER_START);
  }

  public void complete() {
    transition(ACTIVE, COMPLETED, CASE_EXECUTION_NOTIFY_LISTENER_COMPLETE);
  }

  protected void transition(CaseExecutionState from, CaseExecutionState target, CmmnAtomicOperation nextOperation) {
    CaseExecutionState currentFrom = CaseExecutionState.CASE_EXECUTION_STATES.get(currentState);

    // is this case execution already in the target state
    if (currentState == target.getStateCode()) {
      String message = "Cannot perform transition on case execution '"+id+"': the case execution is already in the state '"+currentFrom+"'.";
      throw new ProcessEngineException(message);
    } else
    // is this case execution in the expected state
    if (currentState != from.getStateCode()) {
      // if not throw an exception
      String message = "Cannot perform transition on case execution '"+id+"' to the state '"+target+"': the expected current state is '"+from+"', but was '"+currentFrom+"'.";
      throw new ProcessEngineException(message);
    }

    // perform transition: set the new state
    setCurrentState(target);

    // if a next operation is provided, execute it.
    if (nextOperation != null) {
      performOperation(nextOperation);
    }
  }

  // toString() /////////////////////////////////////////////////

  public String toString() {
    if (isCaseInstanceExecution()) {
      return "CaseInstance[" + getToStringIdentity() + "]";
    } else {
      return "CmmnExecution["+getToStringIdentity() + "]";
    }
  }

  protected String getToStringIdentity() {
    return id;
  }

}
