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
package org.camunda.bpm.engine.rest.impl.history;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricDecisionInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricDecisionInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.history.batch.DeleteHistoricDecisionInstancesDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.history.HistoricDecisionInstanceRestService;
import org.camunda.bpm.engine.rest.sub.history.HistoricDecisionInstanceResource;
import org.camunda.bpm.engine.rest.sub.history.impl.HistoricDecisionInstanceResourceImpl;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricDecisionInstanceRestServiceImpl implements HistoricDecisionInstanceRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricDecisionInstanceRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  public HistoricDecisionInstanceResource getHistoricDecisionInstance(String decisionInstanceId) {
    return new HistoricDecisionInstanceResourceImpl(processEngine, decisionInstanceId);
  }

  public List<HistoricDecisionInstanceDto> getHistoricDecisionInstances(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricDecisionInstanceQueryDto queryHistoricDecisionInstanceDto = new HistoricDecisionInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricDecisionInstances(queryHistoricDecisionInstanceDto, firstResult, maxResults);
  }

  public List<HistoricDecisionInstanceDto> queryHistoricDecisionInstances(HistoricDecisionInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    HistoricDecisionInstanceQuery query = queryDto.toQuery(processEngine);

    List<HistoricDecisionInstance> matchingHistoricDecisionInstances;
    if (firstResult != null || maxResults != null) {
      matchingHistoricDecisionInstances = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingHistoricDecisionInstances = query.list();
    }

    List<HistoricDecisionInstanceDto> historicDecisionInstanceDtoResults = new ArrayList<HistoricDecisionInstanceDto>();
    for (HistoricDecisionInstance historicDecisionInstance : matchingHistoricDecisionInstances) {
      HistoricDecisionInstanceDto resultHistoricDecisionInstanceDto = HistoricDecisionInstanceDto.fromHistoricDecisionInstance(historicDecisionInstance);
      historicDecisionInstanceDtoResults.add(resultHistoricDecisionInstanceDto);
    }
    return historicDecisionInstanceDtoResults;
  }

  private List<HistoricDecisionInstance> executePaginatedQuery(HistoricDecisionInstanceQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  public CountResultDto getHistoricDecisionInstancesCount(UriInfo uriInfo) {
    HistoricDecisionInstanceQueryDto queryDto = new HistoricDecisionInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricDecisionInstancesCount(queryDto);
  }

  public CountResultDto queryHistoricDecisionInstancesCount(HistoricDecisionInstanceQueryDto queryDto) {
    HistoricDecisionInstanceQuery query = queryDto.toQuery(processEngine);

    long count = query.count();

    return new CountResultDto(count);
  }

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

}
