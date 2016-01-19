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
package org.camunda.bpm.engine.impl.migration;

import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.TreeVisitor;
import org.camunda.bpm.engine.impl.tree.TreeWalker.WalkCondition;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * @author Thorben Lindhauer
 *
 */
public class DefaultMigrationPlanValidator implements MigrationPlanValidator {

  public void validateMigrationInstruction(ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition,
      MigrationInstruction migrationInstruction) {
    // TODO: the validation errors should be more informative;
    // also, validation should not end when the first error is found
    // but return a summary of all errors

    final String errorMessage = describeInvalidInstruction(migrationInstruction);

    List<String> sourceActivityIds = migrationInstruction.getSourceActivityIds();
    EnsureUtil.ensureNumberOfElements(BadUserRequestException.class, errorMessage, "sourceActivityIds", sourceActivityIds, 1);

    List<String> targetActivityIds = migrationInstruction.getTargetActivityIds();
    EnsureUtil.ensureNumberOfElements(BadUserRequestException.class, errorMessage, "targetActivityIds", targetActivityIds, 1);

    EnsureUtil.ensureNotNull(BadUserRequestException.class, errorMessage, "sourceActivityId", sourceActivityIds.get(0));
    final ActivityImpl sourceActivity = sourceProcessDefinition.findActivity(sourceActivityIds.get(0));
    EnsureUtil.ensureNotNull(BadUserRequestException.class, errorMessage, "sourceActivity", sourceActivity);

    EnsureUtil.ensureNotNull(BadUserRequestException.class, errorMessage, "targetActivityId", targetActivityIds.get(0));
    final ActivityImpl targetActivity = targetProcessDefinition.findActivity(targetActivityIds.get(0));
    EnsureUtil.ensureNotNull(BadUserRequestException.class, errorMessage, "targetActivity", targetActivity);

    EnsureUtil.ensureInstanceOf(BadUserRequestException.class, errorMessage, "sourceActivityBehavior", sourceActivity.getActivityBehavior(), UserTaskActivityBehavior.class);
    EnsureUtil.ensureInstanceOf(BadUserRequestException.class, errorMessage, "targetActivityBehavior", targetActivity.getActivityBehavior(), UserTaskActivityBehavior.class);

    FlowScopeWalker flowScopeWalker = new FlowScopeWalker(sourceActivity);
    flowScopeWalker.addPostVisitor(new TreeVisitor<ScopeImpl>() {

      ScopeImpl currentTargetScope = targetActivity.getFlowScope();

      @Override
      public void visit(ScopeImpl currentSourceScope) {
        // TODO: externalize the validation condition (i.e. id equality)?!
        if (currentTargetScope == null ||
            (!currentSourceScope.getId().equals(currentTargetScope.getId()))
            && !(isProcessDefinition(currentSourceScope) && isProcessDefinition(currentTargetScope))) {
          throw new BadUserRequestException(errorMessage + ": Source activity " + sourceActivity.getId() + " "
              + "and target activity " + targetActivity.getId() + " are not contained in the same sub process");
        }

        currentTargetScope = currentTargetScope.getFlowScope();
      }
    });
    flowScopeWalker.walkUntil(new WalkCondition<ScopeImpl>() {

      @Override
      public boolean isFulfilled(ScopeImpl element) {
        return element.getFlowScope() == null;
      }
    });
  }

  protected boolean isProcessDefinition(ScopeImpl scope) {
    return scope == scope.getProcessDefinition();
  }

  protected String describeInvalidInstruction(MigrationInstruction instruction) {
    return "Invalid migration instruction " + instruction;
  }

}
