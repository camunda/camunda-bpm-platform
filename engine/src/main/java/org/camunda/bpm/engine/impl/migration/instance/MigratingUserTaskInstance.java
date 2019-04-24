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
package org.camunda.bpm.engine.impl.migration.instance;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.migration.MigrationLogger;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigratingUserTaskInstance implements MigratingInstance {

  public static final MigrationLogger MIGRATION_LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected TaskEntity userTask;
  protected MigratingActivityInstance migratingActivityInstance;

  public MigratingUserTaskInstance(TaskEntity userTask, MigratingActivityInstance migratingActivityInstance) {
    this.userTask = userTask;
    this.migratingActivityInstance = migratingActivityInstance;
  }

  @Override
  public void migrateDependentEntities() {
  }

  @Override
  public boolean isDetached() {
    return userTask.getExecutionId() == null;
  }

  @Override
  public void detachState() {
    userTask.getExecution().removeTask(userTask);
    userTask.setExecution(null);
  }

  @Override
  public void attachState(MigratingScopeInstance owningInstance) {
    ExecutionEntity representativeExecution = owningInstance.resolveRepresentativeExecution();
    representativeExecution.addTask(userTask);

    for (VariableInstanceEntity variable : userTask.getVariablesInternal()) {
      variable.setExecution(representativeExecution);
    }

    userTask.setExecution(representativeExecution);
  }

  @Override
  public void attachState(MigratingTransitionInstance targetTransitionInstance) {
    throw MIGRATION_LOGGER.cannotAttachToTransitionInstance(this);
  }

  @Override
  public void migrateState() {
    userTask.setProcessDefinitionId(migratingActivityInstance.getTargetScope().getProcessDefinition().getId());
    userTask.setTaskDefinitionKey(migratingActivityInstance.getTargetScope().getId());

    migrateHistory();
  }

  protected void migrateHistory() {
    HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();

    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.TASK_INSTANCE_MIGRATE, this)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createTaskInstanceMigrateEvt(userTask);
        }
      });
    }
  }
}
