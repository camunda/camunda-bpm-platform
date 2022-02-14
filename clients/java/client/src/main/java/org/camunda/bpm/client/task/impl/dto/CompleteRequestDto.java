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
package org.camunda.bpm.client.task.impl.dto;

import org.camunda.bpm.client.impl.RequestDto;
import org.camunda.bpm.client.variable.impl.TypedValueField;

import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class CompleteRequestDto extends RequestDto {

  protected Map<String, TypedValueField> variables;
  protected Map<String, TypedValueField> localVariables;

  public CompleteRequestDto(String workerId, Map<String, TypedValueField> variables, Map<String, TypedValueField> localVariables) {
    super(workerId);

    this.variables = variables;
    this.localVariables = localVariables;
  }

  public Map<String, TypedValueField> getVariables() {
    return variables;
  }

  public Map<String, TypedValueField> getLocalVariables() {
    return localVariables;
  }

}
