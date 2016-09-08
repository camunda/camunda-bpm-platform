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

package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;

public class JobFailureCollector implements CommandContextListener {

  protected Throwable failure;
  protected JobEntity job;
  protected String jobId;

  public JobFailureCollector(String jobId) {
    this.jobId = jobId;
  }

  public void setFailure(Throwable failure) {
    // log failure if not already present
    if (this.failure == null) {
      this.failure = failure;
    }
  }

  public Throwable getFailure() {
    return failure;
  }

  @Override
  public void onCommandFailed(CommandContext commandContext, Throwable t) {
    setFailure(t);
  }

  @Override
  public void onCommandContextClose(CommandContext commandContext) {
    // ignore
  }

  public void setJob(JobEntity job) {
    this.job = job;
  }

  public JobEntity getJob() {
    return job;
  }

  public String getJobId() {
    return jobId;
  }

}