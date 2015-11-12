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

import org.camunda.bpm.engine.rest.dto.VariableValueDto;

import java.util.Map;

public class CorrelationMessageDto {

  private String messageName;
  private String businessKey;
  private Map<String, VariableValueDto> correlationKeys;
  private Map<String, VariableValueDto> processVariables;
  private boolean all;

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

}
