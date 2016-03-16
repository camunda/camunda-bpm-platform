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

import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.variable.AbstractPersistentVariableStore;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 *
 */
public class ExecutionEntityVariableStore extends AbstractPersistentVariableStore {

  protected ExecutionEntity executionEntity;

  public ExecutionEntityVariableStore(ExecutionEntity executionEntity) {
    this.executionEntity = executionEntity;
  }

  protected List<VariableInstanceEntity> loadVariableInstances() {
    return executionEntity.loadVariableInstances();
  }

  protected void referenceOwningEntity(VariableInstanceEntity variableInstance) {
    variableInstance.setExecution(executionEntity);
  }

  @Override
  protected void initializeEntitySpecificContext(VariableInstanceEntity variableInstance) {
    variableInstance.setConcurrentLocal(!executionEntity.isScope() || executionEntity.isExecutingScopeLeafActivity());
  }

  protected boolean isAutoFireHistoryEvents() {
    return executionEntity.isAutoFireHistoryEvents();
  }

  @Override
  protected AbstractVariableScope getThisScope() {
    return executionEntity;
  }


}
