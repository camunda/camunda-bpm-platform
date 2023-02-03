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
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;
import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.CleanableHistoricProcessInstanceReportDto;
import org.camunda.bpm.engine.rest.dto.history.CleanableHistoricProcessInstanceReportResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityStatisticsDto;
import org.camunda.bpm.engine.rest.history.HistoricProcessDefinitionRestService;
import org.camunda.bpm.engine.rest.impl.AbstractRestProcessEngineAware;
import org.camunda.bpm.engine.rest.util.QueryUtil;

public class HistoricProcessDefinitionRestServiceImpl extends AbstractRestProcessEngineAware implements HistoricProcessDefinitionRestService {

  public HistoricProcessDefinitionRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    super(processEngine.getName(), objectMapper);
  }

  @Override
  public List<HistoricActivityStatisticsDto> getHistoricActivityStatistics(UriInfo uriInfo, String processDefinitionId) {
    HistoricActivityStatisticsQueryDto queryDto = new HistoricActivityStatisticsQueryDto(getObjectMapper(), processDefinitionId, uriInfo.getQueryParameters());

    HistoricActivityStatisticsQuery query = queryDto.toQuery(getProcessEngine());

    List<HistoricActivityStatisticsDto> result = new ArrayList<>();

    List<HistoricActivityStatistics> statistics = query.unlimitedList();

    for (HistoricActivityStatistics currentStatistics : statistics) {
      result.add(HistoricActivityStatisticsDto.fromHistoricActivityStatistics(currentStatistics));
    }

    return result;
  }

  @Override
  public List<CleanableHistoricProcessInstanceReportResultDto> getCleanableHistoricProcessInstanceReport(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    CleanableHistoricProcessInstanceReportDto queryDto = new CleanableHistoricProcessInstanceReportDto(objectMapper, uriInfo.getQueryParameters());
    CleanableHistoricProcessInstanceReport query = queryDto.toQuery(getProcessEngine());

    List<CleanableHistoricProcessInstanceReportResult> reportResult = QueryUtil.list(query, firstResult, maxResults);

    return CleanableHistoricProcessInstanceReportResultDto.convert(reportResult);
  }

  @Override
  public CountResultDto getCleanableHistoricProcessInstanceReportCount(UriInfo uriInfo) {
    CleanableHistoricProcessInstanceReportDto queryDto = new CleanableHistoricProcessInstanceReportDto(objectMapper, uriInfo.getQueryParameters());
    queryDto.setObjectMapper(objectMapper);
    CleanableHistoricProcessInstanceReport query = queryDto.toQuery(getProcessEngine());

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }

}
