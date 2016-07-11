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

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinitionQuery;
import org.camunda.bpm.engine.rest.DecisionRequirementsDefinitionRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.DecisionRequirementsDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.DecisionRequirementsDefinitionQueryDto;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.repository.DecisionRequirementsDefinitionResource;
import org.camunda.bpm.engine.rest.sub.repository.impl.DecisionRequirementsDefinitionResourceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DecisionRequirementsDefinitionRestServiceImpl extends AbstractRestProcessEngineAware implements DecisionRequirementsDefinitionRestService {

  public DecisionRequirementsDefinitionRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public List<DecisionRequirementsDefinitionDto> getDecisionRequirementsDefinitions(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    DecisionRequirementsDefinitionQueryDto queryDto = new DecisionRequirementsDefinitionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    List<DecisionRequirementsDefinitionDto> dtos = new ArrayList<DecisionRequirementsDefinitionDto>();

    ProcessEngine engine = getProcessEngine();
    DecisionRequirementsDefinitionQuery query = queryDto.toQuery(engine);

    List<DecisionRequirementsDefinition> matchingDefinitions = null;

    if (firstResult != null || maxResults != null) {
      matchingDefinitions = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingDefinitions = query.list();
    }

    for (DecisionRequirementsDefinition definition : matchingDefinitions) {
      DecisionRequirementsDefinitionDto dto = DecisionRequirementsDefinitionDto.fromDecisionRequirementsDefinition(definition);
      dtos.add(dto);
    }
    return dtos;
  }

  private List<DecisionRequirementsDefinition> executePaginatedQuery(DecisionRequirementsDefinitionQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  @Override
  public CountResultDto getDecisionRequirementsDefinitionsCount(UriInfo uriInfo) {
    DecisionRequirementsDefinitionQueryDto queryDto = new DecisionRequirementsDefinitionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());

    ProcessEngine engine = getProcessEngine();
    DecisionRequirementsDefinitionQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    return result;
  }

  @Override
  public DecisionRequirementsDefinitionResource getDecisionRequirementsDefinitionById(String decisionRequirementsDefinitionId) {
    return new DecisionRequirementsDefinitionResourceImpl(getProcessEngine(), decisionRequirementsDefinitionId);
  }

  @Override
  public DecisionRequirementsDefinitionResource getDecisionRequirementsDefinitionByKey(String decisionRequirementsDefinitionKey) {
    DecisionRequirementsDefinition decisionRequirementsDefinition = getProcessEngine().getRepositoryService()
      .createDecisionRequirementsDefinitionQuery()
      .decisionRequirementsDefinitionKey(decisionRequirementsDefinitionKey)
      .withoutTenantId().latestVersion().singleResult();
    
    if (decisionRequirementsDefinition == null) {
      String errorMessage = String.format("No matching decision requirements definition with key: %s and no tenant-id", decisionRequirementsDefinitionKey);
      throw new RestException(Status.NOT_FOUND, errorMessage);
      
    } else {
      return getDecisionRequirementsDefinitionById(decisionRequirementsDefinition.getId());
    }
  }

  @Override
  public DecisionRequirementsDefinitionResource getDecisionRequirementsDefinitionByKeyAndTenantId(String decisionRequirementsDefinitionKey, String tenantId) {
    DecisionRequirementsDefinition decisionRequirementsDefinition = getProcessEngine().getRepositoryService()
      .createDecisionRequirementsDefinitionQuery()
      .decisionRequirementsDefinitionKey(decisionRequirementsDefinitionKey)
      .tenantIdIn(tenantId).latestVersion().singleResult();
    
    if (decisionRequirementsDefinition == null) {
      String errorMessage = String.format("No matching decision requirements definition with key: %s and tenant-id: %s", decisionRequirementsDefinitionKey, tenantId);
      throw new RestException(Status.NOT_FOUND, errorMessage);
      
    } else {
      return getDecisionRequirementsDefinitionById(decisionRequirementsDefinition.getId());
    }
  }

}
