/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;

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

  public MigrationPlanAssert hasInstructions(MigrationInstructionAssert... instructionAsserts) {
    isNotNull();

    List<MigrationInstruction> notExpected = new ArrayList<MigrationInstruction>(actual.getInstructions());
    List<MigrationInstructionAssert> notFound = new ArrayList<MigrationInstructionAssert>();
    Collections.addAll(notFound, instructionAsserts);

    for (MigrationInstructionAssert instructionAssert : instructionAsserts) {
      for (MigrationInstruction instruction : actual.getInstructions()) {
        if (instructionAssert.sourceActivityIds.equals(instruction.getSourceActivityIds())) {
          notFound.remove(instructionAssert);
          notExpected.remove(instruction);
          assertEquals("Target activity ids do not match for instruction " + instruction,
            instructionAssert.targetActivityIds, instruction.getTargetActivityIds());
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

  public static MigrationInstructionAssert migrate(String... sourceActivityIds) {
    return new MigrationInstructionAssert().from(sourceActivityIds);
  }

  public static class MigrationInstructionAssert {
    protected List<String> sourceActivityIds = new ArrayList<String>();
    protected List<String> targetActivityIds = new ArrayList<String>();

    public MigrationInstructionAssert from(String... sourceActivityIds) {
      Collections.addAll(this.sourceActivityIds, sourceActivityIds);
      return this;
    }

    public MigrationInstructionAssert to(String... targetActivityIds) {
      Collections.addAll(this.targetActivityIds, targetActivityIds);
      return this;
    }

    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("MigrationInstructionAssert[sourceActivities=[");
      joinActivityIds(builder, sourceActivityIds);
      builder.append("], targetActivityIds=[");
      joinActivityIds(builder, targetActivityIds);
      return  builder.append("]]").toString();
    }

    protected void joinActivityIds(StringBuilder builder, List<String> activityIds) {
      for (int i = 0; i < activityIds.size(); i++) {
        builder.append(activityIds.get(i));
        if (i < activityIds.size() - 1) {
          builder.append(", ");
        }
      }
    }
  }

}
