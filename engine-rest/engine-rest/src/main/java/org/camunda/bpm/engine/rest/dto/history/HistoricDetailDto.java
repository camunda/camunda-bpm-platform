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

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricFormField;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author Roman Smirnov
 *
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type"
)
@JsonSubTypes({
    @Type(value = HistoricFormFieldDto.class),
    @Type(value = HistoricVariableUpdateDto.class)
})
public abstract class HistoricDetailDto {

  protected String id;
  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected String processInstanceId;
  protected String activityInstanceId;
  protected String executionId;
  protected String caseDefinitionKey;
  protected String caseDefinitionId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String taskId;
  protected String tenantId;
  protected String userOperationId;
  protected Date time;

  public String getId() {
    return id;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getUserOperationId() {
    return userOperationId;
  }

  public Date getTime() {
    return time;
  }

  public static HistoricDetailDto fromHistoricDetail(HistoricDetail historicDetail) {

    HistoricDetailDto dto = null;

    if (historicDetail instanceof HistoricFormField) {
      HistoricFormField historicFormField = (HistoricFormField) historicDetail;
      dto = HistoricFormFieldDto.fromHistoricFormField(historicFormField);

    } else if (historicDetail instanceof HistoricVariableUpdate) {
      HistoricVariableUpdate historicVariableUpdate = (HistoricVariableUpdate) historicDetail;
      dto = HistoricVariableUpdateDto.fromHistoricVariableUpdate(historicVariableUpdate);
    }

    dto.id = historicDetail.getId();
    dto.processDefinitionKey = historicDetail.getProcessDefinitionKey();
    dto.processDefinitionId = historicDetail.getProcessDefinitionId();
    dto.processInstanceId = historicDetail.getProcessInstanceId();
    dto.activityInstanceId = historicDetail.getActivityInstanceId();
    dto.executionId = historicDetail.getExecutionId();
    dto.taskId = historicDetail.getTaskId();
    dto.caseDefinitionKey = historicDetail.getCaseDefinitionKey();
    dto.caseDefinitionId = historicDetail.getCaseDefinitionId();
    dto.caseInstanceId = historicDetail.getCaseInstanceId();
    dto.caseExecutionId = historicDetail.getCaseExecutionId();
    dto.tenantId = historicDetail.getTenantId();
    dto.userOperationId = historicDetail.getUserOperationId();
    dto.time = historicDetail.getTime();

    return dto;
  }

}
