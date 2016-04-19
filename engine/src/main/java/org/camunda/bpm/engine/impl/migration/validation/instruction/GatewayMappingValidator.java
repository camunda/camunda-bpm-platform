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
package org.camunda.bpm.engine.impl.migration.validation.instruction;

import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.behavior.InclusiveGatewayActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ParallelGatewayActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * <p>For synchronizing gateways (inclusive; parallel), the situation in which
 *  more tokens end up at the target gateway than there are incoming sequence flows
 *  must be avoided. Else, the migrated process instance may appear as broken to users
 *  since the migration logic cannot trigger these gateways immediately.
 *
 *  <p>Such situations can be avoided by enforcing that
 *  <ul>
 *  <li>the target gateway has at least the same number of incoming sequence flows
 *  <li>the target gateway's flow scope is not removed
 *  <li>there is not more than one instruction that maps to the target gateway
 *
 * @author Thorben Lindhauer
 */
public class GatewayMappingValidator implements MigrationInstructionValidator {

  @Override
  public void validate(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions,
      MigrationInstructionValidationReportImpl report) {

    ActivityImpl targetActivity = instruction.getTargetActivity();

    if (isWaitStateGateway(targetActivity)) {
      validateIncomingSequenceFlows(instruction, instructions, report);
      validateParentScopeMigrates(instruction, instructions, report);
      validateSingleInstruction(instruction, instructions, report);
    }
  }


  protected void validateIncomingSequenceFlows(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions,
      MigrationInstructionValidationReportImpl report) {
    ActivityImpl sourceActivity = instruction.getSourceActivity();
    ActivityImpl targetActivity = instruction.getTargetActivity();

    int numSourceIncomingFlows = sourceActivity.getIncomingTransitions().size();
    int numTargetIncomingFlows = targetActivity.getIncomingTransitions().size();

    if (numSourceIncomingFlows > numTargetIncomingFlows) {
      report.addFailure("The target gateway must have at least the same number "
          + "of incoming sequence flows that the source gateway has");
    }
  }

  protected void validateParentScopeMigrates(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions,
      MigrationInstructionValidationReportImpl report) {
    ActivityImpl sourceActivity = instruction.getSourceActivity();
    ScopeImpl flowScope = sourceActivity.getFlowScope();

    if (flowScope != flowScope.getProcessDefinition()) {
      if (instructions.getInstructionsBySourceScope(flowScope).isEmpty()) {
        report.addFailure("The gateway's flow scope '" + flowScope.getId() + "' must be mapped");
      }
    }
  }

  protected void validateSingleInstruction(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions,
      MigrationInstructionValidationReportImpl report) {
    ActivityImpl targetActivity = instruction.getTargetActivity();
    List<ValidatingMigrationInstruction> instructionsToTargetGateway =
        instructions.getInstructionsByTargetScope(targetActivity);

    if (instructionsToTargetGateway.size() > 1) {
      report.addFailure("Only one gateway can be mapped to gateway '" + targetActivity.getId() + "'");
    }
  }

  protected boolean isWaitStateGateway(ActivityImpl activity) {
    ActivityBehavior behavior = activity.getActivityBehavior();
    return behavior instanceof ParallelGatewayActivityBehavior
        || behavior instanceof InclusiveGatewayActivityBehavior;
  }


}
