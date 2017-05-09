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

package org.camunda.bpm.engine.migration;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

/**
 * Builder to execute a migration.
 */
public interface MigrationPlanExecutionBuilder {

  /**
   * @param processInstanceIds the process instance ids to migrate.
   */
  MigrationPlanExecutionBuilder processInstanceIds(List<String> processInstanceIds);

  /**
   * @param processInstanceIds the process instance ids to migrate.
   */
  MigrationPlanExecutionBuilder processInstanceIds(String... processInstanceIds);

  /**
   * @param processInstanceQuery a query which selects the process instances to migrate.
   *   Query results are restricted to process instances for which the user has {@link Permissions#READ} permission.
   */
  MigrationPlanExecutionBuilder processInstanceQuery(ProcessInstanceQuery processInstanceQuery);

  /**
   * Skips custom execution listeners when creating/removing activity instances during migration
   */
  MigrationPlanExecutionBuilder skipCustomListeners();

  /**
   * Skips io mappings when creating/removing activity instances during migration
   */
  MigrationPlanExecutionBuilder skipIoMappings();

  /**
   * Execute the migration synchronously.
   *
   * @throws MigratingProcessInstanceValidationException if the migration plan contains
   *  instructions that are not applicable to any of the process instances
   * @throws AuthorizationException
   *   if the user has not all of the following permissions
   *   <ul>
   *      <li>if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE} or</li>
   *      <li>no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  void execute();

  /**
   * Execute the migration asynchronously as batch. The returned batch
   * can be used to track the progress of the migration.
   *
   * @return the batch which executes the migration asynchronously.
   *
   * @throws AuthorizationException
   *   if the user has not all of the following permissions
   *   <ul>
   *     <li>{@link Permissions#MIGRATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION} for source and target</li>
   *     <li>{@link Permissions#CREATE} permission on {@link Resources#BATCH}</li>
   *   </ul>
   */
  Batch executeAsync();
}
