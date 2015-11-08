package org.camunda.bpm.engine.cdi.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.value.TypedValue;

abstract class AbstractVariableMap implements VariableMap {

  @Inject
  protected BusinessProcess businessProcess;

  abstract protected Object getVariable(String variableName);
  abstract protected <T extends TypedValue> T getVariableTyped(String variableName);

  abstract protected void setVariable(String variableName, Object value);

  @Override
  public Object get(Object key) {
    if(key == null) {
      throw new IllegalArgumentException("This map does not support 'null' keys.");
    }
    return getVariable(key.toString());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getValue(String name, Class<T> type) {
    Object object = get(name);
    if (object == null) {
      return null;
    } else if (type.isAssignableFrom(object.getClass())) {
      return (T) object;
    } else {
      throw new ClassCastException("Cannot cast variable named '" + name + "' with value '" + object + "' to type '" + type + "'.");
    }
  }

  @Override
  public <T extends TypedValue> T getValueTyped(String name) {
    if (name == null) {
      throw new IllegalArgumentException("This map does not support 'null' keys.");
    }
    return getVariableTyped(name);
  }

  @Override
  public Object put(String key, Object value) {
    if(key == null) {
      throw new IllegalArgumentException("This map does not support 'null' keys.");
    }
    Object variableBefore = getVariable(key);
    setVariable(key, value);
    return variableBefore;
  }

  @Override
  public void putAll(Map< ? extends String, ? extends Object> m) {
    for (java.util.Map.Entry< ? extends String, ? extends Object> newEntry : m.entrySet()) {
      setVariable(newEntry.getKey(), newEntry.getValue());
    }
  }

  @Override
  public VariableMap putValue(String name, Object value) {
    put(name, value);
    return this;
  }

  @Override
  public VariableMap putValueTyped(String name, TypedValue value) {
    if(name == null) {
      throw new IllegalArgumentException("This map does not support 'null' names.");
    }
    setVariable(name, value);
    return this;
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException(getClass().getName()+".size() is not supported.");
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException(getClass().getName()+".isEmpty() is not supported.");
  }

  @Override
  public boolean containsKey(Object key) {
    throw new UnsupportedOperationException(getClass().getName()+".containsKey() is not supported.");
  }

  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException(getClass().getName()+".containsValue() is not supported.");
  }

  @Override
  public Object remove(Object key) {
    throw new UnsupportedOperationException(getClass().getName()+".remove is unsupported. Use " + getClass().getName() + ".put(key, null)");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException(getClass().getName()+".clear() is not supported.");
  }

  @Override
  public Set<String> keySet() {
    throw new UnsupportedOperationException(getClass().getName()+".keySet() is not supported.");
  }

  @Override
  public Collection<Object> values() {
    throw new UnsupportedOperationException(getClass().getName()+".values() is not supported.");
  }

  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    throw new UnsupportedOperationException(getClass().getName()+".entrySet() is not supported.");
  }

  public VariableContext asVariableContext() {
    throw new UnsupportedOperationException(getClass().getName()+".asVariableContext() is not supported.");
  }

}
