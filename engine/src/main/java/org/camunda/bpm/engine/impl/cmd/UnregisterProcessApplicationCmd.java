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

import java.util.Collections;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Daniel Meyer
 *
 */
public class UnregisterProcessApplicationCmd implements Command<Void> {

  protected boolean removeProcessesFromCache;
  protected Set<String> deploymentIds;

  public UnregisterProcessApplicationCmd(String deploymentId, boolean removeProcessesFromCache) {
    this(Collections.singleton(deploymentId), removeProcessesFromCache);
  }

  public UnregisterProcessApplicationCmd(Set<String> deploymentIds, boolean removeProcessesFromCache) {
    this.deploymentIds = deploymentIds;
    this.removeProcessesFromCache = removeProcessesFromCache;
  }

  public Void execute(CommandContext commandContext) {

    if(deploymentIds == null) {
      throw new ProcessEngineException("Deployment Ids cannot be null.");
    }

    commandContext.getAuthorizationManager().checkCamundaAdmin();

    Context.getProcessEngineConfiguration()
      .getProcessApplicationManager()
      .unregisterProcessApplicationForDeployments(deploymentIds, removeProcessesFromCache);

    return null;

  }

}
