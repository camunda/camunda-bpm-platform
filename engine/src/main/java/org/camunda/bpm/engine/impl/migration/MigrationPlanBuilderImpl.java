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
package org.camunda.bpm.engine.impl.migration;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.impl.cmd.CreateMigrationPlanCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.migration.MigrationInstructionBuilder;
import org.camunda.bpm.engine.migration.MigrationInstructionsBuilder;
import org.camunda.bpm.engine.migration.MigrationPlan;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationPlanBuilderImpl implements MigrationInstructionBuilder, MigrationInstructionsBuilder {

  protected CommandExecutor commandExecutor;

  protected String sourceProcessDefinitionId;
  protected String targetProcessDefinitionId;
  protected List<MigrationInstructionImpl> explicitMigrationInstructions;

  protected boolean mapEqualActivities = false;
  protected boolean updateEventTriggersForGeneratedInstructions = false;

  public MigrationPlanBuilderImpl(CommandExecutor commandExecutor, String sourceProcessDefinitionId,
      String targetProcessDefinitionId) {
    this.commandExecutor = commandExecutor;
    this.sourceProcessDefinitionId = sourceProcessDefinitionId;
    this.targetProcessDefinitionId = targetProcessDefinitionId;
    this.explicitMigrationInstructions = new ArrayList<MigrationInstructionImpl>();
  }

  public MigrationInstructionsBuilder mapEqualActivities() {
    this.mapEqualActivities = true;
    return this;
  }

  public MigrationInstructionBuilder mapActivities(String sourceActivityId, String targetActivityId) {
    this.explicitMigrationInstructions.add(
      new MigrationInstructionImpl(sourceActivityId, targetActivityId)
    );
    return this;
  }

  public MigrationInstructionBuilder updateEventTrigger() {
    explicitMigrationInstructions
      .get(explicitMigrationInstructions.size() - 1)
      .setUpdateEventTrigger(true);
    return this;
  }

  public MigrationInstructionsBuilder updateEventTriggers() {
    this.updateEventTriggersForGeneratedInstructions = true;
    return this;
  }

  public String getSourceProcessDefinitionId() {
    return sourceProcessDefinitionId;
  }

  public String getTargetProcessDefinitionId() {
    return targetProcessDefinitionId;
  }

  public boolean isMapEqualActivities() {
    return mapEqualActivities;
  }

  public boolean isUpdateEventTriggersForGeneratedInstructions() {
    return updateEventTriggersForGeneratedInstructions;
  }

  public List<MigrationInstructionImpl> getExplicitMigrationInstructions() {
    return explicitMigrationInstructions;
  }

  public MigrationPlan build() {
    return commandExecutor.execute(new CreateMigrationPlanCmd(this));
  }

}
