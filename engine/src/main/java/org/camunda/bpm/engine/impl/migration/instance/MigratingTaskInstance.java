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
package org.camunda.bpm.engine.impl.migration.instance;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingTaskInstance implements MigratingInstance {

  protected TaskEntity userTask;
  protected MigratingActivityInstance migratingActivityInstance;

  public MigratingTaskInstance(TaskEntity userTask, MigratingActivityInstance migratingActivityInstance) {
    this.userTask = userTask;
    this.migratingActivityInstance = migratingActivityInstance;
  }

  @Override
  public void migrateDependentEntities() {
  }

  @Override
  public void detachState() {
    userTask.getExecution().removeTask(userTask);
    userTask.setExecution(null);
  }

  @Override
  public void attachState(MigratingActivityInstance owningInstance) {
    ExecutionEntity representativeExecution = owningInstance.resolveRepresentativeExecution();
    representativeExecution.addTask(userTask);
    userTask.setExecution(representativeExecution);
  }

  @Override
  public void migrateState() {
    userTask.setProcessDefinitionId(migratingActivityInstance.targetScope.getProcessDefinition().getId());
  }
}
