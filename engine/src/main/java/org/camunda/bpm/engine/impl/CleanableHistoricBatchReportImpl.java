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

package org.camunda.bpm.engine.impl;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.history.CleanableHistoricBatchReport;
import org.camunda.bpm.engine.history.CleanableHistoricBatchReportResult;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHelper;

public class CleanableHistoricBatchReportImpl extends AbstractQuery<CleanableHistoricBatchReport, CleanableHistoricBatchReportResult> implements CleanableHistoricBatchReport {

  private static final long serialVersionUID = 1L;

  protected Date currentTimestamp;

  public CleanableHistoricBatchReportImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    checkPermissions(commandContext);

    return commandContext.getHistoricBatchManager().findCleanableHistoricBatchesReportCountByCriteria(this, HistoryCleanupHelper.getBatchOperationsForHistoryCleanup(commandContext));
  }

  @Override
  public List<CleanableHistoricBatchReportResult> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    checkPermissions(commandContext);

    return commandContext.getHistoricBatchManager().findCleanableHistoricBatchesReportByCriteria(this, page, HistoryCleanupHelper.getBatchOperationsForHistoryCleanup(commandContext));
  }

  public Date getCurrentTimestamp() {
    return currentTimestamp;
  }

  public void setCurrentTimestamp(Date currentTimestamp) {
    this.currentTimestamp = currentTimestamp;
  }

  private void checkPermissions(CommandContext commandContext) {
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadHistoricBatch();
    }
  }

}
