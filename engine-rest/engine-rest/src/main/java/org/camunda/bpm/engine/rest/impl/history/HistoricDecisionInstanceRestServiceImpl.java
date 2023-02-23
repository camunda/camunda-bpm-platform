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
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.SetRemovalTimeSelectModeForHistoricDecisionInstancesBuilder;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricDecisionInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricDecisionInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.history.batch.DeleteHistoricDecisionInstancesDto;
import org.camunda.bpm.engine.rest.dto.history.batch.removaltime.SetRemovalTimeToHistoricDecisionInstancesDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.history.HistoricDecisionInstanceRestService;
import org.camunda.bpm.engine.rest.sub.history.HistoricDecisionInstanceResource;
import org.camunda.bpm.engine.rest.sub.history.impl.HistoricDecisionInstanceResourceImpl;
import org.camunda.bpm.engine.rest.util.QueryUtil;

public class HistoricDecisionInstanceRestServiceImpl implements HistoricDecisionInstanceRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricDecisionInstanceRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public HistoricDecisionInstanceResource getHistoricDecisionInstance(String decisionInstanceId) {
    return new HistoricDecisionInstanceResourceImpl(processEngine, decisionInstanceId);
  }

  @Override
  public List<HistoricDecisionInstanceDto> getHistoricDecisionInstances(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricDecisionInstanceQueryDto queryHistoricDecisionInstanceDto = new HistoricDecisionInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricDecisionInstances(queryHistoricDecisionInstanceDto, firstResult, maxResults);
  }

  public List<HistoricDecisionInstanceDto> queryHistoricDecisionInstances(HistoricDecisionInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    HistoricDecisionInstanceQuery query = queryDto.toQuery(processEngine);

    List<HistoricDecisionInstance> matchingHistoricDecisionInstances = QueryUtil.list(query, firstResult, maxResults);

    List<HistoricDecisionInstanceDto> historicDecisionInstanceDtoResults = new ArrayList<HistoricDecisionInstanceDto>();
    for (HistoricDecisionInstance historicDecisionInstance : matchingHistoricDecisionInstances) {
      HistoricDecisionInstanceDto resultHistoricDecisionInstanceDto = HistoricDecisionInstanceDto.fromHistoricDecisionInstance(historicDecisionInstance);
      historicDecisionInstanceDtoResults.add(resultHistoricDecisionInstanceDto);
    }
    return historicDecisionInstanceDtoResults;
  }

  @Override
  public CountResultDto getHistoricDecisionInstancesCount(UriInfo uriInfo) {
    HistoricDecisionInstanceQueryDto queryDto = new HistoricDecisionInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricDecisionInstancesCount(queryDto);
  }

  public CountResultDto queryHistoricDecisionInstancesCount(HistoricDecisionInstanceQueryDto queryDto) {
    HistoricDecisionInstanceQuery query = queryDto.toQuery(processEngine);

    long count = query.count();

    return new CountResultDto(count);
  }

  @Override
  public BatchDto deleteAsync(DeleteHistoricDecisionInstancesDto dto) {
    HistoricDecisionInstanceQuery decisionInstanceQuery = null;
    if (dto.getHistoricDecisionInstanceQuery() != null) {
      decisionInstanceQuery = dto.getHistoricDecisionInstanceQuery().toQuery(processEngine);
    }

    try {
      List<String> historicDecisionInstanceIds = dto.getHistoricDecisionInstanceIds();
      String deleteReason = dto.getDeleteReason();
      Batch batch = processEngine.getHistoryService().deleteHistoricDecisionInstancesAsync(historicDecisionInstanceIds, decisionInstanceQuery, deleteReason);
      return BatchDto.fromBatch(batch);
    }
    catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }

  @Override
  public BatchDto setRemovalTimeAsync(SetRemovalTimeToHistoricDecisionInstancesDto dto) {
    HistoryService historyService = processEngine.getHistoryService();

    HistoricDecisionInstanceQuery historicDecisionInstanceQuery = null;

    if (dto.getHistoricDecisionInstanceQuery() != null) {
      historicDecisionInstanceQuery = dto.getHistoricDecisionInstanceQuery().toQuery(processEngine);

    }

    SetRemovalTimeSelectModeForHistoricDecisionInstancesBuilder builder =
      historyService.setRemovalTimeToHistoricDecisionInstances();

    if (dto.isCalculatedRemovalTime()) {
      builder.calculatedRemovalTime();

    }

    Date removalTime = dto.getAbsoluteRemovalTime();
    if (dto.getAbsoluteRemovalTime() != null) {
      builder.absoluteRemovalTime(removalTime);

    }

    if (dto.isClearedRemovalTime()) {
      builder.clearedRemovalTime();

    }

    builder.byIds(dto.getHistoricDecisionInstanceIds());
    builder.byQuery(historicDecisionInstanceQuery);

    if (dto.isHierarchical()) {
      builder.hierarchical();

    }

    Batch batch = builder.executeAsync();
    return BatchDto.fromBatch(batch);
  }

}
