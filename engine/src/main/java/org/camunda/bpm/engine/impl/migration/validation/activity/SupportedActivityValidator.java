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

package org.camunda.bpm.engine.impl.migration.validation.activity;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

public class SupportedActivityValidator implements MigrationActivityValidator {

  public static SupportedActivityValidator INSTANCE = new SupportedActivityValidator();

  public static List<Class<? extends ActivityBehavior>> SUPPORTED_ACTIVITY_BEHAVIORS = new ArrayList<Class<? extends ActivityBehavior>>();

  static {
    SUPPORTED_ACTIVITY_BEHAVIORS.add(SubProcessActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(UserTaskActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(BoundaryEventActivityBehavior.class);
  }

  public boolean valid(ActivityImpl activity) {
    return activity != null && SUPPORTED_ACTIVITY_BEHAVIORS.contains(activity.getActivityBehavior().getClass());
  }

}
