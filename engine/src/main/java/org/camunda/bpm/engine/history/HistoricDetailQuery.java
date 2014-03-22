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
import org.camunda.bpm.engine.runtime.Execution;


/**
 * Programmatic querying for {@link HistoricDetail}s.
 *
 * @author Tom Baeyens
 */
public interface HistoricDetailQuery extends Query<HistoricDetailQuery, HistoricDetail> {

  /**
   * Only select the historic detail with the given id.
   *
   * @param id the historic detail to select
   * @return the query builder
   */
  HistoricDetailQuery detailId(String id);

  /** Only select historic variable updates with the given process instance.
   * {@link ProcessInstance) ids and {@link HistoricProcessInstance} ids match. */
  HistoricDetailQuery processInstanceId(String processInstanceId);

  /** Only select historic variable updates with the given execution.
   * Note that {@link Execution} ids are not stored in the history as first class citizen,
   * only process instances are.*/
  HistoricDetailQuery executionId(String executionId);

  /** Only select historic variable updates associated to the given {@link HistoricActivityInstance activity instance}.
   * @deprecated since 5.2, use {@link #activityInstanceId(String)} instead */
  HistoricDetailQuery activityId(String activityId);

  /** Only select historic variable updates associated to the given {@link HistoricActivityInstance activity instance}. */
  HistoricDetailQuery activityInstanceId(String activityInstanceId);

  /** Only select historic variable updates associated to the given {@link HistoricTaskInstance historic task instance}. */
  HistoricDetailQuery taskId(String taskId);

  /** Only select historic variable updates associated to the given {@link HistoricVariableInstance historic variable instance}. */
  HistoricDetailQuery variableInstanceId(String variableInstanceId);

  /** Only select {@link HistoricFormProperty}s. */
  @Deprecated
  HistoricDetailQuery formProperties();

  /** Only select {@link HistoricFormField}s. */
  HistoricDetailQuery formFields();

  /** Only select {@link HistoricVariableUpdate}s. */
  HistoricDetailQuery variableUpdates();

  /**
   * Disable fetching of byte array values. By default, the query will fetch the value of a byte array.
   * By calling this method you can prevent the values of (potentially large) blob data chunks to be fetched.
   *
   * @return the query builder
   */
  HistoricDetailQuery disableBinaryFetching();

  /** Exclude all task-related {@link HistoricDetail}s, so only items which have no
   * task-id set will be selected. When used together with {@link #taskId(String)}, this
   * call is ignored task details are NOT excluded.
   */
  HistoricDetailQuery excludeTaskDetails();

  HistoricDetailQuery orderByProcessInstanceId();

  HistoricDetailQuery orderByVariableName();

  HistoricDetailQuery orderByFormPropertyId();

  HistoricDetailQuery orderByVariableType();

  HistoricDetailQuery orderByVariableRevision();

  HistoricDetailQuery orderByTime();
}
