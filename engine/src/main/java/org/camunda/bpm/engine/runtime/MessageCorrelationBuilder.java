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
package org.camunda.bpm.engine.runtime;

import java.util.Map;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;

/**
 * <p>A fluent builder for defining message correlation</p>
 *
 * @author Daniel Meyer
 *
 */
public interface MessageCorrelationBuilder {

  /**
   * <p>Correlate the message such that the process instance has a
   * business with the given name and value.</p>
   *
   * @param businessKey the businessKey to correlate on.
   * @return the builder
   */
  MessageCorrelationBuilder processInstanceBusinessKey(String businessKey);

  /**
   * <p>Correlate the message such that the process instance has a
   * variable with the given name and value.</p>
   *
   * @param variableName the name of the process instance variable to correlate on.
   * @param variableValue the value of the process instance variable to correlate on.
   * @return the builder
   */
  MessageCorrelationBuilder processInstanceVariableEquals(String variableName, Object variableValue);

  /**
   * <p>Correlate the message such that a process instance with the given id is selected.</p>
   *
   * @param id the id of the process instance to correlate on.
   * @return the builder
   */
  MessageCorrelationBuilder processInstanceId(String id);

  /**
   * <p>Pass a variable to the execution waiting on the message. Use this method for passing the
   * message's payload.</p>
   *
   * <p>Invoking this method multiple times allows passing multiple variables.</p>
   *
   * @param variableName the name of the variable to set
   * @param variableValue the value of the variable to set
   * @return the builder
   */
  MessageCorrelationBuilder setVariable(String variableName, Object variableValue);

  /**
   * <p>Pass a map of variables to the execution waiting on the message. Use this method
   * for passing the message's payload</p>
   *
   * @param variables the map of variables
   * @return the builder
   */
  MessageCorrelationBuilder setVariables(Map<String, Object> variables);

  /**
   * Executes the message correlation.
   *
   * <p>This will result in either:
   * <ul>
   * <li>Exactly one waiting execution is notified to continue. The notification is performed synchronously.</li>
   * <li>Exactly one Process Instance is started in case the message name matches a message start event of a
   *     process. The instantiation is performed synchronously.</li>
   * <li>MismatchingMessageCorrelationException is thrown. This means that either too many executions match the
   *     correlation or that no execution matches the correlation.</li>
   * </ul>
   * </p>
   *
   * @throws MismatchingMessageCorrelationException
   *          if none or more than one execution or process definition is matched by the correlation
   * @throws AuthorizationException
   *          if one execution is matched and the user has no {@link Permissions#UPDATE} permission on
   *          {@link Resources#PROCESS_INSTANCE} or no {@link Permissions#UPDATE_INSTANCE} permission on
   *          {@link Resources#PROCESS_DEFINITION}.
   *          if one process definition is matched and the user has no {@link Permissions#CREATE} permission on
   *          {@link Resources#PROCESS_INSTANCE} and no {@link Permissions#CREATE_INSTANCE} permission on
   *          {@link Resources#PROCESS_DEFINITION}.
   */
  void correlate();

  /**
   * <p>
   *   Behaves like {@link #correlate()}, however uses pessimistic locking for correlating a waiting execution, meaning
   *   that two threads correlating a message to the same execution in parallel do not end up continuing the
   *   process in parallel until the next wait state is reached
   * </p>
   * <p>
   *   <strong>CAUTION:</strong> Wherever there are pessimistic locks, there is a potential for deadlocks to occur.
   *   This can either happen when multiple messages are correlated in parallel, but also with other
   *   race conditions such as a message boundary event on a user task. The process engine is not able to detect such a potential.
   *   In consequence, the user of this API should investigate this potential in his/her use case and implement
   *   countermeasures if needed.
   * </p>
   * <p>
   *   A less error-prone alternative to this method is to set appropriate async boundaries in the process model
   *   such that parallel message correlation is solved by optimistic locking.
   * </p>
   */
  void correlateExclusively();


  /**
   * Executes the message correlation for multiple messages.
   *
   * <p>This will result in any number of the following:
   * <ul>
   * <li>Any number of waiting executions are notified to continue. The notification is performed synchronously.</li>
   * <li>Zero or one Process Instance is started in case the message name matches a message start event of a
   *     process. The instantiation is performed synchronously.</li>
   * </ul>
   * </p>
   *
   * @throws AuthorizationException
   *          if at least one execution is matched and the user has no {@link Permissions#UPDATE} permission on
   *          {@link Resources#PROCESS_INSTANCE} or no {@link Permissions#UPDATE_INSTANCE} permission on
   *          {@link Resources#PROCESS_DEFINITION}.
   *          if one process definition is matched and the user has no {@link Permissions#CREATE} permission on
   *          {@link Resources#PROCESS_INSTANCE} and no {@link Permissions#CREATE_INSTANCE} permission on
   *          {@link Resources#PROCESS_DEFINITION}.
   *
   */
  void correlateAll();

}
