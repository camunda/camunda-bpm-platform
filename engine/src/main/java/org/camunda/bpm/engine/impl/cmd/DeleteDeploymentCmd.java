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
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeleteDeploymentFailListener;

/**
 * @author Joram Barrez
 * @author Thorben Lindhauer
 */
public class DeleteDeploymentCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(DeleteDeploymentCmd.class.getName());

  protected String deploymentId;
  protected boolean cascade;

  public DeleteDeploymentCmd(String deploymentId, boolean cascade) {
    this.deploymentId = deploymentId;
    this.cascade = cascade;
  }

  public Void execute(CommandContext commandContext) {
    if(deploymentId == null) {
      throw new ProcessEngineException("deploymentId is null");
    }

    commandContext
      .getDeploymentManager()
      .deleteDeployment(deploymentId, cascade);

    DeleteDeploymentFailListener listener = new DeleteDeploymentFailListener(deploymentId);

    try {
      new UnregisterDeploymentCmd(Collections.singleton(deploymentId)).execute(commandContext);
    } finally {
      try {
        commandContext.getTransactionContext().addTransactionListener(TransactionState.ROLLED_BACK, listener);
      } catch (Exception e) {
        log.log(Level.FINE, "Could not register transaction synchronization. Probably the TX has already been rolled back by application code.", e);
        listener.execute(commandContext);
      }
    }


    return null;
  }
}
