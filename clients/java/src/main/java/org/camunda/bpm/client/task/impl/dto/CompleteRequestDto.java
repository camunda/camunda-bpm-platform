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
package org.camunda.bpm.client.task.impl.dto;

import org.camunda.bpm.client.impl.RequestDto;

import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class CompleteRequestDto extends RequestDto {

  protected Map<String, TypedValueDto> variables;
  protected Map<String, TypedValueDto> localVariables;

  public CompleteRequestDto(String workerId, Map<String, TypedValueDto> variables, Map<String, TypedValueDto> localVariables) {
    super(workerId);

    this.variables = variables;
    this.localVariables = localVariables;
  }

  public Map<String, TypedValueDto> getVariables() {
    return variables;
  }

  public Map<String, TypedValueDto> getLocalVariables() {
    return localVariables;
  }

}
