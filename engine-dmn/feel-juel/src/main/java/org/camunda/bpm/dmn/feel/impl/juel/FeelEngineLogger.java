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
package org.camunda.bpm.dmn.feel.impl.juel;

import org.camunda.bpm.dmn.feel.impl.FeelException;

public class FeelEngineLogger extends FeelLogger {

  protected FeelSyntaxException syntaxException(String id, String feelExpression, String description) {
    return new FeelSyntaxException(syntaxExceptionMessage(id, feelExpression, description), feelExpression, description);
  }

  protected FeelSyntaxException syntaxException(String id, String feelExpression, String description, Throwable cause) {
    return new FeelSyntaxException(syntaxExceptionMessage(id, feelExpression, description), feelExpression, description, cause);
  }

  protected String syntaxExceptionMessage(String id, String feelExpression, String description) {
    if (description != null) {
      return exceptionMessage(
        id,
        "Syntax error in expression '{}': {}", feelExpression, description
      );
    }
    else {
      return exceptionMessage(
        id, "Syntax error in expression '{}'", feelExpression
      );
    }
  }

  public FeelSyntaxException invalidNotExpression(String feelExpression) {
    String description = "Expression should have format 'not(...)'";
    return syntaxException("001", feelExpression, description);
  }

  public FeelSyntaxException invalidIntervalExpression(String feelExpression) {
    String description = "Expression should have format '[|(|] endpoint .. endpoint ]|)|['";
    return syntaxException("002", feelExpression, description);
  }

  public FeelSyntaxException invalidComparisonExpression(String feelExpression) {
    String description = "Expression should have format '<=|<|>=|> endpoint'";
    return syntaxException("003", feelExpression, description);
  }

  public FeelException variableMapperIsReadOnly() {
    return new FeelException(exceptionMessage(
      "004",
      "The variable mapper is read only.")
    );
  }

  public FeelException unableToFindMethod(NoSuchMethodException cause, String name, Class<?>... parameterTypes) {
    return new FeelException(exceptionMessage(
      "005",
      "Unable to find method '{}' with parameter types '{}'", name, parameterTypes),
      cause
    );
  }

  public FeelMissingFunctionException unknownFunction(String prefix, String localName) {
    String function = localName;
    if (prefix != null && !prefix.isEmpty()) {
      function = prefix + ":" + localName;
    }
    return new FeelMissingFunctionException(exceptionMessage(
      "006",
      "Unable to resolve function '{}'", function),
      function
    );
  }

  public FeelMissingFunctionException unknownFunction(String feelExpression, FeelMissingFunctionException cause) {
    String function = cause.getFunction();
    return new FeelMissingFunctionException(exceptionMessage(
      "007",
      "Unable to resolve function '{}' in expression '{}'" , function, feelExpression),
      function
    );
  }

  public FeelMissingVariableException unknownVariable(String variable) {
    return new FeelMissingVariableException(exceptionMessage(
      "008",
      "Unable to resolve variable '{}'", variable),
      variable
    );
  }

  public FeelMissingVariableException unknownVariable(String feelExpression, FeelMissingVariableException cause) {
    String variable = cause.getVariable();
    return new FeelMissingVariableException(exceptionMessage(
      "009",
      "Unable to resolve variable '{}' in expression '{}'", variable, feelExpression),
      variable
    );
  }

  public FeelSyntaxException invalidExpression(String feelExpression, Throwable cause) {
    return syntaxException("010", feelExpression, null, cause);
  }

  public FeelException unableToInitializeFeelEngine(Throwable cause) {
    return new FeelException(exceptionMessage(
      "011",
      "Unable to initialize FEEL engine"),
      cause
    );
  }

  public FeelException unableToEvaluateExpression(String simpleUnaryTests, Throwable cause) {
    return new FeelException(exceptionMessage(
      "012",
      "Unable to evaluate expression '{}'", simpleUnaryTests),
      cause
    );
  }

  public FeelConvertException unableToConvertValue(Object value, Class<?> type) {
    return new FeelConvertException(exceptionMessage(
      "013",
      "Unable to convert value '{}' of type '{}' to type '{}'", value, value.getClass(), type),
      value, type
    );
  }

  public FeelConvertException unableToConvertValue(Object value, Class<?> type, Throwable cause) {
    return new FeelConvertException(exceptionMessage(
      "014",
      "Unable to convert value '{}' of type '{}' to type '{}'", value, value.getClass(), type),
      value, type, cause
    );
  }

  public FeelConvertException unableToConvertValue(String feelExpression, FeelConvertException cause) {
    Object value = cause.getValue();
    Class<?> type = cause.getType();
    return new FeelConvertException(exceptionMessage(
      "015",
      "Unable to convert value '{}' of type '{}' to type '{}' in expression '{}'", value, value.getClass(), type, feelExpression),
      cause
    );
  }

  public UnsupportedOperationException simpleExpressionNotSupported() {
    return new UnsupportedOperationException(exceptionMessage(
      "016",
      "Simple Expression not supported by FEEL engine")
    );
  }

  public FeelException unableToEvaluateExpressionAsNotInputIsSet(String simpleUnaryTests, FeelMissingVariableException e) {
    return new FeelException(exceptionMessage(
      "017",
      "Unable to evaluate expression '{}' as no input is set. Maybe the inputExpression is missing or empty.", simpleUnaryTests),
      e
    );
  }

  public FeelMethodInvocationException invalidDateAndTimeFormat(String dateTimeString, Throwable cause) {
    return new FeelMethodInvocationException(exceptionMessage(
      "018",
      "Invalid date and time format in '{}'", dateTimeString),
      cause, "date and time", dateTimeString
    );
  }

  public FeelMethodInvocationException unableToInvokeMethod(String simpleUnaryTests, FeelMethodInvocationException cause) {
    String method = cause.getMethod();
    String[] parameters = cause.getParameters();
    return new FeelMethodInvocationException(exceptionMessage(
      "019",
      "Unable to invoke method '{}' with parameters '{}' in expression '{}'", method, parameters, simpleUnaryTests),
      cause.getCause(), method, parameters
    );
  }

  public FeelSyntaxException invalidListExpression(String feelExpression) {
    String description = "List expression can not have empty elements";
    return syntaxException("020", feelExpression, description);
  }

}
