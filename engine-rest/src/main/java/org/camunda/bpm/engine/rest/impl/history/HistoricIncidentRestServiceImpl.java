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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIncidentDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIncidentQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricIncidentRestService;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricIncidentRestServiceImpl implements HistoricIncidentRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricIncidentRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public List<HistoricIncidentDto> getHistoricIncidents(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricIncidentQueryDto queryDto = new HistoricIncidentQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricIncidentQuery query = queryDto.toQuery(processEngine);

    List<HistoricIncident> queryResult;
    if (firstResult != null || maxResults != null) {
      queryResult = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      queryResult = query.list();
    }

    List<HistoricIncidentDto> result = new ArrayList<HistoricIncidentDto>();
    for (HistoricIncident historicIncident : queryResult) {
      HistoricIncidentDto dto = HistoricIncidentDto.fromHistoricIncident(historicIncident);
      result.add(dto);
    }

    return result;
  }

  @Override
  public CountResultDto getHistoricIncidentsCount(UriInfo uriInfo) {
    HistoricIncidentQueryDto queryDto = new HistoricIncidentQueryDto(objectMapper, uriInfo.getQueryParameters());
    HistoricIncidentQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  private List<HistoricIncident> executePaginatedQuery(HistoricIncidentQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

}
