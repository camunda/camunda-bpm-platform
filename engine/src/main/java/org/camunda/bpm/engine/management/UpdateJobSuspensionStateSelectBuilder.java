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

package org.camunda.bpm.engine.management;

/**
 * Fluent builder to update the suspension state of jobs.
 */
public interface UpdateJobSuspensionStateSelectBuilder {

  /**
   * Selects the job with the given id.
   *
   * @param jobId
   *          id of the job
   * @return the builder
   */
  UpdateJobSuspensionStateBuilder byJobId(String jobId);

  /**
   * Selects the jobs of the job definition with the given id.
   *
   * @param jobDefinitionId
   *          id of the job definition
   * @return the builder
   */
  UpdateJobSuspensionStateBuilder byJobDefinitionId(String jobDefinitionId);

  /**
   * Selects the jobs of the process instance with the given id.
   *
   * @param processInstanceId
   *          id of the process instance
   * @return the builder
   */
  UpdateJobSuspensionStateBuilder byProcessInstanceId(String processInstanceId);

  /**
   * Selects the jobs of the process definition with the given id.
   *
   * @param processDefinitionId
   *          id of the process definition
   * @return the builder
   */
  UpdateJobSuspensionStateBuilder byProcessDefinitionId(String processDefinitionId);

  /**
   * Selects the jobs of the process definitions with the given key.
   *
   * @param processDefinitionKey
   *          key of the process definition
   * @return the builder
   */
  UpdateJobSuspensionStateTenantBuilder byProcessDefinitionKey(String processDefinitionKey);

}
