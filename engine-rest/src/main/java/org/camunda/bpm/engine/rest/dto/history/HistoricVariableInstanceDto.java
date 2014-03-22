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

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.variable.SerializableType;
import org.camunda.bpm.engine.rest.dto.runtime.SerializedObjectDto;

public class HistoricVariableInstanceDto {

  private String id;
  private String name;
  private String type;
  private Object value;
  private String processInstanceId;
  private String activityInstanceId;
  private String errorMessage;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public static HistoricVariableInstanceDto fromHistoricVariableInstance(HistoricVariableInstance historicVariableInstance) {

    HistoricVariableInstanceEntity entity = (HistoricVariableInstanceEntity) historicVariableInstance;

    HistoricVariableInstanceDto dto = new HistoricVariableInstanceDto();

    dto.id = entity.getId();
    dto.name = entity.getVariableName();
    dto.processInstanceId = entity.getProcessInstanceId();
    dto.activityInstanceId = entity.getActivtyInstanceId();
    if(SerializableType.TYPE_NAME.equals(entity.getVariableType().getTypeName())) {
      if(entity.getValue() != null) {
        dto.value = new SerializedObjectDto(entity.getValue());
      }
    } else {
      dto.value = entity.getValue();
    }
    dto.type = entity.getVariableType().getTypeNameForValue(entity.getValue());
    dto.errorMessage = entity.getErrorMessage();

    return dto;
  }

}
