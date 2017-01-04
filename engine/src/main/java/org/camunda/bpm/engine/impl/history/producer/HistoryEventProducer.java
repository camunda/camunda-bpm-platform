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

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.task.IdentityLink;

/**
 * <p>The producer for history events. The history event producer is
 * responsible for extracting data from the runtime structures
 * (Executions, Tasks, ...) and adding the data to a {@link HistoryEvent}.
 *
 * @author Daniel Meyer
 * @author Marcel Wieczorek
 * @author Ingo Richtsmeier
 *
 */
public interface HistoryEventProducer {

  // Process instances //////////////////////////////////////

  /**
   * Creates the history event fired when a process instances is <strong>created</strong>.
   *
   * @param execution the current execution.
   * @return the history event
   */
  HistoryEvent createProcessInstanceStartEvt(DelegateExecution execution);

  /**
   * Creates the history event fired when a process instance is <strong>updated</strong>.
   *
   * @param execution the process instance
   * @return the created history event
   */
  HistoryEvent createProcessInstanceUpdateEvt(DelegateExecution execution);

  /**
   * Creates the history event fired when a process instance is <strong>migrated</strong>.
   *
   * @param execution the process instance
   * @return the created history event
   */
  HistoryEvent createProcessInstanceMigrateEvt(DelegateExecution execution);

  /**
   * Creates the history event fired when a process instance is <strong>ended</strong>.
   *
   * @param execution the current execution.
   * @return the history event
   */
  HistoryEvent createProcessInstanceEndEvt(DelegateExecution execution);

  // Activity instances /////////////////////////////////////

  /**
   * Creates the history event fired when an activity instance is <strong>started</strong>.
   *
   * @param execution the current execution.
   * @return the history event
   */
  HistoryEvent createActivityInstanceStartEvt(DelegateExecution execution);

  /**
   * Creates the history event fired when an activity instance is <strong>updated</strong>.
   *
   * @param execution the current execution.
   * @return the history event
   */
  HistoryEvent createActivityInstanceUpdateEvt(DelegateExecution execution);

  /**
   * Creates the history event fired when an activity instance is <strong>updated</strong>.
   *
   * @param execution the current execution.
   * @param task the task association that is currently updated. (May be null in case there is not task associated.)
   * @return the history event
   */
  HistoryEvent createActivityInstanceUpdateEvt(DelegateExecution execution, DelegateTask task);

  /**
   * Creates the history event which is fired when an activity instance is migrated.
   *
   * @param actInstance the migrated activity instance which contains the new id's
   * @return the created history event
   */
  HistoryEvent createActivityInstanceMigrateEvt(MigratingActivityInstance actInstance);

  /**
   * Creates the history event fired when an activity instance is <strong>ended</strong>.
   *
   * @param execution the current execution.
   * @return the history event
   */
  HistoryEvent createActivityInstanceEndEvt(DelegateExecution execution);


  // Task Instances /////////////////////////////////////////

  /**
   * Creates the history event fired when a task instance is <strong>created</strong>.
   *
   * @param task the task
   * @return the history event
   */
  HistoryEvent createTaskInstanceCreateEvt(DelegateTask task);

  /**
   * Creates the history event fired when a task instance is <strong>updated</strong>.
   *
   * @param task the task
   * @return the history event
   */
  HistoryEvent createTaskInstanceUpdateEvt(DelegateTask task);

  /**
   * Creates the history event fired when a task instance is <strong>migrated</strong>.
   *
   * @param task the task
   * @return the history event
   */
  HistoryEvent createTaskInstanceMigrateEvt(DelegateTask task);

  /**
   * Creates the history event fired when a task instances is <strong>completed</strong>.
   *
   * @param task the task
   * @param deleteReason
   * @return the history event
   */
  HistoryEvent createTaskInstanceCompleteEvt(DelegateTask task, String deleteReason);

  // User Operation Logs ///////////////////////////////

  /**
   * Creates the history event fired whenever an operation has been performed by a user. This is
   * used for logging actions such as creating a new Task, completing a task, canceling a
   * a process instance, ...
   *
   * @param context the {@link UserOperationLogContext} providing the needed informations
   * @return a {@link List} of {@link HistoryEvent}s
   */
  List<HistoryEvent> createUserOperationLogEvents(UserOperationLogContext context);

  // HistoricVariableUpdateEventEntity //////////////////////

  /**
   * Creates the history event fired when a variable is <strong>created</strong>.
   *
   * @param variableInstance the runtime variable instance
   * @param the scope to which the variable is linked
   * @return the history event
   */
  HistoryEvent createHistoricVariableCreateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope);

  /**
   * Creates the history event fired when a variable is <strong>updated</strong>.
   *
   * @param variableInstance the runtime variable instance
   * @param the scope to which the variable is linked
   * @return the history event
   */
  HistoryEvent createHistoricVariableUpdateEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope);

  /**
   * Creates the history event fired when a variable is <strong>migrated</strong>.
   *
   * @param variableInstance the runtime variable instance
   * @param the scope to which the variable is linked
   * @return the history event
   */
  HistoryEvent createHistoricVariableMigrateEvt(VariableInstanceEntity variableInstance);

  /**
   * Creates the history event fired when a variable is <strong>deleted</strong>.
   *
   * @param variableInstance
   * @param variableScopeImpl
   * @return the history event
   */
  HistoryEvent createHistoricVariableDeleteEvt(VariableInstanceEntity variableInstance, VariableScope sourceVariableScope);

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
  HistoryEvent createFormPropertyUpdateEvt(ExecutionEntity execution, String propertyId, String propertyValue, String taskId);

  // Incidents //////////////////////////////////////////

  HistoryEvent createHistoricIncidentCreateEvt(Incident incident);

  HistoryEvent createHistoricIncidentResolveEvt(Incident incident);

  HistoryEvent createHistoricIncidentDeleteEvt(Incident incident);

  HistoryEvent createHistoricIncidentMigrateEvt(Incident incident);

  // Job Log ///////////////////////////////////////////

  /**
   * Creates the history event fired when a job has been <strong>created</strong>.
   *
   * @since 7.3
   */
  HistoryEvent createHistoricJobLogCreateEvt(Job job);

  /**
   * Creates the history event fired when the execution of a job <strong>failed</strong>.
   *
   * @since 7.3
   */
  HistoryEvent createHistoricJobLogFailedEvt(Job job, Throwable exception);

  /**
   * Creates the history event fired when the execution of a job was <strong>successful</strong>.
   *
   * @since 7.3
   */
  HistoryEvent createHistoricJobLogSuccessfulEvt(Job job);

  /**
   * Creates the history event fired when the a job has been <strong>deleted</strong>.
   *
   * @since 7.3
   */
  HistoryEvent createHistoricJobLogDeleteEvt(Job job);

  /**
   * Creates the history event fired when the a batch has been <strong>started</strong>.
   *
   * @since 7.5
   */
  HistoryEvent createBatchStartEvent(Batch batch);


  /**
   * Creates the history event fired when the a batch has been <strong>completed</strong>.
   *
   * @since 7.5
   */
  HistoryEvent createBatchEndEvent(Batch batch);

  /**
   * Fired when an identity link is added
   * @param identitylink
   * @return
   */
  HistoryEvent createHistoricIdentityLinkAddEvent(IdentityLink identitylink);

  /**
   * Fired when an identity links is deleted
   * @param identityLink
   * @return
   */
  HistoryEvent createHistoricIdentityLinkDeleteEvent(IdentityLink identityLink);

  /**
   * Creates the history event when an external task has been <strong>created</strong>.
   *
   * @since 7.7
   */
  HistoryEvent createHistoricExternalTaskLogCreatedEvt(ExternalTask task);

  /**
   * Creates the history event when the execution of an external task has <strong>failed</strong>.
   *
   * @since 7.7
   */
  HistoryEvent createHistoricExternalTaskLogFailedEvt(ExternalTask task);

  /**
   * Creates the history event when the execution of an external task was <strong>successful</strong>.
   *
   * @since 7.7
   */
  HistoryEvent createHistoricExternalTaskLogSuccessfulEvt(ExternalTask task);

  /**
   * Creates the history event when an external task has been <strong>deleted</strong>.
   *
   * @since 7.7
   */
  HistoryEvent createHistoricExternalTaskLogDeletedEvt(ExternalTask task);

}
