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
package org.camunda.bpm.engine.impl.bpmn.parser;

import java.io.Serializable;
import java.util.Comparator;

import javax.script.ScriptException;

import org.camunda.bpm.engine.ProcessEngineException;

/**
 * @author Daniel Meyer
 * @author Ronny Bräunlich
 */
public class ErrorEventDefinition implements Serializable {

  public static Comparator<ErrorEventDefinition> comparator = new Comparator<ErrorEventDefinition>() {
    public int compare(ErrorEventDefinition o1, ErrorEventDefinition o2) {
      return o2.getPrecedence().compareTo(o1.getPrecedence());
    }
  };

  private static final long serialVersionUID = 1L;

  protected final String handlerActivityId;
  protected String errorCode;
  protected Integer precedence =0;
  protected String errorCodeVariable;
  protected String errorMessageVariable;

  public ErrorEventDefinition(String handlerActivityId) {
    this.handlerActivityId=handlerActivityId;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getHandlerActivityId() {
    return handlerActivityId;
  }

  public Integer getPrecedence() {
    // handlers with error code take precedence over catchall-handlers
    return precedence + (errorCode != null ? 1 : 0);
  }

  public void setPrecedence(Integer precedence) {
    this.precedence = precedence;
  }

  public boolean catchesError(String errorCode) {
    return this.errorCode == null || this.errorCode.equals(errorCode);
  }

  public boolean catchesException(Exception ex) {

    if(this.errorCode == null) {
      return false;

    } else {

      // unbox exception
      while ((ex instanceof ProcessEngineException || ex instanceof ScriptException) && ex.getCause() != null) {
        ex = (Exception) ex.getCause();
      }

      // check exception hierarchy
      Class<?> exceptionClass = ex.getClass();
      do {
        if(this.errorCode.equals(exceptionClass.getName())) {
          return true;
        }
        exceptionClass = exceptionClass.getSuperclass();
      } while(exceptionClass != null);

      return false;
    }
  }

  public void setErrorCodeVariable(String errorCodeVariable) {
    this.errorCodeVariable = errorCodeVariable;
  }

  public String getErrorCodeVariable() {
    return errorCodeVariable;
  }

  public void setErrorMessageVariable(String errorMessageVariable) {
    this.errorMessageVariable = errorMessageVariable;
  }

  public String getErrorMessageVariable() {
    return errorMessageVariable;
  }
}
