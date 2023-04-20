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
package org.camunda.bpm.engine.rest.impl.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricVariableInstanceRestService;
import org.camunda.bpm.engine.rest.sub.history.HistoricVariableInstanceResource;
import org.camunda.bpm.engine.rest.sub.history.impl.HistoricVariableInstanceResourceImpl;
import org.camunda.bpm.engine.rest.util.QueryUtil;

public class HistoricVariableInstanceRestServiceImpl implements HistoricVariableInstanceRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricVariableInstanceRestServiceImpl(ObjectMapper objectMapper,ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public HistoricVariableInstanceResource variableInstanceResource(String variableId) {
    return new HistoricVariableInstanceResourceImpl(variableId, processEngine);
  }

  @Override
  public List<HistoricVariableInstanceDto> getHistoricVariableInstances(UriInfo uriInfo, Integer firstResult,
      Integer maxResults, boolean deserializeObjectValues) {
    HistoricVariableInstanceQueryDto queryDto = new HistoricVariableInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricVariableInstances(queryDto, firstResult, maxResults, deserializeObjectValues);
  }

  @Override
  public List<HistoricVariableInstanceDto> queryHistoricVariableInstances(HistoricVariableInstanceQueryDto queryDto,
      Integer firstResult, Integer maxResults, boolean deserializeObjectValues) {
    queryDto.setObjectMapper(objectMapper);
    HistoricVariableInstanceQuery query = queryDto.toQuery(processEngine);
    query.disableBinaryFetching();

    if (!deserializeObjectValues) {
      query.disableCustomObjectDeserialization();
    }

    List<HistoricVariableInstance> matchingHistoricVariableInstances = QueryUtil.list(query, firstResult, maxResults);

    List<HistoricVariableInstanceDto> historicVariableInstanceDtoResults = new ArrayList<HistoricVariableInstanceDto>();
    for (HistoricVariableInstance historicVariableInstance : matchingHistoricVariableInstances) {
      HistoricVariableInstanceDto resultHistoricVariableInstance = HistoricVariableInstanceDto.fromHistoricVariableInstance(historicVariableInstance);
      historicVariableInstanceDtoResults.add(resultHistoricVariableInstance);
    }
    return historicVariableInstanceDtoResults;
  }

  @Override
  public CountResultDto getHistoricVariableInstancesCount(UriInfo uriInfo) {
    HistoricVariableInstanceQueryDto queryDto = new HistoricVariableInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricVariableInstancesCount(queryDto);
  }

  @Override
  public CountResultDto queryHistoricVariableInstancesCount(HistoricVariableInstanceQueryDto queryDto) {
    queryDto.setObjectMapper(objectMapper);
    HistoricVariableInstanceQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }
}
