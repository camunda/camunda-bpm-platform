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
package org.camunda.bpm.engine.rest.sub.repository.impl;

import java.net.URI;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.rest.DeploymentRestService;
import org.camunda.bpm.engine.rest.dto.repository.DeploymentDto;
import org.camunda.bpm.engine.rest.dto.repository.DeploymentWithDefinitionsDto;
import org.camunda.bpm.engine.rest.dto.repository.RedeploymentDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.impl.AbstractRestProcessEngineAware;
import org.camunda.bpm.engine.rest.sub.repository.DeploymentResource;
import org.camunda.bpm.engine.rest.sub.repository.DeploymentResourcesResource;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DeploymentResourceImpl extends AbstractRestProcessEngineAware implements DeploymentResource {

  protected String deploymentId;

  public DeploymentResourceImpl(String processEngineName, String deploymentId, String rootResourcePath, ObjectMapper objectMapper) {
    super(processEngineName, objectMapper);
    this.deploymentId = deploymentId;
    this.relativeRootResourcePath = rootResourcePath;
  }

  public DeploymentDto getDeployment() {
    RepositoryService repositoryService = getProcessEngine().getRepositoryService();
    Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();

    if (deployment == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Deployment with id '" + deploymentId + "' does not exist");
    }

    return DeploymentDto.fromDeployment(deployment);
  }

  public DeploymentResourcesResource getDeploymentResources() {
    return new DeploymentResourcesResourceImpl(getProcessEngine(), deploymentId);
  }

  public DeploymentDto redeploy(UriInfo uriInfo, RedeploymentDto redeployment) {
    DeploymentWithDefinitions deployment = null;
    try {
      deployment = tryToRedeploy(redeployment);

    } catch (NotFoundException e) {
      throw createInvalidRequestException("redeploy", Status.NOT_FOUND, e);

    } catch (NotValidException e) {
      throw createInvalidRequestException("redeploy", Status.BAD_REQUEST, e);
    }

    DeploymentWithDefinitionsDto deploymentDto = DeploymentWithDefinitionsDto.fromDeployment(deployment);

    URI uri = uriInfo.getBaseUriBuilder()
      .path(relativeRootResourcePath)
      .path(DeploymentRestService.PATH)
      .path(deployment.getId())
      .build();

    // GET /
    deploymentDto.addReflexiveLink(uri, HttpMethod.GET, "self");

    return deploymentDto;
  }

  protected DeploymentWithDefinitions tryToRedeploy(RedeploymentDto redeployment) {
    RepositoryService repositoryService = getProcessEngine().getRepositoryService();

    DeploymentBuilder builder = repositoryService.createDeployment();
    builder.nameFromDeployment(deploymentId);

    String tenantId = getDeployment().getTenantId();
    if (tenantId != null) {
      builder.tenantId(tenantId);
    }

    if (redeployment != null) {
      builder = addRedeploymentResources(builder, redeployment);
    } else {
      builder.addDeploymentResources(deploymentId);
    }

    return builder.deployWithResult();
  }

  protected DeploymentBuilder addRedeploymentResources(DeploymentBuilder builder, RedeploymentDto redeployment) {
    builder.source(redeployment.getSource());

    List<String> resourceIds = redeployment.getResourceIds();
    List<String> resourceNames = redeployment.getResourceNames();

    boolean isResourceIdListEmpty = resourceIds == null || resourceIds.isEmpty();
    boolean isResourceNameListEmpty = resourceNames == null || resourceNames.isEmpty();

    if (isResourceIdListEmpty && isResourceNameListEmpty) {
      builder.addDeploymentResources(deploymentId);

    } else {
      if (!isResourceIdListEmpty) {
        builder.addDeploymentResourcesById(deploymentId, resourceIds);
      }
      if (!isResourceNameListEmpty) {
        builder.addDeploymentResourcesByName(deploymentId, resourceNames);
      }
    }
    return builder;
  }

  @Override
  public void deleteDeployment(String deploymentId, UriInfo uriInfo) {
    RepositoryService repositoryService = getProcessEngine().getRepositoryService();
    Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();

    if (deployment == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Deployment with id '" + deploymentId + "' do not exist");
    }

    boolean cascade = isQueryPropertyEnabled(uriInfo, CASCADE);
    boolean skipCustomListeners = isQueryPropertyEnabled(uriInfo, "skipCustomListeners");
    boolean skipIoMappings = isQueryPropertyEnabled(uriInfo, "skipIoMappings");

    repositoryService.deleteDeployment(deploymentId, cascade, skipCustomListeners, skipIoMappings);
  }

  protected boolean isQueryPropertyEnabled(UriInfo uriInfo, String property) {
    MultivaluedMap<String,String> queryParams = uriInfo.getQueryParameters();

    return queryParams.containsKey(property)
        && queryParams.get(property).size() > 0
        && "true".equals(queryParams.get(property).get(0));
  }

  protected InvalidRequestException createInvalidRequestException(String action, Status status, ProcessEngineException cause) {
    String errorMessage = String.format("Cannot %s deployment '%s': %s", action, deploymentId, cause.getMessage());
    return new InvalidRequestException(status, cause, errorMessage);
  }

}
