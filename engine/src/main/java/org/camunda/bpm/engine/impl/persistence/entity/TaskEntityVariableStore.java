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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.List;

import org.camunda.bpm.engine.impl.variable.AbstractPersistentVariableStore;

/**
 * Variable store adapter for TaskEntity
 *
 * @author Daniel Meyer
 *
 */
public class TaskEntityVariableStore extends AbstractPersistentVariableStore {

  private static final long serialVersionUID = 1L;

  protected TaskEntity taskEntity;

  public TaskEntityVariableStore(TaskEntity taskEntity) {
    this.taskEntity = taskEntity;
  }

  protected List<VariableInstanceEntity> loadVariableInstances() {
    return taskEntity.loadVariableInstances();
  }

  protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {
    taskEntity.initializeVariableInstanceBackPointer(variableInstance);
  }
}
