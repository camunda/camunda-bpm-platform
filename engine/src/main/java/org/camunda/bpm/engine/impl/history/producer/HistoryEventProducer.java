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
package org.camunda.bpm.engine.impl.history.producer;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.history.UserOperationLogContext;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.runtime.Incident;

/**
 * <p>The producer for history events. The history event producer is
 * responsible for extracting data from the runtime structures
 * (Executions, Tasks, ...) and adding the data to a {@link HistoryEvent}.
 *
 * @author Daniel Meyer
 * @author Marcel Wieczorek
 *
 */
public interface HistoryEventProducer {

  // Process instances //////////////////////////////////////

  /**
   * Creates the history event fired when an activity instances is <strong>created</strong>.
   *
   * @param execution the current execution.
   * @return the history event
   */
  public HistoryEvent createProcessInstanceStartEvt(DelegateExecution execution);

  /**
   * Creates the history event fired when a process instances is <strong>ended</strong>.
   *
   * @param execution the current execution.
   * @return the history event
   */
  public HistoryEvent createProcessInstanceEndEvt(DelegateExecution execution);


  // Activity instances /////////////////////////////////////

  /**
   * Creates the history event fired when an activity instances is <strong>started</strong>.
   *
   * @param execution the current execution.
   * @return the history event
   */
  public HistoryEvent createActivityInstanceStartEvt(DelegateExecution execution);

  /**
   * Creates the history event fired when an activity instances is <strong>updated</strong>.
   *
   * @param execution the current execution.
   * @param task the task association that is currently updated. (May be null in case there is not task associated.)
   * @return the history event
   */
  public HistoryEvent createActivityInstanceUpdateEvt(DelegateExecution execution, DelegateTask task);

  /**
   * Creates the history event fired when an activity instances is <strong>ended</strong>.
   *
   * @param execution the current execution.
   * @return the history event
   */
  public HistoryEvent createActivityInstanceEndEvt(DelegateExecution execution);


  // Task Instances /////////////////////////////////////////

  /**
   * Creates the history event fired when a task instances is <strong>created</strong>.
   *
   * @param task the task
   * @return the history event
   */
  public HistoryEvent createTaskInstanceCreateEvt(DelegateTask task);

  /**
   * Creates the history event fired when a task instances is <strong>updated</strong>.
   *
   * @param task the task
   * @return the history event
   */
  public HistoryEvent createTaskInstanceUpdateEvt(DelegateTask task);

  /**
   * Creates the history event fired when a task instances is <strong>completed</strong>.
   *
   * @param task the task
   * @param deleteReason
   * @return the history event
   */
  public HistoryEvent createTaskInstanceCompleteEvt(DelegateTask task, String deleteReason);

  // User Operation Logs ///////////////////////////////

  /**
   * Creates the history event fired whenever an operation has been performed by a user. This is
   * used for logging actions such as creating a new Task, completing a task, canceling a
   * a process instance, ...
   *
   * @param context the {@link UserOperationLogContext} providing the needed informations
   * @return a {@link List} of {@link HistoryEvent}s
   */
  public List<HistoryEvent> createUserOperationLogEvents(UserOperationLogContext context);

  // HistoricVariableUpdateEventEntity //////////////////////

  /**
   * Creates the history event fired when a variable is <strong>created</strong>.
   *
   * @param variableInstance the runtime variable instance
   * @param the scope to which the variable is linked
   * @return the history event
   */
  public HistoryEvent createHistoricVariableCreateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope);

  /**
   * Creates the history event fired when a variable is <strong>updated</strong>.
   *
   * @param variableInstance the runtime variable instance
   * @param the scope to which the variable is linked
   * @return the history event
   */
  public HistoryEvent createHistoricVariableUpdateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope);

  /**
   * Creates the history event fired when a variable is <strong>deleted</strong>.
   *
   * @param variableInstance
   * @param variableScopeImpl
   * @return the history event
   */
  public HistoryEvent createHistoricVariableDeleteEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope);

  // Form properties //////////////////////////////////////////

  /**
   * Creates the history event fired when a form property is <strong>updated</strong>.
   *
   * @param processInstance the id for the process instance
   * @param propertyId the id of the form property
   * @param propertyValue the value of the form property
   * @param taskId
   * @return the history event
   */
  public HistoryEvent createFormPropertyUpdateEvt(ExecutionEntity execution, String propertyId, Object propertyValue, String taskId);

  // Incidents //////////////////////////////////////////

  public HistoryEvent createHistoricIncidentCreateEvt(Incident incident);

  public HistoryEvent createHistoricIncidentResolveEvt(Incident incident);

  public HistoryEvent createHistoricIncidentDeleteEvt(Incident incident);

}
