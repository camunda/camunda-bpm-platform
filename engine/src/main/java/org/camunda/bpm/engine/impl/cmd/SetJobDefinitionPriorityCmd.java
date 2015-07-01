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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;

/**
 * @author Thorben Lindhauer
 *
 */
public class SetJobDefinitionPriorityCmd implements Command<Void> {

  protected String jobDefinitionId;
  protected Integer priority;
  protected boolean cascade = false;

  public SetJobDefinitionPriorityCmd(String jobDefinitionId, Integer priority, boolean cascade) {
    this.jobDefinitionId = jobDefinitionId;
    this.priority = priority;
    this.cascade = cascade;
  }

  public Void execute(CommandContext commandContext) {
    ensureNotNull(NotValidException.class, "jobDefinitionId", jobDefinitionId);

    JobDefinitionEntity jobDefinition = commandContext.getJobDefinitionManager().findById(jobDefinitionId);

    ensureNotNull(NotFoundException.class,
        "Job definition with id '" + jobDefinitionId + "' does not exist",
        "jobDefinition",
        jobDefinition);

    checkAuthorization(commandContext, jobDefinition);

    jobDefinition.setJobPriority(priority);

    if (cascade && priority != null) {
      commandContext.getJobManager().updateJobPriorityByDefinitionId(jobDefinitionId, priority);
    }

    return null;
  }

  public void checkAuthorization(CommandContext commandContext, JobDefinitionEntity jobDefinition) {
    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();

    String processDefinitionKey = jobDefinition.getProcessDefinitionKey();
    authorizationManager.checkUpdateProcessDefinitionByKey(processDefinitionKey);

    if (cascade) {
      authorizationManager.checkUpdateProcessInstanceByProcessDefinitionKey(processDefinitionKey);
    }
  }


}
