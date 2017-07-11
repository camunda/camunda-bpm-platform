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

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricCaseActivityStatistics;
import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReportResult;
import org.camunda.bpm.engine.rest.dto.history.HistoricCaseActivityStatisticsDto;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.CleanableHistoricCaseInstanceReportDto;
import org.camunda.bpm.engine.rest.dto.history.CleanableHistoricCaseInstanceReportResultDto;
import org.camunda.bpm.engine.rest.history.HistoricCaseDefinitionRestService;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricCaseDefinitionRestServiceImpl implements HistoricCaseDefinitionRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricCaseDefinitionRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  public List<HistoricCaseActivityStatisticsDto> getHistoricCaseActivityStatistics(String caseDefinitionId) {
    HistoryService historyService = processEngine.getHistoryService();

    List<HistoricCaseActivityStatistics> statistics = historyService.createHistoricCaseActivityStatisticsQuery(caseDefinitionId).list();

    List<HistoricCaseActivityStatisticsDto> result = new ArrayList<HistoricCaseActivityStatisticsDto>();
    for (HistoricCaseActivityStatistics currentStatistics : statistics) {
      result.add(HistoricCaseActivityStatisticsDto.fromHistoricCaseActivityStatistics(currentStatistics));
    }

    return result;
  }

  @Override
  public List<CleanableHistoricCaseInstanceReportResultDto> getCleanableHistoricCaseInstanceReport(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    CleanableHistoricCaseInstanceReportDto queryDto = new CleanableHistoricCaseInstanceReportDto(objectMapper, uriInfo.getQueryParameters());
    CleanableHistoricCaseInstanceReport query = queryDto.toQuery(processEngine);

    List<CleanableHistoricCaseInstanceReportResult> reportResult;
    if (firstResult != null || maxResults != null) {
    reportResult = executePaginatedQuery(query, firstResult, maxResults);
    } else {
    reportResult = query.list();
    }

    return CleanableHistoricCaseInstanceReportResultDto.convert(reportResult);
  }

  private List<CleanableHistoricCaseInstanceReportResult> executePaginatedQuery(CleanableHistoricCaseInstanceReport query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

  @Override
  public CountResultDto getCleanableHistoricCaseInstanceReportCount(UriInfo uriInfo) {
    CleanableHistoricCaseInstanceReportDto queryDto = new CleanableHistoricCaseInstanceReportDto(objectMapper, uriInfo.getQueryParameters());
    queryDto.setObjectMapper(objectMapper);
    CleanableHistoricCaseInstanceReport query = queryDto.toQuery(processEngine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
  }
}
