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
import static java.util.Collections.*;
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

  public List<MigrationInstruction> generate(ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
    return generateInstructionsInScope(sourceProcessDefinition, targetProcessDefinition);
  }

  protected List<MigrationInstruction> generateInstructionsInScope(ScopeImpl sourceScope, ScopeImpl targetScope) {
    List<MigrationInstruction> instructions = new ArrayList<MigrationInstruction>();

    for (ActivityImpl sourceActivity : sourceScope.getActivities()) {

      // matching IDs
      ActivityImpl targetActivity = targetScope.getChildActivity(sourceActivity.getId());

      if(targetActivity != null) {
        if (areEqualScopes(sourceActivity, targetActivity)) {
          instructions.addAll(generateInstructionsInScope(sourceActivity, targetActivity));
        }
        else if (areEqualActivities(sourceActivity, targetActivity)) {
          instructions.add(new MigrationInstructionImpl(
              singletonList(sourceActivity.getId()),
              singletonList(targetActivity.getId())));
        }
      }
    }

    return instructions;
  }

  protected boolean areEqualScopes(ScopeImpl sourceScope, ScopeImpl targetScope) {

    boolean areScopes = !sourceScope.getActivities().isEmpty() && !targetScope.getActivities().isEmpty();
    boolean matchingTypes = (sourceScope == sourceScope.getProcessDefinition() && targetScope == targetScope.getProcessDefinition())
        || (sourceScope.getActivityBehavior().getClass() == targetScope.getActivityBehavior().getClass());

    return matchingTypes && areScopes;
  }

  protected boolean areEqualActivities(ActivityImpl sourceActivity, ActivityImpl targetActivity) {

    boolean matchingTypes = sourceActivity.getActivityBehavior() instanceof UserTaskActivityBehavior
        && targetActivity.getActivityBehavior() instanceof UserTaskActivityBehavior;

    return matchingTypes;
  }

}
