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

import org.camunda.bpm.engine.variable.VariableMap;

/**
 * Represents a delegated mapping of input and output variables.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public interface DelegateVariableMapping {

  /**
   * Maps the input variables into the given variables map.
   * The variables map will be used by the sub process.
   *
   * @param superExecution the execution object of the super (outer) process
   * @param subVariables the variables map of the sub (inner) process
   */
  void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables);

  /**
   * Maps the output variables into the outer process. This means the variables of
   * the sub process, which can be accessed via the subInstance, will be
   * set as variables into the super process, for example via ${superExecution.setVariables}.
   *
   * @param superExecution the execution object of the super (outer) process, which gets the output variables
   * @param subInstance the instance of the sub process, which contains the variables
   */
  void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance);

}
