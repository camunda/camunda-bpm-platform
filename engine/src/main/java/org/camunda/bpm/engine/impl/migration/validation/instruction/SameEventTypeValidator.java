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

import org.camunda.bpm.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class SameEventTypeValidator implements MigrationInstructionValidator {

  @Override
  public void validate(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions,
      MigrationInstructionValidationReportImpl report) {
    ActivityImpl sourceActivity = instruction.getSourceActivity();
    ActivityImpl targetActivity = instruction.getTargetActivity();

    if (isEvent(sourceActivity) && isEvent(targetActivity)) {
      String sourceType = sourceActivity.getProperties().get(BpmnProperties.TYPE);
      String targetType = targetActivity.getProperties().get(BpmnProperties.TYPE);

      if (!sourceType.equals(targetType)) {
        report.addFailure("Events are not of the same type (" + sourceType + " != " + targetType + ")");
      }
    }
  }

  protected boolean isEvent(ActivityImpl activity) {
    ActivityBehavior behavior = activity.getActivityBehavior();
    return behavior instanceof BoundaryEventActivityBehavior
        || behavior instanceof EventSubProcessStartEventActivityBehavior;
  }

}
