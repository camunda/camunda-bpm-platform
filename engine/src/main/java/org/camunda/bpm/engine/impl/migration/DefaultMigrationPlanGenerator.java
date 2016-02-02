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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * @author Thorben Lindhauer
 *
 */
public class DefaultMigrationPlanGenerator implements MigrationInstructionGenerator {

  @Override
  public List<MigrationInstruction> generate(ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
    return generateInstructionsInScope(sourceProcessDefinition, targetProcessDefinition, 1);
  }

  protected List<MigrationInstruction> generateInstructionsInScope(ScopeImpl sourceScope, ScopeImpl targetScope, int allowedScopeDepth) {
    List<MigrationInstruction> instructions = new ArrayList<MigrationInstruction>();

    for (ActivityImpl sourceActivity : sourceScope.getActivities()) {
      for (ActivityImpl targetActivity : targetScope.getActivities()) {
        if (areEqualScopes(sourceActivity, targetActivity)) {
          instructions.add(new MigrationInstructionImpl(
            Collections.singletonList(sourceActivity.getId()), Collections.singletonList(targetActivity.getId())));
          instructions.addAll(generateInstructionsInScope(sourceActivity, targetActivity, allowedScopeDepth));
        }
        else if (areEqualActivities(sourceActivity, targetActivity)) {
          instructions.add(new MigrationInstructionImpl(
            Collections.singletonList(sourceActivity.getId()), Collections.singletonList(targetActivity.getId())));
        }
        else if (allowedScopeDepth > 0 && isScope(targetActivity)) {
          instructions.addAll(generateInstructionsInScope(sourceScope, targetActivity, allowedScopeDepth - 1));
        }
      }
    }

    return instructions;
  }

  protected boolean areEqualScopes(ScopeImpl sourceScope, ScopeImpl targetScope) {

    boolean areScopes = isScope(sourceScope) && isScope(targetScope);
    boolean matchingIds = sourceScope.getId().equals(targetScope.getId());
    boolean matchingTypes = (sourceScope == sourceScope.getProcessDefinition() && targetScope == targetScope.getProcessDefinition())
        || (sourceScope.getActivityBehavior().getClass() == targetScope.getActivityBehavior().getClass());

    return matchingIds && matchingTypes && areScopes;
  }

  protected boolean areEqualActivities(ActivityImpl sourceActivity, ActivityImpl targetActivity) {
    boolean matchingIds = sourceActivity.getId().equals(targetActivity.getId());

    boolean matchingTypes = sourceActivity.getActivityBehavior() instanceof UserTaskActivityBehavior
        && targetActivity.getActivityBehavior() instanceof UserTaskActivityBehavior;

    boolean areBothEitherScopesOrNot = isScope(sourceActivity) == isScope(targetActivity);

    return matchingIds && matchingTypes && areBothEitherScopesOrNot;
  }

  protected boolean isScope(ScopeImpl scope) {
    return scope.isScope();
  }

}
