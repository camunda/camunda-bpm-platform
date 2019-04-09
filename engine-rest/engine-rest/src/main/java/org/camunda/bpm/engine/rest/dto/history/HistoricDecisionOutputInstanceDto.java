/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;

public class HistoricDecisionOutputInstanceDto extends VariableValueDto {

  protected String id;
  protected String decisionInstanceId;
  protected String clauseId;
  protected String clauseName;
  protected String ruleId;
  protected Integer ruleOrder;
  protected String variableName;
  protected String errorMessage;
  protected Date createTime;
  protected Date removalTime;
  protected String rootProcessInstanceId;

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

  public String getRuleId() {
    return ruleId;
  }

  public Integer getRuleOrder() {
    return ruleOrder;
  }

  public String getVariableName() {
    return variableName;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public Date getRemovalTime() {
    return removalTime;
  }

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

  public static HistoricDecisionOutputInstanceDto fromHistoricDecisionOutputInstance(HistoricDecisionOutputInstance historicDecisionOutputInstance) {

    HistoricDecisionOutputInstanceDto dto = new HistoricDecisionOutputInstanceDto();

    dto.id = historicDecisionOutputInstance.getId();
    dto.decisionInstanceId = historicDecisionOutputInstance.getDecisionInstanceId();
    dto.clauseId = historicDecisionOutputInstance.getClauseId();
    dto.clauseName = historicDecisionOutputInstance.getClauseName();
    dto.ruleId = historicDecisionOutputInstance.getRuleId();
    dto.ruleOrder = historicDecisionOutputInstance.getRuleOrder();
    dto.variableName = historicDecisionOutputInstance.getVariableName();
    dto.createTime = historicDecisionOutputInstance.getCreateTime();
    dto.removalTime = historicDecisionOutputInstance.getRemovalTime();
    dto.rootProcessInstanceId = historicDecisionOutputInstance.getRootProcessInstanceId();

    if(historicDecisionOutputInstance.getErrorMessage() == null) {
      VariableValueDto.fromTypedValue(dto, historicDecisionOutputInstance.getTypedValue());
    }
    else {
      dto.errorMessage = historicDecisionOutputInstance.getErrorMessage();
      dto.type = VariableValueDto.toRestApiTypeName(historicDecisionOutputInstance.getTypeName());
    }

    return dto;
  }

}
