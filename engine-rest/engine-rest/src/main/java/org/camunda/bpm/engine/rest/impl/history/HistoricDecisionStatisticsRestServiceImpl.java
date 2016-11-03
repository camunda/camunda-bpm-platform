/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.engine.rest.impl.history;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceStatistics;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceStatisticsQuery;
import org.camunda.bpm.engine.rest.dto.history.HistoricDecisionInstanceStatisticsDto;
import org.camunda.bpm.engine.rest.history.HistoricDecisionStatisticsRestService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Askar Akhmerov
 */
public class HistoricDecisionStatisticsRestServiceImpl implements HistoricDecisionStatisticsRestService {

  protected ProcessEngine processEngine;

  public HistoricDecisionStatisticsRestServiceImpl(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  @Override
  public List<HistoricDecisionInstanceStatisticsDto> getDecisionStatistics(String decisionRequirementsDefinitionId, String decisionInstanceId) {
    List<HistoricDecisionInstanceStatisticsDto> result = new ArrayList<HistoricDecisionInstanceStatisticsDto>();
    HistoricDecisionInstanceStatisticsQuery statisticsQuery = processEngine.getHistoryService()
        .createHistoricDecisionInstanceStatisticsQuery(decisionRequirementsDefinitionId);
    if (decisionInstanceId != null) {
      statisticsQuery.decisionInstanceId(decisionInstanceId);
    }

    for (HistoricDecisionInstanceStatistics stats : statisticsQuery.list()) {
      result.add(HistoricDecisionInstanceStatisticsDto.fromDecisionDefinitionStatistics(stats));
    }

    return result;
  }

}
