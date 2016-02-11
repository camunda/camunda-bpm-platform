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
import java.util.List;

import org.camunda.bpm.engine.impl.migration.validation.MigrationActivityValidator;
import org.camunda.bpm.engine.impl.migration.validation.MigrationActivityValidators;
import org.camunda.bpm.engine.impl.migration.validation.MigrationInstructionValidator;
import org.camunda.bpm.engine.impl.migration.validation.MigrationInstructionValidators;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * @author Thorben Lindhauer
 *
 */
public class DefaultMigrationPlanGenerator implements MigrationInstructionGenerator {

  public static final List<MigrationActivityValidator> sourceActivityValidators = Arrays.asList(
    MigrationActivityValidators.SUPPORTED_ACTIVITY,
    MigrationActivityValidators.SUPPORTED_BOUNDARY_EVENT,
    MigrationActivityValidators.NOT_MULTI_INSTANCE_CHILD,
    MigrationActivityValidators.NOT_EVENT_SUB_PROCESS_CHILD,
    MigrationActivityValidators.HAS_NO_EVENT_SUB_PROCESS_CHILD
  );

  public static final List<MigrationActivityValidator> targetActivityValidators = Arrays.asList(
    MigrationActivityValidators.SUPPORTED_ACTIVITY,
    MigrationActivityValidators.SUPPORTED_BOUNDARY_EVENT,
    MigrationActivityValidators.NOT_MULTI_INSTANCE_CHILD,
    MigrationActivityValidators.NOT_EVENT_SUB_PROCESS_CHILD,
    MigrationActivityValidators.HAS_NO_EVENT_SUB_PROCESS_CHILD
  );

  public static final List<MigrationInstructionValidator> instructionValidators = Arrays.asList(
    MigrationInstructionValidators.SAME_ID_VALIDATOR,
    MigrationInstructionValidators.SAME_SCOPE,
    MigrationInstructionValidators.SAME_EVENT_SCOPE
  );

  public List<MigrationInstruction> generate(ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
    List<MigrationInstruction> migrationInstructions = new ArrayList<MigrationInstruction>();
    generate(sourceProcessDefinition, targetProcessDefinition, sourceProcessDefinition, targetProcessDefinition, migrationInstructions);
    return migrationInstructions;
  }

  public void generate(ScopeImpl sourceScope, ScopeImpl targetScope, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition, List<MigrationInstruction> migrationInstructions) {
    for (ActivityImpl sourceActivity : sourceScope.getActivities()) {
      for (ActivityImpl targetActivity : targetScope.getActivities()) {
        MigrationInstructionImpl migrationInstruction = new MigrationInstructionImpl(sourceActivity.getId(), targetActivity.getId());
        if (canBeMigrated(sourceActivity, sourceProcessDefinition, sourceActivityValidators) && canBeMigrated(targetActivity, targetProcessDefinition, targetActivityValidators) &&
            isValidInstruction(migrationInstruction, sourceProcessDefinition, targetProcessDefinition)) {

          migrationInstructions.add(migrationInstruction);

          if (sourceActivity.isScope() && targetActivity.isScope()) {
            generate(sourceActivity, targetActivity, sourceProcessDefinition, targetProcessDefinition, migrationInstructions);
          }
        }
      }
    }
  }

  protected boolean canBeMigrated(ActivityImpl activity, ProcessDefinitionImpl processDefinition, List<MigrationActivityValidator> activityValidators) {
    for (MigrationActivityValidator activityValidator : activityValidators) {
      if (!activityValidator.canBeMigrated(activity, processDefinition)) {
        return false;
      }
    }
    return true;
  }

  protected boolean isValidInstruction(MigrationInstructionImpl instruction, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
    for (MigrationInstructionValidator instructionValidator : instructionValidators) {
      if (!instructionValidator.isInstructionValid(instruction, sourceProcessDefinition, targetProcessDefinition)) {
        return false;
      }
    }
    return true;
  }

}
