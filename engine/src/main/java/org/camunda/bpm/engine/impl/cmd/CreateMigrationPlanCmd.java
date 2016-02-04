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
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.migration.DefaultMigrationPlanGenerator;
import org.camunda.bpm.engine.impl.migration.MigrationLogger;
import org.camunda.bpm.engine.impl.migration.validation.DefaultMigrationPlanValidator;
import org.camunda.bpm.engine.impl.migration.MigrationPlanBuilderImpl;
import org.camunda.bpm.engine.impl.migration.MigrationPlanImpl;
import org.camunda.bpm.engine.impl.migration.validation.MigrationPlanValidationReportImpl;
import org.camunda.bpm.engine.impl.migration.validation.MigrationPlanValidator;
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
    String sourceProcessDefinitionId = migrationBuilder.getSourceProcessDefinitionId();
    String targetProcessDefinitionId = migrationBuilder.getTargetProcessDefinitionId();
    MigrationPlanImpl migrationPlan = new MigrationPlanImpl(sourceProcessDefinitionId, targetProcessDefinitionId);

    EnsureUtil.ensureNotNull(BadUserRequestException.class, "sourceProcessDefinitionId", sourceProcessDefinitionId);
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "targetProcessDefinitionId", targetProcessDefinitionId);

    ProcessDefinitionImpl sourceProcessDefinition = commandContext.getProcessEngineConfiguration()
        .getDeploymentCache().findProcessDefinitionFromCache(sourceProcessDefinitionId);
    ProcessDefinitionImpl targetProcessDefinition = commandContext.getProcessEngineConfiguration()
        .getDeploymentCache().findProcessDefinitionFromCache(targetProcessDefinitionId);

    EnsureUtil.ensureNotNull(BadUserRequestException.class,
      "source process definition with id " + sourceProcessDefinitionId + " does not exist",
      "sourceProcessDefinition",
      sourceProcessDefinition);

    EnsureUtil.ensureNotNull(BadUserRequestException.class,
      "target process definition with id " + targetProcessDefinitionId + " does not exist",
      "targetProcessDefinition",
      targetProcessDefinition);

    List<MigrationInstruction> instructions = new ArrayList<MigrationInstruction>();

    if (migrationBuilder.isMapEqualActivities()) {

      instructions.addAll(new DefaultMigrationPlanGenerator()
        .generate(sourceProcessDefinition, targetProcessDefinition));
    }

    instructions.addAll(migrationBuilder.getExplicitMigrationInstructions());
    migrationPlan.setInstructions(instructions);

    MigrationPlanValidationReportImpl validationReport = new MigrationPlanValidationReportImpl(migrationPlan);

    MigrationPlanValidator validator = new DefaultMigrationPlanValidator();
    validator.validateMigrationPlan(sourceProcessDefinition, targetProcessDefinition, migrationPlan, validationReport);

    if (validationReport.hasFailures()) {
      throw LOG.failingMigrationPlanValidation(validationReport);
    }

    return migrationPlan;
  }

}
