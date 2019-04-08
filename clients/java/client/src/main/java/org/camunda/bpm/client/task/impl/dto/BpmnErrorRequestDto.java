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

import java.util.Map;

import org.camunda.bpm.client.impl.RequestDto;
import org.camunda.bpm.client.variable.impl.TypedValueField;

/**
 * @author Tassilo Weidner
 */
public class BpmnErrorRequestDto extends RequestDto {

  protected String errorCode;
  protected String errorMessage;
  protected Map<String, TypedValueField> variables;

  public BpmnErrorRequestDto(String workerId, String errorCode) {
    super(workerId);
    this.errorCode = errorCode;
  }

  public BpmnErrorRequestDto(String workerId, String errorCode, String errorMessage) {
    this(workerId, errorCode);
    this.errorMessage = errorMessage;
  }

  public BpmnErrorRequestDto(String workerId, String errorCode, String errorMessage, Map<String, TypedValueField> variables) {
    this(workerId, errorCode, errorMessage);
    this.variables = variables;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Map<String, TypedValueField> getVariables() {
    return variables;
  }

}
