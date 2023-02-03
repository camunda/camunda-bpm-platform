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
package org.camunda.bpm.engine.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.rest.DecisionDefinitionRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.DecisionDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.DecisionDefinitionQueryDto;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.repository.DecisionDefinitionResource;
import org.camunda.bpm.engine.rest.sub.repository.impl.DecisionDefinitionResourceImpl;
import org.camunda.bpm.engine.rest.util.QueryUtil;

public class DecisionDefinitionRestServiceImpl extends AbstractRestProcessEngineAware implements DecisionDefinitionRestService {

  public DecisionDefinitionRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public DecisionDefinitionResource getDecisionDefinitionByKey(String decisionDefinitionKey) {

    DecisionDefinition decisionDefinition = getProcessEngine()
        .getRepositoryService()
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(decisionDefinitionKey)
        .withoutTenantId()
        .latestVersion()
        .singleResult();

    if (decisionDefinition == null) {
      String errorMessage = String.format("No matching decision definition with key: %s and no tenant-id", decisionDefinitionKey);
      throw new RestException(Status.NOT_FOUND, errorMessage);

    } else {
      return getDecisionDefinitionById(decisionDefinition.getId());
    }
  }

  @Override
  public DecisionDefinitionResource getDecisionDefinitionByKeyAndTenantId(String decisionDefinitionKey, String tenantId) {

    DecisionDefinition decisionDefinition = getProcessEngine()
        .getRepositoryService()
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(decisionDefinitionKey)
        .tenantIdIn(tenantId)
        .latestVersion()
        .singleResult();

    if (decisionDefinition == null) {
      String errorMessage = String.format("No matching decision definition with key: %s and tenant-id: %s", decisionDefinitionKey, tenantId);
      throw new RestException(Status.NOT_FOUND, errorMessage);

    } else {
      return getDecisionDefinitionById(decisionDefinition.getId());
    }
  }

  @Override
  public DecisionDefinitionResource getDecisionDefinitionById(String decisionDefinitionId) {
    return new DecisionDefinitionResourceImpl(getProcessEngine(), decisionDefinitionId, relativeRootResourcePath, getObjectMapper());
  }

  @Override
  public List<DecisionDefinitionDto> getDecisionDefinitions(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    DecisionDefinitionQueryDto queryDto = new DecisionDefinitionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    List<DecisionDefinitionDto> definitions = new ArrayList<DecisionDefinitionDto>();

    ProcessEngine engine = getProcessEngine();
    DecisionDefinitionQuery query = queryDto.toQuery(engine);

    List<DecisionDefinition> matchingDefinitions = QueryUtil.list(query, firstResult, maxResults);

    for (DecisionDefinition definition : matchingDefinitions) {
      DecisionDefinitionDto def = DecisionDefinitionDto.fromDecisionDefinition(definition);
      definitions.add(def);
    }
    return definitions;
  }

  @Override
  public CountResultDto getDecisionDefinitionsCount(UriInfo uriInfo) {
    DecisionDefinitionQueryDto queryDto = new DecisionDefinitionQueryDto(getObjectMapper(), uriInfo.getQueryParameters());

    ProcessEngine engine = getProcessEngine();
    DecisionDefinitionQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    return result;
  }

}
