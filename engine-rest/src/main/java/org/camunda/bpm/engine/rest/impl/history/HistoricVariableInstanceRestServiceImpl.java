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
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricVariableInstanceRestService;
import org.camunda.bpm.engine.rest.sub.history.HistoricVariableInstanceResource;
import org.camunda.bpm.engine.rest.sub.history.impl.HistoricVariableInstanceResourceImpl;

public class HistoricVariableInstanceRestServiceImpl implements HistoricVariableInstanceRestService {

  protected ProcessEngine processEngine;

  public HistoricVariableInstanceRestServiceImpl(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  public HistoricVariableInstanceResource variableInstanceResource(String variableId) {
    return new HistoricVariableInstanceResourceImpl(variableId, processEngine);
  }

  @Override
  public List<HistoricVariableInstanceDto> getHistoricVariableInstances(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricVariableInstanceQueryDto queryDto = new HistoricVariableInstanceQueryDto(uriInfo.getQueryParameters());
    return queryHistoricVariableInstances(queryDto, firstResult, maxResults);
  }

  @Override
  public List<HistoricVariableInstanceDto> queryHistoricVariableInstances(HistoricVariableInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    HistoricVariableInstanceQuery query = queryDto.toQuery(processEngine);
    query.disableBinaryFetching();

    List<HistoricVariableInstance> matchingHistoricVariableInstances;
    if (firstResult != null || maxResults != null) {
      matchingHistoricVariableInstances = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingHistoricVariableInstances = query.list();
    }

    List<HistoricVariableInstanceDto> historicVariableInstanceDtoResults = new ArrayList<HistoricVariableInstanceDto>();
    for (HistoricVariableInstance historicVariableInstance : matchingHistoricVariableInstances) {
      HistoricVariableInstanceDto resultHistoricVariableInstance = HistoricVariableInstanceDto.fromHistoricVariableInstance(historicVariableInstance);
      historicVariableInstanceDtoResults.add(resultHistoricVariableInstance);
    }
    return historicVariableInstanceDtoResults;
  }

  private List<HistoricVariableInstance> executePaginatedQuery(HistoricVariableInstanceQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  @Override
  public CountResultDto getHistoricVariableInstancesCount(UriInfo uriInfo) {
    HistoricVariableInstanceQueryDto queryDto = new HistoricVariableInstanceQueryDto(uriInfo.getQueryParameters());
    return queryHistoricVariableInstancesCount(queryDto);
  }

  @Override
  public CountResultDto queryHistoricVariableInstancesCount(HistoricVariableInstanceQueryDto queryDto) {
    HistoricVariableInstanceQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }
}
