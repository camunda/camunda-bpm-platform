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

package org.camunda.bpm.engine.impl.migration;

import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * Check if two activities are equal in the context
 * of the migration of two process definitions.
 *
 * It is required that the matcher for each activity of the source
 * process definition at most one activity of the target process
 * definition matches.
 *
 * For example the matching criterion should not be solely base on
 * the activity name which is a non unique attribute.
 */
public interface MigrationActivityMatcher {

  /**
   * Checks if an activity from the source process definition of
   * a migration matches an activity from the target process
   * definition.
   *
   * @param source the activity from the source process definition
   * @param target the activity from the target process definition
   * @return true if the source activity matches the target activity
   *         in the context of the migration, false otherwise
   */
  boolean matchActivities(ActivityImpl source, ActivityImpl target);

}
