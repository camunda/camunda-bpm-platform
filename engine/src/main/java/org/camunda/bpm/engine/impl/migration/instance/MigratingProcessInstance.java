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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.migration.MigrationLogger;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.TransitionInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingProcessInstance {

  protected static final MigrationLogger LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected String processInstanceId;
  protected List<MigratingActivityInstance> migratingActivityInstances;
  protected List<MigratingTransitionInstance> migratingTransitionInstances;
  protected MigratingActivityInstance rootInstance;

  public MigratingProcessInstance(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    this.migratingActivityInstances = new ArrayList<MigratingActivityInstance>();
    this.migratingTransitionInstances = new ArrayList<MigratingTransitionInstance>();
  }

  public MigratingActivityInstance getRootInstance() {
    return rootInstance;
  }

  public void setRootInstance(MigratingActivityInstance rootInstance) {
    this.rootInstance = rootInstance;
  }

  public Collection<MigratingActivityInstance> getMigratingActivityInstances() {
    return migratingActivityInstances;
  }

  public Collection<MigratingTransitionInstance> getMigratingTransitionInstances() {
    return migratingTransitionInstances;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public MigratingActivityInstance addActivityInstance(
      MigrationInstruction migrationInstruction,
      ActivityInstance activityInstance,
      ScopeImpl sourceScope,
      ScopeImpl targetScope,
      ExecutionEntity scopeExecution) {

    MigratingActivityInstance migratingActivityInstance = new MigratingActivityInstance(
        activityInstance,
        migrationInstruction,
        sourceScope,
        targetScope,
        scopeExecution);

    migratingActivityInstances.add(migratingActivityInstance);

    if (processInstanceId.equals(activityInstance.getId())) {
      rootInstance = migratingActivityInstance;
    }

    return migratingActivityInstance;
  }

  public MigratingTransitionInstance addTransitionInstance(
      MigrationInstruction migrationInstruction,
      TransitionInstance transitionInstance,
      ScopeImpl sourceScope,
      ScopeImpl targetScope,
      ExecutionEntity asyncExecution) {

    MigratingTransitionInstance migratingTransitionInstance = new MigratingTransitionInstance(
        transitionInstance,
        migrationInstruction,
        sourceScope,
        targetScope,
        asyncExecution);

    migratingTransitionInstances.add(migratingTransitionInstance);

    return migratingTransitionInstance;
  }

}
