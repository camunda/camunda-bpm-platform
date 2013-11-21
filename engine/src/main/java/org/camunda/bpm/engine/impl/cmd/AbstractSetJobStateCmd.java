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
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;

/**
 * @author roman.smirnov
 */
public abstract class AbstractSetJobStateCmd implements Command<Void> {

  protected String jobId;
  protected String jobDefinitionId;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;


  public AbstractSetJobStateCmd(String jobId, String jobDefinitionId, String processInstanceId, String processDefinitionId, String processDefinitionKey) {
    this.jobId = jobId;
    this.jobDefinitionId = jobDefinitionId;
    this.processInstanceId = processInstanceId;
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
  }

  public Void execute(CommandContext commandContext) {
    if(jobId == null && jobDefinitionId == null && processInstanceId == null && processDefinitionId == null && processDefinitionKey == null) {
      throw new ProcessEngineException("Job id, job definition id, process instance id, process definition id nor process definition key cannot be null");
    }

    JobManager jobManager = commandContext.getJobManager();
    SuspensionState suspensionState = getSuspensionState();

    if (jobId != null) {
      jobManager.updateJobSuspensionStateById(jobId, suspensionState);
    } else

    if (jobDefinitionId != null) {
      jobManager.updateJobSuspensionStateByJobDefinitionId(jobDefinitionId, suspensionState);
    } else

    if (processInstanceId != null) {
        jobManager.updateJobSuspensionStateByProcessInstanceId(processInstanceId, suspensionState);
    } else

    if (processDefinitionId != null) {
      jobManager.updateJobSuspensionStateByProcessDefinitionId(processDefinitionId, suspensionState);
    } else

    if (processDefinitionKey != null) {
      jobManager.updateJobSuspensionStateByProcessDefinitionKey(processDefinitionKey, suspensionState);
    }

    return null;
  }

  /**
   * Subclasses should return the wanted {@link SuspensionState} here.
   */
  protected abstract SuspensionState getSuspensionState();

}
