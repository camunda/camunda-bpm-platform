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
import org.camunda.bpm.engine.rest.dto.VariableValueDto;

public class HistoricVariableInstanceDto extends VariableValueDto {

  private String id;
  private String name;
  private String processInstanceId;
  private String activityInstanceId;
  private String errorMessage;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
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

    HistoricVariableInstanceDto dto = new HistoricVariableInstanceDto();

    dto.id = historicVariableInstance.getId();
    dto.name = historicVariableInstance.getName();
    dto.processInstanceId = historicVariableInstance.getProcessInstanceId();
    dto.activityInstanceId = historicVariableInstance.getActivityInstanceId();

    if(historicVariableInstance.getErrorMessage() == null) {
      VariableValueDto.fromTypedValue(dto, historicVariableInstance.getTypedValue());
    }
    else {
      dto.errorMessage = historicVariableInstance.getErrorMessage();
      dto.type = VariableValueDto.toRestApiTypeName(historicVariableInstance.getTypeName());
    }

    return dto;
  }

}
