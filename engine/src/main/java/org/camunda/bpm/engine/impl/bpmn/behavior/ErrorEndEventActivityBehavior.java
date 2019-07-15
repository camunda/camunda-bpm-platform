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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.bpmn.helper.BpmnExceptionHandler;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;


/**
 * @author Joram Barrez
 * @author Falko Menge
 */
public class ErrorEndEventActivityBehavior extends AbstractBpmnActivityBehavior {

  protected String errorCode;
  private ParameterValueProvider errorMessageExpression;

  public ErrorEndEventActivityBehavior(String errorCode, ParameterValueProvider errorMessage) {
    this.errorCode = errorCode;
    this.errorMessageExpression = errorMessage;
  }

  public void execute(ActivityExecution execution) throws Exception {
    String errorMessageValue = errorMessageExpression != null ? (String) errorMessageExpression.getValue(execution) : null;
    BpmnExceptionHandler.propagateError(errorCode, errorMessageValue, null, execution);
  }

  public String getErrorCode() {
    return errorCode;
  }
  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public ParameterValueProvider getErrorMessageExpression() {
    return errorMessageExpression;
  }

  public void setErrorMessageExpression(ParameterValueProvider errorMessage) {
    this.errorMessageExpression = errorMessage;
  }
}
