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

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;
import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityStatisticsDto;
import org.camunda.bpm.engine.rest.dto.history.CleanableHistoricProcessInstanceReportResultDto;
import org.camunda.bpm.engine.rest.dto.history.CleanableHistoricProcessInstanceReportDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.history.HistoricProcessDefinitionRestService;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

public class HistoricProcessDefinitionRestServiceImpl implements HistoricProcessDefinitionRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricProcessDefinitionRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public List<HistoricActivityStatisticsDto> getHistoricActivityStatistics(String processDefinitionId, Boolean includeCanceled, Boolean includeFinished,
      Boolean includeCompleteScope, String sortBy, String sortOrder) {
    HistoryService historyService = processEngine.getHistoryService();

    HistoricActivityStatisticsQuery query = historyService.createHistoricActivityStatisticsQuery(processDefinitionId);

    if (includeCanceled != null && includeCanceled) {
      query.includeCanceled();
    }

    if (includeFinished != null && includeFinished) {
      query.includeFinished();
    }

    if (includeCompleteScope != null && includeCompleteScope) {
      query.includeCompleteScope();
    }

    setSortOptions(query, sortOrder, sortBy);

    List<HistoricActivityStatisticsDto> result = new ArrayList<HistoricActivityStatisticsDto>();

    List<HistoricActivityStatistics> statistics = query.list();

    for (HistoricActivityStatistics currentStatistics : statistics) {
      result.add(HistoricActivityStatisticsDto.fromHistoricActivityStatistics(currentStatistics));
    }

    return result;
  }

  private void setSortOptions(HistoricActivityStatisticsQuery query, String sortOrder, String sortBy) {
    boolean sortOptionsValid = (sortBy != null && sortOrder != null) || (sortBy == null && sortOrder == null);

    if (!sortOptionsValid) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Only a single sorting parameter specified. sortBy and sortOrder required");
    }

    if (sortBy != null) {
      if (sortBy.equals("activityId")) {
        query.orderByActivityId();
      } else {
        throw new InvalidRequestException(Status.BAD_REQUEST, "sortBy parameter has invalid value: " + sortBy);
      }
    }

    if (sortOrder != null) {
      if (sortOrder.equals("asc")) {
        query.asc();
      } else
      if (sortOrder.equals("desc")) {
        query.desc();
      } else {
        throw new InvalidRequestException(Status.BAD_REQUEST, "sortOrder parameter has invalid value: " + sortOrder);
      }
    }

  }

  @Override
  public List<CleanableHistoricProcessInstanceReportResultDto> getCleanableHistoricProcessInstanceReport(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    CleanableHistoricProcessInstanceReportDto queryDto = new CleanableHistoricProcessInstanceReportDto(objectMapper, uriInfo.getQueryParameters());
    CleanableHistoricProcessInstanceReport query = queryDto.toQuery(processEngine);

    List<CleanableHistoricProcessInstanceReportResult> reportResult;
    if (firstResult != null || maxResults != null) {
    reportResult = executePaginatedQuery(query, firstResult, maxResults);
    } else {
    reportResult = query.list();
    }

    return CleanableHistoricProcessInstanceReportResultDto.convert(reportResult);
  }

  private List<CleanableHistoricProcessInstanceReportResult> executePaginatedQuery(CleanableHistoricProcessInstanceReport query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }

}
