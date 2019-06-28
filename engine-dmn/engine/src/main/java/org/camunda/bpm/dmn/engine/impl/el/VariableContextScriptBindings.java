/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.dmn.engine.impl.el;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.script.Bindings;

import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * A Script {@link Bindings} implementation wrapping a provided
 * {@link VariableContext} and {@link Bindings} instance.
 *
 * Enhances the Bindings with the variables resolvable through the {@link VariableContext}.
 * The variables are treated as read only: all mutating operations write through to the
 * wrapped {@link Bindings}.
 *
 * @author Daniel Meyer
 *
 */
public class VariableContextScriptBindings implements Bindings {

  protected Bindings wrappedBindings;

  protected VariableContext variableContext;

  public VariableContextScriptBindings(Bindings wrappedBindings, VariableContext variableContext) {
    this.wrappedBindings = wrappedBindings;
    this.variableContext = variableContext;
  }

  /**
   * Dedicated implementation which does not fall back on the {@link #calculateBindingMap()} for performance reasons
   */
  public boolean containsKey(Object key) {
    if(wrappedBindings.containsKey(key)) {
      return true;
    }
    if (key instanceof String) {
      return variableContext.containsVariable((String) key);
    }
    else {
      return false;
    }
  }

  /**
   * Dedicated implementation which does not fall back on the {@link #calculateBindingMap()} for performance reasons
   */
  public Object get(Object key) {
    Object result = null;

    if(wrappedBindings.containsKey(key)) {
      result = wrappedBindings.get(key);
    }
    else {
      if (key instanceof String) {
        TypedValue resolvedValue = variableContext.resolve((String) key);
        result = unpack(resolvedValue);
      }
    }

    return result;
  }

  /**
   * Dedicated implementation which does not fall back on the {@link #calculateBindingMap()} for performance reasons
   */
  public Object put(String name, Object value) {
    // only write to the wrapped bindings
    return wrappedBindings.put(name, value);
  }

  public Set<Entry<String, Object>> entrySet() {
    return calculateBindingMap().entrySet();
  }

  public Set<String> keySet() {
    return calculateBindingMap().keySet();
  }

  public int size() {
    return calculateBindingMap().size();
  }

  public Collection<Object> values() {
    return calculateBindingMap().values();
  }

  public void putAll(Map< ? extends String, ?> toMerge) {
    for (Entry<? extends String, ?> entry : toMerge.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  public Object remove(Object key) {
    return wrappedBindings.remove(key);
  }

  public void clear() {
    wrappedBindings.clear();
  }

  public boolean containsValue(Object value) {
    return calculateBindingMap().containsValue(value);
  }

  public boolean isEmpty() {
    return calculateBindingMap().isEmpty();
  }

  protected Map<String, Object> calculateBindingMap() {

    Map<String, Object> bindingMap = new HashMap<String, Object>();

    Set<String> keySet = variableContext.keySet();
    for (String variableName : keySet) {
      bindingMap.put(variableName, unpack(variableContext.resolve(variableName)));
    }

    Set<Entry<String, Object>> wrappedBindingsEntries = wrappedBindings.entrySet();
    for (Entry<String, Object> entry : wrappedBindingsEntries) {
      bindingMap.put(entry.getKey(), entry.getValue());
    }

    return bindingMap;
  }

  protected Object unpack(TypedValue resolvedValue) {
    if(resolvedValue != null) {
      return resolvedValue.getValue();
    }
    return null;
  }

  public static VariableContextScriptBindings wrap(Bindings wrappedBindings, VariableContext variableContext) {
    return new VariableContextScriptBindings(wrappedBindings, variableContext);
  }

}
