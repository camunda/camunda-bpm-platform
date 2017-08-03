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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.HistoricJobLogQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.AbstractHistoricManager;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.runtime.Job;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricJobLogManager extends AbstractHistoricManager {

  // select /////////////////////////////////////////////////////////////////

  public HistoricJobLogEventEntity findHistoricJobLogById(String historicJobLogId) {
    return (HistoricJobLogEventEntity) getDbEntityManager().selectOne("selectHistoricJobLog", historicJobLogId);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricJobLog> findHistoricJobLogsByDeploymentId(String deploymentId) {
    return getDbEntityManager().selectList("selectHistoricJobLogByDeploymentId", deploymentId);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricJobLog> findHistoricJobLogsByQueryCriteria(HistoricJobLogQueryImpl query, Page page) {
    configureQuery(query);
    return getDbEntityManager().selectList("selectHistoricJobLogByQueryCriteria", query, page);
  }

  public long findHistoricJobLogsCountByQueryCriteria(HistoricJobLogQueryImpl query) {
    configureQuery(query);
    return (Long) getDbEntityManager().selectOne("selectHistoricJobLogCountByQueryCriteria", query);
  }

  // delete ///////////////////////////////////////////////////////////////////

  public void deleteHistoricJobLogById(String id) {
    if (isHistoryEnabled()) {
      deleteExceptionByteArrayByParameterMap("id", id);
      getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogById", id);
    }
  }

  public void deleteHistoricJobLogByJobId(String jobId) {
    if (isHistoryEnabled()) {
      deleteExceptionByteArrayByParameterMap("jobId", jobId);
      getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogByJobId", jobId);
    }
  }

  public void deleteHistoricJobLogsByProcessInstanceIds(List<String> processInstanceIds) {
    deleteExceptionByteArrayByParameterMap("processInstanceIdIn", processInstanceIds.toArray());
    getDbEntityManager().deletePreserveOrder(HistoricJobLogEventEntity.class, "deleteHistoricJobLogByProcessInstanceIds", processInstanceIds);
  }

  public void deleteHistoricJobLogsByProcessDefinitionId(String processDefinitionId) {
    if (isHistoryEnabled()) {
      deleteExceptionByteArrayByParameterMap("processDefinitionId", processDefinitionId);
      getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogByProcessDefinitionId", processDefinitionId);
    }
  }

  public void deleteHistoricJobLogsByDeploymentId(String deploymentId) {
    if (isHistoryEnabled()) {
      deleteExceptionByteArrayByParameterMap("deploymentId", deploymentId);
      getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogByDeploymentId", deploymentId);
    }
  }

  public void deleteHistoricJobLogsByHandlerType(String handlerType) {
    if (isHistoryEnabled()) {
      deleteExceptionByteArrayByParameterMap("handlerType", handlerType);
      getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogByHandlerType", handlerType);
    }
  }

  public void deleteHistoricJobLogsByJobDefinitionId(String jobDefinitionId) {
    if (isHistoryEnabled()) {
      deleteExceptionByteArrayByParameterMap("jobDefinitionId", jobDefinitionId);
      getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogByJobDefinitionId", jobDefinitionId);
    }
  }

  public void deleteHistoricJobLogByBatchIds(List<String> historicBatchIds) {
    if (isHistoryEnabled()) {
      deleteExceptionByteArrayByParameterMap("historicBatchIdIn", historicBatchIds);
      getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogByBatchIds", historicBatchIds);
    }
  }

  // byte array delete ////////////////////////////////////////////////////////

  protected void deleteExceptionByteArrayByParameterMap(String key, Object value) {
    EnsureUtil.ensureNotNull(key, value);
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put(key, value);
    getDbEntityManager().delete(ByteArrayEntity.class, "deleteExceptionByteArraysByIds", parameterMap);
  }

  // fire history events ///////////////////////////////////////////////////////

  public void fireJobCreatedEvent(final Job job) {
    if (isHistoryEventProduced(HistoryEventTypes.JOB_CREATE, job)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createHistoricJobLogCreateEvt(job);
        }
      });
    }
  }

  public void fireJobFailedEvent(final Job job, final Throwable exception) {
    if (isHistoryEventProduced(HistoryEventTypes.JOB_FAIL, job)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createHistoricJobLogFailedEvt(job, exception);
        }
      });
    }
  }

  public void fireJobSuccessfulEvent(final Job job) {
    if (isHistoryEventProduced(HistoryEventTypes.JOB_SUCCESS, job)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createHistoricJobLogSuccessfulEvt(job);
        }
      });
    }
  }

  public void fireJobDeletedEvent(final Job job) {
    if (isHistoryEventProduced(HistoryEventTypes.JOB_DELETE, job)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createHistoricJobLogDeleteEvt(job);
        }
      });
    }
  }


  // helper /////////////////////////////////////////////////////////

  protected boolean isHistoryEventProduced(HistoryEventType eventType, Job job) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    HistoryLevel historyLevel = configuration.getHistoryLevel();
    return historyLevel.isHistoryEventProduced(eventType, job);
  }

  protected void configureQuery(HistoricJobLogQueryImpl query) {
    getAuthorizationManager().configureHistoricJobLogQuery(query);
    getTenantManager().configureQuery(query);
  }

}
