/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.history;

import java.util.Date;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.runtime.CaseInstance;

/**
 * A single execution of a case definition that is stored permanently.
 *
 * @author Sebastian Menski
 */
public interface HistoricCaseInstance {

  /** The case instance id (== as the id of the runtime {@link CaseInstance}). */
  String getId();

  /** The user provided unique reference to this process instance. */
  String getBusinessKey();

  /** The case definition reference. */
  String getCaseDefinitionId();

  /** The case definition key */
  String getCaseDefinitionKey();

  /** The case definition name */
  String getCaseDefinitionName();

  /** The time the case was created. */
  Date getCreateTime();

  /** The time the case was closed. */
  Date getCloseTime();

  /** The difference between {@link #getCloseTime()} and {@link #getCreateTime()}. */
  Long getDurationInMillis();

  /** The authenticated user that created this case instance.
   * @see IdentityService#setAuthenticatedUserId(String) */
  String getCreateUserId();

  /** The case instance id of a potential super case instance or null if no super case instance exists. */
  String getSuperCaseInstanceId();

  /** The process instance id of a potential super process instance or null if no super process instance exists. */
  String getSuperProcessInstanceId();

  /**
   * The id of the tenant this historic case instance belongs to. Can be <code>null</code>
   * if the historic case instance belongs to no single tenant.
   */
  String getTenantId();

  /** Check if the case is active. */
  boolean isActive();

  /** Check if the case is completed. */
  boolean isCompleted();

  /** Check if the case is terminated. */
  boolean isTerminated();

  /** Check if the case is closed. */
  boolean isClosed();

}
