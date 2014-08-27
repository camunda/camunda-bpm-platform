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
package org.camunda.bpm.engine.impl.core.variable;

import org.camunda.bpm.engine.delegate.PersistentVariableInstance;
import org.camunda.bpm.engine.delegate.SerializedVariableValue;

public class PseudoPersistentVariableInstance implements PersistentVariableInstance {

  protected String name;
  protected Object value;

  public PseudoPersistentVariableInstance(String name, Object value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  public String getErrorMessage() {
   throw new UnsupportedOperationException("This variable is not truly persistent");
  }

  public SerializedVariableValue getSerializedValue() {
    throw new UnsupportedOperationException("This variable is not truly persistent");
  }

  public String getTypeName() {
    throw new UnsupportedOperationException("This variable is not truly persistent");
  }

  public String getValueTypeName() {
    return value.getClass().getSimpleName();
  }

  public boolean storesCustomObjects() {
    throw new UnsupportedOperationException("This variable is not truly persistent");
  }

}
