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
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.modification.ProcessInstanceModificationInstructionDto;

public class StartProcessInstanceDto {

  protected Map<String, VariableValueDto> variables;
  protected String businessKey;
  protected String caseInstanceId;
  protected List<ProcessInstanceModificationInstructionDto> startInstructions;
  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;
  protected boolean withVariablesInReturn = false;

  public Map<String, VariableValueDto> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, VariableValueDto> variables) {
    this.variables = variables;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  public List<ProcessInstanceModificationInstructionDto> getStartInstructions() {
    return startInstructions;
  }

  public void setStartInstructions(List<ProcessInstanceModificationInstructionDto> startInstructions) {
    this.startInstructions = startInstructions;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public void setSkipCustomListeners(boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

  public void setSkipIoMappings(boolean skipIoMappings) {
    this.skipIoMappings = skipIoMappings;
  }

  public boolean isWithVariablesInReturn() {
    return withVariablesInReturn;
  }

  public void setWithVariablesInReturn(boolean withVariablesInReturn) {
    this.withVariablesInReturn = withVariablesInReturn;
  }
}
