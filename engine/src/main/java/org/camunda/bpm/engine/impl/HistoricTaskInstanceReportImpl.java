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
import org.camunda.bpm.engine.history.HistoricTaskInstanceReport;
import org.camunda.bpm.engine.history.TaskReportResult;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.TenantCheck;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.camunda.bpm.engine.impl.util.CompareUtil.areNotInAscendingOrder;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Stefan Hentschel.
 */
public class HistoricTaskInstanceReportImpl implements Command<Object>, Serializable, HistoricTaskInstanceReport {

  private static final long serialVersionUID = 1L;

  protected transient CommandExecutor commandExecutor;

  protected TenantCheck tenantCheck = new TenantCheck();

  protected Date completedAfter;
  protected Date completedBefore;
  protected Boolean groupByProcessDefinitionKey;

  public HistoricTaskInstanceReportImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public HistoricTaskInstanceReport completedAfter(Date completedAfter) {
    ensureNotNull(NotValidException.class, "completedAfter", completedAfter);
    this.completedAfter = completedAfter;
    return this;
  }

  public HistoricTaskInstanceReport completedBefore(Date completedBefore) {
    ensureNotNull(NotValidException.class, "completedBefore", completedBefore);
    this.completedBefore = completedBefore;
    return this;
  }

  public HistoricTaskInstanceReport groupByProcessDefinitionKey() {
      this.groupByProcessDefinitionKey = true;
      return this;
  }

  protected boolean hasExcludingConditions() {
    return areNotInAscendingOrder(completedAfter, completedBefore);
  }

  public List<TaskReportResult> executeTask(CommandContext commandContext) {
    return !hasExcludingConditions() ? executeTaskReport(commandContext) : new ArrayList<TaskReportResult>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<TaskReportResult> taskReport() {
    if (commandExecutor!=null) {
      return (List<TaskReportResult>) commandExecutor.execute(this);
    }
    return executeTask(Context.getCommandContext());
  }

  public List<TaskReportResult> executeTaskReport(CommandContext commandContext) {
    return commandContext
      .getTaskReportManager()
      .createHistoricTaskReport(this);
  }

  @SuppressWarnings("unchecked")
  public Object execute(CommandContext commandContext) {
    // since a report does only make sense in context of historic
    // data, the authorization check will be performed here
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadHistoryAnyProcessDefinition();
    }
    return executeTask(commandContext);
  }

  public Boolean isGroupedByProcessDefinitionKey() {
    if(groupByProcessDefinitionKey != null) {
      return groupByProcessDefinitionKey;
    }

    return false;
  }

  public Boolean isGroupedByProcessDefinitionKeyInternal() {
    return groupByProcessDefinitionKey;
  }

  public TenantCheck getTenantCheck() {
    return tenantCheck;
  }
}
