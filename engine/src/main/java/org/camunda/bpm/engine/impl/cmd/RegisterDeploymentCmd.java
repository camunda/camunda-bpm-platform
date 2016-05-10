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

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.repository.Deployment;

/**
 * @author Thorben Lindhauer
 */
public class RegisterDeploymentCmd implements Command<Void> {

  protected String deploymentId;

  public RegisterDeploymentCmd(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public Void execute(CommandContext commandContext) {
    Deployment deployment = commandContext.getDeploymentManager().findDeploymentById(deploymentId);

    ensureNotNull("Deployment " + deploymentId + " does not exist", "deployment", deployment);

    commandContext.getAuthorizationManager().checkCamundaAdmin();

    Context.getProcessEngineConfiguration().getRegisteredDeployments().add(deploymentId);
    return null;
  }

}
