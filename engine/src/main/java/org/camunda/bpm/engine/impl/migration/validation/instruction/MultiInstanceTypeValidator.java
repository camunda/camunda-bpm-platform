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

import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.bpmn.parser.ActivityTypes;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;

/**
 * @author Thorben Lindhauer
 *
 */
public class MultiInstanceTypeValidator implements MigrationInstructionValidator {

  @Override
  public void validate(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions,
      MigrationInstructionValidationReportImpl report) {

    String sourceType = instruction.getSourceActivity().getProperties().get(BpmnProperties.TYPE);
    String targetType = instruction.getTargetActivity().getProperties().get(BpmnProperties.TYPE);

    if (ActivityTypes.MULTI_INSTANCE_BODY.equals(sourceType) && ActivityTypes.MULTI_INSTANCE_BODY.equals(targetType)) {
      ActivityBehavior sourceMiBehavior = instruction.getSourceActivity().getActivityBehavior();
      ActivityBehavior targetMiBehavior = instruction.getTargetActivity().getActivityBehavior();

      if (!sourceMiBehavior.getClass().equals(targetMiBehavior.getClass())) {
        report.addFailure("Source and target activity must be of the same multi-instance type (sequential or parallel)");
      }
    }
  }

}
