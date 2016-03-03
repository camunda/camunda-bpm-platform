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

package org.camunda.bpm.engine.rest.helper;

import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockMigrationPlanBuilder {

  protected String sourceProcessDefinitionId;
  protected String targetProcessDefinitionId;
  protected List<MigrationInstruction> instructions = new ArrayList<MigrationInstruction>();

  public MockMigrationPlanBuilder sourceProcessDefinitionId(String sourceProcessDefinitionId) {
    this.sourceProcessDefinitionId = sourceProcessDefinitionId;
    return this;
  }

  public MockMigrationPlanBuilder targetProcessDefinitionId(String targetProcessDefinitionId) {
    this.targetProcessDefinitionId = targetProcessDefinitionId;
    return this;
  }

  public MockMigrationPlanBuilder instructions(List<MigrationInstruction> instructions) {
    this.instructions = instructions;
    return this;
  }

  public MockMigrationPlanBuilder instruction(MigrationInstruction instruction) {
    instructions.add(instruction);
    return this;
  }

  public MockMigrationPlanBuilder instruction(String sourceActivityId, String targetActivityId) {
    MigrationInstruction instructionMock = new MockMigrationInstructionBuilder()
      .sourceActivityId(sourceActivityId)
      .targetActivityId(targetActivityId)
      .build();
    return instruction(instructionMock);
  }

  public MigrationPlan build() {
    MigrationPlan migrationPlanMock = mock(MigrationPlan.class);
    when(migrationPlanMock.getSourceProcessDefinitionId()).thenReturn(sourceProcessDefinitionId);
    when(migrationPlanMock.getTargetProcessDefinitionId()).thenReturn(targetProcessDefinitionId);
    when(migrationPlanMock.getInstructions()).thenReturn(instructions);
    return migrationPlanMock;
  }

  public MigrationPlanBuilder builder() {
    MigrationPlan migrationPlanMock = build();
    MigrationPlanBuilder migrationPlanBuilderMock = mock(MigrationPlanBuilder.class);
    when(migrationPlanBuilderMock.mapEqualActivities()).thenReturn(migrationPlanBuilderMock);
    when(migrationPlanBuilderMock.mapActivities(anyString(), anyString())).thenReturn(migrationPlanBuilderMock);
    when(migrationPlanBuilderMock.build()).thenReturn(migrationPlanMock);
    return migrationPlanBuilderMock;
  }

}
