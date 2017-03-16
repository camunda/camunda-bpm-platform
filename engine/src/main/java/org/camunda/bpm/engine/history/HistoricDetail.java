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

import java.util.Date;



/** Base class for all kinds of information that is related to
 * either a {@link HistoricProcessInstance} or a {@link HistoricActivityInstance}.
 *
 * @author Tom Baeyens
 */
public interface HistoricDetail {

  /** The unique DB id for this historic detail */
  String getId();

  /** The process definition key reference. */
  String getProcessDefinitionKey();

  /** The process definition reference. */
  String getProcessDefinitionId();

  /** The process instance reference. */
  String getProcessInstanceId();

  /** The activity reference in case this detail is related to an activity instance. */
  String getActivityInstanceId();

  /** The identifier for the path of execution. */
  String getExecutionId();

  /** The case definition key reference. */
  String getCaseDefinitionKey();

  /** The case definition reference. */
  String getCaseDefinitionId();

  /** The case instance reference. */
  String getCaseInstanceId();

  /** The case execution reference. */
  String getCaseExecutionId();

  /** The identifier for the task. */
  String getTaskId();

  /** The time when this detail occurred */
  Date getTime();

  /**
   * The id of the tenant this historic detail belongs to. Can be <code>null</code>
   * if the historic detail belongs to no single tenant.
   */
  String getTenantId();

  /**
   * The id of operation. Helps to link records in different historic tables.
   * References operationId of user operation log entry.
   */
  String getUserOperationId();
}
