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

package org.camunda.bpm.engine.rest.hal;

import org.camunda.bpm.engine.rest.*;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.VariableResource;
import org.camunda.bpm.engine.runtime.VariableInstance;

import javax.ws.rs.core.UriBuilder;
import java.util.Map;

/**
 * @author Sebastian Menski
 */
public class HalVariableValue extends HalResource<HalVariableValue> {

  // add leading / by hand because otherwise it will be encoded as %2F (see CAM-3091)
  public static HalRelation REL_SELF = HalRelation.build("self", VariableResource.class, UriBuilder.fromPath("/{scopeResourcePath}").path("{scopeId}").path("{variablesName}").path("{variableName}"));

  protected String name;
  protected Object value;
  protected String type;
  protected Map<String, Object> valueInfo;

  public static HalVariableValue generateVariableValue(VariableInstance variableInstance, String variableScopeId) {
    if (variableScopeId.equals(variableInstance.getTaskId())) {
      return generateTaskVariableValue(variableInstance, variableScopeId);
    }
    else if (variableScopeId.equals(variableInstance.getProcessInstanceId())) {
      return generateProcessInstanceVariableValue(variableInstance, variableScopeId);
    }
    else if (variableScopeId.equals(variableInstance.getExecutionId())) {
      return generateExecutionVariableValue(variableInstance, variableScopeId);
    }
    else if (variableScopeId.equals(variableInstance.getCaseInstanceId())) {
      return generateCaseInstanceVariableValue(variableInstance, variableScopeId);
    }
    else if (variableScopeId.equals(variableInstance.getCaseExecutionId())) {
      return generateCaseExecutionVariableValue(variableInstance, variableScopeId);
    }
    else {
      throw new RestException("Variable scope id '" + variableScopeId + "' does not match with variable instance '" + variableInstance + "'");
    }
  }

  public static HalVariableValue generateTaskVariableValue(VariableInstance variableInstance, String taskId) {
    return fromVariableInstance(variableInstance)
      .link(REL_SELF, TaskRestService.PATH, taskId, "localVariables");
  }

  public static HalVariableValue generateExecutionVariableValue(VariableInstance variableInstance, String executionId) {
    return fromVariableInstance(variableInstance)
      .link(REL_SELF, ExecutionRestService.PATH, executionId, "localVariables");
  }

  public static HalVariableValue generateProcessInstanceVariableValue(VariableInstance variableInstance, String processInstanceId) {
    return fromVariableInstance(variableInstance)
      .link(REL_SELF, ProcessInstanceRestService.PATH, processInstanceId, "variables");
  }

  public static HalVariableValue generateCaseExecutionVariableValue(VariableInstance variableInstance, String caseExecutionId) {
    return fromVariableInstance(variableInstance)
      .link(REL_SELF, CaseExecutionRestService.PATH, caseExecutionId, "localVariables");
  }

  public static HalVariableValue generateCaseInstanceVariableValue(VariableInstance variableInstance, String caseInstanceId) {
    return fromVariableInstance(variableInstance)
      .link(REL_SELF, CaseInstanceRestService.PATH, caseInstanceId, "variables");
  }

  private HalVariableValue link(HalRelation relation, String resourcePath, String resourceId, String variablesPath) {
    if (resourcePath.startsWith("/")) {
      // trim leading / because otherwise it will be encode as %2F (see CAM-3091)
      resourcePath = resourcePath.substring(1);
    }
    this.linker.createLink(relation, resourcePath, resourceId, variablesPath, this.name);
    return this;
  }

  public static HalVariableValue fromVariableInstance(VariableInstance variableInstance) {
    HalVariableValue dto = new HalVariableValue();

    VariableValueDto variableValueDto = VariableValueDto.fromTypedValue(variableInstance.getTypedValue());

    dto.name = variableInstance.getName();
    dto.value = variableValueDto.getValue();
    dto.type = variableValueDto.getType();
    dto.valueInfo = variableValueDto.getValueInfo();

    return dto;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  public String getType() {
    return type;
  }

  public Map<String, Object> getValueInfo() {
    return valueInfo;
  }

}
