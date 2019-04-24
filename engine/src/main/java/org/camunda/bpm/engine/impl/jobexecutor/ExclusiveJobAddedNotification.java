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
package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.management.Metrics;

/**
 *
 * @author Daniel Meyer
 */
public class ExclusiveJobAddedNotification implements TransactionListener {

  private final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  protected final String jobId;
  protected final JobExecutorContext jobExecutorContext;

  public ExclusiveJobAddedNotification(String jobId, JobExecutorContext jobExecutorContext) {
    this.jobId = jobId;
    this.jobExecutorContext = jobExecutorContext;
  }

  public void execute(CommandContext commandContext) {
    LOG.debugAddingNewExclusiveJobToJobExecutorCOntext(jobId);
    jobExecutorContext.getCurrentProcessorJobQueue().add(jobId);
    logExclusiveJobAdded(commandContext);
  }

  protected void logExclusiveJobAdded(CommandContext commandContext) {
    if (commandContext.getProcessEngineConfiguration().isMetricsEnabled()) {
      commandContext.getProcessEngineConfiguration()
        .getMetricsRegistry()
        .markOccurrence(Metrics.JOB_LOCKED_EXCLUSIVE);
    }
  }

}
