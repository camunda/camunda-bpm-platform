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
package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.ClockUtil;


/**
 * @author Askar Akhmerov
 */
public class SetJobRetriesCmd implements Command<Void>, Serializable {

  protected static final long serialVersionUID = 1L;
  protected static final CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected static final String RETRIES = "retries";
  protected static final String DUE_DATE = "dueDate";

  protected final String jobId;
  protected final String jobDefinitionId;
  protected final List<String> jobIds;
  protected final int retries;
  protected Date dueDate;
  protected final boolean isDueDateSet;

  public SetJobRetriesCmd(String jobId, String jobDefinitionId, int retries, Date dueDate, boolean isDueDateSet) {
    this.jobId = jobId;
    this.jobDefinitionId = jobDefinitionId;
    this.jobIds = null;
    this.retries = retries;
    this.dueDate = dueDate;
    this.isDueDateSet = isDueDateSet;
  }

  public SetJobRetriesCmd(List<String> jobIds, int retries, Date dueDate, boolean isDueDateSet) {
    this.jobId = null;
    this.jobDefinitionId = null;
    this.jobIds = jobIds;
    this.retries = retries;
    this.dueDate = dueDate;
    this.isDueDateSet = isDueDateSet;
  }

  public Void execute(CommandContext commandContext) {
    if(dueDate == null && commandContext.getProcessEngineConfiguration().isEnsureJobDueDateNotNull()) {
      dueDate = ClockUtil.getCurrentTime();
    }
    if (jobId != null) {
      setJobRetriesByJobId(jobId, commandContext);
    } else if(jobDefinitionId != null){
      setJobRetriesByJobDefinitionId(commandContext);
    } else if(jobIds != null) {
      for (String id : jobIds) {
        setJobRetriesByJobId(id, commandContext);
      }
    }

    return null;
  }

  protected void setJobRetriesByJobId(String jobId, CommandContext commandContext) {
    JobEntity job = commandContext
        .getJobManager()
        .findJobById(jobId);
    if (job != null) {
      for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
        checker.checkUpdateRetriesJob(job);
      }

      if (job.isInInconsistentLockState()) {
        job.resetLock();
      }

      List<PropertyChange> propertyChanges = new ArrayList<>();

      int oldRetries = job.getRetries();
      job.setRetries(retries);
      propertyChanges.add(new PropertyChange(RETRIES, oldRetries, job.getRetries()));

      if (isDueDateSet) {
        Date oldDueDate = job.getDuedate();
        job.setDuedate(dueDate);
        propertyChanges.add(new PropertyChange(DUE_DATE, oldDueDate, job.getDuedate()));
      }

      commandContext.getOperationLogManager().logJobOperation(getLogEntryOperation(), job.getId(),
          job.getJobDefinitionId(), job.getProcessInstanceId(), job.getProcessDefinitionId(),
          job.getProcessDefinitionKey(), propertyChanges);
    } else {
      throw LOG.exceptionNoJobFoundForId(jobId);
    }
  }

  protected void setJobRetriesByJobDefinitionId(CommandContext commandContext) {
    JobDefinitionManager jobDefinitionManager = commandContext.getJobDefinitionManager();
    JobDefinitionEntity jobDefinition = jobDefinitionManager.findById(jobDefinitionId);

    if (jobDefinition != null) {
      String processDefinitionId = jobDefinition.getProcessDefinitionId();
      for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
        checker.checkUpdateRetriesProcessInstanceByProcessDefinitionId(processDefinitionId);
      }
    }

    commandContext
        .getJobManager()
        .updateFailedJobRetriesByJobDefinitionId(jobDefinitionId, retries, dueDate, isDueDateSet);

    List<PropertyChange> propertyChanges = new ArrayList<>();
    propertyChanges.add(new PropertyChange(RETRIES, null, retries));

    if (isDueDateSet) {
      propertyChanges.add(new PropertyChange(DUE_DATE, null, dueDate));
    }

    commandContext.getOperationLogManager().logJobOperation(getLogEntryOperation(), null, jobDefinitionId, null,
        null, null, propertyChanges);
  }

  protected String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES;
  }
}
