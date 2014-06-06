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

package org.camunda.bpm.engine.delegate;

/**
 * Execution used in {@link JavaDelegate}s and {@link ExecutionListener}s.
 *
 * @author Tom Baeyens
 */
public interface DelegateExecution extends BaseDelegateExecution, BpmnModelExecutionContext, ProcessEngineServicesAware {

  /** Reference to the overall process instance */
  String getProcessInstanceId();

  /**
   * The business key for the process instance this execution is associated with.
   */
  String getProcessBusinessKey();

  /**
   * The process definition key for the process instance this execution is associated with.
   */
  String getProcessDefinitionId();

  /**
   * Gets the id of the parent of this execution. If null, the execution represents a process-instance.
   */
  String getParentId();

  /**
   * Gets the id of the current activity.
   */
  String getCurrentActivityId();

  /**
   * Gets the name of the current activity.
   */
  String getCurrentActivityName();

  /** return the Id of the activity instance currently executed by this execution */
  String getActivityInstanceId();

  /** return the Id of the parent activity instance currently executed by this execution */
  String getParentActivityInstanceId();

  /** return the Id of the current transition */
  String getCurrentTransitionId();

}
