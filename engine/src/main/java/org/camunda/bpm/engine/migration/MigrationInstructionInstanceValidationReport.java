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
package org.camunda.bpm.engine.migration;

import java.util.List;

/**
 * @author Thorben Lindhauer
 *
 */
public interface MigrationInstructionInstanceValidationReport {

  /**
   * @return the id of the process instance that cannot be migrated
   */
  String getProcessInstanceId();

  /**
   * @return a list of detailed failure reports
   */
  List<MigrationInstructionInstanceValidationFailure> getValidationFailures();

}
