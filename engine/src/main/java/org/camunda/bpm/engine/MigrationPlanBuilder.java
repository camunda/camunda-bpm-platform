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
package org.camunda.bpm.engine;

import java.util.List;

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;

/**
 * @author Thorben Lindhauer
 *
 */
public interface MigrationPlanBuilder {

  /**
   * Automatically adds a set of instructions for activities that are <em>equivalent</em> in both
   * process definitions. By default, this is given if two activities are both user tasks, are on the same
   * level of sub process, and have the same id.
   */
  MigrationPlanBuilder mapEqualActivities();

  /**
   * Adds a migration instruction that maps activity instances of the source activity (of the source process definition)
   * to activity instances of the target activity (of the target process definition)
   */
  MigrationPlanBuilder mapActivities(String sourceActivityId, String targetActivityId);

  /**
   * @return a migration plan with all previously specified instructions
   *
   * @throws MigrationPlanValidationException if the migration plan contains instructions that are not valid
   */
  MigrationPlan build();


}
