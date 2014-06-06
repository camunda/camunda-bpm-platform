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

package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.query.Query;


/**
 * Programmatic querying for {@link HistoricVariableInstance}s.
 *
 * @author Christian Lipphardt (camunda)
 */
public interface HistoricVariableInstanceQuery extends Query<HistoricVariableInstanceQuery, HistoricVariableInstance> {

  /** Only select the variable with the given Id
   * @param the id of the variable to select
   * @return the query object */
  HistoricVariableInstanceQuery variableId(String id);

  /** Only select historic process variables with the given process instance. */
  HistoricVariableInstanceQuery processInstanceId(String processInstanceId);

  /** Only select historic process variables with the given variable name. */
  HistoricVariableInstanceQuery variableName(String variableName);

  /** Only select historic process variables where the given variable name is like. */
  HistoricVariableInstanceQuery variableNameLike(String variableNameLike);

  /**
   * only select historic process variables with the given name and value
   */
  HistoricVariableInstanceQuery variableValueEquals(String variableName, Object variableValue);

  HistoricVariableInstanceQuery orderByProcessInstanceId();

  HistoricVariableInstanceQuery orderByVariableName();

  /** Only select historic variable instances which have one of the task ids. **/
  HistoricVariableInstanceQuery taskIdIn(String... taskIds);

  /** Only select historic variable instances which have one of the executions ids. **/
  HistoricVariableInstanceQuery executionIdIn(String... executionIds);

  /** Only select historic variable instances which have one of the activity instance ids. **/
  HistoricVariableInstanceQuery activityInstanceIdIn(String... activityInstanceIds);

  /**
   * Disable fetching of byte array values. By default, the query will fetch the value of a byte array.
   * By calling this method you can prevent the values of (potentially large) blob data chunks to be fetched.
   *
   * @return the query builder
   */
  HistoricVariableInstanceQuery disableBinaryFetching();

}
