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
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * @author Thorben Lindhauer
 *
 */
public class SetJobPriorityCmd implements Command<Void> {

  protected String jobId;
  protected int priority;

  public SetJobPriorityCmd(String jobId, int priority) {
    this.jobId = jobId;
    this.priority = priority;
  }

  public Void execute(CommandContext commandContext) {
    EnsureUtil.ensureNotNull("job id must not be null", "jobId", jobId);

    JobEntity job = commandContext.getJobManager().findJobById(jobId);
    EnsureUtil.ensureNotNull(NotFoundException.class, "No job found with id '" + jobId + "'", "job", job);

    AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();
    authorizationManager.checkUpdateProcessInstance(job);

    job.setPriority(priority);

    return null;
  }
}
