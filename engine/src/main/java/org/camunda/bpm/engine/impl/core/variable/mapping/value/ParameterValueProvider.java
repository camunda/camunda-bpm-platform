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
package org.camunda.bpm.engine.impl.core.variable.mapping.value;

import org.camunda.bpm.engine.delegate.VariableScope;

/**
 *
 * @author Daniel Meyer
 *
 */
public interface ParameterValueProvider {

  /**
   * @param variableScope the scope in which the value is to be resolved.
   * @return the value
   */
  Object getValue(VariableScope variableScope);

  /**
  * @return true if the value provider:
  *
  * <ul>
  *   <li>Can return a different value depending on the passed variable scope
  *   <li>May uses external data for its resolution
  *   <li>May have side effects
  * </ul>
  *
  * If true, a caller of {@link #getValue(VariableScope)} can assume that:
  *
  * <ul>
  *   <li>passing an empty variable scope returns the same value as passing any other variable scope
  *   <li>Calling {@link #getValue(VariableScope)} multiple times always returns the same value
  *   <li>Calling {@link #getValue(VariableScope)} does not have side effects
  * </ul>
  */
  boolean isDynamic();

}
