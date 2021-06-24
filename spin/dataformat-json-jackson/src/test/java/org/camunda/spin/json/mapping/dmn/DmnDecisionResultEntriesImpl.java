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
package org.camunda.spin.json.mapping.dmn;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Svetlana Dorokhova.
 */
public class DmnDecisionResultEntriesImpl implements DmnDecisionResultEntries {

  protected final Map<String, Object> outputValues = new LinkedHashMap<String, Object>();

  public void putValue(String name, Object value) {
    outputValues.put(name, value);
  }

  public void putAllValues(Map<String, Object> values) {
    outputValues.putAll(values);
  }

  @Override
  public int size() {
    return outputValues.size();
  }

  @Override
  public boolean isEmpty() {
    return outputValues.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return outputValues.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return outputValues.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    return outputValues.get(key);
  }

  @Override
  public Object put(String key, Object value) {
    return outputValues.put(key, value);
  }

  @Override
  public Object remove(Object key) {
    return outputValues.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ?> m) {
    outputValues.putAll(m);
  }

  @Override
  public void clear() {
    outputValues.clear();
  }

  @Override
  public Set<String> keySet() {
    return outputValues.keySet();
  }

  @Override
  public Collection<Object> values() {
    return outputValues.values();
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return outputValues.entrySet();
  }
}
