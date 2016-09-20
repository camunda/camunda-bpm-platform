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

/**
 * Represents one execution of an activity and it's stored permanent for statistics, audit and other business intelligence purposes.
 *
 * @author Christian Stettler
 */
public interface HistoricActivityInstance {

  /** The unique identifier of this historic activity instance. */
  String getId();

  /** return the id of the parent activity instance */
  String getParentActivityInstanceId();

  /** The unique identifier of the activity in the process */
  String getActivityId();

  /** The display name for the activity */
  String getActivityName();

  /**
   * The activity type of the activity.
   * Typically the activity type correspond to the XML tag used in the BPMN 2.0 process definition file.
   *
   * All activity types are available in {@link org.camunda.bpm.engine.ActivityTypes}
   *
   * @see org.camunda.bpm.engine.ActivityTypes
   */
  String getActivityType();

  /** Process definition key reference */
  String getProcessDefinitionKey();

  /** Process definition reference */
  String getProcessDefinitionId();

  /** Process instance reference */
  String getProcessInstanceId();

  /** Execution reference */
  String getExecutionId();

  /** The corresponding task in case of task activity */
  String getTaskId();

  /** The called process instance in case of call activity */
  String getCalledProcessInstanceId();

  /** The called case instance in case of (case) call activity */
  String getCalledCaseInstanceId();

  /** Assignee in case of user task activity */
  String getAssignee();

  /** Time when the activity instance started */
  Date getStartTime();

  /** Time when the activity instance ended */
  Date getEndTime();

  /** Difference between {@link #getEndTime()} and {@link #getStartTime()}.  */
  Long getDurationInMillis();

  /** Did this activity instance complete a BPMN 2.0 scope */
  boolean isCompleteScope();

  /** Was this activity instance canceled */
  boolean isCanceled();

  /**
   * The id of the tenant this historic activity instance belongs to. Can be <code>null</code>
   * if the historic activity instance belongs to no single tenant.
   */
  String getTenantId();

}
