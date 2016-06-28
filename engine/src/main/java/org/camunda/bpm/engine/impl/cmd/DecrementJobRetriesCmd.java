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

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorLogger;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class DecrementJobRetriesCmd extends JobRetryCmd {

  private final static JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  public DecrementJobRetriesCmd(String jobId, Throwable exception) {
    super(jobId, exception);
  }

  public Object execute(CommandContext commandContext) {
    JobEntity job = getJob();
    if (job != null) {
      job.unlock();
      logException(job);
      decrementRetries(job);
      notifyAcquisition(commandContext);
    } else {
      LOG.debugFailedJobNotFound(jobId);
    }
    return null;
  }

}
