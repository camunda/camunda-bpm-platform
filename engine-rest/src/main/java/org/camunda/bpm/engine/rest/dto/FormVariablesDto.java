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
package org.camunda.bpm.engine.rest.dto;

import java.util.Map;
import java.util.TreeMap;

import org.camunda.bpm.engine.rest.dto.runtime.VariableInstanceDto;
import org.camunda.bpm.engine.runtime.VariableInstance;

/**
 * @author Daniel Meyer
 *
 */
public class FormVariablesDto extends TreeMap<String, VariableInstanceDto> implements Map<String, VariableInstanceDto> {

  public static FormVariablesDto fromVariableInstanceMap(Map<String, VariableInstance> formVariables) {
    FormVariablesDto formVariablesDto = new FormVariablesDto();
    for (VariableInstance variableInstance : formVariables.values()) {
      formVariablesDto.put(variableInstance.getName(), VariableInstanceDto.fromVariableInstance(variableInstance));
    }
    return formVariablesDto;
  }

}
