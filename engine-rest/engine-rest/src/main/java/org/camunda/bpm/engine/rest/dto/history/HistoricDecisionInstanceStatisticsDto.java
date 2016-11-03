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

package org.camunda.bpm.engine.rest.dto.history;

import org.camunda.bpm.engine.history.HistoricDecisionInstanceStatistics;

/**
 * @author Askar Akhmerov
 */
public class HistoricDecisionInstanceStatisticsDto {

  protected String decisionDefinitionKey;
  protected int evaluations;

  public int getEvaluations() {
    return evaluations;
  }

  public void setEvaluations(int evaluations) {
    this.evaluations = evaluations;
  }

  public String getDecisionDefinitionKey() {
    return decisionDefinitionKey;
  }

  public void setDecisionDefinitionKey(String decisionDefinitionKey) {
    this.decisionDefinitionKey = decisionDefinitionKey;
  }

  public static HistoricDecisionInstanceStatisticsDto fromDecisionDefinitionStatistics(HistoricDecisionInstanceStatistics stats) {
    HistoricDecisionInstanceStatisticsDto instance = new HistoricDecisionInstanceStatisticsDto();
    instance.setEvaluations(stats.getEvaluations());
    instance.setDecisionDefinitionKey(stats.getDecisionDefinitionKey());
    return instance;
  }
}
