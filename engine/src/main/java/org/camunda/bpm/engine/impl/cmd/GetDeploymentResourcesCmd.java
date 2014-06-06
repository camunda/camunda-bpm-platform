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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.io.Serializable;
import java.util.List;


/**
 * @author kristin.polenz@camunda.com
 */
public class GetDeploymentResourcesCmd implements Command<List>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String deploymentId;

  public GetDeploymentResourcesCmd(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public List execute(CommandContext commandContext) {
    if (deploymentId == null) {
      throw new ProcessEngineException("deploymentId is null");
    }

    return Context
      .getCommandContext()
      .getResourceManager()
      .findResourcesByDeploymentId(deploymentId);
  }

}
