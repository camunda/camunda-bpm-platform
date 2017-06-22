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

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricFinishedDecisionInstanceReportResult;
import org.camunda.bpm.engine.rest.dto.history.HistoricFinishedDecisionInstanceReportDto;
import org.camunda.bpm.engine.rest.history.HistoricDecisionDefinitionRestService;

public class HistoricDecisionDefinitionRestServiceImpl implements HistoricDecisionDefinitionRestService {

  protected ProcessEngine processEngine;

  public HistoricDecisionDefinitionRestServiceImpl(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  @Override
  public List<HistoricFinishedDecisionInstanceReportDto> getHistoricFinishedDecisionInstanceReport() {
    HistoryService historyService = processEngine.getHistoryService();

    List<HistoricFinishedDecisionInstanceReportResult> reportResult = historyService.createHistoricFinishedDecisionInstanceReport().list();
    return HistoricFinishedDecisionInstanceReportDto.convert(reportResult);
  }

}
