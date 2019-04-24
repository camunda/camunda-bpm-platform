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
package org.camunda.bpm.engine.rest.dto;

import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.variable.Variables;

import com.fasterxml.jackson.databind.ObjectMapper;

public class VariableQueryParameterDto extends ConditionQueryParameterDto {

  public VariableQueryParameterDto() {

  }

  public VariableQueryParameterDto(TaskQueryVariableValue variableValue) {
    this.name = variableValue.getName();
    this.operator = OPERATOR_NAME_MAP.get(variableValue.getOperator());
    this.value = variableValue.getValue();
  }

  protected String name;

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public Object resolveValue(ObjectMapper objectMapper) {
    Object value = super.resolveValue(objectMapper);

    if (value != null && Number.class.isAssignableFrom(value.getClass())) {
      return Variables.numberValue((Number) value);
    }

    return value;
  }
}
