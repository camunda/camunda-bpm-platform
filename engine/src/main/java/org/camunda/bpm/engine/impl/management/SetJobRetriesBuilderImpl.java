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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.cmd.SetJobRetriesCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.SetJobRetriesBuilder;

public class SetJobRetriesBuilderImpl implements SetJobRetriesBuilder {

  protected final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected final CommandExecutor commandExecutor;

  protected String jobId;
  protected List<String> jobIds;
  protected String jobDefinitionId;
  protected Integer retries;
  protected Date dueDate;

  protected boolean isDueDateSet = false;
  boolean byJobId = false;
  boolean byJobIds = false;
  boolean byJobDefinitionId = false;

  public SetJobRetriesBuilderImpl(CommandExecutor commandExecutor, int retries) {
    this.commandExecutor = commandExecutor;
    this.retries = retries;
  }

  @Override
  public SetJobRetriesBuilder jobId(String jobId) {
    this.jobId = jobId;
    byJobId = true;
    return this;
  }

  @Override
  public SetJobRetriesBuilder jobIds(List<String> jobIds) {
    this.jobIds = jobIds;
    byJobIds = true;
    return this;
  }

  @Override
  public SetJobRetriesBuilder jobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
    byJobDefinitionId = true;
    return this;
  }

  @Override
  public SetJobRetriesBuilder dueDate(Date dueDate) {
    isDueDateSet = true;
    this.dueDate = dueDate;
    return this;
  }

  @Override
  public void execute() {
    validateParameters();

    if (byJobId || byJobDefinitionId) {
      commandExecutor.execute(new SetJobRetriesCmd(jobId, jobDefinitionId, retries, dueDate, isDueDateSet));
    } else if (byJobIds) {
      commandExecutor.execute(new SetJobRetriesCmd(jobIds, retries, dueDate, isDueDateSet));
    }

  }

  protected void validateParameters() {
    ensureNotNull("commandExecutor", commandExecutor);
    ensureNotNull("retries", retries);
    if (retries < 0) {
      throw LOG.exceptionJobRetriesMustNotBeNegative(retries);
    }

    if (!(byJobId ^ byJobIds ^ byJobDefinitionId)) {
      // more than one or no method specified
      throw LOG.exceptionSettingJobRetriesJobsNotSpecifiedCorrectly();
    }

    if(byJobId || byJobDefinitionId) {
      if ((jobId == null || jobId.isEmpty()) && (jobDefinitionId == null || jobDefinitionId.isEmpty())) {
        throw LOG.exceptionSettingJobRetriesJobsNotSpecifiedCorrectly();
      }
    } else if(byJobIds) {
      ensureNotEmpty("job ids", jobIds);
    }

  }
}