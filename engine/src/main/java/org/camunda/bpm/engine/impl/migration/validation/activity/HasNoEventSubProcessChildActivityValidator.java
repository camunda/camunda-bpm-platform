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

import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

public class HasNoEventSubProcessChildActivityValidator implements MigrationActivityValidator {

  public static HasNoEventSubProcessChildActivityValidator INSTANCE = new HasNoEventSubProcessChildActivityValidator();

  public boolean valid(ActivityImpl activity) {
    return activity != null && !processDefinitionHasEventSubProcessChild(activity) && !hasEventSubProcessChild(activity);
  }

  protected boolean hasEventSubProcessChild(ScopeImpl scope) {
    for (ActivityImpl childActivity : scope.getActivities()) {
      if (childActivity.getActivityBehavior() instanceof EventSubProcessActivityBehavior) {
        return true;
      }
    }
    return false;
  }

  protected boolean processDefinitionHasEventSubProcessChild(ActivityImpl activity) {
    return hasEventSubProcessChild(activity.getProcessDefinition());
  }

}
