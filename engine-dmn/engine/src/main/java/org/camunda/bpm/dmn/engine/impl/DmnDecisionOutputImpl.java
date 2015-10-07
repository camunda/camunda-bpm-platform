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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

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

  public Object getOrDefault(Object key, Object defaultValue) {
    return variables.getOrDefault(key, defaultValue);
  }

  public void forEach(BiConsumer<? super String, ? super Object> action) {
    variables.forEach(action);
  }

  public void replaceAll(BiFunction<? super String, ? super Object, ? extends Object> function) {
    variables.replaceAll(function);
  }

  public Object putIfAbsent(String key, Object value) {
    return variables.putIfAbsent(key, value);
  }

  public Object computeIfAbsent(String key, Function<? super String, ? extends Object> mappingFunction) {
    return variables.computeIfAbsent(key, mappingFunction);
  }

  public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ? extends Object> remappingFunction) {
    return variables.computeIfPresent(key, remappingFunction);
  }

  public Object compute(String key, BiFunction<? super String, ? super Object, ? extends Object> remappingFunction) {
    return variables.compute(key, remappingFunction);
  }

  public <T> T getValue(String arg0, Class<T> arg1) {
    return variables.getValue(arg0, arg1);
  }

  public <T extends TypedValue> T getValueTyped(String arg0) {
    return variables.getValueTyped(arg0);
  }

  public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
    return variables.merge(key, value, remappingFunction);
  }

  public VariableMap putValue(String arg0, Object arg1) {
    return variables.putValue(arg0, arg1);
  }

  public VariableMap putValueTyped(String arg0, TypedValue arg1) {
    return variables.putValueTyped(arg0, arg1);
  }

  public boolean remove(Object key, Object value) {
    return variables.remove(key, value);
  }

  public boolean replace(String key, Object oldValue, Object newValue) {
    return variables.replace(key, oldValue, newValue);
  }

  public Object replace(String key, Object value) {
    return variables.replace(key, value);
  }

}
