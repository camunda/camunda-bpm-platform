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

public interface FeelToJuelTransformer {

  /**
   * Test if an expression can be transformed by this transformer.
   *
   * @param feelExpression the FEEL expression to transform
   * @return true if the expression can be transformed by this transformer, false otherwise
   */
  boolean canTransform(String feelExpression);

  /**
   * Transform the FEEL expression to a JUEL expression.
   *
   * @param transform the {@link FeelToJuelTransform} to use for further transforms
   * @param feelExpression the FEEL expression to transform
   * @param inputName the variable name of the input variable to test against
   * @return the resulting JUEL expression
   */
  String transform(FeelToJuelTransform transform, String feelExpression, String inputName);

}
