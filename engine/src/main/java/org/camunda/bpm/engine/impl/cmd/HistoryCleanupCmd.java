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
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupContext;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.Job;

/**
 * @author Svetlana Dorokhova
 */
public class HistoryCleanupCmd implements Command<Job>, Serializable {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public static final JobDeclaration HISTORY_CLEANUP_JOB_DECLARATION = new HistoryCleanupJobDeclaration();

  private boolean executeAtOnce;

  public HistoryCleanupCmd(boolean executeAtOnce) {
    this.executeAtOnce = executeAtOnce;
  }

  @Override
  public Job execute(CommandContext commandContext) {
    commandContext.getAuthorizationManager().checkAuthorization(Permissions.DELETE_HISTORY, Resources.PROCESS_DEFINITION);

    ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();

    //validate
    if (!executeAtOnce && (processEngineConfiguration.getBatchWindowStartTime() == null || processEngineConfiguration.getBatchWindowStartTime().isEmpty())) {
      throw LOG.exceptionHistoryCleanupWrongConfiguration();
    }

    //find or create job instance
    commandContext.getPropertyManager().acquireExclusiveLockForHistoryCleanupJob();

    JobEntity historyCleanupJob = commandContext.getJobManager().findJobByHandlerType(HistoryCleanupJobHandler.TYPE);
    HistoryCleanupContext historyCleanupContext = new HistoryCleanupContext(executeAtOnce);
    if (historyCleanupJob == null) {
      historyCleanupJob = HISTORY_CLEANUP_JOB_DECLARATION.createJobInstance(historyCleanupContext);
      Context.getCommandContext().getJobManager().insertAndHintJobExecutor(historyCleanupJob);
    } else {
      //apply new configuration
      HISTORY_CLEANUP_JOB_DECLARATION.reconfigure(historyCleanupContext, historyCleanupJob);
      commandContext.getJobManager().reschedule(historyCleanupJob, historyCleanupJob.getDuedate());
    }
    return historyCleanupJob;
  }

}
