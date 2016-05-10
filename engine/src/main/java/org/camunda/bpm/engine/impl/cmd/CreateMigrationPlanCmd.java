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
package org.camunda.bpm.engine.impl.cmd;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.migration.MigrationInstructionGenerator;
import org.camunda.bpm.engine.impl.migration.MigrationLogger;
import org.camunda.bpm.engine.impl.migration.MigrationPlanBuilderImpl;
import org.camunda.bpm.engine.impl.migration.MigrationPlanImpl;
import org.camunda.bpm.engine.impl.migration.validation.instruction.MigrationInstructionValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.instruction.MigrationInstructionValidator;
import org.camunda.bpm.engine.impl.migration.validation.instruction.MigrationPlanValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.instruction.ValidatingMigrationInstruction;
import org.camunda.bpm.engine.impl.migration.validation.instruction.ValidatingMigrationInstructionImpl;
import org.camunda.bpm.engine.impl.migration.validation.instruction.ValidatingMigrationInstructions;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.util.EngineUtilLogger;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;

/**
 * @author Thorben Lindhauer
 *
 */
public class CreateMigrationPlanCmd implements Command<MigrationPlan> {

  public static final MigrationLogger LOG = EngineUtilLogger.MIGRATION_LOGGER;

  protected MigrationPlanBuilderImpl migrationBuilder;

  public CreateMigrationPlanCmd(MigrationPlanBuilderImpl migrationPlanBuilderImpl) {
    this.migrationBuilder = migrationPlanBuilderImpl;
  }

  @Override
  public MigrationPlan execute(CommandContext commandContext) {
    ProcessDefinitionEntity sourceProcessDefinition = getProcessDefinition(commandContext, migrationBuilder.getSourceProcessDefinitionId(), "Source");
    ProcessDefinitionEntity targetProcessDefinition = getProcessDefinition(commandContext, migrationBuilder.getTargetProcessDefinitionId(), "Target");

    checkAuthorization(commandContext, sourceProcessDefinition, targetProcessDefinition);

    MigrationPlanImpl migrationPlan = new MigrationPlanImpl(sourceProcessDefinition.getId(), targetProcessDefinition.getId());
    List<MigrationInstruction> instructions = new ArrayList<MigrationInstruction>();

    if (migrationBuilder.isMapEqualActivities()) {
      instructions.addAll(generateInstructions(commandContext, sourceProcessDefinition, targetProcessDefinition, migrationBuilder.isUpdateEventTriggersForGeneratedInstructions()));
    }

    instructions.addAll(migrationBuilder.getExplicitMigrationInstructions());
    migrationPlan.setInstructions(instructions);

    validateMigrationPlan(commandContext, migrationPlan, sourceProcessDefinition, targetProcessDefinition);

    return migrationPlan;
  }

  protected ProcessDefinitionEntity getProcessDefinition(CommandContext commandContext, String id, String type) {
    EnsureUtil.ensureNotNull(BadUserRequestException.class, type + " process definition id", id);

    try {
      return commandContext.getProcessEngineConfiguration()
        .getDeploymentCache().findDeployedProcessDefinitionById(id);
    }
    catch (NullValueException e) {
      throw LOG.processDefinitionDoesNotExist(id, type);
    }
  }

  protected void checkAuthorization(CommandContext commandContext, ProcessDefinitionEntity sourceProcessDefinition, ProcessDefinitionEntity targetProcessDefinition) {

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkCreateMigrationPlan(sourceProcessDefinition, targetProcessDefinition);
    }
  }

  protected List<MigrationInstruction> generateInstructions(CommandContext commandContext,
      ProcessDefinitionImpl sourceProcessDefinition,
      ProcessDefinitionImpl targetProcessDefinition,
      boolean updateEventTriggers) {
    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();

    // generate instructions
    MigrationInstructionGenerator migrationInstructionGenerator = processEngineConfiguration.getMigrationInstructionGenerator();
    ValidatingMigrationInstructions generatedInstructions = migrationInstructionGenerator.generate(sourceProcessDefinition, targetProcessDefinition, updateEventTriggers);

    // filter only valid instructions
    generatedInstructions.filterWith(processEngineConfiguration.getMigrationInstructionValidators());

    return generatedInstructions.asMigrationInstructions();
  }

  protected void validateMigrationPlan(CommandContext commandContext, MigrationPlanImpl migrationPlan, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
    List<MigrationInstructionValidator> migrationInstructionValidators = commandContext.getProcessEngineConfiguration().getMigrationInstructionValidators();

    MigrationPlanValidationReportImpl planReport = new MigrationPlanValidationReportImpl(migrationPlan);
    ValidatingMigrationInstructions validatingMigrationInstructions = wrapMigrationInstructions(migrationPlan, sourceProcessDefinition, targetProcessDefinition, planReport);

    for (ValidatingMigrationInstruction validatingMigrationInstruction : validatingMigrationInstructions.getInstructions()) {
      MigrationInstructionValidationReportImpl instructionReport = validateInstruction(validatingMigrationInstruction, validatingMigrationInstructions, migrationInstructionValidators);
      if (instructionReport.hasFailures()) {
        planReport.addInstructionReport(instructionReport);
      }
    }

    if (planReport.hasInstructionReports()) {
      throw LOG.failingMigrationPlanValidation(planReport);
    }

  }

  protected MigrationInstructionValidationReportImpl validateInstruction(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions, List<MigrationInstructionValidator> migrationInstructionValidators) {
    MigrationInstructionValidationReportImpl validationReport = new MigrationInstructionValidationReportImpl(instruction.toMigrationInstruction());
    for (MigrationInstructionValidator migrationInstructionValidator : migrationInstructionValidators) {
      migrationInstructionValidator.validate(instruction, instructions, validationReport);
    }
    return validationReport;
  }

  protected ValidatingMigrationInstructions wrapMigrationInstructions(MigrationPlan migrationPlan, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition, MigrationPlanValidationReportImpl planReport) {
    ValidatingMigrationInstructions validatingMigrationInstructions = new ValidatingMigrationInstructions();
    for (MigrationInstruction migrationInstruction : migrationPlan.getInstructions()) {
      MigrationInstructionValidationReportImpl instructionReport = new MigrationInstructionValidationReportImpl(migrationInstruction);

      String sourceActivityId = migrationInstruction.getSourceActivityId();
      String targetActivityId = migrationInstruction.getTargetActivityId();
      if (sourceActivityId != null && targetActivityId != null) {
        ActivityImpl sourceActivity = sourceProcessDefinition.findActivity(sourceActivityId);
        ActivityImpl targetActivity = targetProcessDefinition.findActivity(migrationInstruction.getTargetActivityId());

        if (sourceActivity != null && targetActivity != null) {
          validatingMigrationInstructions.addInstruction(new ValidatingMigrationInstructionImpl(sourceActivity, targetActivity, migrationInstruction.isUpdateEventTrigger()));
        }
        else {
          if (sourceActivity == null) {
            instructionReport.addFailure("Source activity '" + sourceActivityId + "' does not exist");
          }
          if (targetActivity == null) {
            instructionReport.addFailure("Target activity '" + targetActivityId + "' does not exist");
          }
        }
      }
      else {
        if (sourceActivityId == null) {
          instructionReport.addFailure("Source activity id is null");
        }
        if (targetActivityId == null) {
          instructionReport.addFailure("Target activity id is null");
        }
      }

      if (instructionReport.hasFailures()) {
        planReport.addInstructionReport(instructionReport);
      }
    }
    return validatingMigrationInstructions;
  }

}
