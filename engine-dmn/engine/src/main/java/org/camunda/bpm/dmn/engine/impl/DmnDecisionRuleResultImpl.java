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
package org.camunda.bpm.dmn.engine.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DmnDecisionRuleResultImpl implements DmnDecisionRuleResult {

  private static final long serialVersionUID = 1L;

  public static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  protected final Map<String, TypedValue> outputValues = new LinkedHashMap<String, TypedValue>();

  public void putValue(String name, TypedValue value) {
    outputValues.put(name, value);
  }

  public void putAllValues(Map<String, TypedValue> values) {
    outputValues.putAll(values);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getEntry(String name) {
    return (T) outputValues.get(name).getValue();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends TypedValue> T getEntryTyped(String name) {
    return (T) outputValues.get(name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends TypedValue> T getFirstEntryTyped() {
    if (!outputValues.isEmpty()) {
      return (T) outputValues.values().iterator().next();
    } else {
      return null;
    }
  }

  @Override
  public <T extends TypedValue> T getSingleEntryTyped() {
    if (outputValues.size() > 1) {
      throw LOG.decisionOutputHasMoreThanOneValue(this);
    } else {
      return getFirstEntryTyped();
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getFirstEntry() {
    if (!outputValues.isEmpty()) {
      return (T) getFirstEntryTyped().getValue();
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getSingleEntry() {
    if (!outputValues.isEmpty()) {
      return (T) getSingleEntryTyped().getValue();
    } else {
      return null;
    }
  }

  @Override
  public Map<String, Object> getEntryMap() {
    Map<String, Object> valueMap = new HashMap<String, Object>();

    for (String key : outputValues.keySet()) {
      valueMap.put(key, get(key));
    }

    return valueMap;
  }

  public Map<String, TypedValue> getEntryMapTyped() {
    return outputValues;
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
  public Set<String> keySet() {
    return outputValues.keySet();
  }

  @Override
  public Collection<Object> values() {
    List<Object> values = new ArrayList<Object>();

    for (TypedValue typedValue : outputValues.values()) {
      values.add(typedValue.getValue());
    }

    return values;
  }

  @Override
  public String toString() {
    return outputValues.toString();
  }

  @Override
  public boolean containsValue(Object value) {
    return values().contains(value);
  }

  @Override
  public Object get(Object key) {
    TypedValue typedValue = outputValues.get(key);
    if (typedValue != null) {
      return typedValue.getValue();
    } else {
      return null;
    }
  }

  @Override
  public Object put(String key, Object value) {
    throw new UnsupportedOperationException("decision output is immutable");
  }

  @Override
  public Object remove(Object key) {
    throw new UnsupportedOperationException("decision output is immutable");
  }

  @Override
  public void putAll(Map<? extends String, ?> m) {
    throw new UnsupportedOperationException("decision output is immutable");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("decision output is immutable");
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    Set<Entry<String, Object>> entrySet = new HashSet<Entry<String, Object>>();

    for (Entry<String, TypedValue> typedEntry : outputValues.entrySet()) {
      DmnDecisionRuleOutputEntry entry = new DmnDecisionRuleOutputEntry(typedEntry.getKey(), typedEntry.getValue());
      entrySet.add(entry);
    }

    return entrySet;
  }

  protected class DmnDecisionRuleOutputEntry implements Entry<String, Object> {

    protected final String key;
    protected final TypedValue typedValue;

    public DmnDecisionRuleOutputEntry(String key, TypedValue typedValue) {
      this.key = key;
      this.typedValue = typedValue;
    }

    @Override
    public String getKey() {
      return key;
    }

    @Override
    public Object getValue() {
      if (typedValue != null) {
        return typedValue.getValue();
      } else {
        return null;
      }
    }

    @Override
    public Object setValue(Object value) {
      throw new UnsupportedOperationException("decision output entry is immutable");
    }

  }

}
