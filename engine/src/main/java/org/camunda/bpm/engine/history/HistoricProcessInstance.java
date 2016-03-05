/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import org.camunda.bpm.engine.runtime.ProcessInstance;

/** A single execution of a whole process definition that is stored permanently.
 *
 * @author Christian Stettler
 */
public interface HistoricProcessInstance {

  /** The process instance id (== as the id for the runtime {@link ProcessInstance process instance}). */
  String getId();

  /** The user provided unique reference to this process instance. */
  String getBusinessKey();

  /** The process definition key reference. */
  String getProcessDefinitionKey();

  /** The process definition reference. */
  String getProcessDefinitionId();

  /** The time the process was started. */
  Date getStartTime();

  /** The time the process was ended. */
  Date getEndTime();

  /** The difference between {@link #getEndTime()} and {@link #getStartTime()} . */
  Long getDurationInMillis();

  /** Reference to the activity in which this process instance ended.
   *  Note that a process instance can have multiple end events, in this case it might not be deterministic
   *  which activity id will be referenced here. Use a {@link HistoricActivityInstanceQuery} instead to query
   *  for end events of the process instance (use the activityTYpe attribute)
   *  */
  @Deprecated
  String getEndActivityId();

  /** The authenticated user that started this process instance.
   * @see IdentityService#setAuthenticatedUserId(String) */
  String getStartUserId();

  /** The start activity. */
  String getStartActivityId();

  /** Obtains the reason for the process instance's deletion. */
  String getDeleteReason();

  /**
   * The process instance id of a potential super process instance or null if no super process instance exists
   */
  String getSuperProcessInstanceId();

  /**
   * The case instance id of a potential super case instance or null if no super case instance exists
   */
  String getSuperCaseInstanceId();

  /**
   * The case instance id of a potential super case instance or null if no super case instance exists
   */
  String getCaseInstanceId();

  /**
   * The id of the tenant this historic process instance belongs to. Can be <code>null</code>
   * if the historic process instance belongs to no single tenant.
   */
  String getTenantId();

}
