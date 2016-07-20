/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.rest.dto.history;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class HistoricDecisionInstanceDto {

  protected String id;
  protected String decisionDefinitionId;
  protected String decisionDefinitionKey;
  protected String decisionDefinitionName;
  protected Date evaluationTime;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processInstanceId;
  protected String caseDefinitionId;
  protected String caseDefinitionKey;
  protected String caseInstanceId;
  protected String activityId;
  protected String activityInstanceId;
  protected String userId;
  protected List<HistoricDecisionInputInstanceDto> inputs;
  protected List<HistoricDecisionOutputInstanceDto> outputs;
  protected Double collectResultValue;
  protected String rootDecisionInstanceId;
  protected String decisionRequirementsDefinitionId;
  protected String decisionRequirementsDefinitionKey;
  protected String tenantId;

  public String getId() {
    return id;
  }

  public String getDecisionDefinitionId() {
    return decisionDefinitionId;
  }

  public String getDecisionDefinitionKey() {
    return decisionDefinitionKey;
  }

  public String getDecisionDefinitionName() {
    return decisionDefinitionName;
  }

  public Date getEvaluationTime() {
    return evaluationTime;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getActivityId() {
    return activityId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public String getUserId() {
    return userId;
  }

  @JsonInclude(Include.NON_NULL)
  public List<HistoricDecisionInputInstanceDto> getInputs() {
    return inputs;
  }

  @JsonInclude(Include.NON_NULL)
  public List<HistoricDecisionOutputInstanceDto> getOutputs() {
    return outputs;
  }

  public Double getCollectResultValue() {
    return collectResultValue;
  }

  public String getRootDecisionInstanceId() {
    return rootDecisionInstanceId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getDecisionRequirementsDefinitionId() {
    return decisionRequirementsDefinitionId;
  }

  public String getDecisionRequirementsDefinitionKey() {
    return decisionRequirementsDefinitionKey;
  }

  public static HistoricDecisionInstanceDto fromHistoricDecisionInstance(HistoricDecisionInstance historicDecisionInstance) {
    HistoricDecisionInstanceDto dto = new HistoricDecisionInstanceDto();

    dto.id = historicDecisionInstance.getId();
    dto.decisionDefinitionId = historicDecisionInstance.getDecisionDefinitionId();
    dto.decisionDefinitionKey = historicDecisionInstance.getDecisionDefinitionKey();
    dto.decisionDefinitionName = historicDecisionInstance.getDecisionDefinitionName();
    dto.evaluationTime = historicDecisionInstance.getEvaluationTime();
    dto.processDefinitionId = historicDecisionInstance.getProcessDefinitionId();
    dto.processDefinitionKey = historicDecisionInstance.getProcessDefinitionKey();
    dto.processInstanceId = historicDecisionInstance.getProcessInstanceId();
    dto.caseDefinitionId = historicDecisionInstance.getCaseDefinitionId();
    dto.caseDefinitionKey = historicDecisionInstance.getCaseDefinitionKey();
    dto.caseInstanceId = historicDecisionInstance.getCaseInstanceId();
    dto.activityId = historicDecisionInstance.getActivityId();
    dto.activityInstanceId = historicDecisionInstance.getActivityInstanceId();
    dto.userId = historicDecisionInstance.getUserId();
    dto.collectResultValue = historicDecisionInstance.getCollectResultValue();
    dto.rootDecisionInstanceId = historicDecisionInstance.getRootDecisionInstanceId();
    dto.decisionRequirementsDefinitionId = historicDecisionInstance.getDecisionRequirementsDefinitionId();
    dto.decisionRequirementsDefinitionKey = historicDecisionInstance.getDecisionRequirementsDefinitionKey();
    dto.tenantId = historicDecisionInstance.getTenantId();

    try {
      List<HistoricDecisionInputInstanceDto> inputs = new ArrayList<HistoricDecisionInputInstanceDto>();
      for (HistoricDecisionInputInstance input : historicDecisionInstance.getInputs()) {
        HistoricDecisionInputInstanceDto inputDto = HistoricDecisionInputInstanceDto.fromHistoricDecisionInputInstance(input);
        inputs.add(inputDto);
      }
      dto.inputs = inputs;
    }
    catch (ProcessEngineException e) {
      // no inputs fetched
    }

    try {
      List<HistoricDecisionOutputInstanceDto> outputs = new ArrayList<HistoricDecisionOutputInstanceDto>();
      for (HistoricDecisionOutputInstance output : historicDecisionInstance.getOutputs()) {
        HistoricDecisionOutputInstanceDto outputDto = HistoricDecisionOutputInstanceDto.fromHistoricDecisionOutputInstance(output);
        outputs.add(outputDto);
      }
      dto.outputs = outputs;
    }
    catch (ProcessEngineException e) {
      // no outputs fetched
    }

    return dto;
  }

}
