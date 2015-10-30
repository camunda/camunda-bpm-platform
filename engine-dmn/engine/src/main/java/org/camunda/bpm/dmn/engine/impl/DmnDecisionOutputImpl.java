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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DmnDecisionOutputImpl implements DmnDecisionOutput {

  private static final long serialVersionUID = 1L;

  public static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  protected final Map<String, TypedValue> outputValues = new LinkedHashMap<String, TypedValue>();

  public void putValue(String name, TypedValue value) {
    outputValues.put(name, value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends TypedValue> T getValueTyped(String name) {
    return (T) outputValues.get(name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends TypedValue> T getFirstValueTyped() {
    if (!outputValues.isEmpty()) {
      return (T) outputValues.values().iterator().next();
    } else {
      return null;
    }
  }

  @Override
  public <T extends TypedValue> T getSingleValueTyped() {
    if (outputValues.size() > 1) {
      throw LOG.decisionOutputHasMoreThanOneValue(this);
    } else {
      return getFirstValueTyped();
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(String name) {
    if (outputValues.containsKey(name)) {
      return (T) getValueTyped(name).getValue();
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getFirstValue() {
    if (!outputValues.isEmpty()) {
      return (T) getFirstValueTyped().getValue();
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getSingleValue() {
    if (!outputValues.isEmpty()) {
      return (T) getSingleValueTyped().getValue();
    } else {
      return null;
    }
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
  public boolean containsKey(String key) {
    return outputValues.containsKey(key);
  }

  @Override
  public Set<String> keySet() {
    return outputValues.keySet();
  }

  @Override
  public Collection<Object> values() {
    List<Object> values = new ArrayList<Object>();

    for(TypedValue typedValue : outputValues.values()) {
      values.add(typedValue.getValue());
    }

    return values();
  }

  @Override
  public Collection<TypedValue> valuesTyped() {
    return outputValues.values();
  }

  @Override
  public String toString() {
    return outputValues.toString();
  }

}
