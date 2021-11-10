/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.persistence.deploy;

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
    commandExecutor.execute(new DeleteDeploymentFailCmd());
  }

  protected class DeleteDeploymentFailCmd implements Command<Void> {

    @Override
    public Void execute(final CommandContext commandContext) {
      commandContext.runWithoutAuthorization(new RegisterDeploymentCmd(deploymentId));
      if (processApplicationReference != null) {
        commandContext.runWithoutAuthorization(new RegisterProcessApplicationCmd(deploymentId, processApplicationReference));
      }
      return null;
    }

  }
}
