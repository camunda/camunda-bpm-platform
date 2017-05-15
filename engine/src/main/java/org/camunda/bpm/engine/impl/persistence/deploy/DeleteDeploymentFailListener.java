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
package org.camunda.bpm.engine.impl.persistence.deploy;

import java.util.concurrent.Callable;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cmd.RegisterDeploymentCmd;
import org.camunda.bpm.engine.impl.cmd.RegisterProcessApplicationCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

public class DeleteDeploymentFailListener implements TransactionListener {

  protected String deploymentId;
  protected ProcessApplicationReference processApplicationReference;
  protected CommandExecutor commandExecutor;

  public DeleteDeploymentFailListener(String deploymentId, ProcessApplicationReference processApplicationReference, CommandExecutor commandExecutor) {
    this.deploymentId = deploymentId;
    this.processApplicationReference = processApplicationReference;
    this.commandExecutor = commandExecutor;
  }

  public void execute(CommandContext commandContext) {

    //we can not use commandContext parameter here, as it can be in inconsistent state
    commandExecutor.execute(new Command<Void>() {
      @Override
      public Void execute(final CommandContext commandContext) {
        commandContext.runWithoutAuthorization(new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            new RegisterDeploymentCmd(deploymentId).execute(commandContext);
            if (processApplicationReference != null) {
              new RegisterProcessApplicationCmd(deploymentId, processApplicationReference).execute(commandContext);
            }
            return null;
          }
        });
        return null;
      }
    });
  }

}
