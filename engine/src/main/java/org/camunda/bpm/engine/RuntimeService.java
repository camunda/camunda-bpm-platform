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
package org.camunda.bpm.engine;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.migration.MigrationPlanExecutionBuilder;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ConditionEvaluationBuilder;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ModificationBuilder;
import org.camunda.bpm.engine.runtime.NativeExecutionQuery;
import org.camunda.bpm.engine.runtime.NativeProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.runtime.RestartProcessInstanceBuilder;
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;


/** Service which provides access to {@link Deployment}s,
 * {@link ProcessDefinition}s and {@link ProcessInstance}s.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 */
public interface RuntimeService {

  /**
   * Starts a new process instance in the latest version of the process definition with the given key.
   *
   * @param processDefinitionKey key of process definition, cannot be null.
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey);

  /**
   * Starts a new process instance in the latest version of the process
   * definition with the given key.
   *
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   *
   * Note that a business key MUST be unique for the given process definition WHEN you have added
   * a database constraint for it.
   * In this case, only Process instance from different process definition are allowed to have the
   * same business key and the combination of processdefinitionKey-businessKey must be unique.
   *
   * @param processDefinitionKey
   *          key of process definition, cannot be null.
   * @param businessKey
   *          a key that uniquely identifies the process instance in the context
   *          of the given process definition.
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey);

  /**
   * Starts a new process instance in the latest version of the process
   * definition with the given key.
   *
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   *
   * Note that a business key MUST be unique for the given process definition WHEN you have added
   * a database constraint for it.
   * In this case, only Process instance from different process definition are allowed to have the
   * same business key and the combination of processdefinitionKey-businessKey must be unique.
   *
   * @param processDefinitionKey
   *          key of process definition, cannot be null.
   * @param businessKey
   *          a key that uniquely identifies the process instance in the context
   *          of the given process definition.
   * @param caseInstanceId
   *          an id of a case instance to associate the process instance with
   *          a case instance.
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, String caseInstanceId);

  /** Starts a new process instance in the latest version of the process definition with the given key
   *
   * @param processDefinitionKey key of process definition, cannot be null.
   * @param variables the variables to pass, can be null.
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables);

  /**
   * Starts a new process instance in the latest version of the process definition with the given key.
   *
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   *
   * Note that a business key MUST be unique for the given process definition WHEN you have added a
   * database constraint for it.
   * In this case, only Process instance from different process definition are allowed to have the
   * same business key and the combination of processdefinitionKey-businessKey must be unique.
   *
   * The combination of processdefinitionKey-businessKey must be unique.
   *
   * @param processDefinitionKey key of process definition, cannot be null.
   * @param variables the variables to pass, can be null.
   * @param businessKey a key that uniquely identifies the process instance in the context of the
   *                    given process definition.
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables);

  /**
   * Starts a new process instance in the latest version of the process definition with the given key.
   *
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   *
   * Note that a business key MUST be unique for the given process definition WHEN you have added a
   * database constraint for it.
   * In this case, only Process instance from different process definition are allowed to have the
   * same business key and the combination of processdefinitionKey-businessKey must be unique.
   *
   * The combination of processdefinitionKey-businessKey must be unique.
   * @param processDefinitionKey key of process definition, cannot be null.
   * @param variables the variables to pass, can be null.
   * @param businessKey a key that uniquely identifies the process instance in the context of the
   *                    given process definition.
   * @param caseInstanceId
   *          an id of a case instance to associate the process instance with
   *          a case instance.
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, String caseInstanceId, Map<String, Object> variables);

  /** Starts a new process instance in the exactly specified version of the process definition with the given id.
   *
   * @param processDefinitionId the id of the process definition, cannot be null.
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId);

  /**
   * Starts a new process instance in the exactly specified version of the process definition with the given id.
   *
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   *
   * Note that a business key MUST be unique for the given process definition WHEN you have added
   * a database constraint for it.
   * In this case, only Process instance from different process definition are allowed to have the
   * same business key and the combination of processdefinitionKey-businessKey must be unique.
   *
   * @param processDefinitionId the id of the process definition, cannot be null.
   * @param businessKey a key that uniquely identifies the process instance in the context of the
   *                    given process definition.
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey);

  /**
   * Starts a new process instance in the exactly specified version of the process definition with the given id.
   *
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   *
   * Note that a business key MUST be unique for the given process definition WHEN you have added
   * a database constraint for it.
   * In this case, only Process instance from different process definition are allowed to have the
   * same business key and the combination of processdefinitionKey-businessKey must be unique.
   *
   * @param processDefinitionId the id of the process definition, cannot be null.
   * @param businessKey a key that uniquely identifies the process instance in the context of the
   *                    given process definition.
   * @param caseInstanceId
   *          an id of a case instance to associate the process instance with
   *          a case instance.
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, String caseInstanceId);

  /** Starts a new process instance in the exactly specified version of the process definition with the given id.
   *
   * @param processDefinitionId the id of the process definition, cannot be null.
   * @param variables variables to be passed, can be null
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables);

  /**
   * Starts a new process instance in the exactly specified version of the process definition with the given id.
   *
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   *
   * Note that a business key MUST be unique for the given process definition WHEN you have added
   * a database constraint for it.
   * In this case, only Process instance from different process definition are allowed to have the
   * same business key and the combination of processdefinitionKey-businessKey must be unique.
   *
   * @param processDefinitionId the id of the process definition, cannot be null.
   * @param businessKey a key that uniquely identifies the process instance in the context of the
   *                    given process definition.
   * @param variables variables to be passed, can be null
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables);

  /**
   * Starts a new process instance in the exactly specified version of the process definition with the given id.
   *
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   *
   * Note that a business key MUST be unique for the given process definition WHEN you have added
   * a database constraint for it.
   * In this case, only Process instance from different process definition are allowed to have the
   * same business key and the combination of processdefinitionKey-businessKey must be unique.
   *
   * @param processDefinitionId the id of the process definition, cannot be null.
   * @param businessKey a key that uniquely identifies the process instance in the context of the
   *                    given process definition.
   * @param caseInstanceId
   *          an id of a case instance to associate the process instance with
   *          a case instance.
   * @param variables variables to be passed, can be null
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, String caseInstanceId, Map<String, Object> variables);

  /**
   * <p>Signals the process engine that a message is received and starts a new
   * {@link ProcessInstance}.</p>
   *
   * <p>Calling this method can have two different outcomes:
   * <ul>
   * <li>If the message name is associated with a message start event, a new
   * process instance is started.</li>
   * <li>If no subscription to a message with the given name exists, {@link ProcessEngineException}
   * is thrown</li>
   * </ul>
   * </p>
   *
   * @param messageName
   *          the 'name' of the message as specified as an attribute on the
   *          bpmn20 {@code <message name="messageName" />} element.

   * @return the {@link ProcessInstance} object representing the started process instance
   *
   * @throws ProcessEngineException
   *          if no subscription to a message with the given name exists
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 5.9
   */
  ProcessInstance startProcessInstanceByMessage(String messageName);

  /**
   * <p>Signals the process engine that a message is received and starts a new
   * {@link ProcessInstance}.</p>
   *
   * See {@link #startProcessInstanceByMessage(String, Map)}. This method allows
   * specifying a business key.
   *
   * @param messageName
   *          the 'name' of the message as specified as an attribute on the
   *          bpmn20 {@code <message name="messageName" />} element.
   * @param businessKey
   *          the business key which is added to the started process instance
   *
   * @throws ProcessEngineException
   *          if no subscription to a message with the given name exists
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 5.10
   */
  ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey);

  /**
   * <p>Signals the process engine that a message is received and starts a new
   * {@link ProcessInstance}.</p>
   *
   * See {@link #startProcessInstanceByMessage(String)}. In addition, this method allows
   * specifying a the payload of the message as a map of process variables.
   *
   * @param messageName
   *          the 'name' of the message as specified as an attribute on the
   *          bpmn20 {@code <message name="messageName" />} element.
   * @param processVariables
   *          the 'payload' of the message. The variables are added as processes
   *          variables to the started process instance.
   *
   * @return the {@link ProcessInstance} object representing the started process instance
   *
   * @throws ProcessEngineException
   *          if no subscription to a message with the given name exists
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 5.9
   */
  ProcessInstance startProcessInstanceByMessage(String messageName, Map<String, Object> processVariables);

  /**
   * <p>Signals the process engine that a message is received and starts a new
   * {@link ProcessInstance}.</p>
   *
   * See {@link #startProcessInstanceByMessage(String, Map)}. In addition, this method allows
   * specifying a business key.
   *
   * @param messageName
   *          the 'name' of the message as specified as an attribute on the
   *          bpmn20 {@code <message name="messageName" />} element.
   * @param businessKey
   *          the business key which is added to the started process instance
   * @param processVariables
   *          the 'payload' of the message. The variables are added as processes
   *          variables to the started process instance.
   *
   * @return the {@link ProcessInstance} object representing the started process instance
   *
   * @throws ProcessEngineException
   *          if no subscription to a message with the given name exists
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 5.9
   */
  ProcessInstance startProcessInstanceByMessage(String messageName, String businessKey, Map<String, Object> processVariables);

  /**
   * <p>Signals the process engine that a message is received and starts a new
   * {@link ProcessInstance}.</p>
   *
   * See {@link #startProcessInstanceByMessage(String)}. In addition, this method allows
   * specifying the exactly version of the process definition with the given id.
   *
   * @param messageName
   *          the 'name' of the message as specified as an attribute on the
   *          bpmn20 {@code <message name="messageName" />} element, cannot be null.
   * @param processDefinitionId
   *      the id of the process definition, cannot be null.
   *
   * @return the {@link ProcessInstance} object representing the started process instance
   *
   * @throws ProcessEngineException
   *          if no subscription to a message with the given name exists for the
   *          specified version of process definition.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.3
   */
  ProcessInstance startProcessInstanceByMessageAndProcessDefinitionId(String messageName, String processDefinitionId);

  /**
   * <p>Signals the process engine that a message is received and starts a new
   * {@link ProcessInstance}.</p>
   *
   * See {@link #startProcessInstanceByMessage(String, String)}. In addition, this method allows
   * specifying the exactly version of the process definition with the given id.
   *
   * @param messageName
   *          the 'name' of the message as specified as an attribute on the
   *          bpmn20 {@code <message name="messageName" />} element, cannot be null.
   * @param processDefinitionId
   *      the id of the process definition, cannot be null.
   * @param businessKey
   *          the business key which is added to the started process instance
   *
   * @return the {@link ProcessInstance} object representing the started process instance
   *
   * @throws ProcessEngineException
   *          if no subscription to a message with the given name exists for the
   *          specified version of process definition.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.3
   */
  ProcessInstance startProcessInstanceByMessageAndProcessDefinitionId(String messageName, String processDefinitionId, String businessKey);

  /**
   * <p>Signals the process engine that a message is received and starts a new
   * {@link ProcessInstance}.</p>
   *
   * See {@link #startProcessInstanceByMessage(String, Map)}. In addition, this method allows
   * specifying the exactly version of the process definition with the given id.
   *
   * @param messageName
   *          the 'name' of the message as specified as an attribute on the
   *          bpmn20 {@code <message name="messageName" />} element, cannot be null.
   * @param processDefinitionId
   *      the id of the process definition, cannot be null.
   * @param processVariables
   *          the 'payload' of the message. The variables are added as processes
   *          variables to the started process instance.
   *
   * @return the {@link ProcessInstance} object representing the started process instance
   *
   * @throws ProcessEngineException
   *          if no subscription to a message with the given name exists for the
   *          specified version of process definition.
   *
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.3
   */
  ProcessInstance startProcessInstanceByMessageAndProcessDefinitionId(String messageName, String processDefinitionId, Map<String, Object> processVariables);

  /**
   * <p>Signals the process engine that a message is received and starts a new
   * {@link ProcessInstance}.</p>
   *
   * See {@link #startProcessInstanceByMessage(String, String, Map)}. In addition, this method allows
   * specifying the exactly version of the process definition with the given id.
   *
   * @param messageName
   *          the 'name' of the message as specified as an attribute on the
   *          bpmn20 {@code <message name="messageName" />} element, cannot be null.
   * @param processDefinitionId
   *      the id of the process definition, cannot be null.
   * @param businessKey
   *          the business key which is added to the started process instance
   * @param processVariables
   *          the 'payload' of the message. The variables are added as processes
   *          variables to the started process instance.
   *
   * @return the {@link ProcessInstance} object representing the started process instance
   *
   * @throws ProcessEngineException
   *          if no subscription to a message with the given name exists for the
   *          specified version of process definition.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.3
   */
  ProcessInstance startProcessInstanceByMessageAndProcessDefinitionId(String messageName, String processDefinitionId, String businessKey, Map<String, Object> processVariables);

  /**
   * Delete an existing runtime process instance.
   *
   * Deletion propagates upward as far as necessary.
   *
   * @param processInstanceId id of process instance to delete, cannot be null.
   * @param deleteReason reason for deleting, which will be stored in the history. Can be null.
   *
   * @throws BadUserRequestException
   *          when no process instance is found with the given id or id is null.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#DELETE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#DELETE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteProcessInstance(String processInstanceId, String deleteReason);

  /**
   * Delete an existing runtime process instances asynchronously using Batch operation.
   *
   * Deletion propagates upward as far as necessary.
   *
   * @param processInstanceIds id's of process instances to delete, cannot be null if processInstanceQuery is null.
   * @param processInstanceQuery query that will be used to fetch affected process instances.
   *                             Cannot be null if processInstanceIds are null.
   * @param deleteReason reason for deleting, which will be stored in the history. Can be null.
   *
   * @throws BadUserRequestException
   *          when no process instance is found with the given id or id is null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#BATCH}.
   */
  Batch deleteProcessInstancesAsync(List<String> processInstanceIds, ProcessInstanceQuery processInstanceQuery, String deleteReason);

  /**
   * Delete an existing runtime process instances asynchronously using Batch operation.
   *
   * Deletion propagates upward as far as necessary.
   *
   * @param processInstanceIds id's of process instances to delete, cannot be null if processInstanceQuery is null.
   * @param processInstanceQuery query that will be used to fetch affected process instances.
   *                             Cannot be null if processInstanceIds are null.
   * @param deleteReason reason for deleting, which will be stored in the history. Can be null.
   * @param skipCustomListeners skips custom execution listeners when removing instances
   * @throws BadUserRequestException
   *          when no process instance is found with the given id or id is null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#BATCH}.
   */
  Batch deleteProcessInstancesAsync(List<String> processInstanceIds, ProcessInstanceQuery processInstanceQuery, String deleteReason, boolean skipCustomListeners);

  /**
   * Delete an existing runtime process instances asynchronously using Batch operation.
   *
   * Deletion propagates upward as far as necessary.
   *
   * @param processInstanceIds id's of process instances to delete, cannot be null if processInstanceQuery is null.
   * @param processInstanceQuery query that will be used to fetch affected process instances.
   *                             Cannot be null if processInstanceIds are null.
   * @param deleteReason reason for deleting, which will be stored in the history. Can be null.
   * @param skipCustomListeners skips custom execution listeners when removing instances
   * @param skipSubprocesses skips subprocesses when removing instances
   *
   * @throws BadUserRequestException
   *          when no process instance is found with the given id or id is null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#BATCH}.
   */
  Batch deleteProcessInstancesAsync(List<String> processInstanceIds, ProcessInstanceQuery processInstanceQuery, String deleteReason, boolean skipCustomListeners, boolean skipSubprocesses);

  /**
   * Delete an existing runtime process instances asynchronously using Batch operation.
   *
   * Deletion propagates upward as far as necessary.
   *
   * @param processInstanceQuery query that will be used to fetch affected process instances.
   *                             Cannot be null.
   * @param deleteReason reason for deleting, which will be stored in the history. Can be null.
   *
   * @throws BadUserRequestException
   *          when no process instance is found with the given id or id is null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#BATCH}.
   */
  Batch deleteProcessInstancesAsync(ProcessInstanceQuery processInstanceQuery, String deleteReason);

  /**
   * Delete an existing runtime process instances asynchronously using Batch operation.
   *
   * Deletion propagates upward as far as necessary.
   *
   * If both process instances list and query are provided, process instances containing in both sets
   * will be deleted.
   *
   * @param processInstanceIds id's of process instances to delete, cannot be null.
   * @param deleteReason reason for deleting, which will be stored in the history. Can be null.
   *
   * @throws BadUserRequestException
   *          when no process instance is found with the given id or id is null.
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#BATCH}.
   */
  Batch deleteProcessInstancesAsync(List<String> processInstanceIds, String deleteReason);

  /**
   * Delete an existing runtime process instance.
   *
   * Deletion propagates upward as far as necessary.
   *
   * @param processInstanceId id of process instance to delete, cannot be null.
   * @param deleteReason reason for deleting, which will be stored in the history. Can be null.
   * @param skipCustomListeners if true, only the built-in {@link ExecutionListener}s
   * are notified with the {@link ExecutionListener#EVENTNAME_END} event.
   *
   * @throws BadUserRequestException
   *          when no process instance is found with the given id or id is null.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#DELETE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#DELETE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteProcessInstance(String processInstanceId, String deleteReason, boolean skipCustomListeners);

  /**
   * Delete an existing runtime process instance.
   *
   * Deletion propagates upward as far as necessary.
   *
   * @param processInstanceId id of process instance to delete, cannot be null.
   * @param deleteReason reason for deleting, which will be stored in the history. Can be null.
   * @param skipCustomListeners if true, only the built-in {@link ExecutionListener}s
   * are notified with the {@link ExecutionListener#EVENTNAME_END} event.
   * @param externallyTerminated indicator if deletion triggered from external context, for instance
   *                             REST API call
   *
   *
   * @throws BadUserRequestException
   *          when no process instance is found with the given id or id is null.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#DELETE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#DELETE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteProcessInstance(String processInstanceId, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated);


  /**
   * Delete existing runtime process instances.
   *
   * Deletion propagates upward as far as necessary.
   *
   * @param processInstanceIds ids of process instance to delete, cannot be null.
   * @param deleteReason reason for deleting, which will be stored in the history. Can be null.
   * @param skipCustomListeners if true, only the built-in {@link ExecutionListener}s
   * are notified with the {@link ExecutionListener#EVENTNAME_END} event.
   * @param externallyTerminated indicator if deletion triggered from external context, for instance
   *                             REST API call
   *
   *
   * @throws BadUserRequestException
   *          when no process instance is found with the given id or id is null.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#DELETE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#DELETE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteProcessInstances(List<String> processInstanceIds, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated);

  /**
   * Delete existing runtime process instances.
   *
   * Deletion propagates upward as far as necessary.
   *
   * @param processInstanceIds ids of process instance to delete, cannot be null.
   * @param deleteReason reason for deleting, which will be stored in the history. Can be null.
   * @param skipCustomListeners if true, only the built-in {@link ExecutionListener}s
   * are notified with the {@link ExecutionListener#EVENTNAME_END} event.
   * @param externallyTerminated indicator if deletion triggered from external context, for instance
   *                             REST API call
   * @param skipSubprocesses specifies whether subprocesses should be deleted
   *
   *
   * @throws BadUserRequestException
   *          when no process instance is found with the given id or id is null.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#DELETE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#DELETE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteProcessInstances(List<String> processInstanceIds, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated,
  boolean skipSubprocesses);

  /**
   * Delete an existing runtime process instance.
   *
   * Deletion propagates upward as far as necessary.
   *
   * @param processInstanceId id of process instance to delete, cannot be null.
   * @param deleteReason reason for deleting, which will be stored in the history. Can be null.
   * @param skipCustomListeners if true, only the built-in {@link ExecutionListener}s
   * are notified with the {@link ExecutionListener#EVENTNAME_END} event.
   * @param externallyTerminated indicator if deletion triggered from external context, for instance
   *                             REST API call
   * @param skipIoMappings specifies whether input/output mappings for tasks should be invoked
   *
   *
   * @throws BadUserRequestException
   *          when no process instance is found with the given id or id is null.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#DELETE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#DELETE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteProcessInstance(String processInstanceId, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated, boolean skipIoMappings);

  /**
   * Delete an existing runtime process instance.
   *
   * Deletion propagates upward as far as necessary.
   *
   * @param processInstanceId id of process instance to delete, cannot be null.
   * @param deleteReason reason for deleting, which will be stored in the history. Can be null.
   * @param skipCustomListeners if true, only the built-in {@link ExecutionListener}s
   * are notified with the {@link ExecutionListener#EVENTNAME_END} event.
   * @param externallyTerminated indicator if deletion triggered from external context, for instance
   *                             REST API call
   * @param skipIoMappings specifies whether input/output mappings for tasks should be invoked
   * @param skipSubprocesses specifies whether subprocesses should be deleted
   *
   *
   * @throws BadUserRequestException
   *          when no process instance is found with the given id or id is null.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#DELETE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#DELETE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void deleteProcessInstance(String processInstanceId, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated, boolean skipIoMappings,
      boolean skipSubprocesses);

  /**
   * Finds the activity ids for all executions that are waiting in activities.
   * This is a list because a single activity can be active multiple times.
   *
   * Deletion propagates upward as far as necessary.
   *
   * @param executionId id of the process instance or the execution, cannot be null.
   *
   * @throws ProcessEngineException
   *          when no execution exists with the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  List<String> getActiveActivityIds(String executionId);

  /**
   * <p>Allows retrieving the activity instance tree for a given process instance.
   * The activity instance tree is aligned with the concept of scope in the BPMN specification.
   * Activities that are "on the same level of subprocess" (ie. part of the same scope, contained
   * in the same subprocess) will have their activity instances at the same level in the tree.</p>
   *
   * <h2>Examples:</h2>
   * <p><ul>
   *  <li>Process with two parallel user tasks after parallel Gateway: in the activity instance tree you
   *  will see two activity instances below the root instance, one for each user task.</li>
   *  <li>Process with two parallel Multi Instance user tasks after parallel Gateway: in the activity instance
   *  tree, all instances of both user tasks will be listed below the root activity instance. Reason: all
   *  activity instances are at the same level of subprocess.</li>
   *  <li>Usertask inside embedded subprocess: the activity instance three will have 3 levels: the root instance
   *  representing the process instance itself, below it an activity instance representing the instance of the embedded
   *  subprocess, and below this one, the activity instance representing the usertask.</li>
   * </ul></p>
   *
   * <h2>Identity & Uniqueness:</h2>
   * <p>Each activity instance is assigned a unique Id. The id is persistent, if you invoke this method multiple times,
   * the same activity instance ids will be returned for the same activity instances. (However, there might be
   * different executions assigned, see below)</p>
   *
   * <h2>Relation to Executions</h2>
   * <p>The {@link Execution} concept in the process engine is not completely aligned with the activity
   * instance concept because the execution tree is in general not aligned with the activity / scope concept in
   * BPMN. In general, there is a n-1 relationship between Executions and ActivityInstances, ie. at a given
   * point in time, an activity instance can be linked to multiple executions. In addition, it is not guaranteed
   * that the same execution that started a given activity instance will also end it. The process engine performs
   * several internal optimizations concerning the compacting of the execution tree which might lead to executions
   * being reordered and pruned. This can lead to situations where a given execution starts an activity instance
   * but another execution ends it. Another special case is the process instance: if the process instance is executing
   * a non-scope activity (for example a user task) below the process definition scope, it will be referenced
   * by both the root activity instance and the user task activity instance.
   *
   * <p><strong>If you need to interpret the state of a process instance in terms of a BPMN process model, it is usually easier to
   * use the activity instance tree as opposed to the execution tree.</strong></p>
   *
   * @param processInstanceId the id of the process instance for which the activity instance tree should be constructed.
   *
   * @return the activity instance tree for a given process instance or null if no such process instance exists.
   *
   * @throws ProcessEngineException
   *          if processInstanceId is 'null' or an internal error occurs.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.0
   */
  ActivityInstance getActivityInstance(String processInstanceId);

  /**
   * Sends an external trigger to an activity instance that is waiting inside the given execution.
   *
   * Note that you need to provide the exact execution that is waiting for the signal
   * if the process instance contains multiple executions.
   *
   * @param executionId id of process instance or execution to signal, cannot be null.
   *
   * @throws BadUserRequestException
   *          when no execution is found for the given executionId or id is null.
   * @throws SuspendedEntityInteractionException
   *          when the execution is suspended.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void signal(String executionId);

  /**
   * Sends an external trigger to an activity instance that is waiting inside the given execution.
   *
   * Note that you need to provide the exact execution that is waiting for the signal
   * if the process instance contains multiple executions.
   *
   * @param executionId id of process instance or execution to signal, cannot be null.
   * @param signalName name of the signal (can be null)
   * @param signalData additional data of the signal (can be null)
   * @param processVariables a map of process variables (can be null)
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void signal(String executionId, String signalName, Object signalData, Map<String, Object> processVariables);

  /**
   * Sends an external trigger to an activity instance that is waiting inside the given execution.
   *
   * Note that you need to provide the exact execution that is waiting for the signal
   * if the process instance contains multiple executions.
   *
   * @param executionId id of process instance or execution to signal, cannot be null.
   * @param processVariables a map of process variables
   *
   * @throws BadUserRequestException
   *          when no execution is found for the given executionId or id is null.
   * @throws SuspendedEntityInteractionException
   *          when the execution is suspended.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void signal(String executionId, Map<String, Object> processVariables);

  // Variables ////////////////////////////////////////////////////////////////////

  /**
   * All variables visible from the given execution scope (including parent scopes).
   *
   * @param executionId id of process instance or execution, cannot be null.
   *
   * @return the variables or an empty map if no such variables are found.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  Map<String, Object> getVariables(String executionId);

  /**
   * All variables visible from the given execution scope (including parent scopes).
   *
   * @param executionId id of process instance or execution, cannot be null.
   *
   * @return the variables or an empty map if no such variables are found.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.2
   *
   */
  VariableMap getVariablesTyped(String executionId);

  /**
   * All variables visible from the given execution scope (including parent scopes).
   *
   * @param executionId id of process instance or execution, cannot be null.
   * @param deserializeValues if false, {@link SerializableValue}s will not be deserialized
   *
   * @return the variables or an empty map if no such variables are found.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.2
   *
   */
  VariableMap getVariablesTyped(String executionId, boolean deserializeValues);

  /**
   * All variable values that are defined in the execution scope, without taking outer scopes into account.
   * If you have many task local variables and you only need a few, consider using {@link #getVariablesLocal(String, Collection)}
   * for better performance.
   * @param executionId id of execution, cannot be null.
   *
   * @return the variables or an empty map if no such variables are found.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  Map<String, Object> getVariablesLocal(String executionId);

  /**
   * All variable values that are defined in the execution scope, without taking outer scopes into account.
   * If you have many task local variables and you only need a few, consider using {@link #getVariablesLocal(String, Collection)}
   * for better performance.
   *
   * @param executionId id of execution, cannot be null.
   *
   * @return the variables or an empty map if no such variables are found.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  VariableMap getVariablesLocalTyped(String executionId);

  /**
   * All variable values that are defined in the execution scope, without taking outer scopes into account.
   * If you have many task local variables and you only need a few, consider using {@link #getVariablesLocal(String, Collection)}
   * for better performance.
   *
   * @param executionId id of execution, cannot be null.
   * @param deserializeObjectValues if false, {@link SerializableValue}s will not be deserialized
   *
   * @return the variables or an empty map if no such variables are found.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.2
   */
  VariableMap getVariablesLocalTyped(String executionId, boolean deserializeValues);

  /**
   * The variable values for all given variableNames, takes all variables into account which are visible from the given execution scope (including parent scopes).
   *
   * @param executionId id of process instance or execution, cannot be null.
   * @param variableNames the collection of variable names that should be retrieved.
   *
   * @return the variables or an empty map if no such variables are found.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  Map<String,Object> getVariables(String executionId, Collection<String> variableNames);

  /**
   * The variable values for all given variableNames, takes all variables into account which are visible from the given execution scope (including parent scopes).
   * @param executionId id of process instance or execution, cannot be null.
   * @param variableNames the collection of variable names that should be retrieved.
   * @param deserializeObjectValues if false, {@link SerializableValue}s will not be deserialized
   *
   * @return the variables or an empty map if no such variables are found.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.2
   *
   */
  VariableMap getVariablesTyped(String executionId, Collection<String> variableNames, boolean deserializeValues);

  /**
   * The variable values for the given variableNames only taking the given execution scope into account, not looking in outer scopes.
   *
   * @param executionId id of execution, cannot be null.
   * @param variableNames the collection of variable names that should be retrieved.
   *
   * @return the variables or an empty map if no such variables are found.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  Map<String,Object> getVariablesLocal(String executionId, Collection<String> variableNames);

  /**
   * The variable values for the given variableNames only taking the given execution scope into account, not looking in outer scopes.
   * @param executionId id of execution, cannot be null.
   * @param variableNames the collection of variable names that should be retrieved.
   * @param deserializeObjectValues if false, {@link SerializableValue}s will not be deserialized
   *
   * @return the variables or an empty map if no such variables are found.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.2
   *
   */
  VariableMap getVariablesLocalTyped(String executionId, Collection<String> variableNames, boolean deserializeValues);

  /**
   * The variable value.  Searching for the variable is done in all scopes that are visible to the given execution (including parent scopes).
   * Returns null when no variable value is found with the given name or when the value is set to null.
   *
   * @param executionId id of process instance or execution, cannot be null.
   * @param variableName name of variable, cannot be null.
   *
   * @return the variable value or null if the variable is undefined or the value of the variable is null.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  Object getVariable(String executionId, String variableName);

  /**
   * Returns a {@link TypedValue} for the variable. Searching for the variable is done in all scopes that are visible
   * to the given execution (including parent scopes). Returns null when no variable value is found with the given name.
   *
   * @param executionId id of process instance or execution, cannot be null.
   * @param variableName name of variable, cannot be null.
   *
   * @return the variable value or null if the variable is undefined.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.2
   *
   */
  <T extends TypedValue> T getVariableTyped(String executionId, String variableName);

  /**
   * Returns a {@link TypedValue} for the variable. Searching for the variable is done in all scopes that are visible
   * to the given execution (including parent scopes). Returns null when no variable value is found with the given name.
   *
   * @param executionId id of process instance or execution, cannot be null.
   * @param variableName name of variable, cannot be null.
   * @param deserializeValue if false, a {@link SerializableValue} will not be deserialized
   *
   * @return the variable value or null if the variable is undefined.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.2
   *
   */
  <T extends TypedValue> T getVariableTyped(String executionId, String variableName, boolean deserializeValue);

  /**
   * The variable value for an execution. Returns the value when the variable is set
   * for the execution (and not searching parent scopes). Returns null when no variable value is found with the given name or when the value is set to null.
   *
   * @param executionId id of process instance or execution, cannot be null.
   * @param variableName name of variable, cannot be null.
   *
   * @return the variable value or null if the variable is undefined or the value of the variable is null.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  Object getVariableLocal(String executionId, String variableName);

  /**
   * Returns a {@link TypedValue} for the variable. Returns the value when the variable is set
   * for the execution (and not searching parent scopes). Returns null when no variable value is found with the given name.
   *
   * @param executionId id of process instance or execution, cannot be null.
   * @param variableName name of variable, cannot be null.
   *
   * @return the variable value or null if the variable is undefined.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.2
   *
   */
  <T extends TypedValue> T getVariableLocalTyped(String executionId, String variableName);

  /**
   * Returns a {@link TypedValue} for the variable. Searching for the variable is done in all scopes that are visible
   * to the given execution (and not searching parent scopes). Returns null when no variable value is found with the given name.
   *
   * @param executionId id of process instance or execution, cannot be null.
   * @param variableName name of variable, cannot be null.
   * @param deserializeValue if false, a {@link SerializableValue} will not be deserialized
   *
   * @return the variable value or null if the variable is undefined.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#READ_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   *
   * @since 7.2
   *
   */
  <T extends TypedValue> T getVariableLocalTyped(String executionId, String variableName, boolean deserializeValue);

  /**
   * Update or create a variable for an execution.  If the variable does not already exist
   * somewhere in the execution hierarchy (i.e. the specified execution or any ancestor),
   * it will be created in the process instance (which is the root execution).
   *
   * @param executionId id of process instance or execution to set variable in, cannot be null.
   * @param variableName name of variable to set, cannot be null.
   * @param value value to set. When null is passed, the variable is not removed,
   * only it's value will be set to null.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void setVariable(String executionId, String variableName, Object value);

  /**
   * Update or create a variable for an execution (not considering parent scopes).
   * If the variable does not already exist, it will be created in the given execution.
   *
   * @param executionId id of execution to set variable in, cannot be null.
   * @param variableName name of variable to set, cannot be null.
   * @param value value to set. When null is passed, the variable is not removed,
   * only it's value will be set to null.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void setVariableLocal(String executionId, String variableName, Object value);

  /**
   * Update or create given variables for an execution (including parent scopes). If the variables are not already existing, they will be created in the process instance
   * (which is the root execution).
   *
   * @param executionId id of the process instance or the execution, cannot be null.
   * @param variables map containing name (key) and value of variables, can be null.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void setVariables(String executionId, Map<String, ? extends Object> variables);

  /**
   * Update or create given variables for an execution (not considering parent scopes). If the variables are not already existing, it will be created in the given execution.
   *
   * @param executionId id of the execution, cannot be null.
   * @param variables map containing name (key) and value of variables, can be null.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void setVariablesLocal(String executionId, Map<String, ? extends Object> variables);

  /**
   * Removes a variable for an execution.
   *
   * @param executionId id of process instance or execution to remove variable in.
   * @param variableName name of variable to remove.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void removeVariable(String executionId, String variableName);

  /**
   * Removes a variable for an execution (not considering parent scopes).
   *
   * @param executionId id of execution to remove variable in.
   * @param variableName name of variable to remove.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void removeVariableLocal(String executionId, String variableName);

  /**
   * Removes variables for an execution.
   *
   * @param executionId id of process instance or execution to remove variable in.
   * @param variableNames collection containing name of variables to remove.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void removeVariables(String executionId, Collection<String> variableNames);

  /**
   * Remove variables for an execution (not considering parent scopes).
   *
   * @param executionId id of execution to remove variable in.
   * @param variableNames collection containing name of variables to remove.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void removeVariablesLocal(String executionId, Collection<String> variableNames);

  // Queries ////////////////////////////////////////////////////////

  /** Creates a new {@link ExecutionQuery} instance,
   * that can be used to query the executions and process instances. */
  ExecutionQuery createExecutionQuery();

  /**
   * creates a new {@link NativeExecutionQuery} to query {@link Execution}s
   * by SQL directly
   */
  NativeExecutionQuery createNativeExecutionQuery();

  /**
   * Creates a new {@link ProcessInstanceQuery} instance, that can be used
   * to query process instances.
   */
  ProcessInstanceQuery createProcessInstanceQuery();

  /**
   * creates a new {@link NativeProcessInstanceQuery} to query {@link ProcessInstance}s
   * by SQL directly
   */
  NativeProcessInstanceQuery createNativeProcessInstanceQuery();

  /**
   * Creates a new {@link IncidentQuery} instance, that can be used
   * to query incidents.
   */
  IncidentQuery createIncidentQuery();

  /**
   * Creates a new {@link EventSubscriptionQuery} instance, that can be used to query
   * event subscriptions.
   */
  EventSubscriptionQuery createEventSubscriptionQuery();

  /**
   * Creates a new {@link VariableInstanceQuery} instance, that can be used to query
   * variable instances.
   */
  VariableInstanceQuery createVariableInstanceQuery();

  // Process instance state //////////////////////////////////////////

  /**
   * <p>Suspends the process instance with the given id. This means that the
   * execution is stopped, so the <i>token state</i> will not change.
   * However, actions that do not change token state, like setting/removing
   * variables, etc. will succeed.</p>
   *
   * <p>Tasks belonging to this process instance will also be suspended. This means
   * that any actions influencing the tasks' lifecycles will fail, such as
   * <ul>
   *   <li>claiming</li>
   *   <li>completing</li>
   *   <li>delegation</li>
   *   <li>changes in task assignees, owners, etc.</li>
   * </ul>
   * Actions that only change task properties will succeed, such as changing variables
   * or adding comments.
   * </p>
   *
   * <p>If a process instance is in state suspended, the engine will also not
   * execute jobs (timers, messages) associated with this instance.</p>
   *
   * <p>If you have a process instance hierarchy, suspending
   * one process instance from the hierarchy will not suspend other
   * process instances from that hierarchy.</p>
   *
   * <p>Note: for more complex suspend commands use {@link #updateProcessInstanceSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          if no such processInstance can be found.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void suspendProcessInstanceById(String processInstanceId);

  /**
   * <p>Suspends the process instances with the given process definition id.
   * This means that the execution is stopped, so the <i>token state</i>
   * will not change. However, actions that do not change token state, like
   * setting/removing variables, etc. will succeed.</p>
   *
   * <p>Tasks belonging to the suspended process instance will also be suspended.
   * This means that any actions influencing the tasks' lifecycles will fail, such as
   * <ul>
   *   <li>claiming</li>
   *   <li>completing</li>
   *   <li>delegation</li>
   *   <li>changes in task assignees, owners, etc.</li>
   * </ul>
   * Actions that only change task properties will succeed, such as changing variables
   * or adding comments.
   * </p>
   *
   * <p>If a process instance is in state suspended, the engine will also not
   * execute jobs (timers, messages) associated with this instance.</p>
   *
   * <p>If you have a process instance hierarchy, suspending
   * one process instance from the hierarchy will not suspend other
   * process instances from that hierarchy.</p>
   *
   * <p>Note: for more complex suspend commands use {@link #updateProcessInstanceSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          if no such processInstance can be found.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void suspendProcessInstanceByProcessDefinitionId(String processDefinitionId);

  /**
   * <p>Suspends the process instances with the given process definition key.
   * This means that the execution is stopped, so the <i>token state</i>
   * will not change. However, actions that do not change token state, like
   * setting/removing variables, etc. will succeed.</p>
   *
   * <p>Tasks belonging to the suspended process instance will also be suspended.
   * This means that any actions influencing the tasks' lifecycles will fail, such as
   * <ul>
   *   <li>claiming</li>
   *   <li>completing</li>
   *   <li>delegation</li>
   *   <li>changes in task assignees, owners, etc.</li>
   * </ul>
   * Actions that only change task properties will succeed, such as changing variables
   * or adding comments.
   * </p>
   *
   * <p>If a process instance is in state suspended, the engine will also not
   * execute jobs (timers, messages) associated with this instance.</p>
   *
   * <p>If you have a process instance hierarchy, suspending
   * one process instance from the hierarchy will not suspend other
   * process instances from that hierarchy.</p>
   *
   * <p>Note: for more complex suspend commands use {@link #updateProcessInstanceSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          if no such processInstance can be found.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void suspendProcessInstanceByProcessDefinitionKey(String processDefinitionKey);

  /**
   * <p>Activates the process instance with the given id.</p>
   *
   * <p>If you have a process instance hierarchy, activating
   * one process instance from the hierarchy will not activate other
   * process instances from that hierarchy.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateProcessInstanceSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          if no such processInstance can be found.
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void activateProcessInstanceById(String processInstanceId);

  /**
   * <p>Activates the process instance with the given process definition id.</p>
   *
   * <p>If you have a process instance hierarchy, activating
   * one process instance from the hierarchy will not activate other
   * process instances from that hierarchy.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateProcessInstanceSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          if the process definition id is null
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void activateProcessInstanceByProcessDefinitionId(String processDefinitionId);

  /**
   * <p>Activates the process instance with the given process definition key.</p>
   *
   * <p>If you have a process instance hierarchy, activating
   * one process instance from the hierarchy will not activate other
   * process instances from that hierarchy.</p>
   *
   * <p>Note: for more complex activate commands use {@link #updateProcessInstanceSuspensionState()}.</p>
   *
   * @throws ProcessEngineException
   *          if the process definition id is null
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void activateProcessInstanceByProcessDefinitionKey(String processDefinitionKey);

  /**
   * Activate or suspend process instances using a fluent builder. Specify the
   * instances by calling one of the <i>by</i> methods, like
   * <i>byProcessInstanceId</i>. To update the suspension state call
   * {@link UpdateProcessInstanceSuspensionStateBuilder#activate()} or
   * {@link UpdateProcessInstanceSuspensionStateBuilder#suspend()}.
   *
   * @return the builder to update the suspension state
   */
  UpdateProcessInstanceSuspensionStateSelectBuilder updateProcessInstanceSuspensionState();

  // Events ////////////////////////////////////////////////////////////////////////

  /**
   * Notifies the process engine that a signal event of name 'signalName' has
   * been received. Delivers the signal to all executions waiting on
   * the signal and to all process definitions that can started by this signal. <p/>
   *
   * <strong>NOTE:</strong> Notification and instantiation happen synchronously.
   *
   * @param signalName
   *          the name of the signal event
   *
   * @throws AuthorizationException
   *          <li>if notify an execution and the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.</li>
   *          <li>if start a new process instance and the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.</li>
   */
  void signalEventReceived(String signalName);

  /**
   * Notifies the process engine that a signal event of name 'signalName' has
   * been received. Delivers the signal to all executions waiting on
   * the signal and to all process definitions that can started by this signal. <p/>
   *
   * <strong>NOTE:</strong> Notification and instantiation happen synchronously.
   *
   * @param signalName
   *          the name of the signal event
   * @param processVariables
   *          a map of variables added to the execution(s)
   *
   * @throws AuthorizationException
   *          <li>if notify an execution and the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.</li>
   *          <li>if start a new process instance and the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.</li>
   */
  void signalEventReceived(String signalName, Map<String, Object> processVariables);

  /**
   * Notifies the process engine that a signal event of name 'signalName' has
   * been received. This method delivers the signal to a single execution, being the
   * execution referenced by 'executionId'.
   * The waiting execution is notified synchronously.
   *
   * Note that you need to provide the exact execution that is waiting for the signal
   * if the process instance contains multiple executions.
   *
   * @param signalName
   *          the name of the signal event
   * @param executionId
   *          id of the process instance or the execution to deliver the signal to
   *
   * @throws ProcessEngineException
   *          if no such execution exists or if the execution
   *          has not subscribed to the signal
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void signalEventReceived(String signalName, String executionId);

  /**
   * Notifies the process engine that a signal event of name 'signalName' has
   * been received. This method delivers the signal to a single execution, being the
   * execution referenced by 'executionId'.
   * The waiting execution is notified synchronously.
   *
   * Note that you need to provide the exact execution that is waiting for the signal
   * if the process instance contains multiple executions.
   *
   * @param signalName
   *          the name of the signal event
   * @param executionId
   *          the id of the process instance or the execution to deliver the signal to
   * @param processVariables
   *          a map of variables added to the execution(s)
   *
   * @throws ProcessEngineException
   *          if no such execution exists or if the execution
   *          has not subscribed to the signal
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void signalEventReceived(String signalName, String executionId, Map<String, Object> processVariables);

  /**
   * Notifies the process engine that a signal event has been received using a
   * fluent builder.
   *
   * @param signalName
   *          the name of the signal event
   * @return the fluent builder to send the signal
   */
  SignalEventReceivedBuilder createSignalEvent(String signalName);

  /**
   * Notifies the process engine that a message event with name 'messageName' has
   * been received and has been correlated to an execution with id 'executionId'.
   *
   * The waiting execution is notified synchronously.
   *
   * Note that you need to provide the exact execution that is waiting for the message
   * if the process instance contains multiple executions.
   *
   * @param messageName
   *          the name of the message event
   * @param executionId
   *          the id of the process instance or the execution to deliver the message to
   *
   * @throws ProcessEngineException
   *          if no such execution exists or if the execution
   *          has not subscribed to the signal
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void messageEventReceived(String messageName, String executionId);

  /**
   * Notifies the process engine that a message event with the name 'messageName' has
   * been received and has been correlated to an execution with id 'executionId'.
   *
   * The waiting execution is notified synchronously.
   *
   * Note that you need to provide the exact execution that is waiting for the message
   * if the process instance contains multiple executions.
   *
   * @param messageName
   *          the name of the message event
   * @param executionId
   *          the id of the process instance or the execution to deliver the message to
   * @param processVariables
   *          a map of variables added to the execution
   *
   * @throws ProcessEngineException
   *          if no such execution exists or if the execution
   *          has not subscribed to the signal
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void messageEventReceived(String messageName, String executionId, Map<String, Object> processVariables);

  /**
   * Define a complex message correlation using a fluent builder.
   *
   * @param messageName the name of the message. Corresponds to the 'name' element
   * of the message defined in BPMN 2.0 Xml.
   * Can be null to correlate by other criteria (businessKey, processInstanceId, correlationKeys) only.
   *
   * @return the fluent builder for defining the message correlation.
   */
  MessageCorrelationBuilder createMessageCorrelation(String messageName);

  /**
   * Correlates a message to either an execution that is waiting for this message or a process definition
   * that can be started by this message.
   *
   * Notification and instantiation happen synchronously.
   *
   * @param messageName
   *          the name of the message event; if null, matches any event
   *
   * @throws MismatchingMessageCorrelationException
   *          if none or more than one execution or process definition is correlated
   * @throws ProcessEngineException
   *          if messageName is null
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void correlateMessage(String messageName);

  /**
   * Correlates a message to
   * <ul>
   *  <li>
   *    an execution that is waiting for a matching message and belongs to a process instance with the given business key
   *  </li>
   *  <li>
   *    a process definition that can be started by a matching message.
   *  </li>
   * </ul>
   *
   * Notification and instantiation happen synchronously.
   *
   * @param messageName
   *          the name of the message event; if null, matches any event
   * @param businessKey
   *          the business key of process instances to correlate against
   *
   * @throws MismatchingMessageCorrelationException
   *          if none or more than one execution or process definition is correlated
   * @throws ProcessEngineException
   *          if messageName is null and businessKey is null
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void correlateMessage(String messageName, String businessKey);

  /**
   * Correlates a message to
   * <ul>
   *  <li>
   *    an execution that is waiting for a matching message and can be correlated according
   *    to the given correlation keys. This is typically matched against process instance variables.
   *  </li>
   *  <li>
   *    a process definition that can be started by message with the provided name.
   *  </li>
   * </ul>
   *
   * Notification and instantiation happen synchronously.
   *
   * @param messageName
   *          the name of the message event; if null, matches any event
   * @param correlationKeys
   *          a map of key value pairs that are used to correlate the message to an execution
   *
   * @throws MismatchingMessageCorrelationException
   *          if none or more than one execution or process definition is correlated
   * @throws ProcessEngineException
   *          if messageName is null and correlationKeys is null
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void correlateMessage(String messageName, Map<String, Object> correlationKeys);

  /**
   * Correlates a message to
   * <ul>
   *  <li>
   *    an execution that is waiting for a matching message and belongs to a process instance with the given business key
   *  </li>
   *  <li>
   *    a process definition that can be started by this message.
   *  </li>
   * </ul>
   * and updates the process instance variables.
   *
   * Notification and instantiation happen synchronously.
   *
   * @param messageName
   *          the name of the message event; if null, matches any event
   * @param businessKey
   *          the business key of process instances to correlate against
   * @param processVariables
   *          a map of variables added to the execution or newly created process instance
   *
   * @throws MismatchingMessageCorrelationException
   *          if none or more than one execution or process definition is correlated
   * @throws ProcessEngineException
   *          if messageName is null and businessKey is null
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void correlateMessage(String messageName, String businessKey, Map<String, Object> processVariables);

  /**
   * Correlates a message to
   * <ul>
   *  <li>
   *    an execution that is waiting for a matching message and can be correlated according
   *    to the given correlation keys. This is typically matched against process instance variables.
   *  </li>
   *  <li>
   *    a process definition that can be started by this message.
   *  </li>
   * </ul>
   * and updates the process instance variables.
   *
   * Notification and instantiation happen synchronously.
   *
   * @param messageName
   *          the name of the message event; if null, matches any event
   * @param correlationKeys
   *          a map of key value pairs that are used to correlate the message to an execution
   * @param processVariables
   *          a map of variables added to the execution or newly created process instance
   *
   * @throws MismatchingMessageCorrelationException
   *          if none or more than one execution or process definition is correlated
   * @throws ProcessEngineException
   *          if messageName is null and correlationKeys is null
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void correlateMessage(String messageName, Map<String, Object> correlationKeys, Map<String, Object> processVariables);

  /**
   * Correlates a message to
   * <ul>
   *  <li>
   *    an execution that is waiting for a matching message and can be correlated according
   *    to the given correlation keys. This is typically matched against process instance variables.
   *    The process instance it belongs to has to have the given business key.
   *  </li>
   *  <li>
   *    a process definition that can be started by this message.
   *  </li>
   * </ul>
   * and updates the process instance variables.
   *
   * Notification and instantiation happen synchronously.
   *
   * @param messageName
   *          the name of the message event; if null, matches any event
   * @param businessKey
   *          the business key of process instances to correlate against
   * @param correlationKeys
   *          a map of key value pairs that are used to correlate the message to an execution
   * @param processVariables
   *          a map of variables added to the execution or newly created process instance
   *
   * @throws MismatchingMessageCorrelationException
   *          if none or more than one execution or process definition is correlated
   * @throws ProcessEngineException
   *          if messageName is null and businessKey is null and correlationKeys is null
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void correlateMessage(String messageName, String businessKey, Map<String, Object> correlationKeys, Map<String, Object> processVariables);

  /**
   * Define a modification of a process instance in terms of activity cancellations
   * and instantiations via a fluent builder. Instructions are executed in the order they are specified.
   *
   * @param processInstanceId the process instance to modify
   */
  ProcessInstanceModificationBuilder createProcessInstanceModification(String processInstanceId);

  /**
   * Returns a fluent builder to start a new process instance in the exactly
   * specified version of the process definition with the given id. The builder
   * can be used to set further properties and specify instantiation
   * instructions to start the instance at any set of activities in the process.
   * If no instantiation instructions are set then the instance start at the
   * default start activity.
   *
   * @param processDefinitionId
   *          the id of the process definition, cannot be <code>null</code>.
   *
   * @return a builder to create a process instance of the definition
   */
  ProcessInstantiationBuilder createProcessInstanceById(String processDefinitionId);

  /**
   * Returns a fluent builder to start a new process instance in the latest
   * version of the process definition with the given key. The builder can be
   * used to set further properties and specify instantiation instructions to
   * start the instance at any set of activities in the process. If no
   * instantiation instructions are set then the instance start at the default
   * start activity.
   *
   * @param processDefinitionKey
   *          the key of the process definition, cannot be <code>null</code>.
   *
   * @return a builder to create a process instance of the definition
   */
  ProcessInstantiationBuilder createProcessInstanceByKey(String processDefinitionKey);

  /**
   * Creates a migration plan to migrate process instance between different process definitions.
   * Returns a fluent builder that can be used to specify migration instructions and build the plan.
   *
   * @param sourceProcessDefinitionId the process definition that instances are migrated from
   * @param targetProcessDefinitionId the process definition that instances are migrated to
   * @return a fluent builder
   */
  MigrationPlanBuilder createMigrationPlan(String sourceProcessDefinitionId, String targetProcessDefinitionId);

  /**
   * Executes a migration plan for a given list of process instances. The migration can
   * either be executed synchronously or asynchronously. A synchronously migration
   * blocks the caller until the migration was completed. The migration can only be
   * successfully completed if all process instances can be migrated.
   *
   * If the migration is executed asynchronously a {@link Batch} is immediately returned.
   * The migration is then executed as jobs from the process engine and the batch can
   * be used to track the progress of the migration. The Batch splits the migration
   * in smaller chunks which will be executed independently.
   *
   * @param migrationPlan the migration plan to executed
   * @return a fluent builder
   */
  MigrationPlanExecutionBuilder newMigration(MigrationPlan migrationPlan);

  /**
   * Creates a modification of multiple process instances in terms of activity cancellations
   * and instantiations via a fluent builder. Returns a fluent builder that can be used to specify
   * modification instructions and set process instances that should be modified.
   *
   * The modification can
   * either be executed synchronously or asynchronously. A synchronously modification
   * blocks the caller until the modification was completed. The modification can only be
   * successfully completed if all process instances can be modified.
   *
   * If the modification is executed asynchronously a {@link Batch} is immediately returned.
   * The modification is then executed as jobs from the process engine and the batch can
   * be used to track the progress of the modification. The Batch splits the modification
   * in smaller chunks which will be executed independently.
   *
   * @param processDefinitionId the process definition that instances are modified of
   * @return a fluent builder
   */

  ModificationBuilder createModification(String processDefinitionId);

  /**
   * Restarts process instances that are completed or deleted with the initial or last set of variables.
   *
   * @param processDefinitionId the id of the process definition, cannot be null.
   *
   * @throws ProcessEngineException
   *          when no process definition is deployed with the given key or a process instance is still active.
   * @throws AuthorizationException
   *          if the user has not all of the following permissions
   *     <ul>
   *       <li>{@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}</li>
   *       <li>{@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *       <li>{@link Permissions#READ_HISTORY} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *     </ul>
   */
  RestartProcessInstanceBuilder restartProcessInstances(String processDefinitionId);

  /**
   * Creates an incident
   *
   * @param incidentType the type of incident, cannot be null
   * @param executionId execution id, cannot be null
   * @param configuration
   *
   * @return a new incident
   *
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  Incident createIncident(String incidentType, String executionId, String configuration);

  /**
   * Creates an incident
   *
   * @param incidentType the type of incident, cannot be null
   * @param executionId execution id, cannot be null
   * @param configuration
   * @param message
   *
   * @return a new incident
   *
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  Incident createIncident(String incidentType, String executionId, String configuration, String message);

  /**
   * Resolves and remove an incident
   *
   * @param incidentId the id of an incident to resolve
   *
   * @throws AuthorizationException
   *          if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void resolveIncident(String incidentId);

  /**
   * Define a complex condition evaluation using a fluent builder.
   *
   * @return the fluent builder for defining the condition evaluation.
   */
  ConditionEvaluationBuilder createConditionEvaluation();

}
