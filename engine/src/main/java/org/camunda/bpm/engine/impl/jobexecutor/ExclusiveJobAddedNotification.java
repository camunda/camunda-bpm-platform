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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 *
 * @author Daniel Meyer
 */
public class ExclusiveJobAddedNotification implements TransactionListener {

  private static Logger log = Logger.getLogger(ExclusiveJobAddedNotification.class.getName());

  protected final String jobId;
  protected final JobExecutorContext jobExecutorContext;

  public ExclusiveJobAddedNotification(String jobId, JobExecutorContext jobExecutorContext) {
    this.jobId = jobId;
    this.jobExecutorContext = jobExecutorContext;
  }

  public void execute(CommandContext commandContext) {
    if(log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, "Adding new exclusive job to job executor context. Job Id='"+jobId+"'.");
    }
    jobExecutorContext.getCurrentProcessorJobQueue().add(jobId);
  }

}
