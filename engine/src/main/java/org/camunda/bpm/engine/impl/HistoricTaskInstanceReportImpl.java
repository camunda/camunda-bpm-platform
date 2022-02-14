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
package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricTaskInstanceReport;
import org.camunda.bpm.engine.history.HistoricTaskInstanceReportResult;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.TenantCheck;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.query.PeriodUnit;

/**
 * @author Stefan Hentschel.
 */
public class HistoricTaskInstanceReportImpl implements HistoricTaskInstanceReport {

  protected Date completedAfter;
  protected Date completedBefore;

  protected PeriodUnit durationPeriodUnit;

  protected CommandExecutor commandExecutor;

  protected TenantCheck tenantCheck = new TenantCheck();

  public HistoricTaskInstanceReportImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  @Override
  public List<HistoricTaskInstanceReportResult> countByProcessDefinitionKey() {
    CommandContext commandContext = Context.getCommandContext();

    if(commandContext == null) {
      return commandExecutor.execute(new HistoricTaskInstanceCountByProcessDefinitionKey());
    }
    else {
      return executeCountByProcessDefinitionKey(commandContext);
    }
  }

  protected List<HistoricTaskInstanceReportResult> executeCountByProcessDefinitionKey(CommandContext commandContext) {
    return commandContext.getTaskReportManager()
      .selectHistoricTaskInstanceCountByProcDefKeyReport(this);
  }

  @Override
  public List<HistoricTaskInstanceReportResult> countByTaskName() {
    CommandContext commandContext = Context.getCommandContext();

    if(commandContext == null) {
      return commandExecutor.execute(new HistoricTaskInstanceCountByNameCmd());
    }
    else {
      return executeCountByTaskName(commandContext);
    }
  }

  protected List<HistoricTaskInstanceReportResult> executeCountByTaskName(CommandContext commandContext) {
    return commandContext.getTaskReportManager()
      .selectHistoricTaskInstanceCountByTaskNameReport(this);
  }

  @Override
  public List<DurationReportResult> duration(PeriodUnit periodUnit) {
    ensureNotNull(NotValidException.class, "periodUnit", periodUnit);
    this.durationPeriodUnit = periodUnit;

    CommandContext commandContext = Context.getCommandContext();

    if(commandContext == null) {
      return commandExecutor.execute(new ExecuteDurationCmd());
    }
    else {
      return executeDuration(commandContext);
    }
  }


  protected List<DurationReportResult> executeDuration(CommandContext commandContext) {
    return commandContext.getTaskReportManager()
      .createHistoricTaskDurationReport(this);
  }

  public Date getCompletedAfter() {
    return completedAfter;
  }

  public Date getCompletedBefore() {
    return completedBefore;
  }

  @Override
  public HistoricTaskInstanceReport completedAfter(Date completedAfter) {
    ensureNotNull(NotValidException.class, "completedAfter", completedAfter);
    this.completedAfter = completedAfter;
    return this;
  }

  @Override
  public HistoricTaskInstanceReport completedBefore(Date completedBefore) {
    ensureNotNull(NotValidException.class, "completedBefore", completedBefore);
    this.completedBefore = completedBefore;
    return this;
  }

  public TenantCheck getTenantCheck() {
    return tenantCheck;
  }

  public String getReportPeriodUnitName() {
    return durationPeriodUnit.name();
  }

  protected class ExecuteDurationCmd implements Command<List<DurationReportResult>> {

    @Override
    public List<DurationReportResult> execute(CommandContext commandContext) {
      return executeDuration(commandContext);
    }
  }

  protected class HistoricTaskInstanceCountByNameCmd implements Command<List<HistoricTaskInstanceReportResult>> {

    @Override
    public List<HistoricTaskInstanceReportResult> execute(CommandContext commandContext) {
      return executeCountByTaskName(commandContext);
    }
  }

  protected class HistoricTaskInstanceCountByProcessDefinitionKey implements Command<List<HistoricTaskInstanceReportResult>> {

    @Override
    public List<HistoricTaskInstanceReportResult> execute(CommandContext commandContext) {
      return executeCountByProcessDefinitionKey(commandContext);
    }
  }

}
