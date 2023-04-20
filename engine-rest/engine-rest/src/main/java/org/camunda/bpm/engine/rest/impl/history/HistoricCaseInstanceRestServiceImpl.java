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
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstanceQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricCaseInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricCaseInstanceQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricCaseInstanceRestService;
import org.camunda.bpm.engine.rest.sub.history.HistoricCaseInstanceResource;
import org.camunda.bpm.engine.rest.sub.history.impl.HistoricCaseInstanceResourceImpl;
import org.camunda.bpm.engine.rest.util.QueryUtil;

public class HistoricCaseInstanceRestServiceImpl implements HistoricCaseInstanceRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricCaseInstanceRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public HistoricCaseInstanceResource getHistoricCaseInstance(String caseInstanceId) {
    return new HistoricCaseInstanceResourceImpl(processEngine, caseInstanceId);
  }

  @Override
  public List<HistoricCaseInstanceDto> getHistoricCaseInstances(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricCaseInstanceQueryDto queryHistoricCaseInstanceDto = new HistoricCaseInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricCaseInstances(queryHistoricCaseInstanceDto, firstResult, maxResults);
  }

  @Override
  public List<HistoricCaseInstanceDto> queryHistoricCaseInstances(HistoricCaseInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    HistoricCaseInstanceQuery query = queryDto.toQuery(processEngine);

    List<HistoricCaseInstance> matchingHistoricCaseInstances = QueryUtil.list(query, firstResult, maxResults);

    List<HistoricCaseInstanceDto> historicCaseInstanceDtoResults = new ArrayList<HistoricCaseInstanceDto>();
    for (HistoricCaseInstance historicCaseInstance : matchingHistoricCaseInstances) {
      HistoricCaseInstanceDto resultHistoricCaseInstanceDto = HistoricCaseInstanceDto.fromHistoricCaseInstance(historicCaseInstance);
      historicCaseInstanceDtoResults.add(resultHistoricCaseInstanceDto);
    }
    return historicCaseInstanceDtoResults;
  }

  @Override
  public CountResultDto getHistoricCaseInstancesCount(UriInfo uriInfo) {
    HistoricCaseInstanceQueryDto queryDto = new HistoricCaseInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricCaseInstancesCount(queryDto);
  }

  @Override
  public CountResultDto queryHistoricCaseInstancesCount(HistoricCaseInstanceQueryDto queryDto) {
    HistoricCaseInstanceQuery query = queryDto.toQuery(processEngine);

    long count = query.count();

    return new CountResultDto(count);
  }

}
