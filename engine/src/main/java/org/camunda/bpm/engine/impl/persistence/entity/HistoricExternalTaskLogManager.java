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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.impl.HistoricExternalTaskLogQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.util.EnsureUtil;


public class HistoricExternalTaskLogManager extends AbstractManager {

  // select /////////////////////////////////////////////////////////////////

  public HistoricExternalTaskLogEntity findHistoricExternalTaskLogById(String HistoricExternalTaskLogId) {
    return (HistoricExternalTaskLogEntity) getDbEntityManager().selectOne("selectHistoricExternalTaskLog", HistoricExternalTaskLogId);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricExternalTaskLog> findHistoricExternalTaskLogsByQueryCriteria(HistoricExternalTaskLogQueryImpl query, Page page) {
    configureQuery(query);
    return getDbEntityManager().selectList("selectHistoricExternalTaskLogByQueryCriteria", query, page);
  }

  public long findHistoricExternalTaskLogsCountByQueryCriteria(HistoricExternalTaskLogQueryImpl query) {
    configureQuery(query);
    return (Long) getDbEntityManager().selectOne("selectHistoricExternalTaskLogCountByQueryCriteria", query);
  }

  // update ///////////////////////////////////////////////////////////////////

  public DbOperation addRemovalTimeToExternalTaskLogByRootProcessInstanceId(String rootProcessInstanceId, Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("rootProcessInstanceId", rootProcessInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
      .updatePreserveOrder(HistoricExternalTaskLogEntity.class, "updateExternalTaskLogByRootProcessInstanceId", parameters);
  }

  public DbOperation addRemovalTimeToExternalTaskLogByProcessInstanceId(String processInstanceId, Date removalTime, Integer batchSize) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("processInstanceId", processInstanceId);
    parameters.put("removalTime", removalTime);
    parameters.put("maxResults", batchSize);

    return getDbEntityManager()
      .updatePreserveOrder(HistoricExternalTaskLogEntity.class, "updateExternalTaskLogByProcessInstanceId", parameters);
  }

  // delete ///////////////////////////////////////////////////////////////////

  public void deleteHistoricExternalTaskLogsByProcessInstanceIds(List<String> processInstanceIds) {
    deleteExceptionByteArrayByParameterMap("processInstanceIdIn", processInstanceIds.toArray());
    getDbEntityManager().deletePreserveOrder(HistoricExternalTaskLogEntity.class, "deleteHistoricExternalTaskLogByProcessInstanceIds", processInstanceIds);
  }

  public DbOperation deleteExternalTaskLogByRemovalTime(Date removalTime, int minuteFrom, int minuteTo, int batchSize) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("removalTime", removalTime);
    if (minuteTo - minuteFrom + 1 < 60) {
      parameters.put("minuteFrom", minuteFrom);
      parameters.put("minuteTo", minuteTo);
    }
    parameters.put("batchSize", batchSize);

    return getDbEntityManager()
      .deletePreserveOrder(HistoricExternalTaskLogEntity.class, "deleteExternalTaskLogByRemovalTime",
        new ListQueryParameterObject(parameters, 0, batchSize));
  }

  // byte array delete ////////////////////////////////////////////////////////

  protected void deleteExceptionByteArrayByParameterMap(String key, Object value) {
    EnsureUtil.ensureNotNull(key, value);
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put(key, value);
    getDbEntityManager().delete(ByteArrayEntity.class, "deleteErrorDetailsByteArraysByIds", parameterMap);
  }

  // fire history events ///////////////////////////////////////////////////////

  public void fireExternalTaskCreatedEvent(final ExternalTask externalTask) {
    if (isHistoryEventProduced(HistoryEventTypes.EXTERNAL_TASK_CREATE, externalTask)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createHistoricExternalTaskLogCreatedEvt(externalTask);
        }
      });
    }
  }

  public void fireExternalTaskFailedEvent(final ExternalTask externalTask) {
    if (isHistoryEventProduced(HistoryEventTypes.EXTERNAL_TASK_FAIL, externalTask)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createHistoricExternalTaskLogFailedEvt(externalTask);
        }

        @Override
        public void postHandleSingleHistoryEventCreated(HistoryEvent event) {
          ((ExternalTaskEntity) externalTask).setLastFailureLogId(event.getId());
        }
      });
    }
  }

  public void fireExternalTaskSuccessfulEvent(final ExternalTask externalTask) {
    if (isHistoryEventProduced(HistoryEventTypes.EXTERNAL_TASK_SUCCESS, externalTask)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createHistoricExternalTaskLogSuccessfulEvt(externalTask);
        }
      });
    }
  }

  public void fireExternalTaskDeletedEvent(final ExternalTask externalTask) {
    if (isHistoryEventProduced(HistoryEventTypes.EXTERNAL_TASK_DELETE, externalTask)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createHistoricExternalTaskLogDeletedEvt(externalTask);
        }
      });
    }
  }

  // helper /////////////////////////////////////////////////////////

  protected boolean isHistoryEventProduced(HistoryEventType eventType, ExternalTask externalTask) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    HistoryLevel historyLevel = configuration.getHistoryLevel();
    return historyLevel.isHistoryEventProduced(eventType, externalTask);
  }

  protected void configureQuery(HistoricExternalTaskLogQueryImpl query) {
    getAuthorizationManager().configureHistoricExternalTaskLogQuery(query);
    getTenantManager().configureQuery(query);
  }
}
