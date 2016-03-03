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

import org.camunda.bpm.engine.batch.Batch;

/**
 * Builder to execute a migration.
 */
public interface MigrationPlanExecutionBuilder {

  /**
   * @param processInstanceIds the process instance ids to migrate
   */
  MigrationPlanExecutionBuilder processInstanceIds(List<String> processInstanceIds);

  /**
   * Execute the migration synchronously.
   *
   * @throws MigratingProcessInstanceValidationException if the migration plan contains
   *  instructions that are not applicable to any of the process instances
   */
  void execute();

  /**
   * Execute the migration asynchronously as batch. The returned batch
   * can be used to track the progress of the migration.
   *
   * @return the batch which executes the migration asynchronously.
   */
  Batch executeAsync();

}
