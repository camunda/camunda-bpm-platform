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
 * Untyped Null
 *
 * @author Daniel Meyer
 *
 */
public class NullValueImpl implements TypedValue {

  private static final long serialVersionUID = 1L;

  private boolean isTransient;

  // null is always null
  public static final NullValueImpl INSTANCE = new NullValueImpl(false);
  public static final NullValueImpl INSTANCE_TRANSIENT = new NullValueImpl(true);

  private NullValueImpl(boolean isTransient) {
    this.isTransient = isTransient;
  }

  public Object getValue() {
    return null;
  }

  public ValueType getType() {
    return ValueType.NULL;
  }

  public String toString() {
    return "Untyped 'null' value";
  }

  @Override
  public boolean isTransient() {
    return isTransient;
  }

}
