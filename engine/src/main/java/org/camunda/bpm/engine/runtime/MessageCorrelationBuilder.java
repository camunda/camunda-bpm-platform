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

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;

import java.util.Map;

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
   * @throws MismatchingMessageCorrelationException if none or more than one execution or process definition is matched by the correlation
   */
  void correlate();

}
