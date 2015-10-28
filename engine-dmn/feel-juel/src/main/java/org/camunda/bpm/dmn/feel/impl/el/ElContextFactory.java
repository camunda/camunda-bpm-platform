/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.dmn.feel.impl.el;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import org.camunda.bpm.engine.variable.VariableContext;
import org.camunda.bpm.engine.variable.VariableMap;

public interface ElContextFactory {

  /**
   * Create a {@link ELContext} for the given {@link ExpressionFactory} and {@link VariableMap}.
   *
   * @param expressionFactory the {@link ExpressionFactory} to use
   * @param varCtx the {@link VariableMap} to use
   * @return the {@link ELContext} instance
   */
  ELContext createContext(ExpressionFactory expressionFactory, VariableContext varCtx);

}
