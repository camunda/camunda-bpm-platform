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
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.Map;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * Represents a process instance dto extension dto that contains latest variables.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ProcessInstanceWithVariablesDto extends ProcessInstanceDto {

  private Map<String, VariableValueDto> variables;

  public ProcessInstanceWithVariablesDto() {
  }

  public ProcessInstanceWithVariablesDto(ProcessInstance instance) {
    super(instance);
  }

  public Map<String, VariableValueDto> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, VariableValueDto> variables) {
    this.variables = variables;
  }

  public static ProcessInstanceDto fromProcessInstance(ProcessInstanceWithVariables instance) {
    ProcessInstanceWithVariablesDto result = new ProcessInstanceWithVariablesDto(instance);
    VariableMap variables = instance.getVariables();

    result.variables = VariableValueDto.fromMap(variables, true);
    return result;
  }
}
