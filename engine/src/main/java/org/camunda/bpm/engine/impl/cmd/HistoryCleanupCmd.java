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

import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupContext;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHelper;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyManager;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.runtime.Job;

/**
 * @author Svetlana Dorokhova
 */
public class HistoryCleanupCmd implements Command<Job> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public static final JobDeclaration HISTORY_CLEANUP_JOB_DECLARATION = new HistoryCleanupJobDeclaration();

  public static final int MAX_THREADS_NUMBER = 8;

  private boolean immediatelyDue;

  public HistoryCleanupCmd(boolean immediatelyDue) {
    this.immediatelyDue = immediatelyDue;
  }

  @Override
  public Job execute(CommandContext commandContext) {
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();

    authorizationManager.checkCamundaAdmin();

    //validate
    if (!willBeScheduled()) {
      LOG.debugHistoryCleanupWrongConfiguration();
    }

    //find job instance
    List<Job> historyCleanupJobs = getHistoryCleanupJobs();

    int degreeOfParallelism = processEngineConfiguration.getHistoryCleanupDegreeOfParallelism();
    int[][] minuteChunks = HistoryCleanupHelper.listMinuteChunks(degreeOfParallelism);

    if (shouldCreateJobs(historyCleanupJobs)) {
      historyCleanupJobs = createJobs(degreeOfParallelism, minuteChunks);

    }
    else if (shouldReconfigureJobs(historyCleanupJobs)) {
      historyCleanupJobs = reconfigureJobs(historyCleanupJobs, degreeOfParallelism, minuteChunks);

    }
    else if (shouldSuspendJobs(historyCleanupJobs)) {
      suspendJobs(historyCleanupJobs);

    }

    return historyCleanupJobs.size() > 0 ? historyCleanupJobs.get(0) : null;
  }

  protected List<Job> getHistoryCleanupJobs() {
    CommandContext commandContext = Context.getCommandContext();
    return commandContext.getJobManager().findJobsByHandlerType(HistoryCleanupJobHandler.TYPE);
  }

  protected boolean shouldCreateJobs(List<Job> jobs) {
    return jobs.isEmpty() && willBeScheduled();
  }

  protected boolean shouldReconfigureJobs(List<Job> jobs) {
    return !jobs.isEmpty() && willBeScheduled();
  }

  protected boolean shouldSuspendJobs(List<Job> jobs) {
    return !jobs.isEmpty() && !willBeScheduled();
  }

  protected boolean willBeScheduled() {
    CommandContext commandContext = Context.getCommandContext();
    return immediatelyDue || HistoryCleanupHelper.isBatchWindowConfigured(commandContext);
  }

  protected List<Job> createJobs(int degreeOfParallelism, int[][] minuteChunks) {
    CommandContext commandContext = Context.getCommandContext();

    PropertyManager propertyManager = commandContext.getPropertyManager();
    JobManager jobManager = commandContext.getJobManager();

    //exclusive lock
    propertyManager.acquireExclusiveLockForHistoryCleanupJob();

    //check again after lock
    List<Job> historyCleanupJobs = getHistoryCleanupJobs();

    if (historyCleanupJobs.isEmpty()) {
      for (int[] minuteChunk : minuteChunks) {
        JobEntity job = createJob(minuteChunk);
        jobManager.insertAndHintJobExecutor(job);
        historyCleanupJobs.add(job);
      }
    }

    return historyCleanupJobs;
  }

  @SuppressWarnings("unchecked")
  protected List<Job> reconfigureJobs(List<Job> historyCleanupJobs, int degreeOfParallelism, int[][] minuteChunks) {
    CommandContext commandContext = Context.getCommandContext();
    JobManager jobManager = commandContext.getJobManager();

    int size = Math.min(degreeOfParallelism, historyCleanupJobs.size());

    for (int i = 0; i < size; i++) {
      JobEntity historyCleanupJob = (JobEntity) historyCleanupJobs.get(i);

      //apply new configuration
      HistoryCleanupContext historyCleanupContext = createCleanupContext(minuteChunks[i]);

      HISTORY_CLEANUP_JOB_DECLARATION.reconfigure(historyCleanupContext, historyCleanupJob);

      Date newDueDate = HISTORY_CLEANUP_JOB_DECLARATION.resolveDueDate(historyCleanupContext);

      jobManager.reschedule(historyCleanupJob, newDueDate);
    }

    int delta = degreeOfParallelism - historyCleanupJobs.size();

    if (delta > 0) {
      //create new job, as there are not enough of them
      for (int i = size; i < degreeOfParallelism; i++) {
        JobEntity job = createJob(minuteChunks[i]);
        jobManager.insertAndHintJobExecutor(job);
        historyCleanupJobs.add(job);
      }
    }
    else if (delta < 0) {
      //remove jobs, if there are too much of them
      ListIterator<Job> iterator = historyCleanupJobs.listIterator(size);
      while (iterator.hasNext()) {
        JobEntity job = (JobEntity) iterator.next();
        jobManager.deleteJob(job);
        iterator.remove();
      }
    }

    return historyCleanupJobs;
  }

  protected void suspendJobs(List<Job> jobs) {
    for (Job job: jobs) {
      JobEntity jobInstance = (JobEntity) job;
      jobInstance.setSuspensionState(SuspensionState.SUSPENDED.getStateCode());
      jobInstance.setDuedate(null);
    }
  }

  @SuppressWarnings("unchecked")
  protected JobEntity createJob(int[] minuteChunk) {
    HistoryCleanupContext historyCleanupContext = createCleanupContext(minuteChunk);
    return HISTORY_CLEANUP_JOB_DECLARATION.createJobInstance(historyCleanupContext);
  }

  protected HistoryCleanupContext createCleanupContext(int[] minuteChunk) {
    int minuteFrom = minuteChunk[0];
    int minuteTo = minuteChunk[1];
    return new HistoryCleanupContext(immediatelyDue, minuteFrom, minuteTo);
  }
}
