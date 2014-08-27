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

import org.camunda.bpm.engine.delegate.ProcessEngineVariableType;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.rest.dto.runtime.SerializedValueDto;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricVariableUpdateDto extends HistoricDetailDto {

  protected String variableName;
  protected String variableTypeName;
  protected Object value;
  protected int revision;
  protected String errorMessage;
  protected SerializedValueDto serializedValue;

  public String getVariableName() {
    return variableName;
  }

  public String getVariableTypeName() {
    return variableTypeName;
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

  public SerializedValueDto getSerializedValue() {
    return serializedValue;
  }

  public static HistoricVariableUpdateDto fromHistoricVariableUpdate(HistoricVariableUpdate historicVariableUpdate) {

    HistoricVariableUpdateDto dto = new HistoricVariableUpdateDto();

    dto.revision = historicVariableUpdate.getRevision();
    dto.variableName = historicVariableUpdate.getVariableName();
    dto.variableTypeName = historicVariableUpdate.getVariableTypeName();

    if(historicVariableUpdate.storesCustomObjects()) {
      if (!ProcessEngineVariableType.SERIALIZABLE.getName().equals(historicVariableUpdate.getVariableTypeName())) {
        dto.serializedValue = SerializedValueDto.fromSerializedVariableValue(historicVariableUpdate.getSerializedValue());
      }
    } else {
      dto.value = historicVariableUpdate.getValue();
      dto.errorMessage = historicVariableUpdate.getErrorMessage();
    }

    dto.variableTypeName = historicVariableUpdate.getValueTypeName();

    return dto;
  }

}
