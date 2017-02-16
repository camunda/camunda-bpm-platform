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
package org.camunda.bpm.engine.rest.dto.message;

import java.util.Map;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;

public class CorrelationMessageDto {

  private String messageName;
  private String businessKey;
  private Map<String, VariableValueDto> correlationKeys;
  private Map<String, VariableValueDto> localCorrelationKeys;
  private Map<String, VariableValueDto> processVariables;
  private String tenantId;
  private boolean withoutTenantId;
  private String processInstanceId;

  private boolean all = false;
  private boolean resultEnabled = false;

  public String getMessageName() {
    return messageName;
  }

  public void setMessageName(String messageName) {
    this.messageName = messageName;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public Map<String, VariableValueDto> getCorrelationKeys() {
    return correlationKeys;
  }

  public void setCorrelationKeys(Map<String, VariableValueDto> correlationKeys) {
    this.correlationKeys = correlationKeys;
  }

  public Map<String, VariableValueDto> getLocalCorrelationKeys() {
    return localCorrelationKeys;
  }

  public void setLocalCorrelationKeys(Map<String, VariableValueDto> localCorrelationKeys) {
    this.localCorrelationKeys = localCorrelationKeys;
  }

  public Map<String, VariableValueDto> getProcessVariables() {
    return processVariables;
  }

  public void setProcessVariables(Map<String, VariableValueDto> processVariables) {
    this.processVariables = processVariables;
  }

  public boolean isAll() {
    return all;
  }

  public void setAll(boolean all) {
    this.all = all;
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

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public boolean isResultEnabled() {
    return resultEnabled;
  }

  public void setResultEnabled(boolean resultEnabled) {
    this.resultEnabled = resultEnabled;
  }
}
