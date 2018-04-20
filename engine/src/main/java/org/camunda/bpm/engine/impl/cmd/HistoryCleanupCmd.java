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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupContext;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHelper;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;

/**
 * @author Svetlana Dorokhova
 */
public class HistoryCleanupCmd implements Command<Job> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public static final JobDeclaration HISTORY_CLEANUP_JOB_DECLARATION = new HistoryCleanupJobDeclaration();

  public static final int MAX_THREADS_NUMBER = 4;

  private boolean immediatelyDue;

  public HistoryCleanupCmd(boolean immediatelyDue) {
    this.immediatelyDue = immediatelyDue;
  }

  @Override
  public Job execute(CommandContext commandContext) {
    commandContext.getAuthorizationManager().checkCamundaAdmin();

    //validate
    if (!willBeScheduled(commandContext)) {
      LOG.debugHistoryCleanupWrongConfiguration();
    }

    //find job instance
    List<Job> historyCleanupJobs = commandContext.getJobManager().findJobsByHandlerType(HistoryCleanupJobHandler.TYPE);

    boolean createJobs = historyCleanupJobs.isEmpty() && willBeScheduled(commandContext);

    boolean reconfigureJobs = !historyCleanupJobs.isEmpty() && willBeScheduled(commandContext);

    boolean suspendJobs = !historyCleanupJobs.isEmpty() && !willBeScheduled(commandContext);

    if (createJobs) {
      //exclusive lock
      commandContext.getPropertyManager().acquireExclusiveLockForHistoryCleanupJob();

      //check again after lock
      historyCleanupJobs = commandContext.getJobManager().findJobsByHandlerType(HistoryCleanupJobHandler.TYPE);

      if (historyCleanupJobs.isEmpty()) {
        int[][] minuteChunks = HistoryCleanupHelper.listMinuteChunks(commandContext.getProcessEngineConfiguration().getHistoryCleanupNumberOfThreads());
        for (int[] minuteChunk: minuteChunks) {
          final JobEntity jobInstance = HISTORY_CLEANUP_JOB_DECLARATION.createJobInstance(new HistoryCleanupContext(immediatelyDue, minuteChunk[0], minuteChunk[1]));
          historyCleanupJobs.add(jobInstance);
          Context.getCommandContext().getJobManager().insertAndHintJobExecutor(jobInstance);
        }
      }
    } else if (reconfigureJobs) {
      final int numberOfThreads = commandContext.getProcessEngineConfiguration().getHistoryCleanupNumberOfThreads();
      int[][] minuteChunks = HistoryCleanupHelper.listMinuteChunks(commandContext.getProcessEngineConfiguration().getHistoryCleanupNumberOfThreads());

      int i = 0;
      int[] minuteChunk;
      while (i < numberOfThreads) {
        minuteChunk = minuteChunks[i];

        Job job = null;
        try {
          //reconfigure
          job = historyCleanupJobs.get(i);

          //apply new configuration
          HistoryCleanupContext historyCleanupContext = new HistoryCleanupContext(immediatelyDue, minuteChunk[0], minuteChunk[1]);

          JobEntity historyCleanupJob = (JobEntity) job;
          HISTORY_CLEANUP_JOB_DECLARATION.reconfigure(historyCleanupContext, historyCleanupJob);

          // don't set a new due date if the current one is already within the batch window
          Date newDueDate;
          if (!immediatelyDue && historyCleanupJob.getDuedate() != null && HistoryCleanupHelper
            .isWithinBatchWindow(historyCleanupJob.getDuedate(), commandContext)) {
            newDueDate = historyCleanupJob.getDuedate();
          } else {
            newDueDate = HISTORY_CLEANUP_JOB_DECLARATION.resolveDueDate(historyCleanupContext);
          }

          commandContext.getJobManager().reschedule(historyCleanupJob, newDueDate);

        } catch (IndexOutOfBoundsException ex) {
          //create new job, as there are not enough of them
          job = HISTORY_CLEANUP_JOB_DECLARATION.createJobInstance(new HistoryCleanupContext(immediatelyDue, minuteChunk[0], minuteChunk[1]));
          historyCleanupJobs.add(job);
          Context.getCommandContext().getJobManager().insertAndHintJobExecutor((JobEntity) job);
        }

        i++;
      };

      while (i < historyCleanupJobs.size()) {
        //remove jobs, if there are too much of them
        final Job job = historyCleanupJobs.get(i);
        Context.getCommandContext().getJobManager().deleteJob((JobEntity)job);
        i++;
      }


    } else if (suspendJobs) {
      for (Job job: historyCleanupJobs) {
        JobEntity jobInstance = (JobEntity)job;
        jobInstance.setDuedate(null);
        jobInstance.setSuspensionState(SuspensionState.SUSPENDED.getStateCode());
      }
    }
    if (historyCleanupJobs.size() > 0) {
      return historyCleanupJobs.get(0);
    } else {
      return null;
    }
  }

  private boolean willBeScheduled(CommandContext commandContext) {
    return immediatelyDue || HistoryCleanupHelper.isBatchWindowConfigured(commandContext);
  }

}
