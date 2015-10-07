/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.dmn.engine.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.engine.impl.core.variable.VariableMapImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DmnDecisionOutputImpl implements DmnDecisionOutput {

  protected final VariableMap variables = new VariableMapImpl();

  @SuppressWarnings("unchecked")
  public <T> T getValue(String name) {
    return (T) get(name);
  }

  @Override
  public TypedValue getValue() {
    if (!isEmpty()) {
      String key = keySet().iterator().next();
      return getValueTyped(key);
    } else {
      return null;
    }
  }

  public VariableMap putValue(String name, Object value) {
    return variables.putValue(name, value);
  }

  public VariableMap putValueTyped(String name, TypedValue value) {
    return variables.putValueTyped(name, value);
  }

  public <T> T getValue(String name, Class<T> type) {
    return variables.getValue(name, type);
  }

  public <T extends TypedValue> T getValueTyped(String name) {
    return variables.getValueTyped(name);
  }

  public int size() {
    return variables.size();
  }

  public boolean isEmpty() {
    return variables.isEmpty();
  }

  public boolean containsKey(Object key) {
    return variables.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return variables.containsValue(value);
  }

  public Object get(Object key) {
    return variables.get(key);
  }

  public Object put(String key, Object value) {
    return variables.put(key, value);
  }

  public Object remove(Object key) {
    return variables.remove(key);
  }

  public void putAll(Map<? extends String, ? extends Object> m) {
    variables.putAll(m);
  }

  public void clear() {
    variables.clear();
  }

  public Set<String> keySet() {
    return variables.keySet();
  }

  public Collection<Object> values() {
    return variables.values();
  }

  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    return variables.entrySet();
  }

  @Override
  public boolean equals(Object o) {
    return variables.equals(o);
  }

  @Override
  public int hashCode() {
    return variables.hashCode();
  }

}
