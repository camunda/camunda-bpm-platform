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

/**
 * Collects general failures  and the migrating activity instance validation
 * reports for a migrating process instance.
 *
 * A general failures is that the state of the process instance doesn't allow
 * the migration independent from an specific activity instance. For example
 * if non migrated jobs exist.
 */
public interface MigratingProcessInstanceValidationReport {

  /**
   * @return the id of the process instance that the migration plan is applied to
   */
  String getProcessInstanceId();

  /**
   * @return the list of general failures of the migrating process instance
   */
  List<String> getFailures();

  /**
   * @return true if general failures or activity instance validation reports exist, false otherwise
   */
  boolean hasFailures();

  /**
   * @return the list of activity instance validation reports
   */
  List<MigratingActivityInstanceValidationReport> getActivityInstanceReports();

  /**
   * @return the list of transition instance validation reports
   */
  List<MigratingTransitionInstanceValidationReport> getTransitionInstanceReports();

}
