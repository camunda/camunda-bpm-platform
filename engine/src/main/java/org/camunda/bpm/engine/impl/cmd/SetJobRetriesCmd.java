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

import java.io.Serializable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;


/**
 * @author Falko Menge
 */
public class SetJobRetriesCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  protected static final String RETRIES = "retries";

  protected final String jobId;
  protected final String jobDefinitionId;
  protected final int retries;


  public SetJobRetriesCmd(String jobId, String jobDefinitionId, int retries) {
    if ((jobId == null || jobId.isEmpty()) && (jobDefinitionId == null || jobDefinitionId.isEmpty())) {
      throw new ProcessEngineException("Either job definition id or job id has to be provided as parameter.");
    }

    if (retries < 0) {
      throw new ProcessEngineException("The number of job retries must be a non-negative Integer, but '" + retries + "' has been provided.");
    }

    this.jobId = jobId;
    this.jobDefinitionId = jobDefinitionId;
    this.retries = retries;
  }

  public Void execute(CommandContext commandContext) {
    if (jobId != null) {
      setJobRetriesByJobId(commandContext);
    } else {
      setJobRetriesByJobDefinitionId(commandContext);
    }

    return null;
  }

  protected void setJobRetriesByJobId(CommandContext commandContext) {
    JobEntity job = commandContext
        .getJobManager()
        .findJobById(jobId);
    if (job != null) {
      if (job.isInInconsistentLockState()) {
        job.resetLock();
      }
      int oldRetries = job.getRetries();
      job.setRetries(retries);

      PropertyChange propertyChange = new PropertyChange(RETRIES, oldRetries, job.getRetries());
      commandContext.getOperationLogManager().logJobRetryOperation(getLogEntryOperation(), job.getId(),
        job.getJobDefinitionId(), job.getProcessInstanceId(), job.getProcessDefinitionId(),
        job.getProcessDefinitionKey(), propertyChange);
    } else {
      throw new ProcessEngineException("No job found with id '" + jobId + "'.");
    }
  }

  protected void setJobRetriesByJobDefinitionId(CommandContext commandContext) {
    commandContext
        .getJobManager()
        .updateFailedJobRetriesByJobDefinitionId(jobDefinitionId, retries);

    JobDefinitionEntity jobDefinitionEntity = commandContext.getJobDefinitionManager().findById(jobDefinitionId);
    PropertyChange propertyChange = new PropertyChange(RETRIES, null, retries);
    commandContext.getOperationLogManager().logJobRetryOperation(getLogEntryOperation(), null, jobDefinitionId, null,
      jobDefinitionEntity.getProcessDefinitionId(), jobDefinitionEntity.getProcessDefinitionKey(), propertyChange);
  }

  protected String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_RETRY;
  }
}
