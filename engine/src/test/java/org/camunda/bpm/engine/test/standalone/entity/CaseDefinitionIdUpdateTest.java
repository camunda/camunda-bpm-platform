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
package org.camunda.bpm.engine.test.standalone.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

public class CaseDefinitionIdUpdateTest extends PluggableProcessEngineTest {

  @Test
  public void testUpdateCaseDefinitionIdInTask() {
    // given
    final CaseDefinitionEntity caseDefinitionEntity1 = prepareCaseDefinition(UUID.randomUUID().toString());
    final CaseDefinitionEntity caseDefinitionEntity2 = prepareCaseDefinition(UUID.randomUUID().toString());

    final TaskEntity task = new TaskEntity();
    task.setId(UUID.randomUUID().toString());
    task.setCaseDefinitionId(caseDefinitionEntity1.getId());

    createTask(task);

    final TaskEntity createdTask = findTask(task.getId());

    assertThat(createdTask).isNotNull();

    task.setCaseDefinitionId(caseDefinitionEntity2.getId());

    // when
    update(task);
    final TaskEntity updatedTask = findTask(task.getId());

    // then
    assertThat(updatedTask.getCaseDefinitionId()).isEqualTo(caseDefinitionEntity2.getId());

    deleteTask(updatedTask);
    deleteCaseDefinition(caseDefinitionEntity1);
    deleteCaseDefinition(caseDefinitionEntity2);
  }

  @Test
  public void testUpdateCaseDefinitionIdInCaseExecutionEntity() {
    // given
    final CaseDefinitionEntity caseDefinitionEntity1 = prepareCaseDefinition(UUID.randomUUID().toString());
    final CaseDefinitionEntity caseDefinitionEntity2 = prepareCaseDefinition(UUID.randomUUID().toString());

    final CaseExecutionEntity caseExecutionEntity = prepareCaseExecution(caseDefinitionEntity1);

    assertThat(caseExecutionEntity.getCaseDefinitionId()).isEqualTo(caseDefinitionEntity1.getId());

    createCaseExecution(caseExecutionEntity);
    final CaseExecutionEntity createdCaseExecution = findCaseExecution(caseExecutionEntity.getId());

    assertThat(createdCaseExecution).isNotNull();

    createdCaseExecution.setCaseDefinition(caseDefinitionEntity2);

    assertThat(createdCaseExecution.getCaseDefinitionId()).isEqualTo(caseDefinitionEntity2.getId());

    // when
    update(createdCaseExecution);

    // then
    final CaseExecutionEntity updatedCaseExecution = findCaseExecution(createdCaseExecution.getId());
    assertThat(updatedCaseExecution.getCaseDefinitionId()).isEqualTo(caseDefinitionEntity2.getId());

    deleteCaseExecution(updatedCaseExecution);
    deleteCaseDefinition(caseDefinitionEntity1);
    deleteCaseDefinition(caseDefinitionEntity2);
  }

  private CaseExecutionEntity prepareCaseExecution(CaseDefinitionEntity caseDefinitionEntity1) {
    final CaseExecutionEntity caseExecutionEntity = new CaseExecutionEntity();
    caseExecutionEntity.setId(UUID.randomUUID().toString());
    caseExecutionEntity.setCaseDefinition(caseDefinitionEntity1);
    return caseExecutionEntity;
  }

  private CaseDefinitionEntity prepareCaseDefinition(String id) {
    final CaseDefinitionEntity caseDefinitionEntity = new CaseDefinitionEntity();
    caseDefinitionEntity.setId(id);
    caseDefinitionEntity.setKey(UUID.randomUUID().toString());
    caseDefinitionEntity.setDeploymentId(UUID.randomUUID().toString());
    createCaseDefinition(caseDefinitionEntity);
    return caseDefinitionEntity;
  }

  private CaseExecutionEntity findCaseExecution(final String id) {
    return processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<CaseExecutionEntity>() {
      @Override
      public CaseExecutionEntity execute(CommandContext commandContext) {
        return commandContext.getCaseExecutionManager().findCaseExecutionById(id);
      }
    });
  }

  private Void deleteCaseExecution(final CaseExecutionEntity caseExecutionEntity) {
    return processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getCaseExecutionManager().deleteCaseExecution(caseExecutionEntity);
        return null;
      }
    });
  }

  private void createCaseExecution(final CaseExecutionEntity caseExecutionEntity) {
    processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getCaseExecutionManager().insertCaseExecution(caseExecutionEntity);
        return null;
      }
    });
  }

  private void update(final DbEntity entity) {
    processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getDbEntityManager().merge(entity);
        return null;
      }
    });
  }

  private void createCaseDefinition(final CaseDefinitionEntity caseDefinitionEntity) {
    processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getCaseDefinitionManager().insertCaseDefinition(caseDefinitionEntity);
        return null;
      }
    });
  }

  private Void deleteCaseDefinition(final CaseDefinitionEntity caseDefinitionEntity) {
    return processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getCaseDefinitionManager().deleteCaseDefinitionsByDeploymentId(caseDefinitionEntity.getDeploymentId());
        return null;
      }
    });
  }

  private void createTask(final TaskEntity taskEntity) {
    processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getTaskManager().insertTask(taskEntity);
        return null;
      }
    });
  }

  private void deleteTask(final TaskEntity taskEntity) {
    processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getTaskManager().delete(taskEntity);
        return null;
      }
    });
  }

  private TaskEntity findTask(final String id) {
    return processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<TaskEntity>() {
      @Override
      public TaskEntity execute(CommandContext commandContext) {
        return commandContext.getTaskManager().findTaskById(id);
      }
    });
  }
}
