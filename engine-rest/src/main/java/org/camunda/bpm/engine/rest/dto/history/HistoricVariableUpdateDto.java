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

import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Roman Smirnov
 *
 */
@JsonTypeName("variableUpdate")
public class HistoricVariableUpdateDto extends HistoricDetailDto {

  protected String variableName;
  protected String variableInstanceId;
  protected String variableType;
  protected Object value;
  protected Map<String, Object> valueInfo;

  protected int revision;
  protected String errorMessage;

  public String getVariableName() {
    return variableName;
  }

  public String getVariableInstanceId() {
    return variableInstanceId;
  }

  public String getVariableType() {
    return variableType;
  }

  public Object getValue() {
    return value;
  }

  public int getRevision() {
    return revision;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Map<String, Object> getValueInfo() {
    return valueInfo;
  }

  public static HistoricVariableUpdateDto fromHistoricVariableUpdate(HistoricVariableUpdate historicVariableUpdate) {

    HistoricVariableUpdateDto dto = new HistoricVariableUpdateDto();

    dto.revision = historicVariableUpdate.getRevision();
    dto.variableName = historicVariableUpdate.getVariableName();
    dto.variableInstanceId = historicVariableUpdate.getVariableInstanceId();

    if(historicVariableUpdate.getErrorMessage() == null) {
      VariableValueDto variableValueDto = VariableValueDto.fromTypedValue(historicVariableUpdate.getTypedValue());
      dto.value = variableValueDto.getValue();
      dto.variableType = variableValueDto.getType();
      dto.valueInfo = variableValueDto.getValueInfo();
    }
    else {
      dto.errorMessage = historicVariableUpdate.getErrorMessage();
      dto.variableType = VariableValueDto.toRestApiTypeName(historicVariableUpdate.getTypeName());
    }

    return dto;
  }

}
