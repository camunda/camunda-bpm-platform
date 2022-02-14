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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Collections;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.ProcessDataContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorLogger;
import org.camunda.bpm.engine.impl.jobexecutor.JobFailureCollector;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ExecuteJobsCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  private final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  protected String jobId;

  protected JobFailureCollector jobFailureCollector;

  public ExecuteJobsCmd(String jobId, JobFailureCollector jobFailureCollector) {
    this.jobId = jobId;
    this.jobFailureCollector = jobFailureCollector;
  }

  public Void execute(CommandContext commandContext) {
    ensureNotNull("jobId", jobId);

    final JobEntity job = commandContext.getDbEntityManager().selectById(JobEntity.class, jobId);

    final ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    final IdentityService identityService = processEngineConfiguration.getIdentityService();

    final JobExecutorContext jobExecutorContext = Context.getJobExecutorContext();

    if (job == null) {
      if (jobExecutorContext != null) {
        // CAM-1842
        // Job was acquired but does not exist anymore. This is not a problem.
        // It usually means that the job has been deleted after it was acquired which can happen if the
        // the activity instance corresponding to the job is cancelled.
        LOG.debugAcquiredJobNotFound(jobId);
        return null;

      } else {
        throw LOG.jobNotFoundException(jobId);
      }
    }

    jobFailureCollector.setJob(job);

    if (jobExecutorContext == null) { // if null, then we are not called by the job executor
      for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
        checker.checkUpdateJob(job);
      }
      // write a user operation log since we're not called by the job executor
      commandContext.getOperationLogManager().logJobOperation(UserOperationLogEntry.OPERATION_TYPE_EXECUTE,
          jobId, job.getJobDefinitionId(), job.getProcessInstanceId(), job.getProcessDefinitionId(),
          job.getProcessDefinitionKey(), PropertyChange.EMPTY_CHANGE);
    } else {
      jobExecutorContext.setCurrentJob(job);

      // if the job is called by the job executor then set the tenant id of the job
      // as authenticated tenant to enable tenant checks
      String tenantId = job.getTenantId();
      if (tenantId != null) {
        identityService.setAuthentication(null, null, Collections.singletonList(tenantId));
      }
    }

    try {

      // register as command context close lister to intercept exceptions on flush
      commandContext.registerCommandContextListener(jobFailureCollector);

      commandContext.setCurrentJob(job);

      job.execute(commandContext);

    } catch (Throwable t) {
      String failedActivityId = Context.getCommandInvocationContext()
          .getProcessDataContext()
          .getLatestActivityId();

      jobFailureCollector.setFailedActivityId(failedActivityId);
      throw t;
    } finally {
      if (jobExecutorContext != null) {
        jobExecutorContext.setCurrentJob(null);
        identityService.clearAuthentication();
      }
    }

    return null;
  }

}
