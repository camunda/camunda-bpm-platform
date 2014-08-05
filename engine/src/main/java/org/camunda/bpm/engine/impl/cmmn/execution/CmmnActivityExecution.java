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

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.cmmn.instance.CaseTask;
import org.camunda.bpm.model.cmmn.instance.EventListener;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.Milestone;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.camunda.bpm.model.cmmn.instance.Sentry;
import org.camunda.bpm.model.cmmn.instance.Stage;
import org.camunda.bpm.model.cmmn.instance.Task;
import org.camunda.bpm.model.cmmn.instance.UserEventListener;

/**
 * @author Roman Smirnov
 *
 */
public interface CmmnActivityExecution extends DelegateCaseExecution {

  /**
   * <p>Returns the parent of <code>this</code> case execution, or null
   * if there is no parent.</p>

   * @return a {@link CmmnActivityExecution parent} or null.
   */
  CmmnActivityExecution getParent();

  /**
   * <p>Returns <code>true</code> if <code>this</code> case execution
   * is a case instance. If <code>this</code> case execution is not a
   * case instance then <code>false</code> will be returned.</p>
   *
   * @return whether <code>this</code> case execution is a case instance or not.
   */
  boolean isCaseInstanceExecution();

  /**
   * <p>Returns the {@link CaseExecutionState current state} of <code>this</code>
   * case execution.</p>
   *
   * @return the {@link CaseExecutionState current state}
   */
  CaseExecutionState getCurrentState();

  /**
   * <p>Sets the given {@link CaseExecutionState state} as the
   * current state of <code>this</code> case execution.</p>
   *
   * @param currentState the current state to set
   */
  void setCurrentState(CaseExecutionState currentState);

  /**
   * <p>Returns the {@link CaseExecutionState previous state} of <code>this</code>
   * case execution.</p>
   *
   * @return the {@link CaseExecutionState previous state}
   */
  CaseExecutionState getPreviousState();

  /**
   * <p>Returns <code>true</code> iff:<br>
   *  <code>{@link #getCurrentState()} == {@link CaseExecutionState#NEW}</code>
   * </p>
   *
   * @return whether <code>this</code> case execution has as current state {@link CaseExecutionState#NEW}
   */
  boolean isNew();

  /**
   * <p>Returns the {@link CmmnActivity activity} which is associated with
   * <code>this</code> case execution.
   *
   * @return the associated {@link CmmnActivity activity}
   */
  CmmnActivity getActivity();

  /**
   * <p>Creates new child case executions for each given {@link CmmnActivity}.</p>
   *
   * <p>Afterwards each created case executions will be executed (ie. the create
   * listener will be notified etc.).</p>
   *
   * <p>According to the CMMN 1.0 specification:<br>
   * This method can be called when <code>this</code> case execution (which
   * represents a {@link Stage}) transitions to <code>ACTIVE</code> state.
   * The passed collection of {@link CmmnActivity activities} are the planned
   * items that should be executed in this {@link Stage}. So that for each
   * given {@link CmmnActivity} a new case execution will be instantiated.
   * Furthermore for each created child execution there happens a transition
   * to the initial state <code>AVAILABLE</code>.
   * </p>
   *
   * @param activities a collection of {@link CmmnActivity activities} of planned items
   *                   to execute inside <code>this</code> case execution
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not <code>ACTIVE</code>.
   *
   */
  void createChildExecutions(List<CmmnActivity> activities);

  /**
   * <p>Transition to {@link CaseExecutionState#ENABLED} state.</p>
   *
   * <p><code>This</code> case execution must be in {@link CaseExecutionState#AVAILABLE}
   * state to be able to do this transition.</p>
   *
   * <p>It is only possible to enable a case execution which is associated with a
   * {@link Stage} or {@link Task}.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not {@link CaseExecutionState#AVAILABLE}.
   */
  void enable();

  /**
   * <p>Transition to  state.</p>
   *
   * <p><code>This</code> case execution must be in {@link CaseExecutionState#ENABLED}
   * state to be able to do this transition.</p>
   *
   * <p>It is only possible to disable a case execution which is associated with a
   * {@link Stage} or {@link Task}.</p>
   *
   * <p>If <code>this</code> case execution has a parent case execution, that parent
   * case execution will be notified that <code>this</code> case execution has been
   * disabled. This can lead to a completion of the parent case execution, for more
   * details when the parent case execution can be completed see {@link #complete()}.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not {@link CaseExecutionState#ENABLED}.
   */
  void disable();

  /**
   * <p>Transition to {@link CaseExecutionState#ENABLED} state.</p>
   *
   * <p><code>This</code> case execution must be in {@link CaseExecutionState#DISABLED}
   * state to be able to do this transition.</p>
   *
   * <p>It is only possible to re-enable a case execution which is associated with a
   * {@link Stage} or {@link Task}.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not {@link CaseExecutionState#DISABLED}.
   */
  void reenable();

  /**
   * <p>Transition to {@link CaseExecutionState#ACTIVE} state.</p>
   *
   * <p><code>This</code> case execution must be in {@link CaseExecutionState#ENABLED}
   * state to be able to do this transition.</p>
   *
   * <p>It is only possible to start a case execution manually which is associated with a
   * {@link Stage} or {@link Task}.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not {@link CaseExecutionState#ENABLED}.
   */
  void manualStart();

  /**
   * <p>Transition to {@link CaseExecutionState#ACTIVE} state.</p>
   *
   * <p><code>This</code> case execution must be in {@link CaseExecutionState#AVAILABLE}
   * state to be able to do this transition.</p>
   *
   * <p>It is only possible to start a case execution which is associated with a
   * {@link Stage} or {@link Task}.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not {@link CaseExecutionState#AVAILABLE}.
   */
  void start();

  /**
   * <p>Transition to {@link CaseExecutionState#COMPLETED} state.</p>
   *
   * <p><code>This</code> case execution must be in {@link CaseExecutionState#ACTIVE}
   * state to be able to do this transition.</p>
   *
   * <p>It is only possible to complete a case execution which is associated with a
   * {@link Stage} or {@link Task}.</p>
   *
   * <p>If <code>this</code> case execution has a parent case execution, that parent
   * case execution will be notified that <code>this</code> case execution has been
   * completed. This can lead to a completion of the parent case execution, for more
   * details when the parent case execution can be completed see {@link #complete()}.</p>
   *
   * <p>In case of a {@link Stage} the completion can only be performed when the following
   * criteria are fulfilled:<br>
   * <ul>
   *  <li>there are no children in the state {@link CaseExecutionState#ACTIVE} or {@link CaseExecutionState#NEW}</li>
   *  <li>if the property <code>autoComplete</code> of the associated {@link Stage} is set to <strong><code>true</code></strong>:
   *    <ul>
   *      <li>all required (<code>requiredRule</code> evaluates to <code>true</code>) children are in state
   *        <ul>
   *          <li>{@link CaseExecutionState#DISABLED}</li>
   *          <li>{@link CaseExecutionState#COMPLETED}</li>
   *          <li>{@link CaseExecutionState#TERMINATED}</li>
   *          <li>{@link CaseExecutionState#FAILED}</li>
   *        </ul>
   *      </li>
   *    </ul>
   *  </li>
   *  <li>if the property <code>autoComplete</code> of the associated {@link Stage} is set to <strong><code>false</code></strong>:
   *    <ul>
   *      <li>all children are in
   *        <ul>
   *          <li>{@link CaseExecutionState#DISABLED}</li>
   *          <li>{@link CaseExecutionState#COMPLETED}</li>
   *          <li>{@link CaseExecutionState#TERMINATED}</li>
   *          <li>{@link CaseExecutionState#FAILED}</li>
   *        </ul>
   *      </li>
   *    </ul>
   *  </li>
   * </ul>
   * </p>
   *
   * <p>For a {@link Task} instance, this means its purpose has been accomplished:<br>
   *  <ul>
   *    <li>{@link HumanTask} have been completed by human.</li>
   *    <li>{@link CaseTask} have launched a new {@link CaseInstance} and if output parameters
   *        are required and/or the property <code>isBlocking</code> is set to <code>true</code>,
   *        then the launched {@link CaseInstance} has completed and returned the
   *        output parameters.</li>
   *    <li>{@link ProcessTask} have launched a new {@link ProcessInstance} and if output parameters
   *        are required and/or the property <code>isBlocking</code> is set to <code>true</code>,
   *        then the launched {@link ProcessInstance} has completed and returned the
   *        output parameters.</li>
   *  </ul>
   * </p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not {@link CaseExecutionState#ACTIVE} or when the case execution cannot be
   *         completed.
   */
  void complete();

  /**
   * <p>Transition to {@link CaseExecutionState#COMPLETED} state.</p>
   *
   * <p><code>This</code> case execution must be in {@link CaseExecutionState#ACTIVE}
   * state to be able to do this transition.</p>
   *
   * <p>It is only possible to complete a case execution manually which is associated with a
   * {@link Stage} or {@link Task}.</p>
   *
   * <p>If <code>this</code> case execution has a parent case execution, that parent
   * case execution will be notified that <code>this</code> case execution has been
   * completed. This can lead to a completion of the parent case execution, for more
   * details when the parent case execution can be completed see {@link #complete()}.</p>
   *
   * <p>In case of a {@link Stage} the completion can only be performed when the following
   * criteria are fulfilled:<br>
   * <ul>
   *  <li>there are no children in the state {@link CaseExecutionState#ACTIVE} or {@link CaseExecutionState#NEW}</li>
   *  <li>all required (<code>requiredRule</code> evaluates to <code>true</code>) children are in state
   *    <ul>
   *      <li>{@link CaseExecutionState#DISABLED}</li>
   *      <li>{@link CaseExecutionState#COMPLETED}</li>
   *      <li>{@link CaseExecutionState#TERMINATED}</li>
   *      <li>{@link CaseExecutionState#FAILED}</li>
   *    </ul>
   *  </li>
   * </ul>
   * </p>
   *
   * <p>For a {@link Task} instance, this means its purpose has been accomplished:<br>
   *  <ul>
   *    <li>{@link HumanTask} have been completed by human.</li>
   *  </ul>
   * </p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not {@link CaseExecutionState#ACTIVE} or when the case execution cannot be
   *         completed.
   */
  void manualComplete();

  /**
   * <p>Transition to {@link CaseExecutionState#COMPLETED} state.</p>
   *
   * <p><code>This</code> case execution must be in {@link CaseExecutionState#AVAILABLE}
   * state to be able to do this transition.</p>
   *
   * <p>For {@link EventListener event listener} transitions when the event being listened by the
   * {@link EventListener event listener} instance does occur. For a {@link UserEventListener user event
   * listener} instance this transition happens when a human decides to raise the event.</p>
   *
   * </p>For {@link Milestone} instance transitions when one of the achieving {@link Sentry sentries}
   * (entry criteria) is satisfied.</p>
   *
   * <p>If <code>this</code> case execution has a parent case execution, that parent
   * case execution will be notified that <code>this</code> case execution has been
   * completed (ie.the event or milestone occured). This can lead to a completion of
   * the parent case execution, for more details when the parent case execution can
   * be completed see {@link #complete()}.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not {@link CaseExecutionState#AVAILABLE}.
   */
  void occur();

  /**
   * <p>Transition to {@link CaseExecutionState#TERMINATED} state.</p>
   *
   * <p>If <code>this</code> case execution is associated with a {@link Stage} or
   * {@link Task}, then <code>this</code> case execution must be in {@link CaseExecutionState#ACTIVE}
   * state to be able to do this transition.<br>
   * And if <code>this</code> case execution is association with {@link EventListener EventListener}
   * or a {@link Milestone}, then <code>this</code> case execution must be in
   * {@link CaseExecutionState#AVAILABLE} state to be able to do this transition.</p>
   *
   * <p>For a {@link Stage} instance the termination of <code>this</code> case execution
   * will be propagated down to all its contained {@link EventListener EventListener}, {@link Milestone},
   * {@link Stage}, and {@link Task} instances.</p>
   *
   * <p>If <code>this</code> case execution has a parent case execution, that parent
   * case execution will be notified that <code>this</code> case execution has been
   * terminated. This can lead to a completion of the parent case execution, for more
   * details when the parent case execution can be completed see {@link #complete()}.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not in the expected state.
   */
  void terminate();

  /**
   * <p>Transition to {@link CaseExecutionState#TERMINATED} state when the parent
   * {@link Stage} transition to {@link CaseExecutionState#TERMINATED} state.</p>
   *
   * <p><code>This</code> case execution must be in {@link CaseExecutionState#AVAILABLE}
   * or {@link CaseExecutionState#SUSPENDED} state to be able to do this transition.</p>
   *
   * <p>It is only possible to execute a parent termination on a case execution which is
   * associated with a {@link EventListener} or {@link Milestone}.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not in the expected state.
   */
  void parentTerminate();

  /**
   * <p>Transition to {@link CaseExecutionState#TERMINATED} state when the parent
   * {@link Stage} transition to {@link CaseExecutionState#TERMINATED} state or
   * when the exit criteria of the associated {@link Stage} or {@link Task} becomes
   * <code>true</code>.</p>
   *
   * <p><code>This</code> case execution must be in one of the following state to
   * be able to do this transition:
   * <ul>
   *   <li>{@link CaseExecutionState#AVAILABLE},</li>
   *   <li>{@link CaseExecutionState#ENABLED},</li>
   *   <li>{@link CaseExecutionState#DISABLED},</li>
   *   <li>{@link CaseExecutionState#ACTIVE},</li>
   *   <li>{@link CaseExecutionState#SUSPENDED} or</li>
   *   <li>{@link CaseExecutionState#FAILED}</li>
   * </ul>
   *
   * <p>It is only possible to execute an exit on a case execution which is
   * associated with a {@link Stage} or {@link Task}.</p>
   *
   * <p>If this transition is triggered by a fulfilled exit criteria and if
   * <code>this</code> case execution has a parent case execution, that parent
   * case execution will be notified that <code>this</code> case execution has been
   * terminated. This can lead to a completion of the parent case execution, for more
   * details when the parent case execution can be completed see {@link #complete()}.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not in the expected state.
   */
  void exit();

  /**
   * <p>Transition to {@link CaseExecutionState#SUSPENDED} state.</p>
   *
   * <p>If <code>this</code> case execution is associated with a {@link Stage} or
   * {@link Task}, then <code>this</code> case execution must be in {@link CaseExecutionState#ACTIVE}
   * state to be able to do this transition.<br>
   * And if <code>this</code> case execution is association with {@link EventListener EventListener}
   * or a {@link Milestone}, then <code>this</code> case execution must be in
   * {@link CaseExecutionState#AVAILABLE} state to be able to do this transition.</p>
   *
   * <p>For a {@link Stage} instance the suspension of <code>this</code> case execution
   * will be propagated down to all its contained {@link EventListener EventListener}, {@link Milestone},
   * {@link Stage}, and {@link Task} instances.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not in the expected state.
   */
  void suspend();

  /**
   * <p>Transition to {@link CaseExecutionState#SUSPENDED} state when the parent
   * {@link Stage} transition to {@link CaseExecutionState#SUSPENDED} state.</p>
   *
   * <p><code>This</code> case execution must be in one of the following state to
   * be able to do this transition:
   * <ul>
   *   <li>{@link CaseExecutionState#AVAILABLE},</li>
   *   <li>{@link CaseExecutionState#ENABLED},</li>
   *   <li>{@link CaseExecutionState#DISABLED} or</li>
   *   <li>{@link CaseExecutionState#ACTIVE}</li>
   * </ul>
   *
   * <p>It is only possible to execute a parent suspension on a case execution which is
   * associated with a {@link Stage} or {@link Task}.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not in the expected state.
   */
  void parentSuspend();

  /**
   * <p>Transition to either to {@link CaseExecutionState#ACTIVE} state, if <code>this</code>
   * case execution is associated with a {@link Stage} or {@link Task}, or to {@link CaseExecutionState#AVAILABE},
   * if <code>this</code> case execution is associated with a {@link EventListener} or {@link Milestone}.</p>
   *
   * <p><code>This</code> case execution must be in {@link CaseExecutionState#SUSPENDED}
   * state to be able to do this transition.</p>
   *
   * <p>For a {@link Stage} instance the resume of <code>this</code> case execution
   * will be propagated down to all its contained {@link EventListener EventListener}, {@link Milestone},
   * {@link Stage}, and {@link Task} instances.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not in the expected state.
   */
  void resume();

  /**
   * <p>Transition to the previous state ({@link CaseExecutionState#AVAILABLE},
   * {@link CaseExecutionState#ENABLED}, {@link CaseExecutionState#DISABLED} or
   * {@link CaseExecutionState#ACTIVE}) when the parent {@link Stage} transitions
   * out of {@link CaseExecutionState#SUSPENDED}.</p>
   *
   * <p><code>This</code> case execution must be in {@link CaseExecutionState#SUSPENDED}
   * state to be able to do this transition.</p>
   *
   * <p>It is only possible to execute a parent resume on a case execution which is
   * associated with a {@link Stage} or {@link Task}.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not in the expected state.
   */
  void parentResume();

  /**
   * <p>Transition to {@link CaseExecutionState#ACTIVE} state.</p>
   *
   * <p>If <code>this</code> case execution is associated with a {@link Stage} or
   * {@link Task} and is not a case instance, then <code>this</code> case execution
   * must be in {@link CaseExecutionState#FAILED} state to be able to do this transition.<br>
   * And if <code>this</code> case execution is a case instance, then <code>this</code>
   * case instance must be in one of the following state to perform this transition:
   * <ul>
   *   <li>{@link CaseExecutionState#COMPLETED},</li>
   *   <li>{@link CaseExecutionState#SUSPENDED},</li>
   *   <li>{@link CaseExecutionState#TERMINATED} or</li>
   *   <li>{@link CaseExecutionState#FAILED}</li>
   * </ul>
   * </p>
   *
   * <p>In case of a case instance the transition out of {@link CaseExecutionState#SUSPENDED} state
   * the resume will be propagated down to all its contained {@link EventListener EventListener},
   * {@link Milestone}, {@link Stage}, and {@link Task} instances, see {@link #resume()} and
   * {@link #parentResume()}.</p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not in the expected state.
   */
  void reactivate();

  /**
   * <p>Transition to {@link CaseExecutionState#CLOSED} state when no further
   * work or modifications should be allowed for this case instance.</p>
   *
   * <p>It is only possible to close a case instance which is in one of the following
   * states:
   * <ul>
   *   <li>{@link CaseExecutionState#COMPLETED},</li>
   *   <li>{@link CaseExecutionState#SUSPENDED},</li>
   *   <li>{@link CaseExecutionState#TERMINATED} or</li>
   *   <li>{@link CaseExecutionState#FAILED}</li>
   * </ul>
   * </p>
   *
   * @throws ProcessEngineException will be thrown, if <code>this</code> case execution
   *         is not in the expected state.
   */
  void close();

  /**
   * <p>Returns to true, if <code>this</code> case execution is required.</p>
   *
   * @return true if <code>this</code> case execution is required.
   */
  boolean isRequired();

  /**
   * <p>Sets <code>this</code> case execution as required or not required.</p>
   *
   * @param required a boolean value whether <code>this</code> case execution
   * is required or not required.
   */
  void setRequired(boolean required);

  /**
   * <p>Removes <code>this</code> case execution from the parent case execution.</p>
   */
  void remove();

  /**
   * <p>Returns a {@link List} of child case executions. If <code>this</code> case
   * execution has no child case executions an empty {@link List} will be returned.</p>
   *
   * @return a {@link List} of child case executions.
   */
  List<? extends CmmnExecution> getCaseExecutions();

  /**
   * <p>Creates a new sub process instance.</p>
   *
   * <p><code>This</code> case execution will be the super case execution of the
   * created sub process instance.</p>
   *
   * @param processDefinition The {@link PvmProcessDefinition} of the subprocess.
   */
  PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition);

  /**
   * <p>Creates a new sub case instance.</p>
   *
   * <p><code>This</code> case execution will be the super case execution of the
   * created sub case instance.</p>
   *
   * @param caseDefinition The {@link CmmnCaseDefinition} of the subprocess.
   */
  CmmnCaseInstance createSubCaseInstance(CmmnCaseDefinition caseDefinition);

}
