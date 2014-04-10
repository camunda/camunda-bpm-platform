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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.rest.DeploymentRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.DeploymentDto;
import org.camunda.bpm.engine.rest.dto.repository.DeploymentQueryDto;
import org.camunda.bpm.engine.rest.sub.repository.DeploymentResource;
import org.camunda.bpm.engine.rest.sub.repository.DeploymentResourceImpl;

public class DeploymentRestServiceImpl extends AbstractRestProcessEngineAware implements DeploymentRestService {

  public DeploymentRestServiceImpl() {
    super();
  }

	public DeploymentRestServiceImpl(String engineName) {
    super(engineName);
  }

  @Override
  public DeploymentResource getDeploymentById(String deploymentId) {
    return new DeploymentResourceImpl(getProcessEngine(), deploymentId);
  }

  @Override
  public List<DeploymentDto> getDeployments(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    DeploymentQueryDto queryDto = new DeploymentQueryDto(uriInfo.getQueryParameters());
    List<DeploymentDto> deployments = new ArrayList<DeploymentDto>();

    ProcessEngine engine = getProcessEngine();
    DeploymentQuery query = queryDto.toQuery(engine);

    List<Deployment> matchingDeployments = null;

    if (firstResult != null || maxResults != null) {
      matchingDeployments = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingDeployments = query.list();
    }

    for (Deployment deployment : matchingDeployments) {
      DeploymentDto def = DeploymentDto.fromDeployment(deployment);
      deployments.add(def);
    }
    return deployments;
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

  @Override
  public CountResultDto getDeploymentsCount(UriInfo uriInfo) {
    DeploymentQueryDto queryDto = new DeploymentQueryDto(uriInfo.getQueryParameters());

    ProcessEngine engine = getProcessEngine();
    DeploymentQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    return result;
  }

}
