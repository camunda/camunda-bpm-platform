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
import org.camunda.bpm.engine.history.TaskReportResult;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceReportQueryDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceReportResultDto;
import org.camunda.bpm.engine.rest.history.HistoricTaskInstanceReportService;

import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Hentschel.
 */
public class HistoricTaskInstanceReportServiceImpl implements HistoricTaskInstanceReportService {

  protected ProcessEngine engine;
  protected ObjectMapper objectMapper;

  public HistoricTaskInstanceReportServiceImpl(ProcessEngine engine, ObjectMapper objectMapper) {
    this.engine = engine;
    this.objectMapper = objectMapper;
  }

  @Override
  public List<HistoricTaskInstanceReportResultDto> getTaskReportResults(UriInfo uriInfo) {
    HistoricTaskInstanceReportQueryDto queryDto = new HistoricTaskInstanceReportQueryDto(objectMapper, uriInfo.getQueryParameters());
    List<TaskReportResult> taskReportResults = queryDto.executeReport(engine);

    List<HistoricTaskInstanceReportResultDto> dtoList = new ArrayList<HistoricTaskInstanceReportResultDto>();
    for( TaskReportResult taskReportResult : taskReportResults ) {
      dtoList.add(HistoricTaskInstanceReportResultDto.fromHistoricTaskInstanceReportResult(taskReportResult));
    }

    return dtoList;
  }
}
