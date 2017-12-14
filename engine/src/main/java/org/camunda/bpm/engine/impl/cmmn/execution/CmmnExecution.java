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
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.SUSPENDING_ON_PARENT_SUSPENSION;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.SUSPENDING_ON_SUSPENSION;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATED;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATING_ON_EXIT;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATING_ON_PARENT_TERMINATION;
import static org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState.TERMINATING_ON_TERMINATION;
import static org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration.IF_PART;
import static org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration.PLAN_ITEM_ON_PART;
import static org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration.VARIABLE_ON_PART;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_COMPLETE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_CREATE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_DELETE_CASCADE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_DISABLE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_ENABLE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_EXIT;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_FIRE_ENTRY_CRITERIA;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_FIRE_EXIT_CRITERIA;
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
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_SUSPENDING_ON_PARENT_SUSPENSION;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_SUSPENDING_ON_SUSPENSION;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_TERMINATE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_TERMINATING_ON_EXIT;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_TERMINATING_ON_PARENT_TERMINATION;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_EXECUTION_TERMINATING_ON_TERMINATION;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_INSTANCE_CLOSE;
import static org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation.CASE_INSTANCE_CREATE;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureInstanceOf;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.camunda.bpm.engine.delegate.CaseVariableListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.delegate.VariableListener;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnBehaviorLogger;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartEntity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnIfPartDeclaration;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnOnPartDeclaration;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnVariableOnPartDeclaration;
import org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.task.TaskDecorator;
import org.camunda.bpm.engine.impl.variable.listener.CaseVariableListenerInvocation;
import org.camunda.bpm.engine.impl.variable.listener.DelegateCaseVariableInstanceImpl;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Roman Smirnov
 *
 */
public abstract class CmmnExecution extends CoreExecution implements CmmnCaseInstance {

  protected static final CmmnBehaviorLogger LOG = ProcessEngineLogger.CMNN_BEHAVIOR_LOGGER;

  private static final long serialVersionUID = 1L;

  protected transient CmmnCaseDefinition caseDefinition;

  // current position //////////////////////////////////////

  /** current activity */
  protected transient CmmnActivity activity;

  protected boolean required = false;

  protected int previousState;

  protected int currentState = NEW.getStateCode();

  protected Queue<VariableEvent> variableEventsQueue;

  protected transient TaskEntity task;

  /**
   * This property will be used if <code>this</code>
   * {@link CmmnExecution} is in state {@link CaseExecutionState#NEW}
   * to note that an entry criterion is satisfied.
   */
  protected boolean entryCriterionSatisfied = false;

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

  // task /////////////////////////////////////////////////////////////////////

  public TaskEntity getTask() {
    return this.task;
  }

  public void setTask(Task task) {
    this.task = (TaskEntity) task;
  }

  public TaskEntity createTask(TaskDecorator taskDecorator) {
    TaskEntity task = TaskEntity.createAndInsert(this);

    setTask(task);

    taskDecorator.decorate(task, this);

    Context.getCommandContext()
      .getHistoricTaskInstanceManager()
      .createHistoricTask(task);

    // All properties set, now firing 'create' event
    task.fireEvent(TaskListener.EVENTNAME_CREATE);

    return task;
  }

  // super execution  ////////////////////////////////////////////////////////

  public abstract PvmExecutionImpl getSuperExecution();

  public abstract void setSuperExecution(PvmExecutionImpl superExecution);

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

  // sentry //////////////////////////////////////////////////////////////////

  // sentry: (1) create and initialize sentry parts

  protected abstract CmmnSentryPart newSentryPart();

  protected abstract void addSentryPart(CmmnSentryPart sentryPart);

  public void createSentryParts() {
    CmmnActivity activity = getActivity();
    ensureNotNull("Case execution '"+id+"': has no current activity", "activity", activity);

    List<CmmnSentryDeclaration> sentries = activity.getSentries();

    if (sentries != null && !sentries.isEmpty()) {

      for (CmmnSentryDeclaration sentryDeclaration : sentries) {

        CmmnIfPartDeclaration ifPartDeclaration = sentryDeclaration.getIfPart();
        if (ifPartDeclaration != null) {
          CmmnSentryPart ifPart = createIfPart(sentryDeclaration, ifPartDeclaration);
          addSentryPart(ifPart);
        }

        List<CmmnOnPartDeclaration> onPartDeclarations = sentryDeclaration.getOnParts();

        for (CmmnOnPartDeclaration onPartDeclaration : onPartDeclarations) {
          CmmnSentryPart onPart = createOnPart(sentryDeclaration, onPartDeclaration);
          addSentryPart(onPart);
        }

        List<CmmnVariableOnPartDeclaration> variableOnPartDeclarations = sentryDeclaration.getVariableOnParts();
        for(CmmnVariableOnPartDeclaration variableOnPartDeclaration: variableOnPartDeclarations) {
          CmmnSentryPart variableOnPart = createVariableOnPart(sentryDeclaration, variableOnPartDeclaration);
          addSentryPart(variableOnPart);
        }

      }
    }
  }

  protected CmmnSentryPart createOnPart(CmmnSentryDeclaration sentryDeclaration, CmmnOnPartDeclaration onPartDeclaration) {
    CmmnSentryPart sentryPart = createSentryPart(sentryDeclaration, PLAN_ITEM_ON_PART);

    // set the standard event
    String standardEvent = onPartDeclaration.getStandardEvent();
    sentryPart.setStandardEvent(standardEvent);

    // set source case execution
    CmmnActivity source = onPartDeclaration.getSource();
    ensureNotNull("The source of sentry '"+sentryDeclaration.getId()+"' is null.", "source", source);

    String sourceActivityId = source.getId();
    sentryPart.setSource(sourceActivityId);

    // TODO: handle also sentryRef!!! (currently not implemented on purpose)

    return sentryPart;
  }

  protected CmmnSentryPart createIfPart(CmmnSentryDeclaration sentryDeclaration, CmmnIfPartDeclaration ifPartDeclaration) {
    return createSentryPart(sentryDeclaration, IF_PART);
  }

  protected CmmnSentryPart createVariableOnPart(CmmnSentryDeclaration sentryDeclaration, CmmnVariableOnPartDeclaration variableOnPartDeclaration) {
    CmmnSentryPart sentryPart = createSentryPart(sentryDeclaration, VARIABLE_ON_PART);

    // set the variable event
    String variableEvent = variableOnPartDeclaration.getVariableEvent();
    sentryPart.setVariableEvent(variableEvent);

    // set the variable name
    String variableName = variableOnPartDeclaration.getVariableName();
    sentryPart.setVariableName(variableName);

    return sentryPart;
  }

  protected CmmnSentryPart createSentryPart(CmmnSentryDeclaration sentryDeclaration, String type) {
    CmmnSentryPart newSentryPart = newSentryPart();

    // set the type
    newSentryPart.setType(type);

    // set the case instance and case execution
    newSentryPart.setCaseInstance(getCaseInstance());
    newSentryPart.setCaseExecution(this);

    // set sentry id
    String sentryId = sentryDeclaration.getId();
    newSentryPart.setSentryId(sentryId);

    return newSentryPart;
  }

  // sentry: (2) handle transitions

  public void handleChildTransition(CmmnExecution child, String transition) {
    // Step 1: collect all affected sentries
    List<String> affectedSentries = collectAffectedSentries(child, transition);

    // Step 2: fire force update on all case sentry part
    // contained by a affected sentry to provoke an
    // OptimisticLockingException
    forceUpdateOnSentries(affectedSentries);

    // Step 3: check each affected sentry whether it is satisfied.
    // the returned list contains all satisfied sentries
    List<String> satisfiedSentries = getSatisfiedSentries(affectedSentries);

    // Step 4: reset sentries -> satisfied == false
    resetSentries(satisfiedSentries);

    // Step 5: fire satisfied sentries
    fireSentries(satisfiedSentries);

  }

  public void fireIfOnlySentryParts() {
    // the following steps are a workaround, because setVariable()
    // does not check nor fire a sentry!!!
    Set<String> affectedSentries = new HashSet<String>();
    List<CmmnSentryPart> sentryParts = collectSentryParts(getSentries());
    for (CmmnSentryPart sentryPart : sentryParts) {
      if (isNotSatisfiedIfPartOnly(sentryPart)) {
        affectedSentries.add(sentryPart.getSentryId());
      }
    }

    // Step 7: check each not affected sentry whether it is satisfied
    List<String> satisfiedSentries = getSatisfiedSentries(new ArrayList<String>(affectedSentries));

    // Step 8: reset sentries -> satisfied == false
    resetSentries(satisfiedSentries);

    // Step 9: fire satisfied sentries
    fireSentries(satisfiedSentries);
  }

  public void handleVariableTransition(String variableName, String transition) {
    Map<String, List<CmmnSentryPart>> sentries = collectAllSentries();

    List<CmmnSentryPart> sentryParts = collectSentryParts(sentries);

    List<String> affectedSentries = collectAffectedSentriesWithVariableOnParts(variableName, transition, sentryParts);

    List<CmmnSentryPart> affectedSentryParts = getAffectedSentryParts(sentries,affectedSentries);
    forceUpdateOnCaseSentryParts(affectedSentryParts);

    List<String> allSentries = new ArrayList<String>(sentries.keySet());

    List<String> satisfiedSentries = getSatisfiedSentriesInExecutionTree(allSentries, sentries);

    List<CmmnSentryPart> satisfiedSentryParts = getAffectedSentryParts(sentries, satisfiedSentries);
    resetSentryParts(satisfiedSentryParts);

    fireSentries(satisfiedSentries);

  }

  protected List<String> collectAffectedSentries(CmmnExecution child, String transition) {
    List<? extends CmmnSentryPart> sentryParts = getCaseSentryParts();

    List<String> affectedSentries = new ArrayList<String>();

    for (CmmnSentryPart sentryPart : sentryParts) {

      // necessary for backward compatibility
      String sourceCaseExecutionId = sentryPart.getSourceCaseExecutionId();
      String sourceRef = sentryPart.getSource();
      if (child.getActivityId().equals(sourceRef) || child.getId().equals(sourceCaseExecutionId)) {

        String standardEvent = sentryPart.getStandardEvent();
        if (transition.equals(standardEvent)) {
          addIdIfNotSatisfied(affectedSentries, sentryPart);
        }
      }
    }

    return affectedSentries;
  }

  protected boolean isNotSatisfiedIfPartOnly(CmmnSentryPart sentryPart) {
    return IF_PART.equals(sentryPart.getType())
        && getSentries().get(sentryPart.getSentryId()).size() == 1
        && !sentryPart.isSatisfied();
  }

  protected void addIdIfNotSatisfied(List<String> affectedSentries, CmmnSentryPart sentryPart) {
    if (!sentryPart.isSatisfied()) {
      // if it is not already satisfied, then set the
      // current case sentry part to satisfied (=true).
      String sentryId = sentryPart.getSentryId();
      sentryPart.setSatisfied(true);

      // collect the id of affected sentry.
      if (!affectedSentries.contains(sentryId)) {
        affectedSentries.add(sentryId);
      }
    }
  }

  protected List<String> collectAffectedSentriesWithVariableOnParts(String variableName, String variableEvent, List<CmmnSentryPart> sentryParts) {

    List<String> affectedSentries = new ArrayList<String>();

    for (CmmnSentryPart sentryPart : sentryParts) {

      String sentryVariableName = sentryPart.getVariableName();
      String sentryVariableEvent = sentryPart.getVariableEvent();
      CmmnExecution execution = sentryPart.getCaseExecution();
      if (VARIABLE_ON_PART.equals(sentryPart.getType()) && sentryVariableName.equals(variableName)
        && sentryVariableEvent.equals(variableEvent)
        && !hasVariableWithSameNameInParent(execution, sentryVariableName)) {

        addIdIfNotSatisfied(affectedSentries, sentryPart);
      }
    }

    return affectedSentries;
  }

  protected boolean hasVariableWithSameNameInParent(CmmnExecution execution, String variableName) {
    while(execution != null) {
      if (execution.getId().equals(getId())) {
        return false;
      }
      TypedValue variableTypedValue = execution.getVariableLocalTyped(variableName);
      if (variableTypedValue != null) {
        return true;
      }
      execution = execution.getParent();
    }
    return false;
  }

  protected Map<String,List<CmmnSentryPart>> collectAllSentries() {
    Map<String,List<CmmnSentryPart>> sentries = new HashMap<String, List<CmmnSentryPart>>();
    List<? extends CmmnExecution> caseExecutions = getCaseExecutions();
    for(CmmnExecution caseExecution: caseExecutions) {
      sentries.putAll(caseExecution.collectAllSentries());
    }
    sentries.putAll(getSentries());
    return sentries;
  }

  protected List<CmmnSentryPart> getAffectedSentryParts(Map<String,List<CmmnSentryPart>> allSentries, List<String> affectedSentries) {
    List<CmmnSentryPart> affectedSentryParts = new ArrayList<CmmnSentryPart>();
    for(String affectedSentryId: affectedSentries) {
      affectedSentryParts.addAll(allSentries.get(affectedSentryId));
    }
    return affectedSentryParts;
  }

  protected List<CmmnSentryPart> collectSentryParts(Map<String,List<CmmnSentryPart>> sentries) {
    List<CmmnSentryPart> sentryParts = new ArrayList<CmmnSentryPart>();
    for(String sentryId: sentries.keySet()) {
      sentryParts.addAll(sentries.get(sentryId));
    }
    return sentryParts;
  }

  protected void forceUpdateOnCaseSentryParts(List<CmmnSentryPart> sentryParts) {
    // set for each case sentry part forceUpdate flag to true to provoke
    // an OptimisticLockingException if different case sentry parts of the
    // same sentry has been satisfied concurrently.
    for (CmmnSentryPart sentryPart : sentryParts) {
      if (sentryPart instanceof CaseSentryPartEntity) {
        CaseSentryPartEntity sentryPartEntity = (CaseSentryPartEntity) sentryPart;
        sentryPartEntity.forceUpdate();
      }
    }
  }

  /**
   * Checks for each given sentry id whether the corresponding
   * sentry is satisfied.
   */
  protected List<String> getSatisfiedSentries(List<String> sentryIds) {
    List<String> result = new ArrayList<String>();

    if (sentryIds != null) {

      for (String sentryId : sentryIds) {

        if (isSentrySatisfied(sentryId)) {
          result.add(sentryId);
        }
      }
    }

    return result;
  }

  /**
   * Checks for each given sentry id in the execution tree whether the corresponding
   * sentry is satisfied.
   */
  protected List<String> getSatisfiedSentriesInExecutionTree(List<String> sentryIds, Map<String, List<CmmnSentryPart>> allSentries) {
    List<String> result = new ArrayList<String>();

    if (sentryIds != null) {

      for (String sentryId : sentryIds) {
        List<CmmnSentryPart> sentryParts = allSentries.get(sentryId);
        if (isSentryPartsSatisfied(sentryId, sentryParts)) {
          result.add(sentryId);
        }
      }
    }

    return result;
  }

  protected void forceUpdateOnSentries(List<String> sentryIds) {
    for (String sentryId : sentryIds) {
      List<? extends CmmnSentryPart> sentryParts = findSentry(sentryId);
      // set for each case sentry part forceUpdate flag to true to provoke
      // an OptimisticLockingException if different case sentry parts of the
      // same sentry has been satisfied concurrently.
      for (CmmnSentryPart sentryPart : sentryParts) {
        if (sentryPart instanceof CaseSentryPartEntity) {
          CaseSentryPartEntity sentryPartEntity = (CaseSentryPartEntity) sentryPart;
          sentryPartEntity.forceUpdate();
        }
      }
    }
  }

  protected void resetSentries(List<String> sentries) {
    for (String sentry : sentries) {
      List<CmmnSentryPart> parts = getSentries().get(sentry);
      for (CmmnSentryPart part : parts) {
        part.setSatisfied(false);
      }
    }
  }

  protected void resetSentryParts(List<CmmnSentryPart> parts) {
    for (CmmnSentryPart part : parts) {
      part.setSatisfied(false);
    }
  }

  protected void fireSentries(List<String> satisfiedSentries) {
    if (satisfiedSentries != null && !satisfiedSentries.isEmpty()) {
      // if there are satisfied sentries, trigger the associated
      // case executions

      // 1. propagate to all child case executions ///////////////////////////////////////////

      // collect the execution tree.
      ArrayList<CmmnExecution> children = new ArrayList<CmmnExecution>();
      collectCaseExecutionsInExecutionTree(children);

      for (CmmnExecution currentChild : children) {

        // check and fire first exitCriteria
        currentChild.checkAndFireExitCriteria(satisfiedSentries);

        // then trigger entryCriteria
        currentChild.checkAndFireEntryCriteria(satisfiedSentries);
      }

      // 2. check exit criteria of the case instance //////////////////////////////////////////

      if (isCaseInstanceExecution() && isActive()) {
        checkAndFireExitCriteria(satisfiedSentries);
      }

    }
  }

  protected void collectCaseExecutionsInExecutionTree(List<CmmnExecution> children) {
    for(CmmnExecution child: getCaseExecutions()) {
      child.collectCaseExecutionsInExecutionTree(children);
    }
    children.addAll(getCaseExecutions());
  }

  protected void checkAndFireExitCriteria(List<String> satisfiedSentries) {
    if (isActive()) {
      CmmnActivity activity = getActivity();
      ensureNotNull(PvmException.class, "Case execution '"+getId()+"': has no current activity.", "activity", activity);

      // trigger first exitCriteria
      List<CmmnSentryDeclaration> exitCriteria = activity.getExitCriteria();
      for (CmmnSentryDeclaration sentryDeclaration : exitCriteria) {

        if (sentryDeclaration != null && satisfiedSentries.contains(sentryDeclaration.getId())) {
          fireExitCriteria();
          break;
        }
      }
    }
  }

  protected void checkAndFireEntryCriteria(List<String> satisfiedSentries) {
    if (isAvailable() || isNew()) {
      // do that only, when this child case execution
      // is available

      CmmnActivity activity = getActivity();
      ensureNotNull(PvmException.class, "Case execution '"+getId()+"': has no current activity.", "activity", activity);

      List<CmmnSentryDeclaration> criteria = activity.getEntryCriteria();
      for (CmmnSentryDeclaration sentryDeclaration : criteria) {
        if (sentryDeclaration != null && satisfiedSentries.contains(sentryDeclaration.getId())) {
          if (isAvailable()) {
            fireEntryCriteria();
          }
          else {
            entryCriterionSatisfied = true;
          }
          break;
        }
      }
    }
  }

  public void fireExitCriteria() {
    performOperation(CASE_EXECUTION_FIRE_EXIT_CRITERIA);
  }

  public void fireEntryCriteria() {
    performOperation(CASE_EXECUTION_FIRE_ENTRY_CRITERIA);
  }

  // sentry: (3) helper

  public abstract List<? extends CmmnSentryPart> getCaseSentryParts();

  protected abstract List<? extends CmmnSentryPart> findSentry(String sentryId);

  protected abstract Map<String, List<CmmnSentryPart>> getSentries();

  public boolean isSentrySatisfied(String sentryId) {
    List<? extends CmmnSentryPart> sentryParts = findSentry(sentryId);
    return isSentryPartsSatisfied(sentryId, sentryParts);

  }

  protected boolean isSentryPartsSatisfied(String sentryId, List<? extends CmmnSentryPart> sentryParts) {
    // if part will be evaluated in the end
    CmmnSentryPart ifPart = null;

    if (sentryParts != null && !sentryParts.isEmpty()) {
      for (CmmnSentryPart sentryPart : sentryParts) {

        if (PLAN_ITEM_ON_PART.equals(sentryPart.getType())) {

          if (!sentryPart.isSatisfied()) {
            return false;
          }

        } else if (VARIABLE_ON_PART.equals(sentryPart.getType())) {
          if (!sentryPart.isSatisfied()) {
            return false;
          }
        } else { /* IF_PART.equals(sentryPart.getType) == true */

          ifPart = sentryPart;

          // once the ifPart has been satisfied the whole sentry is satisfied
          if (ifPart.isSatisfied()) {
            return true;
          }

        }

      }
    }

    if (ifPart != null) {

      CmmnExecution execution = ifPart.getCaseExecution();
      ensureNotNull("Case execution of sentry '"+ifPart.getSentryId() +"': is null", execution);

      CmmnActivity activity = ifPart.getCaseExecution().getActivity();
      ensureNotNull("Case execution '"+id+"': has no current activity", "activity", activity);

      CmmnSentryDeclaration sentryDeclaration = activity.getSentry(sentryId);
      ensureNotNull("Case execution '"+id+"': has no declaration for sentry '"+sentryId+"'", "sentryDeclaration", sentryDeclaration);

      CmmnIfPartDeclaration ifPartDeclaration = sentryDeclaration.getIfPart();
      ensureNotNull("Sentry declaration '"+sentryId+"' has no definied ifPart, but there should be one defined for case execution '"+id+"'.", "ifPartDeclaration", ifPartDeclaration);

      Expression condition = ifPartDeclaration.getCondition();
      ensureNotNull("A condition was expected for ifPart of Sentry declaration '"+sentryId+"' for case execution '"+id+"'.", "condition", condition);

      Object result = condition.getValue(this);
      ensureInstanceOf("condition expression returns non-Boolean", "result", result, Boolean.class);

      Boolean booleanResult = (Boolean) result;
      ifPart.setSatisfied(booleanResult);
      return booleanResult;

    }

    // if all onParts are satisfied and there is no
    // ifPart then the whole sentry is satisfied.
    return true;
  }

  protected boolean containsIfPartAndExecutionActive(String sentryId, Map<String,List<CmmnSentryPart>> sentries) {
    List<? extends CmmnSentryPart> sentryParts = sentries.get(sentryId);

    for (CmmnSentryPart part : sentryParts) {
      CmmnExecution caseExecution = part.getCaseExecution();
      if (IF_PART.equals(part.getType()) && caseExecution != null
          && caseExecution.isActive()) {
        return true;
      }
    }

    return false;
  }

  public boolean isEntryCriterionSatisfied() {
    return entryCriterionSatisfied;
  }

  // business key ////////////////////////////////////////////////////////////

  public String getCaseBusinessKey() {
    return getCaseInstance().getBusinessKey();
  }

  @Override
  public String getBusinessKey() {
    if (this.isCaseInstanceExecution()) {
      return businessKey;
    }
    else return getCaseBusinessKey();
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

  // variables ////////////////////////////////////////////

  @Override
  public String getVariableScopeKey() {
    return "caseExecution";
  }

  public AbstractVariableScope getParentVariableScope() {
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
    if (!isSuspending() && !isTerminating()) {
      // do not reset the previous state, if this case execution
      // is currently terminating or suspending. otherwise the
      // "real" previous state is lost.
      previousState = this.currentState;
    }
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

  public boolean isSuspending() {
    return currentState == SUSPENDING_ON_SUSPENSION.getStateCode()
        || currentState == SUSPENDING_ON_PARENT_SUSPENSION.getStateCode();
  }

  public boolean isTerminated() {
    return currentState == TERMINATED.getStateCode();
  }

  public boolean isTerminating() {
    return currentState == TERMINATING_ON_TERMINATION.getStateCode()
        || currentState == TERMINATING_ON_PARENT_TERMINATION.getStateCode()
        || currentState == TERMINATING_ON_EXIT.getStateCode();
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

  public List<CmmnExecution> createChildExecutions(List<CmmnActivity> activities) {
    List<CmmnExecution> children = new ArrayList<CmmnExecution>();

    // first create new child case executions
    for (CmmnActivity currentActivity : activities) {
      CmmnExecution child = createCaseExecution(currentActivity);
      children.add(child);
    }

    return children;
  }


  public void triggerChildExecutionsLifecycle(List<CmmnExecution> children) {
    // then notify create listener for each created
    // child case execution
    for (CmmnExecution child : children) {

      if (isActive()) {
        if (child.isNew()) {
          child.performOperation(CASE_EXECUTION_CREATE);
        }
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
    performOperation(CASE_EXECUTION_TERMINATING_ON_TERMINATION);
  }

  public void performTerminate() {
    performOperation(CASE_EXECUTION_TERMINATE);
  }

  public void parentTerminate() {
    performOperation(CASE_EXECUTION_TERMINATING_ON_PARENT_TERMINATION);
  }

  public void performParentTerminate() {
    performOperation(CASE_EXECUTION_PARENT_TERMINATE);
  }

  public void exit() {
    performOperation(CASE_EXECUTION_TERMINATING_ON_EXIT);
  }

  public void parentComplete() {
    performOperation(CmmnAtomicOperation.CASE_EXECUTION_PARENT_COMPLETE);
  }

  public void performExit() {
    performOperation(CASE_EXECUTION_EXIT);
  }

  public void suspend() {
    performOperation(CASE_EXECUTION_SUSPENDING_ON_SUSPENSION);
  }

  public void performSuspension() {
    performOperation(CASE_EXECUTION_SUSPEND);
  }

  public void parentSuspend() {
    performOperation(CASE_EXECUTION_SUSPENDING_ON_PARENT_SUSPENSION);
  }

  public void performParentSuspension() {
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

  // variable listeners
  public void dispatchEvent(VariableEvent variableEvent) {
    boolean invokeCustomListeners =
        Context
          .getProcessEngineConfiguration()
          .isInvokeCustomVariableListeners();

    Map<String, List<VariableListener<?>>> listeners = getActivity()
        .getVariableListeners(variableEvent.getEventName(), invokeCustomListeners);

    // only attempt to invoke listeners if there are any (as this involves resolving the upwards execution hierarchy)
    if (!listeners.isEmpty()) {
      getCaseInstance().queueVariableEvent(variableEvent, invokeCustomListeners);
    }
  }

  protected void queueVariableEvent(VariableEvent variableEvent, boolean includeCustomerListeners) {

    Queue<VariableEvent> variableEventsQueue = getVariableEventQueue();

    variableEventsQueue.add(variableEvent);

    // if this is the first event added, trigger listener invocation
    if (variableEventsQueue.size() == 1) {
      invokeVariableListeners(includeCustomerListeners);
    }
  }

  protected void invokeVariableListeners(boolean includeCustomerListeners) {
    Queue<VariableEvent> variableEventsQueue = getVariableEventQueue();

    while (!variableEventsQueue.isEmpty()) {
      // do not remove the event yet, as otherwise new events will immediately be dispatched
      VariableEvent nextEvent = variableEventsQueue.peek();

      CmmnExecution sourceExecution = (CmmnExecution) nextEvent.getSourceScope();

      DelegateCaseVariableInstanceImpl delegateVariable =
          DelegateCaseVariableInstanceImpl.fromVariableInstance(nextEvent.getVariableInstance());
      delegateVariable.setEventName(nextEvent.getEventName());
      delegateVariable.setSourceExecution(sourceExecution);

      Map<String, List<VariableListener<?>>> listenersByActivity =
          sourceExecution.getActivity().getVariableListeners(delegateVariable.getEventName(), includeCustomerListeners);

      CmmnExecution currentExecution = sourceExecution;
      while (currentExecution != null) {

        if (currentExecution.getActivityId() != null) {
          List<VariableListener<?>> listeners = listenersByActivity.get(currentExecution.getActivityId());

          if (listeners != null) {
            delegateVariable.setScopeExecution(currentExecution);

            for (VariableListener<?> listener : listeners) {
              try {
                CaseVariableListener caseVariableListener = (CaseVariableListener) listener;
                CaseVariableListenerInvocation invocation = new CaseVariableListenerInvocation(caseVariableListener, delegateVariable, currentExecution);
                Context.getProcessEngineConfiguration()
                  .getDelegateInterceptor()
                  .handleInvocation(invocation);
              } catch (Exception e) {
                throw LOG.invokeVariableListenerException(e);
              }
            }
          }
        }

        currentExecution = currentExecution.getParent();
      }

      // finally remove the event from the queue
      variableEventsQueue.remove();
    }
  }

  protected Queue<VariableEvent> getVariableEventQueue() {
    if (variableEventsQueue == null) {
      variableEventsQueue = new LinkedList<VariableEvent>();
    }

    return variableEventsQueue;
  }

  // toString() //////////////////////////////////////  ///////////

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
