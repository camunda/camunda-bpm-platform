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
import java.util.List;

import org.camunda.bpm.engine.impl.migration.validation.activity.MigrationActivityValidator;
import org.camunda.bpm.engine.impl.migration.validation.instruction.ValidatingMigrationInstructionImpl;
import org.camunda.bpm.engine.impl.migration.validation.instruction.ValidatingMigrationInstructions;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * @author Thorben Lindhauer
 */
public class DefaultMigrationInstructionGenerator implements MigrationInstructionGenerator {

  protected List<MigrationActivityValidator> migrationActivityValidators = new ArrayList<MigrationActivityValidator>();
  protected MigrationActivityMatcher migrationActivityMatcher;

  public DefaultMigrationInstructionGenerator(MigrationActivityMatcher migrationActivityMatcher) {
    this.migrationActivityMatcher = migrationActivityMatcher;
  }

  public MigrationInstructionGenerator migrationActivityValidators(List<MigrationActivityValidator> migrationActivityValidators) {
    this.migrationActivityValidators = migrationActivityValidators;
    return this;
  }

  public ValidatingMigrationInstructions generate(ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
    ValidatingMigrationInstructions migrationInstructions = new ValidatingMigrationInstructions();
    generate(sourceProcessDefinition, targetProcessDefinition, sourceProcessDefinition, targetProcessDefinition, migrationInstructions);
    return migrationInstructions;
  }

  public void generate(ScopeImpl sourceScope,
      ScopeImpl targetScope,
      ProcessDefinitionImpl sourceProcessDefinition,
      ProcessDefinitionImpl targetProcessDefinition,
      ValidatingMigrationInstructions migrationInstructions) {
    for (ActivityImpl sourceActivity : sourceScope.getActivities()) {
      for (ActivityImpl targetActivity : targetScope.getActivities()) {
        if (isValidActivity(sourceActivity) && isValidActivity(targetActivity) && migrationActivityMatcher.matchActivities(sourceActivity, targetActivity)) {
          migrationInstructions.addInstruction(new ValidatingMigrationInstructionImpl(sourceActivity, targetActivity));
          generate(sourceActivity, targetActivity, sourceProcessDefinition, targetProcessDefinition, migrationInstructions);
        }
      }
    }
  }

  protected boolean isValidActivity(ActivityImpl activity) {
    for (MigrationActivityValidator migrationActivityValidator : migrationActivityValidators) {
      if (!migrationActivityValidator.valid(activity)) {
        return false;
      }
    }
    return true;
  }

}
