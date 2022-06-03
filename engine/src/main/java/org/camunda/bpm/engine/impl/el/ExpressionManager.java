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
package org.camunda.bpm.engine.impl.el;

import java.lang.reflect.Method;

/**
 * <p>
 * Central manager for all expressions.
 * </p>
 * <p>
 * Process parsers will use this to build expression objects that are stored in
 * the process definitions.
 * </p>
 * <p>
 * Then also this class is used as an entry point for runtime evaluation of the
 * expressions.
 * </p>
 */
public interface ExpressionManager {
  
  /**
   * @param expression
   * @return a parsed expression
   */
  Expression createExpression(String expression);

  /**
   * <p>
   * Adds a custom function to the expression manager that can be used in
   * expression evaluation later on. Ideally, use this in the setup phase of the
   * expression manager, i.e. before the first invocation of
   * {@link #createExpression(String) createExpression}.
   * </p>
   * 
   * @param name
   * @param function
   */
  void addFunction(String name, Method function);
}
