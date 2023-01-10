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
import org.camunda.bpm.engine.impl.cmd.SetJobsRetriesBatchCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.SetJobRetriesAsyncBuilder;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

public class SetJobRetriesAsyncBuilderImpl implements SetJobRetriesAsyncBuilder {

  protected final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected final CommandExecutor commandExecutor;

  protected JobQuery jobQuery;
  protected List<String> processInstanceIds;
  protected ProcessInstanceQuery processInstanceQuery;
  protected HistoricProcessInstanceQuery historicProcessInstanceQuery;
  protected List<String> jobIds;
  protected Integer retries;
  protected Date dueDate;

  protected boolean byJobs = false;
  protected boolean byProcess = false;

  public SetJobRetriesAsyncBuilderImpl(CommandExecutor commandExecutor, int retries) {
    this.commandExecutor = commandExecutor;
    this.retries = retries;
  }

  @Override
  public SetJobRetriesAsyncBuilder jobQuery(JobQuery query) {
    this.jobQuery = query;
    byJobs = true;
    return this;
  }

  @Override
  public SetJobRetriesAsyncBuilder processInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
    byProcess = true;
    return this;
  }

  @Override
  public SetJobRetriesAsyncBuilder processInstanceQuery(ProcessInstanceQuery query) {
    this.processInstanceQuery = query;
    byProcess = true;
    return this;
  }

  @Override
  public SetJobRetriesAsyncBuilder historicProcessInstanceQuery(HistoricProcessInstanceQuery query) {
    this.historicProcessInstanceQuery = query;
    byProcess = true;
    return this;
  }

  @Override
  public SetJobRetriesAsyncBuilder jobIds(List<String> jobIds) {
    this.jobIds = jobIds;
    byJobs = true;
    return this;
  }

  @Override
  public SetJobRetriesAsyncBuilder dueDate(Date dueDate) {
    this.dueDate = dueDate;
    return this;
  }

  @Override
  public Batch execute() {
    validateParameters();
    if(byJobs) {
      return commandExecutor.execute(new SetJobsRetriesBatchCmd(jobIds, jobQuery, retries, dueDate));
    } else if(byProcess) {
      return commandExecutor.execute(new SetJobsRetriesByProcessBatchCmd(processInstanceIds, processInstanceQuery,
          historicProcessInstanceQuery, retries, dueDate));
    }
    // should not happen, validate method throws exception in this case
    return null;
  }

  protected void validateParameters() {
    ensureNotNull("commandExecutor", commandExecutor);
    ensureNotNull("retries", retries);

    if(!byJobs && !byProcess) {
      throw LOG.exceptionSettingJobRetriesAsyncNoJobsSpecified();
    } else if(byJobs && byProcess) {
      throw LOG.exceptionSettingJobRetriesAsyncJobsNotSpecifiedCorrectly();
    }
  }
}
