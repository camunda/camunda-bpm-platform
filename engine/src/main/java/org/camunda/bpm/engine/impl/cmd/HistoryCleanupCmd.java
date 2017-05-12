/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import java.util.Date;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupContext;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHelper;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.runtime.Job;

/**
 * @author Svetlana Dorokhova
 */
public class HistoryCleanupCmd implements Command<Job>, Serializable {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public static final JobDeclaration HISTORY_CLEANUP_JOB_DECLARATION = new HistoryCleanupJobDeclaration();

  private boolean immediatelyDue;

  public HistoryCleanupCmd(boolean immediatelyDue) {
    this.immediatelyDue = immediatelyDue;
  }

  @Override
  public Job execute(CommandContext commandContext) {
    commandContext.getAuthorizationManager().checkAuthorization(Permissions.DELETE_HISTORY, Resources.PROCESS_DEFINITION);

    //validate
    if (!willBeScheduled(commandContext)) {
      LOG.debugHistoryCleanupWrongConfiguration();
    }

    //find job instance
    JobEntity historyCleanupJob = commandContext.getJobManager().findJobByHandlerType(HistoryCleanupJobHandler.TYPE);

    boolean createJob = historyCleanupJob == null && willBeScheduled(commandContext);

    boolean reconfigureJob = historyCleanupJob != null && willBeScheduled(commandContext);

    boolean suspendJob = historyCleanupJob != null && !willBeScheduled(commandContext);

    if (createJob) {
      //exclusive lock
      commandContext.getPropertyManager().acquireExclusiveLockForHistoryCleanupJob();

      //check again after lock
      historyCleanupJob = commandContext.getJobManager().findJobByHandlerType(HistoryCleanupJobHandler.TYPE);

      if (historyCleanupJob == null) {
        historyCleanupJob = HISTORY_CLEANUP_JOB_DECLARATION.createJobInstance(new HistoryCleanupContext(immediatelyDue));
        Context.getCommandContext().getJobManager().insertAndHintJobExecutor(historyCleanupJob);
      }
    } else if (reconfigureJob) {
      //apply new configuration
      HistoryCleanupContext historyCleanupContext = new HistoryCleanupContext(immediatelyDue);
      HISTORY_CLEANUP_JOB_DECLARATION.reconfigure(historyCleanupContext, historyCleanupJob);
      Date newDueDate = HISTORY_CLEANUP_JOB_DECLARATION.resolveDueDate(historyCleanupContext);
      commandContext.getJobManager().reschedule(historyCleanupJob, newDueDate);
    } else if (suspendJob) {
      historyCleanupJob.setDuedate(null);
      historyCleanupJob.setSuspensionState(SuspensionState.SUSPENDED.getStateCode());
    }

    return historyCleanupJob;
  }

  private boolean willBeScheduled(CommandContext commandContext) {
    return immediatelyDue || HistoryCleanupHelper.isBatchWindowConfigured(commandContext);
  }

}
