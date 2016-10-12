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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

/**
 * @author Askar Akhmerov
 */
public class AbstractSetJobRetriesCmd {
  protected static final String RETRIES = "retries";

  protected void setJobRetriesByJobId(String jobId, int retries, CommandContext commandContext) {
    JobEntity job = commandContext
        .getJobManager()
        .findJobById(jobId);
    if (job != null) {
      for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
        checker.checkUpdateJob(job);
      }

      if (job.isInInconsistentLockState()) {
        job.resetLock();
      }
      int oldRetries = job.getRetries();
      job.setRetries(retries);

      PropertyChange propertyChange = new PropertyChange(RETRIES, oldRetries, job.getRetries());
      commandContext.getOperationLogManager().logJobOperation(getLogEntryOperation(), job.getId(),
          job.getJobDefinitionId(), job.getProcessInstanceId(), job.getProcessDefinitionId(),
          job.getProcessDefinitionKey(), propertyChange);
    } else {
      throw new ProcessEngineException("No job found with id '" + jobId + "'.");
    }

  }

  protected String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES;
  }
}
