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
package org.camunda.bpm.engine.variable.impl.value;

import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Used when the type of an object has not been specified by the user and
 * needs to be autodetected.
 *
 * @author Daniel Meyer
 *
 */
public class UntypedValueImpl implements TypedValue {

  private static final long serialVersionUID = 1L;

  protected Object value;

  protected boolean isTransient;

  public UntypedValueImpl(Object object) {
    this(object, false);
  }

  public UntypedValueImpl(Object object, boolean isTransient) {
    this.value = object;
    this.isTransient = isTransient;
  }

  public Object getValue() {
    return value;
  }

  public ValueType getType() {
    // no type
    return null;
  }

  @Override
  public String toString() {
    return "Untyped value '"+value+"', isTransient = " + isTransient;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    result = prime * result + (isTransient ? 1 : 0);
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
    UntypedValueImpl other = (UntypedValueImpl) obj;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    if (isTransient != other.isTransient()) {
      return false;
    }
    return true;
  }

  @Override
  public boolean isTransient() {
    return isTransient;
  }

  public void setTransient(boolean isTransient) {
    this.isTransient = isTransient;
  }

}
