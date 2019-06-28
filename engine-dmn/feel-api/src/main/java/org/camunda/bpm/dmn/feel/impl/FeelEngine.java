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
package org.camunda.bpm.dmn.feel.impl;

import org.camunda.bpm.engine.variable.context.VariableContext;

/**
 * Engine to evaluate FEEL expressions.
 */
public interface FeelEngine {

  /**
   * Evaluate a FEEL simple expression
   *
   * @param simpleExpression the simple expression to evaluate
   * @param variableContext the variable context which are available
   * @param <T> the expected return type
   * @return the result of the simple expression
   *
   * @throws FeelException
   *           if the expression cannot be evaluated
   */
  <T> T evaluateSimpleExpression(String simpleExpression, VariableContext variableContext);

  /**
   * Evaluate a FEEL simple unary tests expression
   *
   * @param simpleUnaryTests the simple unary tests expression to evaluate
   * @param inputName the name of the variable which is tested
   * @param variableContext the variable context are available
   * @return the result of the simple unary tests expression
   *
   * @throws FeelException
   *           if the expression cannot be evaluated
   */
  boolean evaluateSimpleUnaryTests(String simpleUnaryTests, String inputName, VariableContext variableContext);

}
