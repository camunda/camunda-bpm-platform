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
package org.camunda.bpm.engine.impl.migration.validation;

import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.TreeWalker;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;

/**
 * @author Thorben Lindhauer
 *
 */
public class DefaultMigrationPlanValidator implements MigrationPlanValidator {

  public void validateMigrationPlan(ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition,
                                    MigrationPlan migrationPlan, MigrationPlanValidationReport validationReport) {

    for (MigrationInstruction instruction : migrationPlan.getInstructions()) {
      try {
        validateMigrationInstruction(sourceProcessDefinition, targetProcessDefinition, instruction);
      }
      catch (BadUserRequestException e) {
        validationReport.addValidationFailure(instruction, e.getMessage());
      }
    }

  }

  public void validateMigrationInstruction(ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition,
                                           MigrationInstruction instruction) {

    ensureOneToOneMapping(instruction);

    String sourceActivityId = instruction.getSourceActivityIds().get(0);
    String targetActivityId = instruction.getTargetActivityIds().get(0);
    ActivityImpl sourceActivity = sourceProcessDefinition.findActivity(sourceActivityId);
    ActivityImpl targetActivity = targetProcessDefinition.findActivity(targetActivityId);

    ensureMappedActivitiesExist(instruction, sourceActivity, targetActivity);
    ensureSupportedActivity(instruction, sourceActivity, targetActivity);
    ensureNotChildOfMultiInstance(instruction, sourceActivity, targetActivity);
  }

  protected void ensureOneToOneMapping(MigrationInstruction instruction) {
    List<String> sourceActivityIds = instruction.getSourceActivityIds();
    List<String> targetActivityIds = instruction.getTargetActivityIds();

    if (sourceActivityIds.size() != 1 || targetActivityIds.size() != 1) {
      throw new BadUserRequestException("only one to one mappings are supported");
    }

    if (sourceActivityIds.get(0) == null || targetActivityIds.get(0) == null) {
      throw new BadUserRequestException("the source activity id and target activity id must not be null");
    }
  }

  protected void ensureMappedActivitiesExist(MigrationInstruction instruction, ActivityImpl sourceActivity, ActivityImpl targetActivity) {
    String errorMessage = null;
    if (sourceActivity == null && targetActivity == null) {
      errorMessage = "the source activity and target activity does not exist";
    }
    else if (sourceActivity == null) {
      errorMessage = "the source activity does not exist";
    }
    else if (targetActivity == null) {
      errorMessage = "the target activity does not exist";
    }

    if (errorMessage != null) {
      throw new BadUserRequestException(errorMessage);
    }
  }

  protected void ensureSupportedActivity(MigrationInstruction instruction, ActivityImpl sourceActivity, ActivityImpl targetActivity) {
    if (isScope(sourceActivity)) {
      ensureSameActivityType(instruction, sourceActivity, targetActivity, SubProcessActivityBehavior.class);
    }
    else {
      ensureSameActivityType(instruction, sourceActivity, targetActivity, UserTaskActivityBehavior.class);
    }
  }

  protected void ensureSameActivityType(MigrationInstruction instruction, ActivityImpl sourceActivity, ActivityImpl targetActivity, Class<? extends ActivityBehavior> type) {
    boolean sourceHasExpectedType = type.isAssignableFrom(sourceActivity.getActivityBehavior().getClass());
    boolean targetHasExpectedType = type.isAssignableFrom(targetActivity.getActivityBehavior().getClass());

    if (sourceHasExpectedType && !targetHasExpectedType) {
      throw new BadUserRequestException("the source activity is of type '" + type.getName() + "' but the target activity not");
    }
    else if (!sourceHasExpectedType && targetHasExpectedType) {
      throw new BadUserRequestException("the target activity is of type '" + type.getName() + "' but the source activity not");
    }
    else if (!sourceHasExpectedType) {
      throw new BadUserRequestException("the source and target activity must be of type '" + type.getName() + "'");
    }
  }

  protected void ensureNotChildOfMultiInstance(MigrationInstruction instruction, ActivityImpl sourceActivity, ActivityImpl targetActivity) {
    boolean sourceActivityHasMultiInstanceParent = hasMultiInstanceParent(sourceActivity);
    boolean targetActivityHasMultiInstanceParent = hasMultiInstanceParent(targetActivity);
    if (sourceActivityHasMultiInstanceParent || targetActivityHasMultiInstanceParent) {
      throw new BadUserRequestException("multi instance child activities are currently not supported");
    }
  }

  protected boolean hasMultiInstanceParent(ActivityImpl activity) {
    FlowScopeWalker flowScopeWalker = new FlowScopeWalker(activity);
    flowScopeWalker.walkUntil(new TreeWalker.WalkCondition<ScopeImpl>() {
      public boolean isFulfilled(ScopeImpl element) {
        return isProcessDefinition(element) || isMultiInstance(element);
      }
    });

    return isMultiInstance(flowScopeWalker.getCurrentElement());
  }

  protected boolean isMultiInstance(ScopeImpl scope) {
    return !isProcessDefinition(scope) && scope.getActivityBehavior() instanceof MultiInstanceActivityBehavior;
  }

  protected boolean isProcessDefinition(ScopeImpl scope) {
    return scope == scope.getProcessDefinition();
  }

  protected boolean isScope(ActivityImpl sourceActivity) {
    return !sourceActivity.getActivities().isEmpty();
  }

}
