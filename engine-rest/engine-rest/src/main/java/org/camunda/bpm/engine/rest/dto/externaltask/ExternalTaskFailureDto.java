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
package org.camunda.bpm.engine.rest.dto.externaltask;

import java.util.Map;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;

/**
 * @author Thorben Lindhauer
 * @author Askar Akhmerov
 */
public class ExternalTaskFailureDto extends HandleExternalTaskDto {

  //short error description
  protected String errorMessage;
  //full stack trace or error information
  protected String errorDetails;
  protected long retryTimeout;
  protected int retries;
  protected Map<String, VariableValueDto> variables;
  protected Map<String, VariableValueDto> localVariables;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public long getRetryTimeout() {
    return retryTimeout;
  }

  public void setRetryTimeout(long retryTimeout) {
    this.retryTimeout = retryTimeout;
  }

  public int getRetries() {
    return retries;
  }

  public void setRetries(int retries) {
    this.retries = retries;
  }

  public String getErrorDetails() {
    return errorDetails;
  }

  public void setErrorDetails(String errorDetails) {
    this.errorDetails = errorDetails;
  }

  public Map<String, VariableValueDto> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, VariableValueDto> variables) {
    this.variables = variables;
  }

  public Map<String, VariableValueDto> getLocalVariables() {
    return localVariables;
  }

  public void setLocalVariables(Map<String, VariableValueDto> localVariables) {
    this.localVariables = localVariables;
  }

}
