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
package org.camunda.bpm.client;

import java.util.Date;
import java.util.Map;

/**
 * <p>Represents a locked task</p>
 *
 * @author Tassilo Weidner
 */
public interface LockedTask {

  /**
   * @return the id of the activity that this external task belongs to
   */
  String getActivityId();

  /**
   * @return the id of the activity instance that the external task belongs to
   */
  String getActivityInstanceId();

  /**
   * @return the error message that was supplied when the last failure of this task was reported
   */
  String getErrorMessage();

  /**
   * @return the error details submitted with the latest reported failure executing this task
   */
  String getErrorDetails();

  /**
   * @return the id of the execution that the external task belongs to
   */
  String getExecutionId();

  /**
   * @return the id of the external task
   */
  String getId();

  /**
   * @return the date that the task's most recent lock expires or has expired
   */
  Date getLockExpirationTime();

  /**
   * @return the id of the process definition the external task is defined in
   */
  String getProcessDefinitionId();

  /**
   * @return the key of the process definition the external task is defined in
   */
  String getProcessDefinitionKey();

  /**
   * @return the id of the process instance the external task belongs to
   */
  String getProcessInstanceId();

  /**
   * @return the number of retries the task currently has left
   */
  Integer getRetries();

  /**
   * @return a flag indicating whether the external task is suspended or not
   */
  boolean isSuspended();

  /**
   * @return the id of the worker that possesses or possessed the most recent lock
   */
  String getWorkerId();

  /**
   * @return the topic name of the external task
   */
  String getTopicName();

  /**
   * @return the id of the tenant the external task belongs to
   */
  String getTenantId();

  /**
   * @return the priority of the external task
   */
  long getPriority();

  /**
   * @return a map of variables that contains an entry for each variable that
   * was specified at fetching time, if such a variable exists in the task's
   * ancestor execution hierarchy.
   */
  Map<String, Object> getVariables();

}

