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
package org.camunda.bpm.engine.impl.pvm.delegate;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Falko Menge
 */
public interface ActivityExecution extends DelegateExecution {

  /* Process instance/activity/transition retrieval */

  /**
   * returns the current {@link PvmActivity} of the execution.
   */
  PvmActivity getActivity();

  /** invoked to notify the execution that a new activity instance is started */
  void enterActivityInstance();

  /** invoked to notify the execution that an activity instance is ended. */
  void leaveActivityInstance();

  void setActivityInstanceId(String id);

  /** return the Id of the activity instance currently executed by this execution */
  String getActivityInstanceId();

  /** return the Id of the parent activity instance currently executed by this execution */
  String getParentActivityInstanceId();

  /**
   * leaves the current activity by taking the given transition.
   */
  void take(PvmTransition transition);


  /* Execution management */

  /**
   * creates a new execution. This execution will be the parent of the newly created execution.
   * properties processDefinition, processInstance and activity will be initialized.
   */
  ActivityExecution createExecution();

  /**
   * creates a new sub process instance.
   * The current execution will be the super execution of the created execution.
   *
   * @param processDefinition The {@link PvmProcessDefinition} of the subprocess.
   */
  PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition);

  /**
   * @see #createSubProcessInstance(PvmProcessDefinition)
   *
   * @param processDefinition The {@link PvmProcessDefinition} of the subprocess.
   * @param businessKey the business key of the process instance
   */
  PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey);

  /**
   * returns the parent of this execution, or null if there no parent.
   */
  ActivityExecution getParent();

  /**
   * returns the list of execution of which this execution the parent of.
   */
  List<? extends ActivityExecution> getExecutions();

  /**
   * ends this execution.
   */
  void end(boolean isScopeComplete);

  /* State management */

  /**
   * makes this execution active or inactive.
   */
  void setActive(boolean isActive);

  /**
   * returns whether this execution is currently active.
   */
  boolean isActive();

  /**
   * returns whether this execution has ended or not.
   */
  boolean isEnded();

  /**
   * changes the concurrent indicator on this execution.
   */
  void setConcurrent(boolean isConcurrent);

  /**
   * returns whether this execution is concurrent or not.
   */
  boolean isConcurrent();

  /**
   * returns whether this execution is a process instance or not.
   */
  boolean isProcessInstance();

  /**
   * Inactivates this execution.
   * This is useful for example in a join: the execution
   * still exists, but it is not longer active.
   */
  void inactivate();

  /**
   * Returns whether this execution is a scope.
   */
  boolean isScope();

  /**
   * Changes whether this execution is a scope or not
   */
  void setScope(boolean isScope);

  /**
   * Returns whether this execution completed the parent scope.
   */
  boolean isCompleteScope();

  /**
   * Returns whether this execution has been canceled.
   */
  boolean isCanceled();

  /**
   * Retrieves all executions which are concurrent and inactive at the given activity.
   */
  List<ActivityExecution> findInactiveConcurrentExecutions(PvmActivity activity);

  /**
   * Takes the given outgoing transitions, and potentially reusing
   * the given list of executions that were previously joined.
   */
  void takeAll(List<PvmTransition> outgoingTransitions, List<ActivityExecution> joinedExecutions);

  /**
   * Executes the {@link ActivityBehavior} associated with the given activity.
   */
  void executeActivity(PvmActivity activity);

  /**
   * Called when an execution is interrupted. This will remove all associated entities
   * such as event subscriptions, jobs, ...
   */
  void interruptScope(String reason);

  /** An activity which is to be started next. */
  PvmActivity getNextActivity();

}
