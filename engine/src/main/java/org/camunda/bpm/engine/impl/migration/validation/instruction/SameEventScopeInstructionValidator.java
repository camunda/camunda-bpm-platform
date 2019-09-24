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
package org.camunda.bpm.engine.impl.migration.validation.instruction;

import java.util.List;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

public class SameEventScopeInstructionValidator implements MigrationInstructionValidator {

  public void validate(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions, MigrationInstructionValidationReportImpl report) {
    ActivityImpl sourceActivity = instruction.getSourceActivity();
    if (isCompensationBoundaryEvent(sourceActivity)) {
      // this is not required for compensation boundary events since their
      // event scopes need not be active at runtime
      return;
    }

    ScopeImpl sourceEventScope = instruction.getSourceActivity().getEventScope();
    ScopeImpl targetEventScope = instruction.getTargetActivity().getEventScope();

    if (sourceEventScope == null || sourceEventScope == sourceActivity.getFlowScope()) {
      // event scopes must only match if the event scopes are not the flow scopes
      // => validation necessary for boundary events;
      // => validation not necessary for event subprocesses
      return;
    }

    if (targetEventScope == null) {
      // target event scope is allowed to be null for user tasks with timeout listeners
      // if all listeners are removed in the target
      if (!isUserTaskWithTimeoutListener(sourceActivity)) {
        report.addFailure("The source activity's event scope (" + sourceEventScope.getId() + ") must be mapped but the "
            + "target activity has no event scope");
      }
    }
    else {
      ScopeImpl mappedSourceEventScope = findMappedEventScope(sourceEventScope, instruction, instructions);
      if (mappedSourceEventScope == null || !mappedSourceEventScope.getId().equals(targetEventScope.getId())) {
        report.addFailure("The source activity's event scope (" + sourceEventScope.getId() + ") "
            + "must be mapped to the target activity's event scope (" + targetEventScope.getId() + ")");
      }
    }
  }

  protected boolean isCompensationBoundaryEvent(ActivityImpl sourceActivity) {
    String activityType = sourceActivity.getProperties().get(BpmnProperties.TYPE);
    return ActivityTypes.BOUNDARY_COMPENSATION.equals(activityType);
  }

  protected boolean isUserTaskWithTimeoutListener(ActivityImpl sourceActivity) {
    return ActivityTypes.TASK_USER_TASK.equals(sourceActivity.getProperties().get(BpmnProperties.TYPE)) &&
        sourceActivity.isScope() && sourceActivity.equals(sourceActivity.getEventScope()) &&
        sourceActivity.getProperties().get(BpmnProperties.TIMEOUT_LISTENER_DECLARATIONS) != null &&
        !sourceActivity.getProperties().get(BpmnProperties.TIMEOUT_LISTENER_DECLARATIONS).isEmpty();
  }

  protected ScopeImpl findMappedEventScope(ScopeImpl sourceEventScope, ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions) {
    if (sourceEventScope != null) {
      if (sourceEventScope == sourceEventScope.getProcessDefinition()) {
        return instruction.getTargetActivity().getProcessDefinition();
      }
      else {
        List<ValidatingMigrationInstruction> eventScopeInstructions = instructions.getInstructionsBySourceScope(sourceEventScope);
        if (eventScopeInstructions.size() > 0) {
          return eventScopeInstructions.get(0).getTargetActivity();
        }
      }
    }
    return null;
  }

  protected void addFailure(ValidatingMigrationInstruction instruction,
      MigrationInstructionValidationReportImpl report, String sourceScopeId, String targetScopeId) {
    report.addFailure("The source activity's event scope (" + sourceScopeId + ") "
        + "must be mapped to the target activity's event scope (" + targetScopeId + ")");
  }

}
