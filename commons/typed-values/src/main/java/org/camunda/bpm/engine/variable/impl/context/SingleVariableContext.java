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
package org.camunda.bpm.engine.variable.impl.context;

import java.util.Collections;
import java.util.Set;

import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * An {@link VariableContext} allowing to resolve a single variable only.
 *
 * @author Daniel Meyer
 *
 */
public class SingleVariableContext implements VariableContext {

  protected final TypedValue typedValue;
  protected String name;

  public SingleVariableContext(String name, TypedValue typedValue) {
    this.name = name;
    this.typedValue = typedValue;
  }

  public TypedValue resolve(String variableName) {
    if(containsVariable(variableName)) {
      return typedValue;
    }
    else {
      return null;
    }
  }

  public boolean containsVariable(String name) {
    if(this.name == null) {
      return name == null;
    }
    else {
      return this.name.equals(name);
    }
  }

  public Set<String> keySet() {
    return Collections.singleton(name);
  }

  public static SingleVariableContext singleVariable(String name, TypedValue value) {
    return new SingleVariableContext(name, value);
  }

}
