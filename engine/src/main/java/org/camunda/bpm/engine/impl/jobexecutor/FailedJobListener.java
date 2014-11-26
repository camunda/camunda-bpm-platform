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

import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;


/**
 * @author Frederik Heremans
 * @author Bernd Ruecker
 */
public class FailedJobListener implements TransactionListener, CommandContextListener {

  private static Logger log = Logger.getLogger(FailedJobListener.class.getName());

  protected CommandExecutor commandExecutor;
  protected String jobId;
  protected Throwable exception;

  public FailedJobListener(CommandExecutor commandExecutor, String jobId, Throwable exception) {
    this(commandExecutor, jobId);
    this.exception = exception;
  }

  public FailedJobListener(CommandExecutor commandExecutor, String jobId) {
    this.commandExecutor = commandExecutor;
    this.jobId = jobId;
  }

  public void execute(CommandContext commandContext) {
    FailedJobCommandFactory failedJobCommandFactory = commandContext.getFailedJobCommandFactory();
    Command<Object> cmd = failedJobCommandFactory.getCommand(jobId, exception);

    log.fine("Using FailedJobCommandFactory '" + failedJobCommandFactory.getClass() + "' and command of type '" + cmd.getClass() + "'");
    commandExecutor.execute(cmd);
  }

  public void setException(Throwable exception) {
    this.exception = exception;
  }

  public Throwable getException() {
    return exception;
  }

  public void onCommandContextClose(CommandContext commandContext) {
    // ignored
  }

  public void onCommandFailed(CommandContext commandContext, Throwable t) {
    // log exception if not already present
    if(this.exception == null) {
      this.exception = t;
    }
  }

}
