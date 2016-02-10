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
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.rest.CaseDefinitionRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.CaseDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.CaseDefinitionQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.repository.CaseDefinitionResource;
import org.camunda.bpm.engine.rest.sub.repository.impl.CaseDefinitionResourceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Roman Smirnov
 *
 */
public class CaseDefinitionRestServiceImpl extends AbstractRestProcessEngineAware implements CaseDefinitionRestService {

  public CaseDefinitionRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public CaseDefinitionResource getCaseDefinitionByKey(String caseDefinitionKey) {
    if(caseDefinitionKey == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Query parameter 'caseDefinitionKey' cannot be null");
    }

    ProcessEngine engine = getProcessEngine();
    RepositoryService repositoryService = engine.getRepositoryService();

    List<CaseDefinition> caseDefinitions = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey(caseDefinitionKey)
      .latestVersion()
      .list();

    if (caseDefinitions.isEmpty()) {
      String errorMessage = String.format("No matching case definition with key: %s ", caseDefinitionKey);
      throw new RestException(Status.NOT_FOUND, errorMessage);

    } else if(caseDefinitions.size() > 1) {
      String errorMessage = String.format(
          "Found multiple case definition with key '%s' for different tenants. You have to request the case definition by id instead of key.",
          caseDefinitionKey);
      throw new RestException(Status.BAD_REQUEST, errorMessage);

    } else {
      CaseDefinition caseDefinition = caseDefinitions.get(0);
      return getCaseDefinitionById(caseDefinition.getId());
    }
  }

  @Override
  public CaseDefinitionResource getCaseDefinitionById(String caseDefinitionId) {
    return new CaseDefinitionResourceImpl(getProcessEngine(), caseDefinitionId, relativeRootResourcePath, getObjectMapper());
  }

  @Override
  public List<CaseDefinitionDto> getCaseDefinitions(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    CaseDefinitionQueryDto queryDto = new CaseDefinitionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    List<CaseDefinitionDto> definitions = new ArrayList<CaseDefinitionDto>();

    ProcessEngine engine = getProcessEngine();
    CaseDefinitionQuery query = queryDto.toQuery(engine);

    List<CaseDefinition> matchingDefinitions = null;

    if (firstResult != null || maxResults != null) {
      matchingDefinitions = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingDefinitions = query.list();
    }

    for (CaseDefinition definition : matchingDefinitions) {
      CaseDefinitionDto def = CaseDefinitionDto.fromCaseDefinition(definition);
      definitions.add(def);
    }
    return definitions;
  }

  private List<CaseDefinition> executePaginatedQuery(CaseDefinitionQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  @Override
  public CountResultDto getCaseDefinitionsCount(UriInfo uriInfo) {
    CaseDefinitionQueryDto queryDto = new CaseDefinitionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());

    ProcessEngine engine = getProcessEngine();
    CaseDefinitionQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    return result;
  }

}
