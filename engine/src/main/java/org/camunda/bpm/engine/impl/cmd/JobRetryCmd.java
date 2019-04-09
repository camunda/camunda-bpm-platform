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

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.cfg.TransactionContext;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.MessageAddedNotification;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;

/**
 * @author Roman Smirnov
 */
public abstract class JobRetryCmd implements Command<Object> {

  protected static final long serialVersionUID = 1L;
  protected String jobId;
  protected Throwable exception;

  public JobRetryCmd(String jobId, Throwable exception) {
    this.jobId = jobId;
    this.exception = exception;
  }

  protected JobEntity getJob() {
    return Context
        .getCommandContext()
        .getJobManager()
        .findJobById(jobId);
  }

  protected void logException(JobEntity job) {
    if(exception != null) {
      job.setExceptionMessage(exception.getMessage());
      job.setExceptionStacktrace(getExceptionStacktrace());
    }
  }

  protected void decrementRetries(JobEntity job) {
    if (exception == null || shouldDecrementRetriesFor(exception)) {
      job.setRetries(job.getRetries() - 1);
    }
  }

  protected String getExceptionStacktrace() {
    return ExceptionUtil.getExceptionStacktrace(exception);
  }

  protected boolean shouldDecrementRetriesFor(Throwable t) {
    return !(t instanceof OptimisticLockingException);
  }

  protected void notifyAcquisition(CommandContext commandContext) {
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    MessageAddedNotification messageAddedNotification = new MessageAddedNotification(jobExecutor);
    TransactionContext transactionContext = commandContext.getTransactionContext();
    transactionContext.addTransactionListener(TransactionState.COMMITTED, messageAddedNotification);
  }
}
