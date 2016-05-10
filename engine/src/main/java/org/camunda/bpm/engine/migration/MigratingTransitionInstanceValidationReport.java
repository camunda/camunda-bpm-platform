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

import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * Collects all failures for a migrating transition instance.
 */
public interface MigratingTransitionInstanceValidationReport {

  /**
   * @return the id of the source scope of the migrating transition instance
   */
  String getSourceScopeId();

  /**
   * @return the transition instance id of this report
   */
  String getTransitionInstanceId();

  /**
   * @return the migration instruction that cannot be applied
   */
  MigrationInstruction getMigrationInstruction();

  /**
   * @return true if the reports contains failures, false otherwise
   */
  boolean hasFailures();

  /**
   * @return the list of failures
   */
  List<String> getFailures();

}
