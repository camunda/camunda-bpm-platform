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

import static org.camunda.bpm.engine.impl.util.CompareUtil.areNotInAscendingOrder;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricProcessInstanceReport;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.TenantCheck;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.query.PeriodUnit;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricProcessInstanceReportImpl implements HistoricProcessInstanceReport {

  private static final long serialVersionUID = 1L;

  protected Date startedAfter;
  protected Date startedBefore;
  protected String[] processDefinitionIdIn;
  protected String[] processDefinitionKeyIn;

  protected PeriodUnit durationPeriodUnit;

  protected CommandExecutor commandExecutor;

  protected TenantCheck tenantCheck = new TenantCheck();

  public HistoricProcessInstanceReportImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
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

  public List<DurationReportResult> duration(PeriodUnit periodUnit) {
    ensureNotNull(NotValidException.class, "periodUnit", periodUnit);
    this.durationPeriodUnit = periodUnit;

    CommandContext commandContext = Context.getCommandContext();

    if(commandContext == null) {
      return commandExecutor.execute(new ExecuteDurationReportCmd());
    }
    else {
      return executeDurationReport(commandContext);
    }

  }

  public List<DurationReportResult> executeDurationReport(CommandContext commandContext) {

    doAuthCheck(commandContext);

    if(areNotInAscendingOrder(startedAfter, startedBefore)) {
      return Collections.emptyList();
    }

    return commandContext
      .getHistoricReportManager()
      .selectHistoricProcessInstanceDurationReport(this);

  }

  protected void doAuthCheck(CommandContext commandContext) {
    // since a report does only make sense in context of historic
    // data, the authorization check will be performed here
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      if (processDefinitionIdIn == null && processDefinitionKeyIn == null) {
        checker.checkReadHistoryAnyProcessDefinition();
      } else {
        List<String> processDefinitionKeys = new ArrayList<String>();
        if (processDefinitionKeyIn != null) {
          processDefinitionKeys.addAll(Arrays.asList(processDefinitionKeyIn));
        }

        if (processDefinitionIdIn != null) {
          for (String processDefinitionId : processDefinitionIdIn) {
            ProcessDefinition processDefinition = commandContext.getProcessDefinitionManager()
              .findLatestProcessDefinitionById(processDefinitionId);

            if (processDefinition != null && processDefinition.getKey() != null) {
              processDefinitionKeys.add(processDefinition.getKey());
            }
          }
        }

        if (!processDefinitionKeys.isEmpty()) {
          for (String processDefinitionKey : processDefinitionKeys) {
            checker.checkReadHistoryProcessDefinition(processDefinitionKey);
          }
        }
      }
    }
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

  public TenantCheck getTenantCheck() {
    return tenantCheck;
  }

  public String getReportPeriodUnitName() {
    return durationPeriodUnit.name();
  }

  protected class ExecuteDurationReportCmd implements Command<List<DurationReportResult>> {

    @Override
    public List<DurationReportResult> execute(CommandContext commandContext) {
      return executeDurationReport(commandContext);
    }
  }
}
