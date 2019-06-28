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
package org.camunda.bpm.dmn.feel.impl.juel.transform;

import java.lang.reflect.Method;

public interface FeelToJuelFunctionTransformer extends FeelToJuelTransformer {

  /**
   * Get the name of the function.
   *
   * @return the name of function
   */
  String getName();

  /**
   * Get the method reference which implements the function to transform to.
   * Note: The implementation should resolve the method reference only once at creation
   * and not within every call of these method.
   *
   * @return the method reference
   */
  Method getMethod();

}
