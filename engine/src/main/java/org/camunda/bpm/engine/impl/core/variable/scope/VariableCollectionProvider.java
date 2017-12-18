/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.core.variable.scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableStore.VariablesProvider;

/**
 * @author Thorben Lindhauer
 *
 */
public class VariableCollectionProvider<T extends CoreVariableInstance> implements VariablesProvider<T> {

  protected Collection<T> variables;

  public VariableCollectionProvider(Collection<T> variables) {
    this.variables = variables;
  }

  @Override
  public Collection<T> provideVariables() {
    if (variables == null) {
      return new ArrayList<T>();
    }
    else {
      return variables;
    }
  }

  @Override
  public Collection<T> provideVariables(Collection<String> variablesNames) {
    if (variablesNames == null) {
      return provideVariables();
    }

    List<T> result = new ArrayList<T>();
    if (variables != null) {
      for (T variable : variables) {
        if (variablesNames.contains(variable.getName())) {
          result.add(variable);
        }
      }
    }
    return result;
  }

  public static <T extends CoreVariableInstance> VariableCollectionProvider<T> emptyVariables() {
    return new VariableCollectionProvider<T>(Collections.<T>emptySet());
  }

}
