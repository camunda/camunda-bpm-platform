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
package org.camunda.bpm.engine.impl.management;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.SetJobsRetriesByProcessBatchCmd;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.SetJobRetriesByProcessAsyncBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

public class SetJobRetriesByProcessAsyncBuilderImpl implements SetJobRetriesByProcessAsyncBuilder {

  protected final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected final CommandExecutor commandExecutor;

  protected List<String> processInstanceIds;
  protected ProcessInstanceQuery processInstanceQuery;
  protected HistoricProcessInstanceQuery historicProcessInstanceQuery;
  protected Integer retries;
  protected Date dueDate;
  protected boolean isDueDateSet;

  public SetJobRetriesByProcessAsyncBuilderImpl(CommandExecutor commandExecutor, int retries) {
    this.commandExecutor = commandExecutor;
    this.retries = retries;
  }

  @Override
  public SetJobRetriesByProcessAsyncBuilder processInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  @Override
  public SetJobRetriesByProcessAsyncBuilder processInstanceQuery(ProcessInstanceQuery query) {
    this.processInstanceQuery = query;
    return this;
  }

  @Override
  public SetJobRetriesByProcessAsyncBuilder historicProcessInstanceQuery(HistoricProcessInstanceQuery query) {
    this.historicProcessInstanceQuery = query;
    return this;
  }

  @Override
  public SetJobRetriesByProcessAsyncBuilder dueDate(Date dueDate) {
    this.dueDate = dueDate;
    isDueDateSet = true;
    return this;
  }

  @Override
  public Batch executeAsync() {
    validateParameters();
    return commandExecutor.execute(new SetJobsRetriesByProcessBatchCmd(processInstanceIds, processInstanceQuery,
        historicProcessInstanceQuery, retries, dueDate, isDueDateSet));
  }

  protected void validateParameters() {
    ensureNotNull("commandExecutor", commandExecutor);
    ensureNotNull("retries", retries);

    boolean isProcessInstanceIdsNull = processInstanceIds == null || processInstanceIds.isEmpty();
    boolean isProcessInstanceQueryNull = processInstanceQuery == null;
    boolean isHistoricProcessInstanceQueryNull = historicProcessInstanceQuery == null;

    if(isProcessInstanceIdsNull && isProcessInstanceQueryNull && isHistoricProcessInstanceQueryNull) {
      throw LOG.exceptionSettingJobRetriesAsyncNoProcessesSpecified();
    }
  }
}
