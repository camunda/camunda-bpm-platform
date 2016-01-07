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

import static org.camunda.bpm.engine.impl.util.CompareUtil.areNotInAscendingOrder;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricProcessInstanceReport;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricProcessInstanceReportImpl extends AbstractReport implements HistoricProcessInstanceReport {

  private static final long serialVersionUID = 1L;

  protected Date startedAfter;
  protected Date startedBefore;
  protected String[] processDefinitionIdIn;
  protected String[] processDefinitionKeyIn;

  public HistoricProcessInstanceReportImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  // query parameter ///////////////////////////////////////////////

  public HistoricProcessInstanceReport startedAfter(Date startedAfter) {
    ensureNotNull(NotValidException.class, "startedAfter", startedAfter);
    this.startedAfter = startedAfter;
    return this;
  }

  public HistoricProcessInstanceReport startedBefore(Date startedBefore) {
    ensureNotNull(NotValidException.class, "startedBefore", startedBefore);
    this.startedBefore = startedBefore;
    return this;
  }

  public HistoricProcessInstanceReport processDefinitionIdIn(String... processDefinitionIds) {
    ensureNotNull(NotValidException.class, "", "processDefinitionIdIn", (Object[]) processDefinitionIds);
    this.processDefinitionIdIn = processDefinitionIds;
    return this;
  }

  public HistoricProcessInstanceReport processDefinitionKeyIn(String... processDefinitionKeys) {
    ensureNotNull(NotValidException.class, "", "processDefinitionKeyIn", (Object[]) processDefinitionKeys);
    this.processDefinitionKeyIn = processDefinitionKeys;
    return this;
  }

  // report execution /////////////////////////////////////////////

  @Override
  protected boolean hasExcludingConditions() {
    return areNotInAscendingOrder(startedAfter, startedBefore);
  }

  public List<DurationReportResult> executeDurationReport(CommandContext commandContext) {
    return commandContext
      .getHistoricReportManager()
      .createHistoricProcessInstanceDurationReport(this);
  }

  // getter //////////////////////////////////////////////////////

  public Date getStartedAfter() {
    return startedAfter;
  }

  public Date getStartedBefore() {
    return startedBefore;
  }

  public String[] getProcessDefinitionIdIn() {
    return processDefinitionIdIn;
  }

  public String[] getProcessDefinitionKeyIn() {
    return processDefinitionKeyIn;
  }

}
