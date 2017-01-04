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

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.impl.HistoricExternalTaskLogQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

  // delete ///////////////////////////////////////////////////////////////////

  public void deleteHistoricExternalTaskLogsByProcessInstanceId(String processInstanceId) {
    deleteExceptionByteArrayByParameterMap("processInstanceId", processInstanceId);
    getDbEntityManager().delete(HistoricExternalTaskLogEntity.class, "deleteHistoricExternalTaskLogByProcessInstanceId", processInstanceId);
  }

  // byte array delete ////////////////////////////////////////////////////////

  protected void deleteExceptionByteArrayByParameterMap(String key, String value) {
    EnsureUtil.ensureNotNull(key, value);
    Map<String, String> parameterMap = new HashMap<String, String>();
    parameterMap.put(key, value);
    getDbEntityManager().delete(ByteArrayEntity.class, "deleteErrorDetailsByteArraysByIds", parameterMap);
  }

  // fire history events ///////////////////////////////////////////////////////

  public void fireExternalTaskCreatedEvent(final ExternalTask externalTask) {
    HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
      @Override
      public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
        return producer.createHistoricExternalTaskLogCreatedEvt(externalTask);
      }
    });
  }

  public void fireExternalTaskFailedEvent(final ExternalTask externalTask) {
    HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
      @Override
      public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
        return producer.createHistoricExternalTaskLogFailedEvt(externalTask);
      }
    });
  }

  public void fireExternalTaskSuccessfulEvent(final ExternalTask externalTask) {
    HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
      @Override
      public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
        return producer.createHistoricExternalTaskLogSuccessfulEvt(externalTask);
      }
    });
  }

  public void fireExternalTaskDeletedEvent(final ExternalTask externalTask) {
    HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
      @Override
      public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
        return producer.createHistoricExternalTaskLogDeletedEvt(externalTask);
      }
    });
  }

  // helper /////////////////////////////////////////////////////////

  protected void configureQuery(HistoricExternalTaskLogQueryImpl query) {
    getAuthorizationManager().configureHistoricExternalTaskLogQuery(query);
    getTenantManager().configureQuery(query);
  }
}
