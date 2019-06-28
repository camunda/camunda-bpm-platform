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
package org.camunda.bpm.dmn.engine.delegate;

import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * The output for a evaluated decision.
 *
 * <p>
 *   In a decision table implementation an output can have a human readable
 *   name and a name which can be used to reference the output value in
 *   the decision result.
 * </p>
 *
 * <p>
 *   The human readable name is the {@code label} attribute of the DMN XML
 *   {@code output} element. You can access this name by the {@link #getName()}
 *   getter.
 * </p>
 *
 * <p>
 *   The output name to reference the output value in the decision result
 *   is the {@code name} attribute of the DMN XML {@code output} element.
 *   You can access this output name by the {@link #getOutputName()}
 *   getter.
 * </p>
 *
 * <p>
 *   The {@code id} and {@code value} of the evaluated decision table
 *   output entry can be access by the {@link #getId()} and {@link #getValue()}
 *   getter.
 * </p>
 */
public interface DmnEvaluatedOutput {

  /**
   * @return the id of the evaluated output or null if not set
   */
  String getId();

  /**
   * @return the name of the evaluated output or null if not set
   */
  String getName();

  /**
   * @return the output name of the evaluated output or null if not set
   */
  String getOutputName();

  /**
   * @return the value of the evaluated output or null if non set
   */
  TypedValue getValue();

}
