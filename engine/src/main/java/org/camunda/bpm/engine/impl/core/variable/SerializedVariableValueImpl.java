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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.SerializedVariableValue;

/**
 * @author Thorben Lindhauer
 */
public class SerializedVariableValueImpl implements SerializedVariableValue {

  protected Object value;
  protected Map<String, Object> config = new HashMap<String, Object>();

  public Object getValue() {
    return value;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public void setConfigValue(String key, Object value) {
    this.config.put(key, value);
  }

  public void setConfig(Map<String, Object> serializedValue) {
    this.config.putAll(serializedValue);
  }

  public String toString() {
    if(value != null) {
      return value.toString();
    } else {
      return super.toString();
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((config == null) ? 0 : config.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    SerializedVariableValueImpl other = (SerializedVariableValueImpl) obj;
    if (config == null) {
      if (other.config != null)
        return false;
    } else if (!config.equals(other.config))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

}
