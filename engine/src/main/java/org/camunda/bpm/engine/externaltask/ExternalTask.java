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
package org.camunda.bpm.engine.externaltask;

import java.util.Date;

/**
 * @author Thorben Lindhauer
 *
 */
public interface ExternalTask {

  /**
   * @return the id of the task
   */
  String getId();

  /**
   * @return the name of the topic the task belongs to
   */
  String getTopicName();

  /**
   * @return the id of the worker that has locked the task
   */
  String getWorkerId();

  /**
   * @return the absolute time at which the lock expires
   */
  Date getLockExpirationTime();

  /**
   * @return the id of the process instance the task exists in
   */
  String getProcessInstanceId();

  /**
   * @return the id of the execution that the task is assigned to
   */
  String getExecutionId();

  /**
   * @return the id of the activity for which the task is created
   */
  String getActivityId();

  /**
   * @return the id of the activity instance in which context the task exists
   */
  String getActivityInstanceId();

  /**
   * @return the id of the process definition the task's activity belongs to
   */
  String getProcessDefinitionId();

  /**
   * @return the key of the process definition the task's activity belongs to
   */
  String getProcessDefinitionKey();

}
