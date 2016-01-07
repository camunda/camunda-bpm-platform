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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.ReportResult;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceReportDto;
import org.camunda.bpm.engine.rest.dto.history.ReportResultDto;
import org.camunda.bpm.engine.rest.history.HistoricProcessInstanceRestService;
import org.camunda.bpm.engine.rest.sub.history.HistoricProcessInstanceResource;
import org.camunda.bpm.engine.rest.sub.history.impl.HistoricProcessInstanceResourceImpl;

import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricProcessInstanceRestServiceImpl implements HistoricProcessInstanceRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricProcessInstanceRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public HistoricProcessInstanceResource getHistoricProcessInstance(String processInstanceId) {
    return new HistoricProcessInstanceResourceImpl(processEngine, processInstanceId);
  }

  @Override
  public List<HistoricProcessInstanceDto> getHistoricProcessInstances(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    HistoricProcessInstanceQueryDto queryHistoriProcessInstanceDto = new HistoricProcessInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricProcessInstances(queryHistoriProcessInstanceDto, firstResult, maxResults);
  }

  @Override
  public List<HistoricProcessInstanceDto> queryHistoricProcessInstances(HistoricProcessInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    queryDto.setObjectMapper(objectMapper);
    HistoricProcessInstanceQuery query = queryDto.toQuery(processEngine);

    List<HistoricProcessInstance> matchingHistoricProcessInstances;
    if (firstResult != null || maxResults != null) {
      matchingHistoricProcessInstances = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingHistoricProcessInstances = query.list();
    }

    List<HistoricProcessInstanceDto> historicProcessInstanceDtoResults = new ArrayList<HistoricProcessInstanceDto>();
    for (HistoricProcessInstance historicProcessInstance : matchingHistoricProcessInstances) {
      HistoricProcessInstanceDto resultHistoricProcessInstanceDto = HistoricProcessInstanceDto.fromHistoricProcessInstance(historicProcessInstance);
      historicProcessInstanceDtoResults.add(resultHistoricProcessInstanceDto);
    }
    return historicProcessInstanceDtoResults;
  }

  private List<HistoricProcessInstance> executePaginatedQuery(HistoricProcessInstanceQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  @Override
  public CountResultDto getHistoricProcessInstancesCount(UriInfo uriInfo) {
    HistoricProcessInstanceQueryDto queryDto = new HistoricProcessInstanceQueryDto(objectMapper, uriInfo.getQueryParameters());
    return queryHistoricProcessInstancesCount(queryDto);
  }

  @Override
  public CountResultDto queryHistoricProcessInstancesCount(HistoricProcessInstanceQueryDto queryDto) {
    queryDto.setObjectMapper(objectMapper);
    HistoricProcessInstanceQuery query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

  @Override
  public List<ReportResultDto> getHistoricProcessInstancesReport(UriInfo uriInfo) {
    HistoricProcessInstanceReportDto reportDto = new HistoricProcessInstanceReportDto(objectMapper, uriInfo.getQueryParameters());
    List<? extends ReportResult> reports = reportDto.executeReport(processEngine);

    List<ReportResultDto> result = new ArrayList<ReportResultDto>();
    for (ReportResult report : reports) {
      result.add(ReportResultDto.fromReportResult(report));
    }

    return result;
  }
}
