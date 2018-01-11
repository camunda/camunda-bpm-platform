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
package org.camunda.bpm.engine.variable.impl.type;

import java.util.Map;

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

  @Override
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

  protected IllegalArgumentException unsupportedConversion(ValueType typeToConvertTo) {
    return new IllegalArgumentException("The type " + getName() + " supports no conversion from type: " + typeToConvertTo.getName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractValueTypeImpl other = (AbstractValueTypeImpl) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  protected Boolean isTransient(Map<String, Object> valueInfo) {
    if (valueInfo != null && valueInfo.containsKey(VALUE_INFO_TRANSIENT)) {
      Object isTransient = valueInfo.get(VALUE_INFO_TRANSIENT);
      if (isTransient instanceof Boolean) {
        return (Boolean) isTransient;
      } else {
        throw new IllegalArgumentException("The property 'transient' should have a value of type 'boolean'.");
      }
    }
    return false;
  }

}
