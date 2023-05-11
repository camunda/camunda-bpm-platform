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
import java.util.Collections;
import java.util.Date;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;


/**
 * @author Kristin Polenz
 */
public class SetJobDuedateCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  private final String jobId;
  private Date newDuedate;
  private final boolean cascade;

  public SetJobDuedateCmd(String jobId, Date newDuedate, boolean cascade) {
    if (jobId == null || jobId.length() < 1) {
      throw new ProcessEngineException("The job id is mandatory, but '" + jobId + "' has been provided.");
    }
    this.jobId = jobId;
    this.newDuedate = newDuedate;
    this.cascade = cascade;
  }

  public Void execute(CommandContext commandContext) {
    JobEntity job = commandContext
            .getJobManager()
            .findJobById(jobId);
    if (job != null) {

      for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
        checker.checkUpdateJob(job);
      }
      
      commandContext.getOperationLogManager().logJobOperation(UserOperationLogEntry.OPERATION_TYPE_SET_DUEDATE, jobId, 
          job.getJobDefinitionId(), job.getProcessInstanceId(), job.getProcessDefinitionId(), job.getProcessDefinitionKey(),
          Collections.singletonList(new PropertyChange("duedate", job.getDuedate(), newDuedate)));

      // for timer jobs cascade due date changes
      if (cascade && newDuedate != null && job instanceof TimerEntity) {
        long offset = newDuedate.getTime() - job.getDuedate().getTime();
        ((TimerEntity) job).setRepeatOffset(((TimerEntity) job).getRepeatOffset() + offset);
      }

      job.setDuedate(newDuedate);
    } else {
      throw new ProcessEngineException("No job found with id '" + jobId + "'.");
    }
    return null;
  }
}
