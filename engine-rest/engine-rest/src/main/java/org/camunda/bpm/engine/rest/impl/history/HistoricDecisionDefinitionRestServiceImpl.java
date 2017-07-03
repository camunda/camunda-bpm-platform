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

import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReportResult;
import org.camunda.bpm.engine.rest.dto.history.CleanableHistoricDecisionInstanceReportDto;
import org.camunda.bpm.engine.rest.dto.history.CleanableHistoricDecisionInstanceReportResultDto;
import org.camunda.bpm.engine.rest.history.HistoricDecisionDefinitionRestService;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HistoricDecisionDefinitionRestServiceImpl implements HistoricDecisionDefinitionRestService {

  protected ObjectMapper objectMapper;
  protected ProcessEngine processEngine;

  public HistoricDecisionDefinitionRestServiceImpl(ObjectMapper objectMapper, ProcessEngine processEngine) {
    this.objectMapper = objectMapper;
    this.processEngine = processEngine;
  }

  @Override
  public List<CleanableHistoricDecisionInstanceReportResultDto> getCleanableHistoricDecisionInstanceReport(UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    CleanableHistoricDecisionInstanceReportDto queryDto = new CleanableHistoricDecisionInstanceReportDto(objectMapper, uriInfo.getQueryParameters());
    CleanableHistoricDecisionInstanceReport query = queryDto.toQuery(processEngine);

    List<CleanableHistoricDecisionInstanceReportResult> reportResult;
    if (firstResult != null || maxResults != null) {
      reportResult = executePaginatedQuery(query, firstResult, maxResults);
      } else {
      reportResult = query.list();
      }

    return CleanableHistoricDecisionInstanceReportResultDto.convert(reportResult);
  }

  private List<CleanableHistoricDecisionInstanceReportResult> executePaginatedQuery(CleanableHistoricDecisionInstanceReport query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults);
  }
}
