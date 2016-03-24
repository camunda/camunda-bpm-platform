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

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;

/**
 * Fluent builder to update the suspension state of jobs.
 */
public interface UpdateJobSuspensionStateBuilder {

  /**
   * Activates the provided jobs.
   *
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#UPDATE} permission on
   *           {@link Resources#PROCESS_INSTANCE} or no
   *           {@link Permissions#UPDATE_INSTANCE} permission on
   *           {@link Resources#PROCESS_DEFINITION}.
   */
  void activate();

  /**
   * Suspends the provided jobs. If a job is in state suspended, it will not be
   * executed by the job executor.
   *
   * @throws AuthorizationException
   *           if the user has no {@link Permissions#UPDATE} permission on
   *           {@link Resources#PROCESS_INSTANCE} or no
   *           {@link Permissions#UPDATE_INSTANCE} permission on
   *           {@link Resources#PROCESS_DEFINITION}.
   */
  void suspend();

}
