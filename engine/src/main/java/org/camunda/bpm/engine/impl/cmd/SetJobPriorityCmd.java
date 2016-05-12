/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * @author Thorben Lindhauer
 *
 */
public class SetJobPriorityCmd implements Command<Void> {

  public static final String JOB_PRIORITY_PROPERTY = "priority";

  protected String jobId;
  protected long priority;

  public SetJobPriorityCmd(String jobId, long priority) {
    this.jobId = jobId;
    this.priority = priority;
  }

  public Void execute(CommandContext commandContext) {
    EnsureUtil.ensureNotNull("job id must not be null", "jobId", jobId);

    JobEntity job = commandContext.getJobManager().findJobById(jobId);
    EnsureUtil.ensureNotNull(NotFoundException.class, "No job found with id '" + jobId + "'", "job", job);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateJob(job);
    }

    long currentPriority = job.getPriority();
    job.setPriority(priority);

    createOpLogEntry(commandContext, currentPriority, job);

    return null;
  }

  protected void createOpLogEntry(CommandContext commandContext, long previousPriority, JobEntity job) {
    PropertyChange propertyChange = new PropertyChange(JOB_PRIORITY_PROPERTY, previousPriority, job.getPriority());
    commandContext
      .getOperationLogManager()
      .logJobOperation(
          UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY,
          job.getId(),
          job.getJobDefinitionId(),
          job.getProcessInstanceId(),
          job.getProcessDefinitionId(),
          job.getProcessDefinitionKey(),
          propertyChange);
  }
}
