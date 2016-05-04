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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;


/**
 * @author kristin.polenz@camunda.com
 */
public class GetDeploymentResourceForIdCmd implements Command<InputStream>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String deploymentId;
  protected String resourceId;

  public GetDeploymentResourceForIdCmd(String deploymentId, String resourceId) {
    this.deploymentId = deploymentId;
    this.resourceId = resourceId;
  }

  public InputStream execute(CommandContext commandContext) {
    ensureNotNull("deploymentId", deploymentId);
    ensureNotNull("resourceId", resourceId);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadDeployment(deploymentId);
    }

    ResourceEntity resource = commandContext
      .getResourceManager()
      .findResourceByDeploymentIdAndResourceId(deploymentId, resourceId);
    ensureNotNull("no resource found with id '" + resourceId + "' in deployment '" + deploymentId + "'", "resource", resource);
    return new ByteArrayInputStream(resource.getBytes());
  }

}
