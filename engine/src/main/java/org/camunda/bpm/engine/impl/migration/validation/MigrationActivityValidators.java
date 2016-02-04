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

package org.camunda.bpm.engine.impl.migration.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.TreeWalker;

public class MigrationActivityValidators {

  // Validators

  public static final MigrationActivityValidator SUPPORTED_ACTIVITY = new AbstractMigrationActivityValidator() {
    @SuppressWarnings("unchecked")
    public final Set<Class<? extends ActivityBehavior>> SUPPORTED_ACTIVITY_BEHAVIORS = new HashSet<Class<? extends ActivityBehavior>>(
      Arrays.asList(SubProcessActivityBehavior.class, UserTaskActivityBehavior.class)
    );

    public boolean canBeMigrated(ActivityImpl activity, ProcessDefinitionImpl processDefinition) {
      return SUPPORTED_ACTIVITY_BEHAVIORS.contains(activity.getActivityBehavior().getClass());
    }
  };

  public static final MigrationActivityValidator NOT_MULTI_INSTANCE_CHILD = new AbstractMigrationActivityValidator() {
    public boolean canBeMigrated(ActivityImpl activity, ProcessDefinitionImpl processDefinition) {
      return !hasMultiInstanceParent(activity);
    }
  };

  public static final MigrationActivityValidator HAS_NO_BOUNDARY_EVENT = new AbstractMigrationActivityValidator() {
    public boolean canBeMigrated(ActivityImpl activity, ProcessDefinitionImpl processDefinition) {
      return !isScope(activity) || !hasBoundaryEvent(activity, processDefinition);
    }
  };


  // Helper

  protected static boolean hasMultiInstanceParent(ActivityImpl activity) {
    FlowScopeWalker flowScopeWalker = new FlowScopeWalker(activity);
    flowScopeWalker.walkUntil(new TreeWalker.WalkCondition<ScopeImpl>() {
      public boolean isFulfilled(ScopeImpl element) {
        return isProcessDefinition(element) || isMultiInstance(element);
      }
    });

    return isMultiInstance(flowScopeWalker.getCurrentElement());
  }

  protected static boolean hasBoundaryEvent(ScopeImpl scope, ProcessDefinitionImpl processDefinition) {
    ScopeImpl flowScope = scope.getFlowScope();
    for (ActivityImpl siblingActivity : flowScope.getActivities()) {
      if (scope.equals(siblingActivity.getEventScope())) {
        return true;
      }
    }
    return false;
  }

  protected static boolean isMultiInstance(ScopeImpl scope) {
    return !isProcessDefinition(scope) && scope.getActivityBehavior() instanceof MultiInstanceActivityBehavior;
  }

  protected static boolean isProcessDefinition(ScopeImpl scope) {
    return scope == scope.getProcessDefinition();
  }

  protected static boolean isScope(ActivityImpl activity) {
    return activity.isScope();
  }

}
