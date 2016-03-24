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
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class MigratingProcessElementInstance implements MigratingInstance {

  protected MigrationInstruction migrationInstruction;

  protected ScopeImpl sourceScope;
  protected ScopeImpl targetScope;
  // changes from source to target scope during migration
  protected ScopeImpl currentScope;

  protected MigratingActivityInstance parentInstance;

  public ScopeImpl getSourceScope() {
    return sourceScope;
  }

  public ScopeImpl getTargetScope() {
    return targetScope;
  }

  public ScopeImpl getCurrentScope() {
    return currentScope;
  }

  public MigrationInstruction getMigrationInstruction() {
    return migrationInstruction;
  }

  public MigratingActivityInstance getParent() {
    return parentInstance;
  }

  public abstract void setParent(MigratingActivityInstance parentInstance);

  public abstract void addMigratingDependentInstance(MigratingInstance migratingInstance);

  public abstract ExecutionEntity resolveRepresentativeExecution();

}
