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
package org.camunda.bpm.qa.upgrade.util;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * @author Thorben Lindhauer
 *
 */
public class ThrowBpmnErrorDelegate implements JavaDelegate, ExecutionListener {

  public static final String ERROR_INDICATOR_VARIABLE = "throwError";
  public static final String ERROR_NAME_VARIABLE = "errorName";

  public static final String EXCEPTION_INDICATOR_VARIABLE = "throwException";
  public static final String EXCEPTION_MESSAGE_VARIABLE = "exceptionMessage";

  public static final String DEFAULT_ERROR_NAME = ThrowBpmnErrorDelegate.class.getSimpleName();
  public static final String DEFAULT_EXCEPTION_MESSAGE = DEFAULT_ERROR_NAME;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    throwErrorIfRequested(execution);
    throwExceptionIfRequested(execution);
  }

  @Override
  public void notify(DelegateExecution execution) throws Exception {
    execute(execution);
  }

  protected void throwErrorIfRequested(DelegateExecution execution) {
    Boolean shouldThrowError = (Boolean) execution.getVariable(ERROR_INDICATOR_VARIABLE);

    if (Boolean.TRUE.equals(shouldThrowError)) {
      String errorName = (String) execution.getVariable(ERROR_NAME_VARIABLE);
      if (errorName == null) {
        errorName = DEFAULT_ERROR_NAME;
      }

      throw new BpmnError(errorName);
    }
  }

  protected void throwExceptionIfRequested(DelegateExecution execution) {
    Boolean shouldThrowException = (Boolean) execution.getVariable(EXCEPTION_INDICATOR_VARIABLE);

    if (Boolean.TRUE.equals(shouldThrowException)) {
      String exceptionMessage = (String) execution.getVariable(EXCEPTION_MESSAGE_VARIABLE);
      if (exceptionMessage == null) {
        exceptionMessage = DEFAULT_EXCEPTION_MESSAGE;
      }

      throw new ThrowBpmnErrorDelegateException(exceptionMessage);
    }
  }

  public static class ThrowBpmnErrorDelegateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ThrowBpmnErrorDelegateException(String message) {
      super(message);
    }

  }

}
