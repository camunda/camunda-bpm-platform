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

import java.util.Map;
import javax.ws.rs.core.UriBuilder;

import org.camunda.bpm.engine.delegate.ProcessEngineVariableType;
import org.camunda.bpm.engine.rest.CaseExecutionRestService;
import org.camunda.bpm.engine.rest.CaseInstanceRestService;
import org.camunda.bpm.engine.rest.ExecutionRestService;
import org.camunda.bpm.engine.rest.ProcessInstanceRestService;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.runtime.SerializedObjectDto;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.VariableResource;
import org.camunda.bpm.engine.runtime.VariableInstance;

/**
 * @author Sebastian Menski
 */
public class HalVariableValue extends HalResource<HalVariableValue> {

  public static HalRelation REL_SELF = HalRelation.build("self", VariableResource.class, UriBuilder.fromPath("{scopeResourcePath}").path("{scopeId}").path("{variablesName}").path("{variableName}"));

  protected String name;
  protected Object value;
  protected String type;
  protected String variableType;
  protected Map<String, Object> serializationConfig;

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
    this.linker.createLink(relation, resourcePath, resourceId, variablesPath, this.name);
    return this;
  }

  public static HalVariableValue fromVariableInstance(VariableInstance variableInstance) {
    HalVariableValue dto = new HalVariableValue();

    dto.name = variableInstance.getName();
    dto.value = variableInstance.getValue();
    dto.type = variableInstance.getValueTypeName();
    dto.variableType = variableInstance.getTypeName();

    if (variableInstance.storesCustomObjects()) {
      if (ProcessEngineVariableType.SERIALIZABLE.getName().equals(variableInstance.getTypeName())) {
        if (variableInstance.getValue() != null) {
          dto.value = new SerializedObjectDto(variableInstance.getValue());
        }
      } else {
        dto.value = variableInstance.getSerializedValue().getValue();
        dto.serializationConfig = variableInstance.getSerializedValue().getConfig();
      }
    } else {
      dto.value = variableInstance.getValue();
    }

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

  public String getVariableType() {
    return variableType;
  }

  public Map<String, Object> getSerializationConfig() {
    return serializationConfig;
  }
}
