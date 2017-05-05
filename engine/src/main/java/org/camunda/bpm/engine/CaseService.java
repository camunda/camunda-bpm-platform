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
import java.util.Map;

import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionCommandBuilder;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceBuilder;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Service which provides access to {@link CaseInstance case instances}
 * and {@link CaseExecution case executions}.
 *
 * @author Roman Smirnov
 *
 * @since 7.2
 *
 */
public interface CaseService {

  /**
   * <p>Creates a new {@link CaseInstance} of the latest version of the case definition
   * with the given key. The new case instance will be in the <code>ACTIVE</code> state.</p>
   *
   * @param caseDefinitionKey the key of the case definition to instantiate
   *
   * @throws NotValidException when the given case definition key is null.
   * @throws NotFoundException when no case definition is deployed with the given key.
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  CaseInstance createCaseInstanceByKey(String caseDefinitionKey);

  /**
   * <p>Creates a new {@link CaseInstance} of the latest version of the case definition
   * with the given key. The new case instance will be in the <code>ACTIVE</code> state.</p>
   *
   * <p>A business key can be provided to associate the case instance with a
   * certain identifier that has a clear business meaning. This business key can
   * then be used to easily look up that case instance, see
   * {@link CaseInstanceQuery#caseInstanceBusinessKey(String)}. Providing such a
   * business key is definitely a best practice.</p>
   *
   * <p>Note that a business key MUST be unique for the given case definition WHEN
   * you have added a database constraint for it. In this case, only case instance
   * from different case definition are allowed to have the same business key and
   * the combination of caseDefinitionKey-businessKey must be unique.</p>
   *
   * @param caseDefinitionKey the key of the case definition to instantiate
   * @param businessKey
   *          a key that uniquely identifies the case instance in the context
   *          of the given case definition.
   *
   * @throws NotValidException when the given case definition key is null.
   * @throws NotFoundException when no case definition is deployed with the given key.
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  CaseInstance createCaseInstanceByKey(String caseDefinitionKey, String businessKey);

  /**
   * <p>Creates a new {@link CaseInstance} of the latest version of the case definition
   * with the given key. The new case instance will be in the <code>ACTIVE</code> state.</p>
   *
   * @param caseDefinitionKey the key of the case definition to instantiate
   * @param variables variables to be set on the new case instance
   *
   * @throws NotValidException when the given case definition key is null.
   * @throws NotFoundException when no case definition is deployed with the given key.
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  CaseInstance createCaseInstanceByKey(String caseDefinitionKey, Map<String, Object> variables);

  /**
   * <p>Creates a new {@link CaseInstance} of the latest version of the case definition
   * with the given key. The new case instance will be in the <code>ACTIVE</code> state.</p>
   *
   * <p>A business key can be provided to associate the case instance with a
   * certain identifier that has a clear business meaning. This business key can
   * then be used to easily look up that case instance, see
   * {@link CaseInstanceQuery#caseInstanceBusinessKey(String)}. Providing such a
   * business key is definitely a best practice.</p>
   *
   * <p>Note that a business key MUST be unique for the given case definition WHEN
   * you have added a database constraint for it. In this case, only case instance
   * from different case definition are allowed to have the same business key and
   * the combination of caseDefinitionKey-businessKey must be unique.</p>
   *
   * @param caseDefinitionKey the key of the case definition to instantiate.
   * @param businessKey
   *          a key that uniquely identifies the case instance in the context
   *          of the given case definition.
   * @param variables variables to be set on the new case instance.
   *
   * @throws NotValidException when the given case definition key is null.
   * @throws NotFoundException when no case definition is deployed with the given key.
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  CaseInstance createCaseInstanceByKey(String caseDefinitionKey, String businessKey, Map<String, Object> variables);

  /**
   * <p>Creates a new {@link CaseInstance} in the exactly specified version identify by the provided
   * process definition id. The new case instance will be in the <code>ACTIVE</code> state.</p>
   *
   * @param caseDefinitionId the id of the case definition to instantiate
   *
   * @throws NotValidException when the given case definition id is null.
   * @throws NotFoundException when no case definition is deployed with the given id.
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  CaseInstance createCaseInstanceById(String caseDefinitionId);

  /**
   * <p>Creates a new {@link CaseInstance} in the exactly specified version identify by the provided
   * process definition id. The new case instance will be in the <code>ACTIVE</code> state.</p>
   *
   * <p>A business key can be provided to associate the case instance with a
   * certain identifier that has a clear business meaning. This business key can
   * then be used to easily look up that case instance, see
   * {@link CaseInstanceQuery#caseInstanceBusinessKey(String)}. Providing such a
   * business key is definitely a best practice.</p>
   *
   * <p>Note that a business key MUST be unique for the given case definition WHEN
   * you have added a database constraint for it. In this case, only case instance
   * from different case definition are allowed to have the same business key and
   * the combination of caseDefinitionKey-businessKey must be unique.</p>
   *
   * @param caseDefinitionId the id of the case definition to instantiate
   * @param businessKey
   *          a key that uniquely identifies the case instance in the context
   *          of the given case definition.
   *
   * @throws NotValidException when the given case definition id is null.
   * @throws NotFoundException when no case definition is deployed with the given id.
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  CaseInstance createCaseInstanceById(String caseDefinitionId, String businessKey);

  /**
   * <p>Creates a new {@link CaseInstance} in the exactly specified version identify by the provided
   * process definition id. The new case instance will be in the <code>ACTIVE</code> state.</p>
   *
   * @param caseDefinitionId the id of the case definition to instantiate
   * @param variables variables to be set on the new case instance.
   *
   * @throws NotValidException when the given case definition id is null.
   * @throws NotFoundException when no case definition is deployed with the given id.
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  CaseInstance createCaseInstanceById(String caseDefinitionId, Map<String, Object> variables);

  /**
   * <p>Creates a new {@link CaseInstance} in the exactly specified version identify by the provided
   * process definition id. The new case instance will be in the <code>ACTIVE</code> state.</p>
   *
   * <p>A business key can be provided to associate the case instance with a
   * certain identifier that has a clear business meaning. This business key can
   * then be used to easily look up that case instance, see
   * {@link CaseInstanceQuery#caseInstanceBusinessKey(String)}. Providing such a
   * business key is definitely a best practice.</p>
   *
   * <p>Note that a business key MUST be unique for the given case definition WHEN
   * you have added a database constraint for it. In this case, only case instance
   * from different case definition are allowed to have the same business key and
   * the combination of caseDefinitionKey-businessKey must be unique.</p>
   *
   * @param caseDefinitionId the id of the case definition to instantiate
   * @param businessKey
   *          a key that uniquely identifies the case instance in the context
   *          of the given case definition.
   * @param variables variables to be set on the new case instance.
   *
   * @throws NotValidException when the given case definition id is null.
   * @throws NotFoundException when no case definition is deployed with the given id.
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  CaseInstance createCaseInstanceById(String caseDefinitionId, String businessKey, Map<String, Object> variables);

  /**
   * <p>Starts the case execution identified by the given id manually.
   * Performs the transition from state
   * <code>ENABLED</code> to state <code>ACTIVE</code>.</p>
   *
   * <p>According to CMMN 1.0 specification, the state <code>ACTIVE</code> means that the
   * {@link Stage} or {@link Task} related to the case execution does the following:
   *   <ul>
   *     <li>{@link Task}: the {@link Task task} is completed immediately</li>
   *     <li>{@link HumanTask}: a new {@link org.camunda.bpm.engine.task.Task user task} is instantiated</li>
   *     <li>{@link ProcessTask}: a new {@link ProcessInstance process instance} is instantiated</li>
   *     <li>{@link CaseTask}: a new {@link CaseInstance case instance} is instantiated</li>
   *   </ul>
   * </p>
   *
   * @param caseExecutionId the id of the case execution to manually start
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the
   *      given case execution id
   * @throws NotAllowedException when the transition is not allowed to be done or
   *      when the case execution is a case instance
   * @throws ProcessEngineException when an internal exception happens during the execution
   *     of the command.
   */
  void manuallyStartCaseExecution(String caseExecutionId);

  /**
   * <p>Starts the case execution identified by the given id manually.
   * Performs a transition from state
   * <code>ENABLED</code> to state <code>ACTIVE</code>.</p>
   *
   * <p>According to CMMN 1.0 specification, the state <code>ACTIVE</code> means that the
   * {@link Stage} or {@link Task} related to the case execution does the following:
   *   <ul>
   *     <li>{@link Task}: the {@link Task task} is completed immediately</li>
   *     <li>{@link HumanTask}: a new {@link org.camunda.bpm.engine.task.Task user task} is instantiated</li>
   *     <li>{@link ProcessTask}: a new {@link ProcessInstance process instance} is instantiated</li>
   *     <li>{@link CaseTask}: a new {@link CaseInstance case instance} is instantiated</li>
   *   </ul>
   * </p>
   *
   * @param caseExecutionId the id of the case execution to manually start
   * @param variables variables to be set on the case execution
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the
   *      given case execution id
   * @throws NotAllowedException when the transition is not allowed to be done or
   *      when the case execution is a case instance
   * @throws ProcessEngineException when an internal exception happens during the execution
   *     of the command.
   */
  void manuallyStartCaseExecution(String caseExecutionId, Map<String, Object> variables);

  /**
   * <p>Disables the case execution  identified by the given id.
   * Performs a transition from state <code>ENABLED</code>
   * to state <code>DISABLED</code>.</p>
   *
   * <p>According to CMMN 1.0 specification, the state <code>DISABLED</code> means that the
   * {@link Stage} or {@link Task} related to the case execution should not be executed
   * in this case instance.</p>
   *
   * <p>If the given case execution has a parent case execution, that parent
   * case execution will be notified that the given case execution has been
   * disabled. This can lead to a completion of the parent case execution if
   * the completion criteria are fulfilled.</p>
   *
   * @param caseExecutionId the id of the case execution to disable
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the
   *      given case execution id
   * @throws NotAllowedException when the transition is not allowed to be done or
   *      when the case execution is a case instance
   * @throws ProcessEngineException when an internal exception happens during the execution
   *     of the command.
   */
  void disableCaseExecution(String caseExecutionId);

  /**
   * <p>Disables the case execution identified by the given id.
   * Performs a transition from state <code>ENABLED</code>
   * to state <code>DISABLED</code>.</p>
   *
   * <p>According to CMMN 1.0 specification, the state <code>DISABLED</code> means that the
   * {@link Stage} or {@link Task} related to the case execution should not be executed
   * in this case instance.</p>
   *
   * <p>If the given case execution has a parent case execution, that parent
   * case execution will be notified that the given case execution has been
   * disabled. This can lead to a completion of the parent case execution if
   * the completion criteria are fulfilled.</p>
   *
   * @param caseExecutionId the id of the case execution to disable
   * @param variables variables to be set on the case execution
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the
   *      given case execution id
   * @throws NotAllowedException when the transition is not allowed to be done or
   *      when the case execution is a case instance
   * @throws ProcessEngineException when an internal exception happens during the execution
   *     of the command.
   */
  void disableCaseExecution(String caseExecutionId, Map<String, Object> variables);

  /**
   * <p>Re-enables the case execution identified by the given id.
   * Performs a transition from state <code>DISABLED</code>
   * to state <code>ENABLED</code>.</p>
   *
   * <p>According to CMMN 1.0 specification, the state <code>DISABLED</code> means that the
   * {@link Stage} or {@link Task} related to the case execution pends for a decision
   * to become <code>ACTIVE</code> or <code>DISABLED</code>.</p>
   *
   * @param caseExecutionId the id of the case execution to re-enable
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the
   *      given case execution id
   * @throws NotAllowedException when the transition is not allowed to be done or
   *      when the case execution is a case instance
   * @throws ProcessEngineException when an internal exception happens during the execution
   *     of the command.
   */
  void reenableCaseExecution(String caseExecutionId);

  /**
   * <p>Re-enables the case execution identified by the given id.
   * Performs a transition from state <code>DISABLED</code>
   * to state <code>ENABLED</code>.</p>
   *
   * <p>According to CMMN 1.0 specification, the state <code>DISABLED</code> means that the
   * {@link Stage} or {@link Task} related to the case execution pends for a decision
   * to become <code>ACTIVE</code> or <code>DISABLED</code>.</p>
   *
   * @param caseExecutionId the id of the case execution to re-enable
   * @param variables variables to be set on the case execution
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the
   *      given case execution id
   * @throws NotAllowedException when the transition is not allowed to be done or
   *      when the case execution is a case instance
   * @throws ProcessEngineException when an internal exception happens during the execution
   *     of the command.
   */
  void reenableCaseExecution(String caseExecutionId, Map<String, Object> variables);

  /**
   *
   * <p>Completes the case execution identified by the given id.
   * Performs a transition from state <code>ACTIVE</code>
   * to state <code>COMPLETED</code>.</p>
   *
   * <p>It is only possible to complete a case execution which is associated with a
   * {@link Stage} or {@link Task}.</p>
   *
   * <p>In case of a {@link Stage}, the completion can only be performed when the following
   * criteria are fulfilled:<br>
   * <ul>
   *  <li>there are no children in the state <code>ACTIVE</code></li>
   * </ul>
   * </p>
   *
   * <p>For a {@link Task} instance, this means its purpose has been accomplished:<br>
   *  <ul>
   *    <li>{@link HumanTask} has been completed by human.</li>
   *  </ul>
   * </p>
   *
   * <p>If the given case execution has a parent case execution, that parent
   * case execution will be notified that the given case execution has been
   * completed. This can lead to a completion of the parent case execution if
   * the completion criteria are fulfilled.</p>
   *
   * @param caseExecutionId the id of the case execution to complete
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the
   *      given case execution id
   * @throws NotAllowedException when the transition is not allowed to be done
   * @throws ProcessEngineException when an internal exception happens during the execution
   *     of the command.
   */
  void completeCaseExecution(String caseExecutionId);

  /**
  *
  * <p>Completes the case execution identified by the given id.
  * Performs a transition from state <code>ACTIVE</code>
  * to state <code>COMPLETED</code>.</p>
  *
  * <p>It is only possible to complete a case execution which is associated with a
  * {@link Stage} or {@link Task}.</p>
  *
  * <p>In case of a {@link Stage}, the completion can only be performed when the following
  * criteria are fulfilled:<br>
  * <ul>
  *  <li>there are no children in the state <code>ACTIVE</code></li>
  * </ul>
  * </p>
  *
  * <p>For a {@link Task} instance, this means its purpose has been accomplished:<br>
  *  <ul>
  *    <li>{@link HumanTask} has been completed by human.</li>
  *  </ul>
  * </p>
  *
  * <p>If the given case execution has a parent case execution, that parent
  * case execution will be notified that the given case execution has been
  * completed. This can lead to a completion of the parent case execution if
  * the completion criteria are fulfilled.</p>
  *
  * @param caseExecutionId the id of the case execution to complete
  * @param variables variables to be set on the case execution
  *
  * @throws NotValidException when the given case execution id is null
  * @throws NotFoundException when no case execution is found for the
  *      given case execution id
  * @throws NotAllowedException when the transition is not allowed to be done
  * @throws ProcessEngineException when an internal exception happens during the execution
  *     of the command.
  */
  void completeCaseExecution(String caseExecutionId, Map<String, Object> variables);

  /**
   * <p>Closes the case instance the execution identified by the given id
   * belongs to. Once closed, no further work or modifications are
   * allowed for the case instance.
   * Performs a transition from state <code>COMPLETED</code>
   * to state <code>CLOSED</code>.</p>
   *
   * @param caseExecutionId the id of the case execution to close
   *   the case instance for
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the
   *      given case execution id
   * @throws NotAllowedException when the transition is not allowed to be done
   * @throws ProcessEngineException when an internal exception happens during the execution
   *     of the command.
   */
  void closeCaseInstance(String caseExecutionId);

  /**
   * <p>Terminates the case execution identified by the given id.
   * Performs the transition from <code>ACTIVE</code> to state <code>TERMINATED</code>
   * if the case execution belongs to a case model or a task or a stage.
   * Performs the transition from <code>AVAILABLE</code> to state <code>TERMINATED</code> if the case
   * execution belongs to a milestone.</p>
   *
   * @param caseExecutionId the id of the case execution to be terminated
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the
   *      given case execution id
   * @throws NotAllowedException when the transition is not allowed to be done
   * @throws ProcessEngineException when an internal exception happens during the execution
   *     of the command.
   */
  void terminateCaseExecution(String caseExecutionId);

  /**
  *
  * <p>Terminates the case execution identified by the given id.
  * Performs the transition from <code>ACTIVE</code> to state <code>TERMINATED</code>
  * if the case execution belongs to either a case model or a task or a stage.
  * Performs the transition from <code>AVAILABLE</code> to state <code>TERMINATED</code> if the case
  * execution belongs to a milestone.</p>
  *
  *
  * @param caseExecutionId the id of the case execution to terminate
  * @param variables variables to be set on the case execution
  *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the
   *      given case execution id
   * @throws NotAllowedException when the transition is not allowed to be done
   * @throws ProcessEngineException when an internal exception happens during the execution
   *     of the command.
   */
  void terminateCaseExecution(String caseExecutionId, Map<String, Object> variables);

  // fluent API ////////////////////////////////////////////////////////////

  /**
   * <p>Define a {@link CaseInstance} using a fluent builder.</p>
   *
   * <p>Starts a new case instance with the latest version of the corresponding case definition.</p>
   *
   * @param caseDefinitionKey the key of a case definition to create a new case instance of, cannot be null
   *
   * @return a {@link CaseInstanceBuilder fluent builder} for defining a new case instance
   */
  CaseInstanceBuilder withCaseDefinitionByKey(String caseDefinitionKey);

  /**
   * <p>Define a {@link CaseInstance} using a fluent builder.</p>
   *
   * <p>Starts a new case instance with the case definition version corresponding to the given id.</p>
   *
   * @param caseDefinitionId the id of a case definition to create a new case instance, cannot be null
   *
   * @return a {@link CaseInstanceBuilder fluent builder} for defining a new case instance
   */
  CaseInstanceBuilder withCaseDefinition(String caseDefinitionId);

  /**
   * <p>Define a command to be executed for a {@link CaseExecution} using a fluent builder.</p>
   *
   * @param caseExecutionId the id of a case execution to define a command for it
   *
   * @return a {@link CaseExecutionCommandBuilder fluent builder} for defining a command
   *         for a case execution
   */
  CaseExecutionCommandBuilder withCaseExecution(String caseExecutionId);

  // Query API ///////////////////////////////////////////////////////////

  /**
   * <p>Creates a new {@link CaseInstanceQuery} instance, that can be used
   * to query case instances.</p>
   */
  CaseInstanceQuery createCaseInstanceQuery();

  /**
   * <p>Creates a new {@link CaseExecutionQuery} instance,
   * that can be used to query the executions and case instances.</p>
   */
  CaseExecutionQuery createCaseExecutionQuery();

  // Variables //////////////////////////////////////////////////////////

  /**
   * <p>All variables visible from the given execution scope (including parent scopes).</p>
   *
   * <p>If you have many local variables and you only need a few, consider
   * using {@link #getVariables(String, Collection)} for better performance.</p>
   *
   * @param caseExecutionId the id of a case instance or case execution, cannot be null
   *
   * @return the variables or an empty map if no such variables are found
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  Map<String, Object> getVariables(String caseExecutionId);

  /**
   * <p>All variables visible from the given execution scope (including parent scopes).</p>
  *
  * <p>If you have many local variables and you only need a few, consider
  * using {@link #getVariables(String, Collection)} for better performance.</p>
  *
  * @param caseExecutionId the id of a case instance or case execution, cannot be null
  *
  * @return the variables or an empty map if no such variables are found
  *
  * @throws NotValidException when the given case execution id is null
  * @throws NotFoundException when no case execution is found for the given case execution id
  * @throws ProcessEngineException when an internal exception happens during the execution of the command
  */
 VariableMap getVariablesTyped(String caseExecutionId);

  /**
   * <p>All variables visible from the given execution scope (including parent scopes).</p>
  *
  * <p>If you have many local variables and you only need a few, consider
  * using {@link #getVariables(String, Collection)} for better performance.</p>
  *
  * @param caseExecutionId the id of a case instance or case execution, cannot be null
  * @param deserializeValues if false, the process engine will not attempt to deserialize {@link SerializableValue SerializableValues}.
  *
  * @return the variables or an empty map if no such variables are found
  *
  * @throws NotValidException when the given case execution id is null
  * @throws NotFoundException when no case execution is found for the given case execution id
  * @throws ProcessEngineException when an internal exception happens during the execution of the command
  */
 VariableMap getVariablesTyped(String caseExecutionId, boolean deserializeValues);

  /**
   * <p>All variable values that are defined in the case execution scope, without
   * taking outer scopes into account.</p>
   *
   * <p>If you have many local variables and you only need a few, consider
   * using {@link #getVariablesLocal(String, Collection)} for better performance.</p>
   *
   * @param caseExecutionId the id of a case execution, cannot be null
   *
   * @return the variables or an empty map if no such variables are found
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  Map<String, Object> getVariablesLocal(String caseExecutionId);

  /**
   * <p>All variable values that are defined in the case execution scope, without
   * taking outer scopes into account.</p>
   *
   * <p>If you have many local variables and you only need a few, consider
   * using {@link #getVariablesLocal(String, Collection)} for better performance.</p>
   *
   * @param caseExecutionId the id of a case execution, cannot be null
   *
   * @return the variables or an empty map if no such variables are found
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  VariableMap getVariablesLocalTyped(String caseExecutionId);

  /**
   * <p>All variable values that are defined in the case execution scope, without
   * taking outer scopes into account.</p>
   *
   * <p>If you have many local variables and you only need a few, consider
   * using {@link #getVariablesLocal(String, Collection)} for better performance.</p>
   *
   * @param caseExecutionId the id of a case execution, cannot be null
   * @param deserializeValues if false, the process engine will not attempt to deserialize {@link SerializableValue SerializableValues}.
   *
   * @return the variables or an empty map if no such variables are found
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  VariableMap getVariablesLocalTyped(String caseExecutionId, boolean deserializeValues);

  /**
   * <p>The variable values for all given variableNames, takes all variables
   * into account which are visible from the given case execution scope
   * (including parent scopes).</p>
   *
   * @param caseExecutionId the id of a case instance or case execution, cannot be null
   * @param variableNames the collection of variable names that should be retrieved
   *
   * @return the variables or an empty map if no such variables are found
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  Map<String, Object> getVariables(String caseExecutionId, Collection<String> variableNames);

  /**
   * <p>The variable values for all given variableNames, takes all variables
   * into account which are visible from the given case execution scope
   * (including parent scopes).</p>
   *
   * @param caseExecutionId the id of a case instance or case execution, cannot be null
   * @param variableNames the collection of variable names that should be retrieved
   * @param deserializeValues if false, {@link SerializableValue SerializableValues} will not be deserialized
   *
   * @return the variables or an empty map if no such variables are found
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  VariableMap getVariablesTyped(String caseExecutionId, Collection<String> variableNames, boolean deserializeValues);

  /**
   * <p>The variable values for the given variableNames only taking the given case
   * execution scope into account, not looking in outer scopes.</p>
   *
   * @param caseExecutionId the id of a case execution, cannot be null
   * @param variableNames the collection of variable names that should be retrieved
   *
   * @return the variables or an empty map if no such variables are found
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  Map<String,Object> getVariablesLocal(String caseExecutionId, Collection<String> variableNames);

  /**
   * <p>The variable values for the given variableNames only taking the given case
   * execution scope into account, not looking in outer scopes.</p>
   *
   * @param caseExecutionId the id of a case execution, cannot be null
   * @param variableNames the collection of variable names that should be retrieved
   * @param deserializeValues if false, the process engine will not attempt to deserialize {@link SerializableValue SerializableValues}.
   *
   * @return the variables or an empty map if no such variables are found
   *
   * @throws NotValidException when the given case execution id is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  VariableMap getVariablesLocalTyped(String caseExecutionId, Collection<String> variableNames, boolean deserializeValues);

  /**
   * <p>Searching for the variable is done in all scopes that are visible
   * to the given case execution (including parent scopes).</p>
   *
   * <p>Returns null when no variable value is found with the given name or
   * when the value is set to null.</p>
   *
   * @param caseExecutionId the id of a case instance or case execution, cannot be null
   * @param variableName the name of a variable, cannot be null
   *
   * @return the variable value or null if the variable is undefined or the value of the variable is null
   *
   * @throws NotValidException when the given case execution id or variable name is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  Object getVariable(String caseExecutionId, String variableName);

  /**
   * <p>Searching for the variable is done in all scopes that are visible
   * to the given case execution (including parent scopes).</p>
   *
   * <p>Returns null when no variable value is found with the given name or
   * when the value is set to null.</p>
   *
   * @param caseExecutionId the id of a case instance or case execution, cannot be null
   * @param variableName the name of a variable, cannot be null
   *
   * @return the variable value or null if the variable is undefined or the value of the variable is null
   *
   * @throws NotValidException when the given case execution id or variable name is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  <T extends TypedValue> T getVariableTyped(String caseExecutionId, String variableName);

  /**
   * <p>Searching for the variable is done in all scopes that are visible
   * to the given case execution (including parent scopes).</p>
   *
   * <p>Returns null when no variable value is found with the given name or
   * when the value is set to null.</p>
   *
   * @param caseExecutionId the id of a case instance or case execution, cannot be null
   * @param variableName the name of a variable, cannot be null
   * @param deserializeValue if false, {@link SerializableValue SerializableValues} will not be deserialized
   *
   * @return the variable value or null if the variable is undefined or the value of the variable is null
   *
   * @throws NotValidException when the given case execution id or variable name is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  <T extends TypedValue> T getVariableTyped(String caseExecutionId, String variableName, boolean deserializeValue);

  /**
   * <p>The variable value for an case execution. Returns the value when the variable is set
   * for the case execution (and not searching parent scopes).</p>
   *
   * <p>Returns null when no variable value is found with the given name or when the value is
   * set to null.</p>
   *
   * @param caseExecutionId the id of a case instance or case execution, cannot be null
   * @param variableName the name of a variable, cannot be null
   *
   * @return the variable value or null if the variable is undefined or the value of the variable is null
   *
   * @throws NotValidException when the given case execution id or variable name is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  Object getVariableLocal(String caseExecutionId, String variableName);

  /**
   * <p>The variable value for an case execution. Returns the value when the variable is set
   * for the case execution (and not searching parent scopes).</p>
   *
   * <p>Returns null when no variable value is found with the given name or when the value is
   * set to null.</p>
   *
   * @param caseExecutionId the id of a case instance or case execution, cannot be null
   * @param variableName the name of a variable, cannot be null
   *
   * @return the variable value or null if the variable is undefined or the value of the variable is null
   *
   * @throws NotValidException when the given case execution id or variable name is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  <T extends TypedValue> T getVariableLocalTyped(String caseExecutionId, String variableName);

  /**
   * <p>The variable value for an case execution. Returns the value when the variable is set
   * for the case execution (and not searching parent scopes).</p>
   *
   * <p>Returns null when no variable value is found with the given name or when the value is
   * set to null.</p>
   *
   * @param caseExecutionId the id of a case instance or case execution, cannot be null
   * @param variableName the name of a variable, cannot be null
   * @param deserializeValue if false, {@link SerializableValue SerializableValues} will not be deserialized
   *
   * @return the variable value or null if the variable is undefined or the value of the variable is null
   *
   * @throws NotValidException when the given case execution id or variable name is null
   * @throws NotFoundException when no case execution is found for the given case execution id
   * @throws ProcessEngineException when an internal exception happens during the execution of the command
   */
  <T extends TypedValue> T getVariableLocalTyped(String caseExecutionId, String variableName, boolean deserializeValue);

  /**
   * <p>Pass a map of variables to the case execution. If the variables do not already
   * exist, they are created in the case instance (which is the root execution).
   * Otherwise existing variables are updated.</p>
   *
   * @param caseExecutionId the case execution to set the variables for
   * @param variables the map of variables
   */
  void setVariables(String caseExecutionId, Map<String, Object> variables);

  /**
   * <p>Pass a map of variables to the case execution (not considering parent scopes).</p>
   *
   * @param caseExecutionId the case execution to set the variables for
   * @param variables the map of variables
   */
  void setVariablesLocal(String caseExecutionId, Map<String, Object> variables);

  /**
   * <p>Pass a variable to the case execution. If the variable does not already
   * exist, it is created in the case instance (which is the root execution).
   * Otherwise, the existing variable is updated.</p>
   *
   * @param caseExecutionId the case execution to set the variable for
   * @param variableName the name of the variable to set
   * @param variableValue the value of the variable to set
   *
   * @throws NotValidException when the given variable name is null
   */
  void setVariable(String caseExecutionId, String variableName, Object variableValue);

  /**
   * <p>Pass a local variable to the case execution (not considering parent scopes).</p>
   *
   * @param caseExecutionId the case execution to set the variable for
   * @param variableName the name of the variable to set
   * @param variableValue the value of the variable to set
   *
   * @throws NotValidException when the given variable name is null
   */
  void setVariableLocal(String caseExecutionId, String variableName, Object variableValue);

  /**
   * <p>Pass a collection of names identifying variables to be removed from a
   * case execution.</p>
   *
   * @param caseExecutionId the case execution to remove the variables from
   * @param variableNames a collection of names of variables to remove
   */
  void removeVariables(String caseExecutionId, Collection<String> variableNames);

  /**
   * <p>Pass a collection of names identifying local variables to be removed from a
   * case execution (not considering parent scopes).</p>
   *
   * @param caseExecutionId the case execution to remove the variables from
   * @param variableNames a collection of names of variables to remove
   */
  void removeVariablesLocal(String caseExecutionId, Collection<String> variableNames);

  /**
   * <p>Pass a name of a variable to be removed from a case execution.</p>
   *
   * @param caseExecutionId the case execution to remove the variable from
   * @param variableName the name of the variable to remove
   *
   * @throws NotValidException when the given variable name is null
   */
  void removeVariable(String caseExecutionId, String variableName);

  /**
   * <p>Pass a variable name of a local variable to be removed from a case execution
   * (not considering parent scopes).</p>
   *
   * @param caseExecutionId the case execution to remove the variable from
   * @param variableName the name of a variable to remove
   *
   * @throws NotValidException when the given variable name is null
   */
  void removeVariableLocal(String caseExecutionId, String variableName);
}
