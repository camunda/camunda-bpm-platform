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
package org.camunda.bpm.engine.rest.impl;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.*;
import org.camunda.bpm.engine.rest.DeploymentRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.DeploymentDto;
import org.camunda.bpm.engine.rest.dto.repository.DeploymentQueryDto;
import org.camunda.bpm.engine.rest.dto.repository.DeploymentWithDefinitionsDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData.FormPart;
import org.camunda.bpm.engine.rest.sub.repository.DeploymentResource;
import org.camunda.bpm.engine.rest.sub.repository.impl.DeploymentResourceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DeploymentRestServiceImpl extends AbstractRestProcessEngineAware implements DeploymentRestService {

  public final static String DEPLOYMENT_NAME = "deployment-name";
  public final static String ENABLE_DUPLICATE_FILTERING = "enable-duplicate-filtering";
  public final static String DEPLOY_CHANGED_ONLY = "deploy-changed-only";
  public final static String DEPLOYMENT_SOURCE = "deployment-source";
  public final static String TENANT_ID = "tenant-id";

  protected static final Set<String> RESERVED_KEYWORDS = new HashSet<String>();

  static {
    RESERVED_KEYWORDS.add(DEPLOYMENT_NAME);
    RESERVED_KEYWORDS.add(ENABLE_DUPLICATE_FILTERING);
    RESERVED_KEYWORDS.add(DEPLOY_CHANGED_ONLY);
    RESERVED_KEYWORDS.add(DEPLOYMENT_SOURCE);
    RESERVED_KEYWORDS.add(TENANT_ID);
  }

	public DeploymentRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  public DeploymentResource getDeployment(String deploymentId) {
    return new DeploymentResourceImpl(getProcessEngine().getName(), deploymentId, relativeRootResourcePath, getObjectMapper());
  }

  public List<DeploymentDto> getDeployments(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    DeploymentQueryDto queryDto = new DeploymentQueryDto(getObjectMapper(), uriInfo.getQueryParameters());

    ProcessEngine engine = getProcessEngine();
    DeploymentQuery query = queryDto.toQuery(engine);

    List<Deployment> matchingDeployments;
    if (firstResult != null || maxResults != null) {
      matchingDeployments = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingDeployments = query.list();
    }

    List<DeploymentDto> deployments = new ArrayList<DeploymentDto>();
    for (Deployment deployment : matchingDeployments) {
      DeploymentDto def = DeploymentDto.fromDeployment(deployment);
      deployments.add(def);
    }
    return deployments;
  }

  public DeploymentWithDefinitionsDto createDeployment(UriInfo uriInfo, MultipartFormData payload) {
    DeploymentBuilder deploymentBuilder = extractDeploymentInformation(payload);

    if(!deploymentBuilder.getResourceNames().isEmpty()) {
      DeploymentWithDefinitions deployment = deploymentBuilder.deployWithResult();

      DeploymentWithDefinitionsDto deploymentDto = DeploymentWithDefinitionsDto.fromDeployment(deployment);


      URI uri = uriInfo.getBaseUriBuilder()
        .path(relativeRootResourcePath)
        .path(DeploymentRestService.PATH)
        .path(deployment.getId())
        .build();

      // GET
      deploymentDto.addReflexiveLink(uri, HttpMethod.GET, "self");

      return deploymentDto;

    } else {
      throw new InvalidRequestException(Status.BAD_REQUEST, "No deployment resources contained in the form upload.");
    }
  }

  private DeploymentBuilder extractDeploymentInformation(MultipartFormData payload) {
    DeploymentBuilder deploymentBuilder = getProcessEngine().getRepositoryService().createDeployment();

    Set<String> partNames = payload.getPartNames();

    for (String name : partNames) {
      FormPart part = payload.getNamedPart(name);

      if (!RESERVED_KEYWORDS.contains(name)) {
        deploymentBuilder.addInputStream(part.getFileName(), new ByteArrayInputStream(part.getBinaryContent()));
      }
    }

    FormPart deploymentName = payload.getNamedPart(DEPLOYMENT_NAME);
    if (deploymentName != null) {
      deploymentBuilder.name(deploymentName.getTextContent());
    }

    FormPart deploymentSource = payload.getNamedPart(DEPLOYMENT_SOURCE);
    if (deploymentSource != null) {
      deploymentBuilder.source(deploymentSource.getTextContent());
    }

    FormPart deploymentTenantId = payload.getNamedPart(TENANT_ID);
    if (deploymentTenantId != null) {
      deploymentBuilder.tenantId(deploymentTenantId.getTextContent());
    }

    extractDuplicateFilteringForDeployment(payload, deploymentBuilder);
    return deploymentBuilder;
  }

  private void extractDuplicateFilteringForDeployment(MultipartFormData payload, DeploymentBuilder deploymentBuilder) {
    boolean enableDuplicateFiltering = false;
    boolean deployChangedOnly = false;

    FormPart deploymentEnableDuplicateFiltering = payload.getNamedPart(ENABLE_DUPLICATE_FILTERING);
    if (deploymentEnableDuplicateFiltering != null) {
      enableDuplicateFiltering = Boolean.parseBoolean(deploymentEnableDuplicateFiltering.getTextContent());
    }

    FormPart deploymentDeployChangedOnly = payload.getNamedPart(DEPLOY_CHANGED_ONLY);
    if (deploymentDeployChangedOnly != null) {
      deployChangedOnly = Boolean.parseBoolean(deploymentDeployChangedOnly.getTextContent());
    }

    // deployChangedOnly overrides the enableDuplicateFiltering setting
    if (deployChangedOnly) {
      deploymentBuilder.enableDuplicateFiltering(true);
    } else if (enableDuplicateFiltering) {
      deploymentBuilder.enableDuplicateFiltering(false);
    }
  }

  private List<Deployment> executePaginatedQuery(DeploymentQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  public CountResultDto getDeploymentsCount(UriInfo uriInfo) {
    DeploymentQueryDto queryDto = new DeploymentQueryDto(getObjectMapper(), uriInfo.getQueryParameters());

    ProcessEngine engine = getProcessEngine();
    DeploymentQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    return result;
  }

}
