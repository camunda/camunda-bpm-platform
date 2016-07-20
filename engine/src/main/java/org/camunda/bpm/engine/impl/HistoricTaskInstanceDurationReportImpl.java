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

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricTaskInstanceDurationReport;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

import java.util.Date;
import java.util.List;

import static org.camunda.bpm.engine.impl.util.CompareUtil.areNotInAscendingOrder;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Stefan Hentschel.
 */
public class HistoricTaskInstanceDurationReportImpl extends AbstractHistoricProcessInstanceReport implements HistoricTaskInstanceDurationReport {

  protected Date completedAfter;
  protected Date completedBefore;

  public HistoricTaskInstanceDurationReportImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public List<DurationReportResult> executeDurationReport(CommandContext commandContext) {
    return commandContext
      .getTaskReportManager()
      .createHistoricTaskDurationReport(this);
  }

  @Override
  protected boolean hasExcludingConditions() {
    return areNotInAscendingOrder(completedAfter, completedBefore);
  }

  public Date getCompletedAfter() {
    return completedAfter;
  }

  public Date getCompletedBefore() {
    return completedBefore;
  }

  @Override
  public HistoricTaskInstanceDurationReport completedAfter(Date completedAfter) {
    ensureNotNull(NotValidException.class, "completedAfter", completedAfter);
    this.completedAfter = completedAfter;
    return this;
  }

  @Override
  public HistoricTaskInstanceDurationReport completedBefore(Date completedBefore) {
    ensureNotNull(NotValidException.class, "completedBefore", completedBefore);
    this.completedBefore = completedBefore;
    return this;
  }
}
