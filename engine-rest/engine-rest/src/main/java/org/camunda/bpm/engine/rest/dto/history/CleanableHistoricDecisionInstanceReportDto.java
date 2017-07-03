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

package org.camunda.bpm.engine.rest.dto.history;

import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReport;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CleanableHistoricDecisionInstanceReportDto extends AbstractQueryDto<CleanableHistoricDecisionInstanceReport>{

  private String[] decisionDefinitionIdIn;
  private String[] decisionDefinitionKeyIn;

  public CleanableHistoricDecisionInstanceReportDto() {
  }

  public CleanableHistoricDecisionInstanceReportDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam(value = "decisionDefinitionIdIn", converter = StringArrayConverter.class)
  public void setDecisionDefinitionIdIn(String[] decisionDefinitionIdIn) {
    this.decisionDefinitionIdIn = decisionDefinitionIdIn;
  }

  @CamundaQueryParam(value = "decisionDefinitionKeyIn", converter = StringArrayConverter.class)
  public void setDecisionDefinitionKeyIn(String[] decisionDefinitionKeyIn) {
    this.decisionDefinitionKeyIn = decisionDefinitionKeyIn;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return false;
  }

  @Override
  protected CleanableHistoricDecisionInstanceReport createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createCleanableHistoricDecisionInstanceReport();
  }

  @Override
  protected void applyFilters(CleanableHistoricDecisionInstanceReport query) {
    if (decisionDefinitionIdIn != null && decisionDefinitionIdIn.length > 0) {
      query.decisionDefinitionIdIn(decisionDefinitionIdIn);
    }
    if (decisionDefinitionKeyIn != null && decisionDefinitionKeyIn.length > 0) {
      query.decisionDefinitionKeyIn(decisionDefinitionKeyIn);
    }

  }

  @Override
  protected void applySortBy(CleanableHistoricDecisionInstanceReport query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
  }
}
