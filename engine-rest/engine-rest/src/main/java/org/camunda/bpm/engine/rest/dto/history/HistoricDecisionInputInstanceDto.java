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

import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;

public class HistoricDecisionInputInstanceDto extends VariableValueDto {

  protected String id;
  protected String decisionInstanceId;
  protected String clauseId;
  protected String clauseName;
  protected String errorMessage;

  public String getId() {
    return id;
  }

  public String getDecisionInstanceId() {
    return decisionInstanceId;
  }

  public String getClauseId() {
    return clauseId;
  }

  public String getClauseName() {
    return clauseName;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public static HistoricDecisionInputInstanceDto fromHistoricDecisionInputInstance(HistoricDecisionInputInstance historicDecisionInputInstance) {

    HistoricDecisionInputInstanceDto dto = new HistoricDecisionInputInstanceDto();

    dto.id = historicDecisionInputInstance.getId();
    dto.decisionInstanceId = historicDecisionInputInstance.getDecisionInstanceId();
    dto.clauseId = historicDecisionInputInstance.getClauseId();
    dto.clauseName = historicDecisionInputInstance.getClauseName();

    if(historicDecisionInputInstance.getErrorMessage() == null) {
      VariableValueDto.fromTypedValue(dto, historicDecisionInputInstance.getTypedValue());
    }
    else {
      dto.errorMessage = historicDecisionInputInstance.getErrorMessage();
      dto.type = VariableValueDto.toRestApiTypeName(historicDecisionInputInstance.getTypeName());
    }

    return dto;
  }

}
