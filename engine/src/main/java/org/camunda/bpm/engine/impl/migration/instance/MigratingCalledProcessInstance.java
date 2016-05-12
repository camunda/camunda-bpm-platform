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

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.migration.MigrationLogger;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingCalledProcessInstance implements MigratingInstance {

  public static final MigrationLogger MIGRATION_LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected ExecutionEntity processInstance;

  public MigratingCalledProcessInstance(ExecutionEntity processInstance) {
    this.processInstance = processInstance;
  }

  @Override
  public boolean isDetached() {
    return processInstance.getSuperExecutionId() == null;
  }

  @Override
  public void detachState() {
    processInstance.setSuperExecution(null);
  }

  @Override
  public void attachState(MigratingScopeInstance targetActivityInstance) {
    processInstance.setSuperExecution(targetActivityInstance.resolveRepresentativeExecution());
  }

  @Override
  public void attachState(MigratingTransitionInstance targetTransitionInstance) {
    throw MIGRATION_LOGGER.cannotAttachToTransitionInstance(this);
  }

  @Override
  public void migrateState() {
    // nothing to do
  }

  @Override
  public void migrateDependentEntities() {
    // nothing to do
  }

}
