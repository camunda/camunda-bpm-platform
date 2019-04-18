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

import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

public class AdditionalFlowScopeInstructionValidator implements MigrationInstructionValidator {

  public void validate(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions, MigrationInstructionValidationReportImpl report) {
    ValidatingMigrationInstruction ancestorScopeInstruction = getClosestPreservedAncestorScopeMigrationInstruction(instruction, instructions);
    ScopeImpl targetScope = instruction.getTargetActivity();

    if (ancestorScopeInstruction != null && targetScope != null && targetScope != targetScope.getProcessDefinition()) {
      ScopeImpl parentInstanceTargetScope = ancestorScopeInstruction.getTargetActivity();
      if (parentInstanceTargetScope != null && !parentInstanceTargetScope.isAncestorFlowScopeOf(targetScope)) {
        report.addFailure("The closest mapped ancestor '" + ancestorScopeInstruction.getSourceActivity().getId() + "' is mapped to scope '" +
          parentInstanceTargetScope.getId() + "' which is not an ancestor of target scope '" + targetScope.getId() + "'");
      }
    }
  }

  protected ValidatingMigrationInstruction getClosestPreservedAncestorScopeMigrationInstruction(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions) {
    ScopeImpl parent = instruction.getSourceActivity().getFlowScope();

    while (parent != null && instructions.getInstructionsBySourceScope(parent).isEmpty()) {
      parent = parent.getFlowScope();
    }

    if (parent != null) {
      return instructions.getInstructionsBySourceScope(parent).get(0);
    }
    else {
      return null;
    }
  }

}
