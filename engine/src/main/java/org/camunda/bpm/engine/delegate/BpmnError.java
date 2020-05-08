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
package org.camunda.bpm.engine.delegate;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.bpmn.parser.Error;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;


/**
 * Special exception that can be used to throw a BPMN Error from
 * {@link JavaDelegate}s and expressions.
 * 
 * This should only be used for business faults, which shall be handled by a
 * Boundary Error Event or Error Event Sub-Process modeled in the process
 * definition. Technical errors should be represented by other exception types.
 * 
 * This class represents an actual instance of a BPMN Error, whereas
 * {@link Error} represents an Error definition.
 * 
 * @author Falko Menge
 */
public class BpmnError extends ProcessEngineException {
  
  private static final long serialVersionUID = 1L;

  private String errorCode;
  private String errorMessage;

  public BpmnError(String errorCode) {
    super(exceptionMessage(errorCode, null));
    setErrorCode(errorCode);
  }
          
  public BpmnError(String errorCode, String message) {
    super(exceptionMessage(errorCode, message));
    setErrorCode(errorCode);
    setMessage(message);
  }

  public BpmnError(String errorCode, String message, Throwable cause) {
    super(exceptionMessage(errorCode, message), cause);
    setErrorCode(errorCode);
    setMessage(message);
  }

  public BpmnError(String errorCode, Throwable cause) {
    super(exceptionMessage(errorCode, null), cause);
    setErrorCode(errorCode);
  }

  private static String exceptionMessage(String errorCode, String message) {
    if (message == null) {
      return "";
    } else {
      return message + " (errorCode='" + errorCode + "')";
    }
  }

  protected void setErrorCode(String errorCode) {
    ensureNotEmpty("Error Code", errorCode);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String toString() {
    return super.toString() + " (errorCode='" + errorCode + "')";
  }

  protected void setMessage(String errorMessage) {
    ensureNotEmpty("Error Message", errorMessage);
    this.errorMessage = errorMessage;
  }

  @Override
  public String getMessage() {
    return errorMessage;
  }
}
