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
package org.camunda.bpm.engine.impl.core.variable.type;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class AbstractValueTypeImpl implements ValueType {

  private static final long serialVersionUID = 1L;

  protected String name;

  public AbstractValueTypeImpl(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return name;
  }

  public boolean isAbstract() {
    return false;
  }

  public ValueType getParent() {
    return null;
  }

  public boolean canConvertFromTypedValue(TypedValue typedValue) {
    return false;
  }

  public TypedValue convertFromTypedValue(TypedValue typedValue) {
    throw unsupportedConversion(typedValue.getType());
  }

  protected ProcessEngineException unsupportedConversion(ValueType typeToConvertTo) {
    return new ProcessEngineException("The type " + getName() + " supports no conversion from type: " + typeToConvertTo.getName());
  }

}
