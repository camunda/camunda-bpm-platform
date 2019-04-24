/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.migration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.behavior.ConditionalEventBehavior;
import org.camunda.bpm.engine.impl.migration.validation.activity.MigrationActivityValidator;
import org.camunda.bpm.engine.impl.migration.validation.instruction.CannotAddMultiInstanceInnerActivityValidator;
import org.camunda.bpm.engine.impl.migration.validation.instruction.CannotRemoveMultiInstanceInnerActivityValidator;
import org.camunda.bpm.engine.impl.migration.validation.instruction.MigrationInstructionValidator;
import org.camunda.bpm.engine.impl.migration.validation.instruction.UpdateEventTriggersValidator;
import org.camunda.bpm.engine.impl.migration.validation.instruction.ValidatingMigrationInstruction;
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
  protected List<MigrationInstructionValidator> migrationInstructionValidators = new ArrayList<MigrationInstructionValidator>();
  protected MigrationActivityMatcher migrationActivityMatcher;

  public DefaultMigrationInstructionGenerator(MigrationActivityMatcher migrationActivityMatcher) {
    this.migrationActivityMatcher = migrationActivityMatcher;
  }

  public MigrationInstructionGenerator migrationActivityValidators(List<MigrationActivityValidator> migrationActivityValidators) {
    this.migrationActivityValidators = migrationActivityValidators;
    return this;
  }

  public MigrationInstructionGenerator migrationInstructionValidators(List<MigrationInstructionValidator> migrationInstructionValidators) {

    this.migrationInstructionValidators = new ArrayList<MigrationInstructionValidator>();
    for (MigrationInstructionValidator validator : migrationInstructionValidators) {
      // ignore the following two validators during generation. Enables multi-instance bodies to be mapped.
      // this procedure is fine because these validators are again applied after all instructions have been generated
      if (!(validator instanceof CannotAddMultiInstanceInnerActivityValidator
          || validator instanceof CannotRemoveMultiInstanceInnerActivityValidator)) {
        this.migrationInstructionValidators.add(validator);
      }
    }

    return this;
  }

  public ValidatingMigrationInstructions generate(ProcessDefinitionImpl sourceProcessDefinition,
      ProcessDefinitionImpl targetProcessDefinition,
      boolean updateEventTriggers) {
    ValidatingMigrationInstructions migrationInstructions = new ValidatingMigrationInstructions();
    generate(sourceProcessDefinition,
        targetProcessDefinition,
        sourceProcessDefinition,
        targetProcessDefinition,
        migrationInstructions,
        updateEventTriggers);
    return migrationInstructions;
  }

  protected List<ValidatingMigrationInstruction> generateInstructionsForActivities(
      Collection<ActivityImpl> sourceActivities,
      Collection<ActivityImpl> targetActivities,
      boolean updateEventTriggers,
      ValidatingMigrationInstructions existingInstructions) {

    List<ValidatingMigrationInstruction> generatedInstructions = new ArrayList<ValidatingMigrationInstruction>();

    for (ActivityImpl sourceActivity : sourceActivities) {
      if (!existingInstructions.containsInstructionForSourceScope(sourceActivity)) {
        for (ActivityImpl targetActivity : targetActivities) {


          if (isValidActivity(sourceActivity)
            && isValidActivity(targetActivity)
            && migrationActivityMatcher.matchActivities(sourceActivity, targetActivity)) {

            //for conditional events the update event trigger must be set
            boolean updateEventTriggersForInstruction = sourceActivity.getActivityBehavior() instanceof ConditionalEventBehavior ||
                                                        updateEventTriggers && UpdateEventTriggersValidator.definesPersistentEventTrigger(sourceActivity);

            ValidatingMigrationInstruction generatedInstruction = new ValidatingMigrationInstructionImpl(sourceActivity, targetActivity, updateEventTriggersForInstruction);
            generatedInstructions.add(generatedInstruction);
          }
        }
      }
    }

    return generatedInstructions;
  }

  public void generate(ScopeImpl sourceScope,
      ScopeImpl targetScope,
      ProcessDefinitionImpl sourceProcessDefinition,
      ProcessDefinitionImpl targetProcessDefinition,
      ValidatingMigrationInstructions existingInstructions,
      boolean updateEventTriggers) {

    List<ValidatingMigrationInstruction> flowScopeInstructions = generateInstructionsForActivities(
      sourceScope.getActivities(),
      targetScope.getActivities(),
      updateEventTriggers,
      existingInstructions);

    existingInstructions.addAll(flowScopeInstructions);

    List<ValidatingMigrationInstruction> eventScopeInstructions = generateInstructionsForActivities(
      sourceScope.getEventActivities(),
      targetScope.getEventActivities(),
      updateEventTriggers,
      existingInstructions);

    existingInstructions.addAll(eventScopeInstructions);

    existingInstructions.filterWith(migrationInstructionValidators);

    for (ValidatingMigrationInstruction generatedInstruction : flowScopeInstructions) {
      if (existingInstructions.contains(generatedInstruction)) {
        generate(
            generatedInstruction.getSourceActivity(),
            generatedInstruction.getTargetActivity(),
            sourceProcessDefinition,
            targetProcessDefinition,
            existingInstructions,
            updateEventTriggers);
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
