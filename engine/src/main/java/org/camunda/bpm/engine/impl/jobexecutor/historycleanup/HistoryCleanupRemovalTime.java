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
package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskMeterLogEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.Metrics;

/**
 * @author Tassilo Weidner
 */
public class HistoryCleanupRemovalTime extends HistoryCleanupHandler {

  protected Map<Class<? extends DbEntity>, DbOperation> deleteOperations = new HashMap<>();

  public void performCleanup() {
    deleteOperations.putAll(performProcessCleanup());

    if (isDmnEnabled()) {
      deleteOperations.putAll(performDmnCleanup());
    }

    DbOperation batchCleanup = performBatchCleanup();
    deleteOperations.put(batchCleanup.getEntityType(), batchCleanup);

    if (getTaskMetricsTimeToLive() != null) {
      DbOperation taskMetricsCleanup = performTaskMetricsCleanup();
      deleteOperations.put(taskMetricsCleanup.getEntityType(), taskMetricsCleanup);
    }
  }

  protected Map<Class<? extends DbEntity>, DbOperation> performDmnCleanup() {
    return Context
        .getCommandContext()
        .getHistoricDecisionInstanceManager()
        .deleteHistoricDecisionsByRemovalTime(ClockUtil.getCurrentTime(),
            configuration.getMinuteFrom(), configuration.getMinuteTo(), getBatchSize());
  }

  protected Map<Class<? extends DbEntity>, DbOperation> performProcessCleanup() {
    return Context
        .getCommandContext()
        .getHistoricProcessInstanceManager()
        .deleteHistoricProcessInstancesByRemovalTime(ClockUtil.getCurrentTime(),
            configuration.getMinuteFrom(), configuration.getMinuteTo(), getBatchSize());
  }

  protected DbOperation performBatchCleanup() {
    return Context
        .getCommandContext()
        .getHistoricBatchManager()
        .deleteHistoricBatchesByRemovalTime(ClockUtil.getCurrentTime(),
            configuration.getMinuteFrom(), configuration.getMinuteTo(), getBatchSize());
  }

  protected DbOperation performTaskMetricsCleanup() {
    return Context
        .getCommandContext()
        .getMeterLogManager()
        .deleteTaskMetricsByRemovalTime(ClockUtil.getCurrentTime(), getTaskMetricsTimeToLive(),
            configuration.getMinuteFrom(), configuration.getMinuteTo(), getBatchSize());
  }

  protected Map<String, Long> reportMetrics() {
    Map<String, Long> reports = new HashMap<>();

    DbOperation deleteOperationProcessInstance = deleteOperations.get(HistoricProcessInstanceEntity.class);
    if (deleteOperationProcessInstance != null) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_PROCESS_INSTANCES, (long) deleteOperationProcessInstance.getRowsAffected());
    }

    DbOperation deleteOperationDecisionInstance = deleteOperations.get(HistoricDecisionInstanceEntity.class);
    if (deleteOperationDecisionInstance != null) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_DECISION_INSTANCES, (long) deleteOperationDecisionInstance.getRowsAffected());
    }

    DbOperation deleteOperationBatch = deleteOperations.get(HistoricBatchEntity.class);
    if (deleteOperationBatch != null) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_BATCH_OPERATIONS, (long) deleteOperationBatch.getRowsAffected());
    }

    DbOperation deleteOperationTaskMetric = deleteOperations.get(TaskMeterLogEntity.class);
    if (deleteOperationTaskMetric != null) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_TASK_METRICS, (long) deleteOperationTaskMetric.getRowsAffected());
    }

    return reports;
  }

  protected boolean isDmnEnabled() {
    return Context
        .getProcessEngineConfiguration()
        .isDmnEnabled();
  }

  protected Integer getTaskMetricsTimeToLive() {
    return Context
        .getProcessEngineConfiguration()
        .getParsedTaskMetricsTimeToLive();
  }

  protected boolean shouldRescheduleNow() {
    int batchSize = getBatchSize();

    for (DbOperation deleteOperation : deleteOperations.values()) {
      if (deleteOperation.getRowsAffected() == batchSize) {
        return true;
      }
    }

    return false;
  }

  public int getBatchSize() {
    return Context
        .getProcessEngineConfiguration()
        .getHistoryCleanupBatchSize();
  }

}
