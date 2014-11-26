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

package org.camunda.bpm.engine.impl.scripting;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;

/**
 * A script which source code is dynamically determined during the execution.
 * Therefore it has to be executed in the context of an atomic operation.
 *
 * @author Sebastian Menski
 */
public class DynamicSourceExecutableScript extends DynamicExecutableScript {

  public DynamicSourceExecutableScript(String language, Expression scriptSourceExpression) {
    super(scriptSourceExpression, language);
  }

  public String getScriptSource(VariableScope variableScope) {
    return evaluateExpression(variableScope);
  }

}
