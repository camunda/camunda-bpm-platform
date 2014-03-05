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
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricTaskInstanceRestService;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricTaskInstanceRestServiceImpl implements HistoricTaskInstanceRestService {

  protected ProcessEngine processEngine;

  public HistoricTaskInstanceRestServiceImpl(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  @Override
  public List<HistoricTaskInstanceDto> getHistoricTaskInstances(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricTaskInstanceQueryDto queryDto = new HistoricTaskInstanceQueryDto(uriInfo.getQueryParameters());
    return queryHistoricTaskInstances(queryDto, firstResult, maxResults);
  }

  @Override
  public List<HistoricTaskInstanceDto> queryHistoricTaskInstances(HistoricTaskInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    HistoricTaskInstanceQuery query = queryDto.toQuery(processEngine);

    List<HistoricTaskInstance> match;
    if (firstResult != null || maxResults != null) {
      match = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      match = query.list();
    }

    List<HistoricTaskInstanceDto> result = new ArrayList<HistoricTaskInstanceDto>();
    for (HistoricTaskInstance taskInstance : match) {
      HistoricTaskInstanceDto taskInstanceDto = HistoricTaskInstanceDto.fromHistoricTaskInstance(taskInstance);
      result.add(taskInstanceDto);
    }
    return result;
  }

  private List<HistoricTaskInstance> executePaginatedQuery(HistoricTaskInstanceQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  @Override
  public CountResultDto getHistoricTaskInstancesCount(UriInfo uriInfo) {
    HistoricTaskInstanceQueryDto queryDto = new HistoricTaskInstanceQueryDto(uriInfo.getQueryParameters());
    return queryHistoricTaskInstancesCount(queryDto);
  }

  @Override
  public CountResultDto queryHistoricTaskInstancesCount(HistoricTaskInstanceQueryDto queryDto) {
    HistoricTaskInstanceQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

}
