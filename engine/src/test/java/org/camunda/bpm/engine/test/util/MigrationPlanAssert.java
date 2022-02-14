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
package org.camunda.bpm.engine.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class MigrationPlanAssert {

  protected MigrationPlan actual;

  public MigrationPlanAssert(MigrationPlan actual) {
    this.actual = actual;
  }

  public MigrationPlanAssert isNotNull() {
    assertNotNull("The migration plan is null", actual);

    return this;
  }

  public MigrationPlanAssert hasSourceProcessDefinition(ProcessDefinition sourceProcessDefinition) {
    return hasSourceProcessDefinitionId(sourceProcessDefinition.getId());
  }

  public MigrationPlanAssert hasSourceProcessDefinitionId(String sourceProcessDefinitionId) {
    isNotNull();
    assertEquals("The source process definition id does not match", sourceProcessDefinitionId, actual.getSourceProcessDefinitionId());

    return this;
  }

  public MigrationPlanAssert hasTargetProcessDefinition(ProcessDefinition targetProcessDefinition) {
    return hasTargetProcessDefinitionId(targetProcessDefinition.getId());
  }

  public MigrationPlanAssert hasTargetProcessDefinitionId(String targetProcessDefinitionId) {
    isNotNull();
    assertEquals("The target process definition id does not match", targetProcessDefinitionId, actual.getTargetProcessDefinitionId());

    return this;
  }

  public MigrationPlanAssert variablesNull() {
    isNotNull();
    assertNull(actual.getVariables());
    return this;
  }

  public MigrationPlanAssert variablesEmpty() {
    isNotNull();
    assertTrue(actual.getVariables() != null && actual.getVariables().isEmpty());
    return this;
  }

  public MigrationPlanAssert hasVariables(MigrationVariableAssert... variableAsserts) {
    isNotNull();

    Map<String, Object> notExpected = new HashMap<>(actual.getVariables());
    List<MigrationVariableAssert> notFound = new ArrayList<>();
    Collections.addAll(notFound, variableAsserts);

    Arrays.stream(variableAsserts).forEachOrdered(variableAssert ->
        actual.getVariables().keySet().stream()
            .filter(name -> variableAssert.name.equals(name))
            .forEachOrdered(name -> {
              notFound.remove(variableAssert);
              notExpected.remove(name);

              org.assertj.core.api.Assertions.assertThat(name)
                  .as("Variable name does not match for variable %s", name)
                  .isEqualTo(variableAssert.name);

              TypedValue typedValue = actual.getVariables().getValueTyped(name);
              org.assertj.core.api.Assertions.assertThat(typedValue.getValue())
                  .as("Variable value does not match for variable %s", name)
                  .isEqualTo(variableAssert.value);
              org.assertj.core.api.Assertions.assertThat(variableAssert.typed ? "typed" : "untyped")
                  .as("Variable %s: value typed/untyped does not match", name)
                  .isEqualTo(typedValue instanceof UntypedValueImpl ? "untyped" : "typed");
            }));

    if (!notExpected.isEmpty() || ! notFound.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      builder.append("\nActual migration variables:\n\t")
          .append(actual.getVariables()).append("\n");
      if (!notExpected.isEmpty()) {
        builder.append("Unexpected migration variables:\n\t").append(notExpected).append("\n");
      }
      if (!notFound.isEmpty()) {
        builder.append("Migration variables missing:\n\t").append(notFound);
      }
      fail(builder.toString());
    }
    return this;
  }

  public MigrationPlanAssert hasInstructions(MigrationInstructionAssert... instructionAsserts) {
    isNotNull();

    List<MigrationInstruction> notExpected = new ArrayList<>(actual.getInstructions());
    List<MigrationInstructionAssert> notFound = new ArrayList<>();
    Collections.addAll(notFound, instructionAsserts);

    for (MigrationInstructionAssert instructionAssert : instructionAsserts) {
      for (MigrationInstruction instruction : actual.getInstructions()) {
        if (instructionAssert.sourceActivityId.equals(instruction.getSourceActivityId())) {
          notFound.remove(instructionAssert);
          notExpected.remove(instruction);
          assertEquals("Target activity ids do not match for instruction " + instruction,
            instructionAssert.targetActivityId, instruction.getTargetActivityId());
          if (instructionAssert.updateEventTrigger != null) {
            assertEquals("Expected instruction to update event trigger: " + instructionAssert.updateEventTrigger + " but is: " + instruction.isUpdateEventTrigger(),
              instructionAssert.updateEventTrigger, instruction.isUpdateEventTrigger());
          }
        }
      }
    }

    if (!notExpected.isEmpty() || ! notFound.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      builder.append("\nActual migration instructions:\n\t").append(actual.getInstructions()).append("\n");
      if (!notExpected.isEmpty()) {
        builder.append("Unexpected migration instructions:\n\t").append(notExpected).append("\n");
      }
      if (!notFound.isEmpty()) {
        builder.append("Migration instructions missing:\n\t").append(notFound);
      }
      fail(builder.toString());
    }

    return this;
  }

  public MigrationPlanAssert hasEmptyInstructions() {
    isNotNull();

    List<MigrationInstruction> instructions = actual.getInstructions();
    assertTrue("Expected migration plan has no instructions but has: " + instructions, instructions.isEmpty());

    return this;
  }

  public static MigrationPlanAssert assertThat(MigrationPlan migrationPlan) {
    return new MigrationPlanAssert(migrationPlan);
  }

  public static MigrationInstructionAssert migrate(String sourceActivityId) {
    return new MigrationInstructionAssert().from(sourceActivityId);
  }

  public static class MigrationInstructionAssert {
    protected String sourceActivityId;
    protected String targetActivityId;
    protected Boolean updateEventTrigger;

    public MigrationInstructionAssert from(String sourceActivityId) {
      this.sourceActivityId = sourceActivityId;
      return this;
    }

    public MigrationInstructionAssert to(String targetActivityId) {
      this.targetActivityId = targetActivityId;
      return this;
    }

    public MigrationInstructionAssert updateEventTrigger(boolean updateEventTrigger) {
      this.updateEventTrigger = updateEventTrigger;
      return this;
    }

    public String toString() {
      return new MigrationInstructionImpl(sourceActivityId, targetActivityId).toString();
    }

  }

  public static MigrationVariableAssert variable() {
    return new MigrationVariableAssert();
  }

  public static class MigrationVariableAssert {

    protected String name;
    protected Object value;
    protected boolean typed;

    public MigrationVariableAssert name(String name) {
      this.name = name;
      return this;
    }

    public MigrationVariableAssert value(Object value) {
      this.value = value;
      return this;
    }

    public MigrationVariableAssert typed() {
      this.typed = true;
      return this;
    }
  }

}
