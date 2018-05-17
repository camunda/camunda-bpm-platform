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
 * An empty variable context implementation which does
 * not allow to resolve any variables.
 *
 * @author Daniel Meyer
 *
 */
public class EmptyVariableContext implements VariableContext {

  public final static EmptyVariableContext INSTANCE = new EmptyVariableContext();

  private EmptyVariableContext() {
    // hidden
  }

  public TypedValue resolve(String variableName) {
    return null;
  }

  public boolean containsVariable(String variableName) {
    return false;
  }

  public Set<String> keySet() {
    return Collections.emptySet();
  }

}
