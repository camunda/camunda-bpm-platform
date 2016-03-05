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
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.ReferenceWalker;

public class HasNoEventSubProcessParentActivityValidator implements MigrationActivityValidator {

  public static HasNoEventSubProcessParentActivityValidator INSTANCE = new HasNoEventSubProcessParentActivityValidator();

  public boolean valid(ActivityImpl activity) {
    return activity != null && !hasEventSubProcessParent(activity);
  }

  protected boolean hasEventSubProcessParent(ActivityImpl activity) {
    FlowScopeWalker flowScopeWalker = new FlowScopeWalker(activity);
    flowScopeWalker.walkUntil(new ReferenceWalker.WalkCondition<ScopeImpl>() {
      public boolean isFulfilled(ScopeImpl element) {
        return isProcessDefinition(element) || isEventSubProcess(element);
      }
    });

    return isEventSubProcess(flowScopeWalker.getCurrentElement());
  }

  protected boolean isEventSubProcess(ScopeImpl scope) {
    return !isProcessDefinition(scope) && scope.getActivityBehavior() instanceof EventSubProcessActivityBehavior;
  }

  protected boolean isProcessDefinition(ScopeImpl scope) {
    return scope == scope.getProcessDefinition();
  }

}
