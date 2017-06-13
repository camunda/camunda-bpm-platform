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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.rest.IncidentRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.IncidentDto;
import org.camunda.bpm.engine.rest.dto.runtime.IncidentQueryDto;
import org.camunda.bpm.engine.rest.sub.repository.impl.IncidentResourceImpl;
import org.camunda.bpm.engine.rest.sub.runtime.IncidentResource;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Smirnov
 *
 */
public class IncidentRestServiceImpl extends AbstractRestProcessEngineAware implements IncidentRestService {

  public IncidentRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, objectMapper);
  }

  @Override
  public List<IncidentDto> getIncidents(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    IncidentQueryDto queryDto = new IncidentQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    IncidentQuery query = queryDto.toQuery(processEngine);

    List<Incident> queryResult;
    if (firstResult != null || maxResults != null) {
      queryResult = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      queryResult = query.list();
    }

    List<IncidentDto> result = new ArrayList<IncidentDto>();
    for (Incident incident : queryResult) {
      IncidentDto dto = IncidentDto.fromIncident(incident);
      result.add(dto);
    }

    return result;
  }

  @Override
  public CountResultDto getIncidentsCount(UriInfo uriInfo) {
    IncidentQueryDto queryDto = new IncidentQueryDto(getObjectMapper(), uriInfo.getQueryParameters());
    IncidentQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  private List<Incident> executePaginatedQuery(IncidentQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  @Override
  public IncidentResource getIncident(String incidentId) {
    return new IncidentResourceImpl(getProcessEngine(), incidentId, getObjectMapper());
  }
}
