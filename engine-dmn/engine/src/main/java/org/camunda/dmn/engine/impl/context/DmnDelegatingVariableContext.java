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

package org.camunda.dmn.engine.impl.context;

import java.util.HashMap;
import java.util.Map;

import org.camunda.dmn.engine.context.DmnVariableContext;
import org.camunda.dmn.engine.impl.context.DmnVariableContextImpl;

public class DmnDelegatingVariableContext extends DmnVariableContextImpl {

  protected DmnVariableContext delegateVariableContext;

  public DmnDelegatingVariableContext(DmnVariableContext delegateVariableContext) {
    this.delegateVariableContext = delegateVariableContext;
  }

  public Map<String, Object> getVariables() {
    HashMap<String, Object> collectedVariables = new HashMap<String, Object>(delegateVariableContext.getVariables());
    collectedVariables.putAll(variables);
    return collectedVariables;
  }

  @SuppressWarnings("unchecked")
  public <T> T getVariable(String name) {
    if (variables.containsKey(name)) {
      return (T) variables.get(name);
    }
    else {
      return delegateVariableContext.getVariable(name);
    }
  }

}
