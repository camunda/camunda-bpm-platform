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
package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.Metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class HistoryCleanupRemovalTime extends HistoryCleanupHandler {

  protected Map<Class<? extends DbEntity>, DbOperation> deleteOperations;

  protected boolean isDmnEnabled;
  protected boolean isMetricsEnabled;
  protected int batchSizePerDeleteOperation;

  public HistoryCleanupRemovalTime() {
    deleteOperations = new HashMap<>();

    // store information before command context has been closed
    isDmnEnabled = isDmnEnabled();
    isMetricsEnabled = isMetricsEnabled();
    batchSizePerDeleteOperation = getBatchSizePerDeleteOperation();
  }

  public void performCleanup() {
    deleteOperations.putAll(performProcessCleanup());

    if (isDmnEnabled) {
      deleteOperations.putAll(performDmnCleanup());
    }

    DbOperation batchCleanup = performBatchCleanup();
    deleteOperations.put(batchCleanup.getEntityType(), batchCleanup);
  }

  public void execute(CommandContext commandContext) {
    if (isMetricsEnabled) {
      reportMetrics();
    }

    setRescheduleNow(isMaxBatchExceeded());

    super.execute(commandContext);
  }

  protected Map<Class<? extends DbEntity>, DbOperation> performDmnCleanup() {
    return Context.getCommandContext().getHistoricDecisionInstanceManager()
      .deleteHistoricDecisionsByRemovalTime(ClockUtil.getCurrentTime(),
        configuration.getMinuteFrom(), configuration.getMinuteTo(), getBatchSizePerDeleteOperation());
  }

  protected Map<Class<? extends DbEntity>, DbOperation> performProcessCleanup() {
    return Context.getCommandContext().getHistoricProcessInstanceManager()
      .deleteHistoricProcessInstancesByRemovalTime(ClockUtil.getCurrentTime(),
        configuration.getMinuteFrom(), configuration.getMinuteTo(), getBatchSizePerDeleteOperation());
  }

  protected DbOperation performBatchCleanup() {
    return Context.getCommandContext().getHistoricBatchManager()
      .deleteHistoricBatchesByRemovalTime(ClockUtil.getCurrentTime(),
        configuration.getMinuteFrom(), configuration.getMinuteTo(), getBatchSizePerDeleteOperation());
  }

  protected boolean isDmnEnabled() {
    return Context.getProcessEngineConfiguration()
      .isDmnEnabled();
  }

  protected boolean isMetricsEnabled() {
    return Context.getProcessEngineConfiguration()
      .isHistoryCleanupMetricsEnabled();
  }

  protected void reportMetrics() {
    DbOperation deleteOperationProcessInstance = deleteOperations.get(HistoricProcessInstanceEntity.class);
    if (deleteOperationProcessInstance != null) {
      reportValue(Metrics.HISTORY_CLEANUP_REMOVED_PROCESS_INSTANCES, deleteOperationProcessInstance.getRowsAffected());
    }

    if (isDmnEnabled) {
      DbOperation deleteOperationDecisionInstance = deleteOperations.get(HistoricDecisionInstanceEntity.class);
      if (deleteOperationDecisionInstance != null) {
        reportValue(Metrics.HISTORY_CLEANUP_REMOVED_DECISION_INSTANCES, deleteOperationDecisionInstance.getRowsAffected());
      }
    }

    DbOperation deleteOperationBatch = deleteOperations.get(HistoricBatchEntity.class);
    if (deleteOperationBatch != null) {
      reportValue(Metrics.HISTORY_CLEANUP_REMOVED_BATCH_OPERATIONS, deleteOperationBatch.getRowsAffected());
    }
  }

  protected boolean isMaxBatchExceeded() {
    for (DbOperation deleteOperation : deleteOperations.values()) {
      if (deleteOperation.getRowsAffected() == batchSizePerDeleteOperation) {
        return true;
      }
    }

    return false;
  }

  public int getBatchSizePerDeleteOperation() {
    return Context.getProcessEngineConfiguration()
      .getHistoryCleanupBatchSize();
  }

}
