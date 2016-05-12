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

import java.io.Serializable;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;


/**
 * @author Frederik Heremans
 */
public class GetJobExceptionStacktraceCmd implements Command<String>, Serializable{

  private static final long serialVersionUID = 1L;
  private String jobId;

  public GetJobExceptionStacktraceCmd(String jobId) {
    this.jobId = jobId;
  }


  public String execute(CommandContext commandContext) {
    ensureNotNull("jobId", jobId);

    JobEntity job = commandContext
      .getJobManager()
      .findJobById(jobId);

    ensureNotNull("No job found with id " + jobId, "job", job);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadJob(job);
    }
    
    return job.getExceptionStacktrace();
  }


}
