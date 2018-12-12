/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorLogger;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * Unlock job.
 *
 * @author Thomas Skjolberg
 */

public class UnlockJobCmd implements Command<Void> {

  protected static final long serialVersionUID = 1L;

  private final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  protected String jobId;

  public UnlockJobCmd(String jobId) {
    this.jobId = jobId;
  }

  protected JobEntity getJob() {
    return Context.getCommandContext().getJobManager().findJobById(jobId);
  }

  public Void execute(CommandContext commandContext) {
    JobEntity job = getJob();

    if (Context.getJobExecutorContext() == null) {
      EnsureUtil.ensureNotNull("Job with id " + jobId + " does not exist", "job", job);
    }
    else if (Context.getJobExecutorContext() != null && job == null) {
      // CAM-1842
      // Job was acquired but does not exist anymore. This is not a problem.
      // It usually means that the job has been deleted after it was acquired which can happen if the
      // the activity instance corresponding to the job is cancelled.
      LOG.debugAcquiredJobNotFound(jobId);
      return null;
    }

    job.unlock();

    return null;
  }
}
