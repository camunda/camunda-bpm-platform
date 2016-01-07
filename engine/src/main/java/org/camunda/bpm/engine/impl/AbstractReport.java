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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.AuthorizationCheck;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.query.Report;
import org.camunda.bpm.engine.query.PeriodUnit;

/**
 * @author Roman Smirnov
 *
 */
public abstract class AbstractReport extends AuthorizationCheck implements Command<Object>, Report, Serializable {

  private static final long serialVersionUID = 1L;

  public static enum ReportType {
    DURATION
  }
  protected transient CommandExecutor commandExecutor;
  protected ReportType reportType;

  protected PeriodUnit reportPeriodUnit;

  protected AbstractReport() {
  }

  protected AbstractReport(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public Object execute(CommandContext commandContext) {
    // since a report does only make sense in context of historic
    // data, the authorization check will be performed here
    commandContext.getAuthorizationManager().checkReadHistoryAnyProcessDefinition();
    return executeDuration(commandContext);
  }

  @SuppressWarnings("unchecked")
  public List<DurationReportResult> duration(PeriodUnit periodUnit) {
    ensureNotNull(NotValidException.class, "periodUnit", periodUnit);
    reportType = ReportType.DURATION;
    reportPeriodUnit = periodUnit;
    if (commandExecutor!=null) {
      return (List<DurationReportResult>) commandExecutor.execute(this);
    }
    return executeDuration(Context.getCommandContext());
  }

  public List<DurationReportResult> executeDuration(CommandContext commandContext) {
    return !hasExcludingConditions() ? executeDurationReport(commandContext) : new ArrayList<DurationReportResult>();
  }

  public abstract List<DurationReportResult> executeDurationReport(CommandContext commandContext);

  /**
   * Whether or not the report query has excluding conditions. If the query has excluding conditions,
   * (e.g. started before and after are excluding), the SQL query is avoided and a default result is
   * returned. The returned result is the same as if the SQL was executed and there were no entries.
   *
   * @return {@code true} if the report query does have excluding conditions, {@code false} otherwise
   */
  protected boolean hasExcludingConditions() {
    return false;
  }

  // getter / setter ///////////////////////////////////////////////////

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public AbstractReport setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    return this;
  }

  public PeriodUnit getReportPeriodUnit() {
    return reportPeriodUnit;
  }

  public String getReportPeriodUnitName() {
    return getReportPeriodUnit().toString();
  }

  public ReportType getReportType() {
    return reportType;
  }

  public String getReportTypeName() {
    return getReportType().toString();
  }

}
