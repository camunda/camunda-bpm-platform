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
package org.camunda.bpm.dmn.feel.impl.scala.function;

import java.util.Collection;
import java.util.Optional;

/**
 * Provides one or more functions which can be used in an FEEL expression.
 */
public interface FeelCustomFunctionProvider {

  /**
   * Returns the function for the given name.
   *
   * @param functionName the name of the function
   * @return the function or {@link Optional#empty()}, if no function is provided for this name
   */
  Optional<CustomFunction> resolveFunction(String functionName);

  /**
   * Returns the names of all functions.
   *
   * @return the names of all functions
   */
  Collection<String> getFunctionNames();

}
