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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityInstanceQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricActivityInstanceRestService;

public class HistoricActivityInstanceRestServiceImpl implements HistoricActivityInstanceRestService {

  protected ProcessEngine processEngine;

  public HistoricActivityInstanceRestServiceImpl(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  @Override
  public List<HistoricActivityInstanceDto> getHistoricActivityInstances(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricActivityInstanceQueryDto queryHistoricActivityInstanceDto = new HistoricActivityInstanceQueryDto(uriInfo.getQueryParameters());
    return queryHistoricActivityInstances(queryHistoricActivityInstanceDto, firstResult, maxResults);
  }

  @Override
  public List<HistoricActivityInstanceDto> queryHistoricActivityInstances(HistoricActivityInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    HistoricActivityInstanceQuery query = queryDto.toQuery(processEngine);

    List<HistoricActivityInstance> matchingHistoricActivityInstances;
    if (firstResult != null || maxResults != null) {
      matchingHistoricActivityInstances = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingHistoricActivityInstances = query.list();
    }

    List<HistoricActivityInstanceDto> historicActivityInstanceResults = new ArrayList<HistoricActivityInstanceDto>();
    for (HistoricActivityInstance historicActivityInstance : matchingHistoricActivityInstances) {
      HistoricActivityInstanceDto resultHistoricActivityInstance = HistoricActivityInstanceDto.fromHistoricActivityInstance(historicActivityInstance);
      historicActivityInstanceResults.add(resultHistoricActivityInstance);
    }
    return historicActivityInstanceResults;
  }

  private List<HistoricActivityInstance> executePaginatedQuery(HistoricActivityInstanceQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  @Override
  public CountResultDto getHistoricActivityInstancesCount(UriInfo uriInfo) {
    HistoricActivityInstanceQueryDto queryDto = new HistoricActivityInstanceQueryDto(uriInfo.getQueryParameters());
    return queryHistoricActivityInstancesCount(queryDto);
  }

  @Override
  public CountResultDto queryHistoricActivityInstancesCount(HistoricActivityInstanceQueryDto queryDto) {
    HistoricActivityInstanceQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }
}
