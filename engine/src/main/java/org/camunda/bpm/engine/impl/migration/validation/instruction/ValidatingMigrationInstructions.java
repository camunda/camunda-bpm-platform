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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.CollectionUtil;

/**
 * @author Thorben Lindhauer
 *
 */
public class ValidatingMigrationInstructions {

  protected List<ValidatingMigrationInstruction> instructions;
  protected Map<ScopeImpl, List<ValidatingMigrationInstruction>> instructionsBySourceScope;
  protected Map<ScopeImpl, List<ValidatingMigrationInstruction>> instructionsByTargetScope;

  public ValidatingMigrationInstructions(List<ValidatingMigrationInstruction> instructions) {
    this.instructions = instructions;
    instructionsBySourceScope = new HashMap<ScopeImpl, List<ValidatingMigrationInstruction>>();
    instructionsByTargetScope = new HashMap<ScopeImpl, List<ValidatingMigrationInstruction>>();

    for (ValidatingMigrationInstruction instruction : instructions) {
      indexInstruction(instruction);
    }
  }

  public ValidatingMigrationInstructions() {
    this(new ArrayList<ValidatingMigrationInstruction>());
  }

  public void addInstruction(ValidatingMigrationInstruction instruction) {
    instructions.add(instruction);
    indexInstruction(instruction);
  }

  protected void indexInstruction(ValidatingMigrationInstruction instruction) {
    CollectionUtil.addToMapOfLists(instructionsBySourceScope, instruction.getSourceActivity(), instruction);
    CollectionUtil.addToMapOfLists(instructionsByTargetScope, instruction.getTargetActivity(), instruction);
  }

  public List<ValidatingMigrationInstruction> getInstructions() {
    return instructions;
  }

  public List<ValidatingMigrationInstruction> getInstructionsBySourceScope(ScopeImpl scope) {
    List<ValidatingMigrationInstruction> instructions = instructionsBySourceScope.get(scope);

    if (instructions == null) {
      return Collections.emptyList();
    }
    else {
      return instructions;
    }
  }

  public List<ValidatingMigrationInstruction> getInstructionsByTargetScope(ScopeImpl scope) {
    List<ValidatingMigrationInstruction> instructions = instructionsByTargetScope.get(scope);

    if (instructions == null) {
      return Collections.emptyList();
    }
    else {
      return instructions;
    }
  }
}
