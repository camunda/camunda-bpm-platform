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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.management.Metrics;

/**
 * Batch of work for history cleanup.
 * @author Svetlana Dorokhova.
 */
public class HistoryCleanupBatch extends HistoryCleanupHandler {

  protected List<String> historicProcessInstanceIds = Collections.emptyList();
  protected List<String> historicDecisionInstanceIds = Collections.emptyList();
  protected List<String> historicCaseInstanceIds = Collections.emptyList();
  protected List<String> historicBatchIds = Collections.emptyList();
  protected List<String> taskMetricIds = Collections.emptyList();

  public List<String> getHistoricProcessInstanceIds() {
    return historicProcessInstanceIds;
  }

  public void setHistoricProcessInstanceIds(List<String> historicProcessInstanceIds) {
    this.historicProcessInstanceIds = historicProcessInstanceIds;
  }

  public List<String> getHistoricDecisionInstanceIds() {
    return historicDecisionInstanceIds;
  }

  public void setHistoricDecisionInstanceIds(List<String> historicDecisionInstanceIds) {
    this.historicDecisionInstanceIds = historicDecisionInstanceIds;
  }

  public List<String> getHistoricCaseInstanceIds() {
    return historicCaseInstanceIds;
  }

  public void setHistoricCaseInstanceIds(List<String> historicCaseInstanceIds) {
    this.historicCaseInstanceIds = historicCaseInstanceIds;
  }

  public List<String> getHistoricBatchIds() {
    return historicBatchIds;
  }

  public void setHistoricBatchIds(List<String> historicBatchIds) {
    this.historicBatchIds = historicBatchIds;
  }

  public List<String> getTaskMetricIds() {
    return taskMetricIds;
  }

  public void setTaskMetricIds(List<String> taskMetricIds) {
    this.taskMetricIds = taskMetricIds;
  }

  /**
   * Size of the batch.
   */
  public int size() {
    return historicProcessInstanceIds.size() + historicDecisionInstanceIds.size() + historicCaseInstanceIds.size() + historicBatchIds.size() + taskMetricIds.size();
  }

  public void performCleanup() {
    CommandContext commandContext = Context.getCommandContext();
    HistoryCleanupHelper.prepareNextBatch(this, commandContext);

    if (size() > 0) {
      if (historicProcessInstanceIds.size() > 0) {
        commandContext.getHistoricProcessInstanceManager().deleteHistoricProcessInstanceByIds(historicProcessInstanceIds);
      }
      if (historicDecisionInstanceIds.size() > 0) {
        commandContext.getHistoricDecisionInstanceManager().deleteHistoricDecisionInstanceByIds(historicDecisionInstanceIds);
      }
      if (historicCaseInstanceIds.size() > 0) {
        commandContext.getHistoricCaseInstanceManager().deleteHistoricCaseInstancesByIds(historicCaseInstanceIds);
      }
      if (historicBatchIds.size() > 0) {
        commandContext.getHistoricBatchManager().deleteHistoricBatchesByIds(historicBatchIds);
      }
      if (taskMetricIds.size() > 0) {
        commandContext.getMeterLogManager().deleteTaskMetricsById(taskMetricIds);
      }
    }
  }

  @Override
  protected Map<String, Long> reportMetrics() {
    Map<String, Long> reports = new HashMap<>();

    if (historicProcessInstanceIds.size() > 0) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_PROCESS_INSTANCES, (long) historicProcessInstanceIds.size());
    }
    if (historicDecisionInstanceIds.size() > 0) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_DECISION_INSTANCES, (long) historicDecisionInstanceIds.size());
    }
    if (historicCaseInstanceIds.size() > 0) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_CASE_INSTANCES, (long) historicCaseInstanceIds.size());
    }
    if (historicBatchIds.size() > 0) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_BATCH_OPERATIONS, (long) historicBatchIds.size());
    }
    if (taskMetricIds.size() > 0) {
      reports.put(Metrics.HISTORY_CLEANUP_REMOVED_TASK_METRICS, (long) taskMetricIds.size());
    }

    return reports;
  }

  @Override
  boolean shouldRescheduleNow() {
    return size() >= getBatchSizeThreshold();
  }

  public Integer getBatchSizeThreshold() {
    return Context
        .getProcessEngineConfiguration()
        .getHistoryCleanupBatchThreshold();
  }

}
