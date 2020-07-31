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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.UUID;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseActivityInstanceEntity;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

public class CaseDefinitionIdHistoryUpdateTest extends PluggableProcessEngineTest {

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void testUpdateCaseDefinitionIdInCaseExecutionEntity() {
    // given
    final CaseDefinitionEntity caseDefinitionEntity1 = prepareCaseDefinition(UUID.randomUUID().toString());
    final CaseDefinitionEntity caseDefinitionEntity2 = prepareCaseDefinition(UUID.randomUUID().toString());

    final CaseExecutionEntity caseExecutionEntity = prepareCaseExecution(caseDefinitionEntity1);

    assertThat(caseExecutionEntity.getCaseDefinitionId()).isEqualTo(caseDefinitionEntity1.getId());

    createCaseExecution(caseExecutionEntity);

    caseExecutionEntity.setCaseDefinition(caseDefinitionEntity2);

    // Create
    final HistoricCaseActivityInstanceEventEntity historicCaseActivityInstanceEntity = prepareHistoricCaseActivityInstance(caseDefinitionEntity1);
    createCaseExecutionHistory(historicCaseActivityInstanceEntity);

    // when
    // Set new caseDefinitionId and update
    historicCaseActivityInstanceEntity.setCaseDefinitionId(caseDefinitionEntity2.getId());
    historicCaseActivityInstanceEntity.setEventType(HistoryEventTypes.CASE_ACTIVITY_INSTANCE_UPDATE.getEventName());
    updateCaseExecutionHistory(historicCaseActivityInstanceEntity);

    // then
    // Read from DB and assert
    HistoricCaseActivityInstanceEntity updatedInstance = findHistoricCaseActivityInstance(historicCaseActivityInstanceEntity.getId());
    assertThat(updatedInstance.getCaseDefinitionId()).isEqualTo(caseDefinitionEntity2.getId());

    deleteHistoricCaseActivityInstance(historicCaseActivityInstanceEntity);
    deleteCaseExecution(caseExecutionEntity);
    deleteCaseDefinition(caseDefinitionEntity1);
    deleteCaseDefinition(caseDefinitionEntity2);
  }

  private HistoricCaseActivityInstanceEventEntity prepareHistoricCaseActivityInstance(CaseDefinitionEntity caseDefinitionEntity1) {
    HistoricCaseActivityInstanceEventEntity historicCaseActivityInstanceEntity = new HistoricCaseActivityInstanceEventEntity();
    historicCaseActivityInstanceEntity.setId(UUID.randomUUID().toString());
    historicCaseActivityInstanceEntity.setCaseDefinitionId(caseDefinitionEntity1.getId());
    historicCaseActivityInstanceEntity.setCaseInstanceId(UUID.randomUUID().toString());
    historicCaseActivityInstanceEntity.setCaseActivityId(UUID.randomUUID().toString());
    historicCaseActivityInstanceEntity.setCreateTime(new Date());
    return historicCaseActivityInstanceEntity;
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

  private Void createCaseExecutionHistory(final HistoricCaseActivityInstanceEventEntity historicCaseActivityInstanceEntity) {
    return processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getDbEntityManager().insert(historicCaseActivityInstanceEntity);
        return null;
      }
    });
  }

  private Void updateCaseExecutionHistory(final HistoricCaseActivityInstanceEventEntity historicCaseActivityInstanceEntity) {
    return processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getDbEntityManager().merge(historicCaseActivityInstanceEntity);
        return null;
      }
    });
  }

  private HistoricCaseActivityInstanceEntity findHistoricCaseActivityInstance(final String id) {
    return processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<HistoricCaseActivityInstanceEntity>() {
      @Override
      public HistoricCaseActivityInstanceEntity execute(CommandContext commandContext) {
        return (HistoricCaseActivityInstanceEntity) commandContext.getDbEntityManager().selectOne("selectHistoricCaseActivityInstance", id);
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

  private Void deleteHistoricCaseActivityInstance(final HistoricCaseActivityInstanceEventEntity entity) {
    return processEngineConfiguration.getCommandExecutorTxRequiresNew().execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getHistoricCaseActivityInstanceManager().deleteHistoricCaseActivityInstancesByCaseInstanceIds(singletonList(entity.getCaseInstanceId()));
        return null;
      }
    });
  }
}
