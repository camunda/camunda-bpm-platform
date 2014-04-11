/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.rest.sub.repository.impl;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.rest.dto.repository.DeploymentResourceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.repository.DeploymentResourcesResource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status;

/**
 * @author Sebastian Menski
 */
public class DeploymentResourcesResourceImpl implements DeploymentResourcesResource {

  protected final ProcessEngine engine;
  protected final String deploymentId;

  public DeploymentResourcesResourceImpl(ProcessEngine engine, String deploymentId) {
    this.engine = engine;
    this.deploymentId = deploymentId;
  }

  public List<DeploymentResourceDto> getDeploymentResources() {
    List<Resource> resources = engine.getRepositoryService().getDeploymentResources(deploymentId);

    List<DeploymentResourceDto> deploymentResources = new ArrayList<DeploymentResourceDto>();
    for (Resource resource : resources) {
      deploymentResources.add(DeploymentResourceDto.fromResources(resource));
    }

    if (!deploymentResources.isEmpty()) {
      return deploymentResources;
    }
    else {
      throw new InvalidRequestException(Status.NOT_FOUND,
        "Deployment resources for deployment id '" + deploymentId + "' do not exist.");
    }
  }

  public DeploymentResourceDto getDeploymentResource(String resourceId) {
    List<DeploymentResourceDto> deploymentResources = getDeploymentResources();
    for (DeploymentResourceDto deploymentResource : deploymentResources) {
      if (deploymentResource.getId().equals(resourceId)) {
        return deploymentResource;
      }
    }

    throw new InvalidRequestException(Status.NOT_FOUND,
      "Deployment resource with resource id '" + resourceId + "' for deployment id '" + deploymentId + "' does not exist.");
  }

  public InputStream getDeploymentResourceData(String resourceId) {
    InputStream resourceAsStream = engine.getRepositoryService().getResourceAsStreamById(deploymentId, resourceId);

    if (resourceAsStream != null) {
      return resourceAsStream;
    }
    else {
      throw new InvalidRequestException(Status.NOT_FOUND,
        "Deployment resource '" + resourceId + "' for deployment id '" + deploymentId + "' does not exist.");
    }
  }
}
