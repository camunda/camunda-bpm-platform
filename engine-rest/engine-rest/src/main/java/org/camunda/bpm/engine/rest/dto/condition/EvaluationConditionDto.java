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
package org.camunda.bpm.engine.rest.dto.condition;

import java.util.Map;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;

public class EvaluationConditionDto {
  private String businessKey;
  private Map<String, VariableValueDto> variables;
  private String tenantId;
  private boolean withoutTenantId;
  private String processDefinitionId;

  public String getBusinessKey() {
    return businessKey;
  }
  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }
  public Map<String, VariableValueDto> getVariables() {
    return variables;
  }
  public void setVariables(Map<String, VariableValueDto> variables) {
    this.variables = variables;
  }
  public String getTenantId() {
    return tenantId;
  }
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }
  public boolean isWithoutTenantId() {
    return withoutTenantId;
  }
  public void setWithoutTenantId(boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public void setProcessDefinitionId(String processInstanceId) {
    this.processDefinitionId = processInstanceId;
  }
}
