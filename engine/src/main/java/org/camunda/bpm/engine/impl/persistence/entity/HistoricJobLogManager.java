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
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.runtime.Job;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricJobLogManager extends AbstractManager {

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
    getAuthorizationManager().configureHistoricJobLogQuery(query);
    return getDbEntityManager().selectList("selectHistoricJobLogByQueryCriteria", query, page);
  }

  public long findHistoricJobLogsCountByQueryCriteria(HistoricJobLogQueryImpl query) {
    getAuthorizationManager().configureHistoricJobLogQuery(query);
    return (Long) getDbEntityManager().selectOne("selectHistoricJobLogCountByQueryCriteria", query);
  }

  // delete ///////////////////////////////////////////////////////////////////

  public void deleteHistoricJobLogById(String id) {
    deleteExceptionByteArrayByParameterMap("id", id);
    getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogById", id);
  }

  public void deleteHistoricJobLogByJobId(String jobId) {
    deleteExceptionByteArrayByParameterMap("jobId", jobId);
    getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogByJobId", jobId);
  }

  public void deleteHistoricJobLogsByProcessInstanceId(String processInstanceId) {
    deleteExceptionByteArrayByParameterMap("processInstanceId", processInstanceId);
    getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogByProcessInstanceId", processInstanceId);
  }

  public void deleteHistoricJobLogsByProcessDefinitionId(String processDefinitionId) {
    deleteExceptionByteArrayByParameterMap("processDefinitionId", processDefinitionId);
    getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogByProcessDefinitionId", processDefinitionId);
  }

  public void deleteHistoricJobLogsByDeploymentId(String deploymentId) {
    deleteExceptionByteArrayByParameterMap("deploymentId", deploymentId);
    getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogByDeploymentId", deploymentId);
  }

  public void deleteHistoricJobLogsByHandlerType(String handlerType) {
    deleteExceptionByteArrayByParameterMap("handlerType", handlerType);
    getDbEntityManager().delete(HistoricJobLogEventEntity.class, "deleteHistoricJobLogByHandlerType", handlerType);
  }

  // byte array delete ////////////////////////////////////////////////////////

  protected void deleteExceptionByteArrayByParameterMap(String key, String value) {
    EnsureUtil.ensureNotNull(key, value);
    Map<String, String> parameterMap = new HashMap<String, String>();
    parameterMap.put(key, value);
    getDbEntityManager().delete(ByteArrayEntity.class, "deleteExceptionByteArraysByIds", parameterMap);
  }

  // fire history events ///////////////////////////////////////////////////////

  public void fireJobCreatedEvent(Job job) {
    if (isHistoryEventProduced(HistoryEventTypes.JOB_CREATE, job)) {
      HistoryEventProducer eventProducer = getHistoryEventProducer();
      HistoryEvent event = eventProducer.createHistoricJobLogCreateEvt(job);
      handleEvent(event);
    }
  }

  public void fireJobFailedEvent(Job job, Throwable exception) {
    if (isHistoryEventProduced(HistoryEventTypes.JOB_FAIL, job)) {
      HistoryEventProducer eventProducer = getHistoryEventProducer();
      HistoryEvent event = eventProducer.createHistoricJobLogFailedEvt(job, exception);
      handleEvent(event);
    }
  }

  public void fireJobSuccessfulEvent(Job job) {
    if (isHistoryEventProduced(HistoryEventTypes.JOB_SUCCESS, job)) {
      HistoryEventProducer eventProducer = getHistoryEventProducer();
      HistoryEvent event = eventProducer.createHistoricJobLogSuccessfulEvt(job);
      handleEvent(event);
    }
  }

  public void fireJobDeletedEvent(Job job) {
    if (isHistoryEventProduced(HistoryEventTypes.JOB_DELETE, job)) {
      HistoryEventProducer eventProducer = getHistoryEventProducer();
      HistoryEvent event = eventProducer.createHistoricJobLogDeleteEvt(job);
      handleEvent(event);
    }
  }

  protected void handleEvent(HistoryEvent event) {
    HistoryEventHandler eventHandler = getHistoryEventHandler();
    eventHandler.handleEvent(event);
  }

  // helper /////////////////////////////////////////////////////////

  protected HistoryEventProducer getHistoryEventProducer() {
    return Context.getProcessEngineConfiguration().getHistoryEventProducer();
  }

  protected HistoryEventHandler getHistoryEventHandler() {
    return Context.getProcessEngineConfiguration().getHistoryEventHandler();
  }

  protected boolean isHistoryEventProduced(HistoryEventType eventType, Job job) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    HistoryLevel historyLevel = configuration.getHistoryLevel();
    return historyLevel.isHistoryEventProduced(eventType, job);
  }

}
