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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: drobisch
 */
public class VariableListDto {
  List<VariableValueDto> variables;

  public VariableListDto() {
  }
  
  public VariableListDto(List<VariableValueDto> variables) {
    this.variables = variables;
  }

  public void setVariables(List<VariableValueDto> variables) {
    this.variables = variables;
  }
  
  public List<VariableValueDto> getVariables() {
    return variables;
  }
  
  public Map<String, Object> toMap() {
    Map<String, Object> variablesMap = new HashMap<String, Object>();
    for (VariableValueDto variable : variables) {
      variablesMap.put(variable.getName(), variable.getValue());
    }
    return variablesMap;
  }
}
