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

import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 *
 */
public class SimpleVariableStore extends MapBasedVariableStore {

  public static class SimpleVariableInstance implements CoreVariableInstance {

    protected String name;
    protected TypedValue value;

    public SimpleVariableInstance(String name, TypedValue value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
    public TypedValue getTypedValue(boolean deserialize) {
      return value;
    }
    public void setValue(TypedValue value) {
      this.value = value;
    }
  }

  @Override
  public CoreVariableInstance createVariableInstance(String variableName, TypedValue value, AbstractVariableScope sourceActivityExecution) {
    SimpleVariableInstance instance = new SimpleVariableInstance(variableName, value);
    variables.put(variableName, instance);
    return instance;
  }

  @Override
  public void setVariableValue(CoreVariableInstance variableInstance, TypedValue value, AbstractVariableScope sourceActivityExecution) {
    ((SimpleVariableInstance) variableInstance).value = value;
  }

  @Override
  public void createTransientVariable(String variableName, TypedValue value, AbstractVariableScope sourceActivityExecution) {
    // all variables are transient in this store
    createVariableInstance(variableName, value, sourceActivityExecution);
  }


}
