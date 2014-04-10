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
package org.camunda.bpm.engine.rest.sub.repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.rest.dto.repository.DeploymentResourceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

public class DeploymentResourceImpl implements DeploymentResource {

  private ProcessEngine engine;
  private String deploymentId;

  public DeploymentResourceImpl(ProcessEngine engine, String deploymentId) {
    this.engine = engine;
    this.deploymentId = deploymentId;
  }

  @Override
  public DeploymentResourceDto getDeploymentResource(String resourceId) {
    DeploymentResourceDto deploymentResource = null;

    List<DeploymentResourceDto> deploymentResources = getDeploymentResources();
    for (DeploymentResourceDto deploymentResourceDto : deploymentResources) {
      if (resourceId.equals(deploymentResourceDto.getId())) {
        deploymentResource = new DeploymentResourceDto();
        deploymentResource.setDeploymentId(deploymentId);
        deploymentResource.setId(deploymentResourceDto.getId());
        deploymentResource.setName(deploymentResourceDto.getName());
        break;
      }
    }

    if (deploymentResource != null) {
      return deploymentResource;
    } else {
      throw new InvalidRequestException(Status.NOT_FOUND, "Deployment resource with resource id '" + resourceId + "' does not exist in deployment with deployment id '" + deploymentId + "'.");
    }
  }

  @Override
  public List<DeploymentResourceDto> getDeploymentResources() {
    List<DeploymentResourceDto> deploymentResources = new ArrayList<DeploymentResourceDto>();

    List<Resource> resources = engine.getRepositoryService().getDeploymentResources(deploymentId);
    for (Resource resource : resources) {
      DeploymentResourceDto deploymentResourceDto = DeploymentResourceDto.fromResources(resource);
      deploymentResources.add(deploymentResourceDto);
    }

    if (!deploymentResources.isEmpty()) {
      return deploymentResources;
    } else {
      throw new InvalidRequestException(Status.NOT_FOUND, "Deployment resources for deployment Id '"+deploymentId + "' do not exist.");
    }

  }

  @Override
  public InputStream getDeploymentData(String resourceId) {
      InputStream resourceAsStream = engine.getRepositoryService().getResourceAsStreamById(deploymentId, resourceId);

      if (resourceAsStream != null) {
        return resourceAsStream;
      } else {
        throw new InvalidRequestException(Status.NOT_FOUND, "No deployment resource stream exists for resource id '" + resourceId + "' in deployment with deployment id '" + deploymentId + "'.");
      }
  }

}
