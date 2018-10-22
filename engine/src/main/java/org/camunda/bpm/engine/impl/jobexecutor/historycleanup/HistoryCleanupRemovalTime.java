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

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.operation.DbOperation;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.ClockUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Tassilo Weidner
 */
public class HistoryCleanupRemovalTime extends HistoryCleanupHandler {

  protected Set<DbOperation> deleteOperations;

  public HistoryCleanupRemovalTime() {
    deleteOperations = new HashSet<>();
  }

  public void performCleanup() {
    deleteOperations.addAll(performProcessCleanup());

    if (isDmnEnabled()) {
      deleteOperations.addAll(performDmnCleanup());
    }
  }

  public void execute(CommandContext commandContext) {
    setRescheduleNow(isMaxBatchExceeded());

    super.execute(commandContext);
  }

  protected Set<DbOperation> performDmnCleanup() {
    return Context.getCommandContext().getHistoricDecisionInstanceManager()
      .deleteHistoricDecisionsByRemovalTime(ClockUtil.getCurrentTime(),
        configuration.getMinuteFrom(), configuration.getMinuteTo(), getBatchSizePerDeleteOperation());
  }

  protected Set<DbOperation> performProcessCleanup() {
    return Context.getCommandContext().getHistoricProcessInstanceManager()
      .deleteHistoricProcessInstancesByRemovalTime(ClockUtil.getCurrentTime(),
        configuration.getMinuteFrom(), configuration.getMinuteTo(), getBatchSizePerDeleteOperation());
  }

  protected boolean isDmnEnabled() {
    return Context.getCommandContext().getProcessEngineConfiguration()
      .isDmnEnabled();
  }

  protected boolean isMaxBatchExceeded() {
    for (DbOperation deleteOperation : deleteOperations) {
      if (deleteOperation.getRowsAffected() == getBatchSizePerDeleteOperation()) {
        return true;
      }
    }

    return false;
  }

  public int getBatchSizePerDeleteOperation() {
    return Context.getCommandContext().getProcessEngineConfiguration()
      .getHistoryCleanupBatchSize();
  }

}
