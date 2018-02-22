package org.camunda.spin.json.mapping.dmn;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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
