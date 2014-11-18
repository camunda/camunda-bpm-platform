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
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricCaseActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricCaseActivityInstanceQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricCaseActivityInstanceRestService;
import org.camunda.bpm.engine.rest.sub.history.HistoricCaseActivityInstanceResource;
import org.camunda.bpm.engine.rest.sub.history.impl.HistoricCaseActivityInstanceResourceImpl;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

public class HistoricCaseActivityInstanceRestServiceImpl implements HistoricCaseActivityInstanceRestService {

  protected ProcessEngine processEngine;
  protected ObjectMapper objectMapper;

  public HistoricCaseActivityInstanceRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  public HistoricCaseActivityInstanceResource getHistoricCaseInstance(String caseActivityInstanceId) {
    return new HistoricCaseActivityInstanceResourceImpl(processEngine, caseActivityInstanceId);
  }

  public List<HistoricCaseActivityInstanceDto> getHistoricCaseActivityInstances(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricCaseActivityInstanceQueryDto queryHistoricCaseActivityInstanceDto = new HistoricCaseActivityInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricCaseActivityInstances(queryHistoricCaseActivityInstanceDto, firstResult, maxResults);
  }

  public List<HistoricCaseActivityInstanceDto> queryHistoricCaseActivityInstances(HistoricCaseActivityInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    HistoricCaseActivityInstanceQuery query = queryDto.toQuery(processEngine);

    List<HistoricCaseActivityInstance> matchingHistoricCaseActivityInstances;
    if (firstResult != null || maxResults != null) {
      matchingHistoricCaseActivityInstances = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingHistoricCaseActivityInstances = query.list();
    }

    List<HistoricCaseActivityInstanceDto> historicCaseActivityInstanceResults = new ArrayList<HistoricCaseActivityInstanceDto>();
    for (HistoricCaseActivityInstance historicCaseActivityInstance : matchingHistoricCaseActivityInstances) {
      HistoricCaseActivityInstanceDto resultHistoricCaseActivityInstance = HistoricCaseActivityInstanceDto.fromHistoricCaseActivityInstance(historicCaseActivityInstance);
      historicCaseActivityInstanceResults.add(resultHistoricCaseActivityInstance);
    }
    return historicCaseActivityInstanceResults;
  }

  private List<HistoricCaseActivityInstance> executePaginatedQuery(HistoricCaseActivityInstanceQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  @Override
  public CountResultDto getHistoricCaseActivityInstancesCount(UriInfo uriInfo) {
    HistoricCaseActivityInstanceQueryDto queryDto = new HistoricCaseActivityInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricCaseActivityInstancesCount(queryDto);
  }

  public CountResultDto queryHistoricCaseActivityInstancesCount(HistoricCaseActivityInstanceQueryDto queryDto) {
    HistoricCaseActivityInstanceQuery query = queryDto.toQuery(processEngine);

    long count = query.count();

    return new CountResultDto(count);
  }
}
