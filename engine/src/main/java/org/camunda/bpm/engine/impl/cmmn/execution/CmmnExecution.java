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

import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.ACTIVE;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.AVAILABLE;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.CLOSED;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.COMPLETED;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.DISABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.ENABLED;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.FAILED;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.SUSPENDED;
import static org.camunda.bpm.engine.impl.cmmn.execution.PlanItemState.TERMINATED;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_NOTIFY_LISTENER_CREATE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.PLAN_ITEM_NOTIFY_LISTENER_COMPLETE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.PLAN_ITEM_NOTIFY_LISTENER_CREATE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.PLAN_ITEM_NOTIFY_LISTENER_DISABLE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.PLAN_ITEM_NOTIFY_LISTENER_ENABLE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.PLAN_ITEM_NOTIFY_LISTENER_MANUAL_START;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.PLAN_ITEM_NOTIFY_LISTENER_RE_ENABLE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.PLAN_ITEM_NOTIFY_LISTENER_START;

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

  /** the unique id of the current activity instance */
  protected String activityInstanceId;

  protected int previousState = -1;

  protected int state;

  public CmmnExecution() {
  }

  // plan items ///////////////////////////////////////////////////////////////

  public abstract List<? extends CmmnExecution> getPlanItems();

  public CmmnExecution findPlanItem(String activityId) {
    if ((getActivity()!=null) && (getActivity().getId().equals(activityId))) {
     return this;
   }
   for (CmmnExecution nestedExecution : getPlanItems()) {
     CmmnExecution result = nestedExecution.findPlanItem(activityId);
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

  public boolean isCaseInstance() {
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

  // activity instance id //////////////////////////////////////////////////////

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  protected abstract String generateActivityInstanceId(String activityId);

  // next activity /////////////////////////////////////////////////////////////

  public CmmnActivity getNextActivity() {
    return nextActivity;
  }

  public void setNextActivity(CmmnActivity nextActivity) {
    this.nextActivity = nextActivity;
  }

  // parent activity instance id ////////////////////////////////

  public String getParentActivityInstanceId() {
    if(isCaseInstance()) {
      return id;

    } else {
      CmmnExecution parent = getParent();

      if (parent != null) {
        return parent.getActivityInstanceId();
      }

      return null;
    }
  }

  // variables ////////////////////////////////////////////

  protected CoreVariableScope getParentVariableScope() {
    return getParent();
  }

  // state /////////////////////////////////////////////////////

  public int getState() {
    return state;
  }

  public void setState(PlanItemState state) {
    previousState = this.state;
    this.state = state.getStateCode();
  }

  public void setState(int state) {
    this.state = state;
  }

  public boolean isAvailable() {
    return state == AVAILABLE.getStateCode();
  }

  public boolean isEnabled() {
    return state == ENABLED.getStateCode();
  }

  public boolean isDisabled() {
    return state == DISABLED.getStateCode();
  }

  public boolean isActive() {
    return state == ACTIVE.getStateCode();
  }

  public boolean isCompleted() {
    return state == COMPLETED.getStateCode();
  }

  public boolean isSuspended() {
    return state == SUSPENDED.getStateCode();
  }

  public boolean isTerminated() {
    return state == TERMINATED.getStateCode();
  }

  public boolean isFailed() {
    return state == FAILED.getStateCode();
  }

  public boolean isClosed() {
    return state == CLOSED.getStateCode();
  }

  // previous state /////////////////////////////////////////////

  public int getPreviousState() {
    return previousState;
  }

  public void setPreviousState(int previousState) {
    this.previousState = state;
  }

  // state transition ///////////////////////////////////////////

  /** Creates new case instance without businessKey and variables */
  public void create() {
    create(null, null);
  }

  /** Creates new case instance with variables but without businessKey */
  public void create(Map<String, Object> variables) {
    create(null, variables);
  }

  /** Creates new case instance with businessKey but without variables */
  public void create(String businessKey) {
    create(businessKey, null);
  }

  /** Creates new case instance with businessKey and variables */
  public void create(String businessKey, Map<String, Object> variables) {

    if(variables != null) {
      setVariables(variables);
    }

    if(businessKey != null) {
      setBusinessKey(businessKey);
    }

    // the case instance is "ACTIVE" after creation
    setState(ACTIVE);

    performOperation(CASE_NOTIFY_LISTENER_CREATE);
  }

  public void create(List<CmmnActivity> activities) {
    // this execution must be in the active state
    if (!isActive()) {
      // TODO: provide a proper exception message
      throw new ProcessEngineException();
    }

    List<CmmnExecution> children = new ArrayList<CmmnExecution>();

    for (CmmnActivity currentActivity : activities) {
      CmmnExecution child = createPlanItem(currentActivity);
      children.add(child);
    }

    for (CmmnExecution child : children) {
      if (isActive()) {
        child.performOperation(PLAN_ITEM_NOTIFY_LISTENER_CREATE);
      } else {
        log.fine("Not taking child '" + child + "', parent plan item has ended.");
      }
    }
  }

  protected abstract CmmnExecution createPlanItem(CmmnActivity activity);

  protected abstract CmmnExecution newPlanItem();

  public void enable() {
    transition(AVAILABLE, ENABLED, PLAN_ITEM_NOTIFY_LISTENER_ENABLE);
  }

  public void disable() {
    transition(ENABLED, DISABLED, PLAN_ITEM_NOTIFY_LISTENER_DISABLE);
  }

  public void reenable() {
    transition(DISABLED, ENABLED, PLAN_ITEM_NOTIFY_LISTENER_RE_ENABLE);
  }

  public void manualStart() {
    transition(ENABLED, ACTIVE, PLAN_ITEM_NOTIFY_LISTENER_MANUAL_START);
  }

  public void start() {
    transition(AVAILABLE, ACTIVE, PLAN_ITEM_NOTIFY_LISTENER_START);
  }

  public void complete() {
    transition(ACTIVE, COMPLETED, PLAN_ITEM_NOTIFY_LISTENER_COMPLETE);
  }

  protected void transition(PlanItemState from, PlanItemState to, CmmnAtomicOperation nextOperation) {
    // is this execution in the expected state
    if (state != from.getStateCode()) {
      // if not throw an exception
      // TODO: provide proper exception message
      throw new ProcessEngineException();
    }

    // perform transition: set the new state
    setState(to);

    // if a next operation is provided, execute it.
    if (nextOperation != null) {
      performOperation(nextOperation);
    }
  }

  // toString() /////////////////////////////////////////////////

  public String toString() {
    if (isCaseInstance()) {
      return "CaseInstance[" + getToStringIdentity() + "]";
    } else {
      return "CmmnExecution["+getToStringIdentity() + "]";
    }
  }

  protected String getToStringIdentity() {
    return id;
  }

}
