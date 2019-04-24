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
package org.camunda.bpm.engine.runtime;

import java.util.Map;

/**
 * @author Thorben Lindhauer
 */
public interface ActivityInstantiationBuilder<T extends ActivityInstantiationBuilder<T>> {

  /**
   * If an instruction is submitted before then the variable is set when the
   * instruction is executed. Otherwise, the variable is set on the process
   * instance itself.
   */
  T setVariable(String name, Object value);

  /**
   * If an instruction is submitted before then the local variable is set when
   * the instruction is executed. Otherwise, the variable is set on the process
   * instance itself.
   */
  T setVariableLocal(String name, Object value);

  /**
   * If an instruction is submitted before then all variables are set when the
   * instruction is executed. Otherwise, the variables are set on the process
   * instance itself.
   */
  T setVariables(Map<String, Object> variables);

  /**
   * If an instruction is submitted before then all local variables are set when
   * the instruction is executed. Otherwise, the variables are set on the
   * process instance itself.
   */
  T setVariablesLocal(Map<String, Object> variables);

}
