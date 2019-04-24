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
package org.camunda.bpm.engine.impl.history.producer;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricCaseInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * @author Sebastian Menski
 */
public class CacheAwareCmmnHistoryEventProducer extends DefaultCmmnHistoryEventProducer {

  @Override
  protected HistoricCaseInstanceEventEntity loadCaseInstanceEventEntity(CaseExecutionEntity caseExecutionEntity) {
    final String caseInstanceId = caseExecutionEntity.getCaseInstanceId();

    HistoricCaseInstanceEventEntity cachedEntity = findInCache(HistoricCaseInstanceEventEntity.class, caseInstanceId);

    if (cachedEntity != null) {
      return cachedEntity;
    }
    else {
      return newCaseInstanceEventEntity(caseExecutionEntity);
    }

  }

  @Override
  protected HistoricCaseActivityInstanceEventEntity loadCaseActivityInstanceEventEntity(CaseExecutionEntity caseExecutionEntity) {
    final String caseActivityInstanceId = caseExecutionEntity.getId();

    HistoricCaseActivityInstanceEventEntity cachedEntity = findInCache(HistoricCaseActivityInstanceEventEntity.class, caseActivityInstanceId);

    if (cachedEntity != null) {
      return cachedEntity;
    }
    else {
      return newCaseActivityInstanceEventEntity(caseExecutionEntity);
    }

  }

  /** find a cached entity by primary key */
  protected <T extends HistoryEvent> T findInCache(Class<T> type, String id) {
    return Context.getCommandContext()
      .getDbEntityManager()
      .getCachedEntity(type, id);
  }

}
