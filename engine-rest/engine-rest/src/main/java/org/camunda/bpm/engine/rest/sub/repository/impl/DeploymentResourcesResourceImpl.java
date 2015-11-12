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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.rest.dto.repository.DeploymentResourceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.repository.DeploymentResourcesResource;

/**
 * @author Sebastian Menski
 */
public class DeploymentResourcesResourceImpl implements DeploymentResourcesResource {

  protected static final Map<String, String> MEDIA_TYPE_MAPPING = new HashMap<String, String>();

  static {
    MEDIA_TYPE_MAPPING.put("bpmn", MediaType.APPLICATION_XML);
    MEDIA_TYPE_MAPPING.put("cmmn", MediaType.APPLICATION_XML);
    MEDIA_TYPE_MAPPING.put("dmn", MediaType.APPLICATION_XML);
    MEDIA_TYPE_MAPPING.put("json", MediaType.APPLICATION_JSON);
    MEDIA_TYPE_MAPPING.put("xml", MediaType.APPLICATION_XML);

    MEDIA_TYPE_MAPPING.put("gif", "image/gif");
    MEDIA_TYPE_MAPPING.put("jpeg", "image/jpeg");
    MEDIA_TYPE_MAPPING.put("jpe", "image/jpeg");
    MEDIA_TYPE_MAPPING.put("jpg", "image/jpeg");
    MEDIA_TYPE_MAPPING.put("png", "image/png");
    MEDIA_TYPE_MAPPING.put("svg", "image/svg+xml");
    MEDIA_TYPE_MAPPING.put("tiff", "image/tiff");
    MEDIA_TYPE_MAPPING.put("tif", "image/tiff");

    MEDIA_TYPE_MAPPING.put("groovy", "text/plain");
    MEDIA_TYPE_MAPPING.put("java", "text/plain");
    MEDIA_TYPE_MAPPING.put("js", "text/plain");
    MEDIA_TYPE_MAPPING.put("php", "text/plain");
    MEDIA_TYPE_MAPPING.put("py", "text/plain");
    MEDIA_TYPE_MAPPING.put("rb", "text/plain");

    MEDIA_TYPE_MAPPING.put("html", "text/html");
    MEDIA_TYPE_MAPPING.put("txt", "text/plain");
  }

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

  public Response getDeploymentResourceData(String resourceId) {
    RepositoryService repositoryService = engine.getRepositoryService();
    InputStream resourceAsStream = repositoryService.getResourceAsStreamById(deploymentId, resourceId);

    if (resourceAsStream != null) {

      DeploymentResourceDto resource = getDeploymentResource(resourceId);

      String name = resource.getName();

      String filename = null;
      String mediaType = null;

      if (name != null) {
        name = name.replace("\\", "/");
        String[] filenameParts = name.split("/");
        if (filenameParts.length > 0) {
          int idx = filenameParts.length-1;
          filename = filenameParts[idx];
        }

        String[] extensionParts = name.split("\\.");
        if (extensionParts.length > 0) {
          int idx = extensionParts.length-1;
          String extension = extensionParts[idx];
          if (extension != null) {
            mediaType = MEDIA_TYPE_MAPPING.get(extension);
          }
        }
      }

      if (filename == null) {
        filename = "data";
      }

      if (mediaType == null) {
        mediaType = MediaType.APPLICATION_OCTET_STREAM;
      }

      return Response
          .ok(resourceAsStream, mediaType)
          .header("Content-Disposition", "attachment; filename=" + filename)
          .build();
    }
    else {
      throw new InvalidRequestException(Status.NOT_FOUND,
        "Deployment resource '" + resourceId + "' for deployment id '" + deploymentId + "' does not exist.");
    }
  }
}
