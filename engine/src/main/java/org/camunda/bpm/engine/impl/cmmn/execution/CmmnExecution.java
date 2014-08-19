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
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.NEW;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.SUSPENDED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATED;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_COMPLETE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_CREATE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_DELETE_CASCADE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_DISABLE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_ENABLE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_EXIT;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_MANUAL_COMPLETE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_MANUAL_START;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_OCCUR;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_PARENT_RESUME;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_PARENT_SUSPEND;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_PARENT_TERMINATE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_RESUME;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_RE_ACTIVATE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_RE_ENABLE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_START;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_SUSPEND;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_TERMINATE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_INSTANCE_CLOSE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_INSTANCE_CREATE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.variable.CorePersistentVariableScope;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Roman Smirnov
 *
 */
public abstract class CmmnExecution extends CoreExecution implements CmmnCaseInstance {

  private static final long serialVersionUID = 1L;

  protected transient CmmnCaseDefinition caseDefinition;

  // current position //////////////////////////////////////

  /** current activity */
  protected transient CmmnActivity activity;

  /** the activity which is to be started next */
  protected transient CmmnActivity nextActivity;

  protected boolean required;

  protected int previousState;

  protected int currentState = NEW.getStateCode();

  public CmmnExecution() {
  }

  // plan items ///////////////////////////////////////////////////////////////

  public abstract List<? extends CmmnExecution> getCaseExecutions();

  protected abstract List<? extends CmmnExecution> getCaseExecutionsInternal();

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

  // sub process instance ////////////////////////////////////////////////////

  public abstract PvmExecutionImpl getSubProcessInstance();

  public abstract void setSubProcessInstance(PvmExecutionImpl subProcessInstance);

  public abstract PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition);

  public abstract PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey);

  public abstract PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey, String caseInstanceId);

  // sub-/super- case instance ////////////////////////////////////////////////////

  public abstract CmmnExecution getSubCaseInstance();

  public abstract void setSubCaseInstance(CmmnExecution subCaseInstance);

  public abstract CmmnExecution createSubCaseInstance(CmmnCaseDefinition caseDefinition);

  public abstract CmmnExecution createSubCaseInstance(CmmnCaseDefinition caseDefinition, String businessKey);

  public abstract CmmnExecution getSuperCaseExecution();

  public abstract void setSuperCaseExecution(CmmnExecution superCaseExecution);

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

  public CorePersistentVariableScope getParentVariableScope() {
    return getParent();
  }

  //delete/remove /////////////////////////////////////////////////////

  public void deleteCascade() {
   performOperation(CASE_EXECUTION_DELETE_CASCADE);
  }

  public void remove() {
   CmmnExecution parent = getParent();
   if (parent!=null) {
     parent.getCaseExecutionsInternal().remove(this);
   }
  }

  // required //////////////////////////////////////////////////

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  // state /////////////////////////////////////////////////////

  public CaseExecutionState getCurrentState() {
    return CaseExecutionState.CASE_EXECUTION_STATES.get(getState());
  }

  public void setCurrentState(CaseExecutionState currentState) {
    previousState = this.currentState;
    this.currentState = currentState.getStateCode();
  }

  public int getState() {
    return currentState;
  }

  public void setState(int state) {
    this.currentState = state;
  }

  public boolean isNew() {
    return currentState == NEW.getStateCode();
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

  public CaseExecutionState getPreviousState() {
    return CaseExecutionState.CASE_EXECUTION_STATES.get(getPrevious());
  }

  public int getPrevious() {
    return previousState;
  }

  public void setPrevious(int previous) {
    this.previousState = previous;
  }

  // state transition ///////////////////////////////////////////

  public void create() {
    create(null);
  }

  public void create(Map<String, Object> variables) {
    if(variables != null) {
      setVariables(variables);
    }

    performOperation(CASE_INSTANCE_CREATE);
  }

  public void createChildExecutions(List<CmmnActivity> activities) {
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
        child.performOperation(CASE_EXECUTION_CREATE);
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
    performOperation(CASE_EXECUTION_ENABLE);
  }

  public void disable() {
    performOperation(CASE_EXECUTION_DISABLE);
  }

  public void reenable() {
    performOperation(CASE_EXECUTION_RE_ENABLE);
  }

  public void manualStart() {
    performOperation(CASE_EXECUTION_MANUAL_START);
  }

  public void start() {
    performOperation(CASE_EXECUTION_START);
  }

  public void complete() {
    performOperation(CASE_EXECUTION_COMPLETE);
  }

  public void manualComplete() {
    performOperation(CASE_EXECUTION_MANUAL_COMPLETE);
  }

  public void occur() {
    performOperation(CASE_EXECUTION_OCCUR);
  }

  public void terminate() {
    performOperation(CASE_EXECUTION_TERMINATE);
  }

  public void parentTerminate() {
    performOperation(CASE_EXECUTION_PARENT_TERMINATE);
  }

  public void exit() {
    performOperation(CASE_EXECUTION_EXIT);
  }

  public void suspend() {
    performOperation(CASE_EXECUTION_SUSPEND);
  }

  public void parentSuspend() {
    performOperation(CASE_EXECUTION_PARENT_SUSPEND);
  }

  public void resume() {
    performOperation(CASE_EXECUTION_RESUME);
  }

  public void parentResume() {
    performOperation(CASE_EXECUTION_PARENT_RESUME);
  }

  public void reactivate() {
    performOperation(CASE_EXECUTION_RE_ACTIVATE);
  }

  public void close() {
    performOperation(CASE_INSTANCE_CLOSE);
  }

  // toString() /////////////////////////////////////////////////

  public String toString() {
    if (isCaseInstanceExecution()) {
      return "CaseInstance["+getToStringIdentity()+"]";
    } else {
      return "CmmnExecution["+getToStringIdentity() + "]";
    }
  }

  protected String getToStringIdentity() {
    return id;
  }

}
