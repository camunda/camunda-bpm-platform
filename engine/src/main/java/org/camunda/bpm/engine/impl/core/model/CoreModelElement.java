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
package org.camunda.bpm.engine.impl.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.delegate.VariableListener;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 *
 */
public abstract class CoreModelElement implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String name;
  protected Properties properties = new Properties();

  /** contains built-in listeners */
  protected Map<String, List<DelegateListener<? extends BaseDelegateExecution>>> builtInListeners = new HashMap<String, List<DelegateListener<? extends BaseDelegateExecution>>>();

  /** contains all listeners (built-in + user-provided) */
  protected Map<String, List<DelegateListener<? extends BaseDelegateExecution>>> listeners = new HashMap<String, List<DelegateListener<? extends BaseDelegateExecution>>>();

  protected Map<String, List<VariableListener<?>>> builtInVariableListeners =
      new HashMap<String, List<VariableListener<?>>>();

  protected Map<String, List<VariableListener<?>>> variableListeners =
      new HashMap<String, List<VariableListener<?>>>();

  public CoreModelElement(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  /**
   * @see Properties#set(PropertyKey, Object)
   */
  public void setProperty(String name, Object value) {
    properties.set(new PropertyKey<Object>(name), value);
  }

  /**
   * @see Properties#get(PropertyKey)
   */
  public Object getProperty(String name) {
    return properties.get(new PropertyKey<Object>(name));
  }

  /**
   * Returns the properties of the element.
   *
   * @return the properties
   */
  public Properties getProperties() {
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  //event listeners //////////////////////////////////////////////////////////

  public List<DelegateListener<? extends BaseDelegateExecution>> getListeners(String eventName) {
    List<DelegateListener<? extends BaseDelegateExecution>> listenerList = getListeners().get(eventName);
    if (listenerList != null) {
      return listenerList;
    }
    return Collections.emptyList();
  }

  public List<DelegateListener<? extends BaseDelegateExecution>> getBuiltInListeners(String eventName) {
    List<DelegateListener<? extends BaseDelegateExecution>> listenerList = getBuiltInListeners().get(eventName);
    if (listenerList != null) {
      return listenerList;
    }
    return Collections.emptyList();
  }

  public List<VariableListener<?>> getVariableListenersLocal(String eventName) {
    List<VariableListener<?>> listenerList = getVariableListeners().get(eventName);
    if (listenerList != null) {
      return listenerList;
    }
    return Collections.emptyList();
  }

  public List<VariableListener<?>> getBuiltInVariableListenersLocal(String eventName) {
    List<VariableListener<?>> listenerList = getBuiltInVariableListeners().get(eventName);
    if (listenerList != null) {
      return listenerList;
    }
    return Collections.emptyList();
  }

  public void addListener(String eventName, DelegateListener<? extends BaseDelegateExecution> listener) {
    addListener(eventName, listener, -1);
  }

  public void addBuiltInListener(String eventName, DelegateListener<? extends BaseDelegateExecution> listener) {
    addBuiltInListener(eventName, listener, -1);
  }

  public void addBuiltInListener(String eventName, DelegateListener<? extends BaseDelegateExecution> listener, int index) {
    addListenerToMap(listeners, eventName, listener, index);
    addListenerToMap(builtInListeners, eventName, listener, index);
  }

  public void addListener(String eventName, DelegateListener<? extends BaseDelegateExecution> listener, int index) {
    addListenerToMap(listeners, eventName, listener, index);
  }

  protected <T> void addListenerToMap(Map<String, List<T>> listenerMap, String eventName, T listener, int index) {
    List<T> listeners = listenerMap.get(eventName);
    if (listeners == null) {
      listeners = new ArrayList<T>();
      listenerMap.put(eventName, listeners);
    }
    if (index < 0) {
      listeners.add(listener);
    } else {
      listeners.add(index, listener);
    }
  }

  public void addVariableListener(String eventName, VariableListener<?> listener) {
    addVariableListener(eventName, listener, -1);
  }

  public void addVariableListener(String eventName, VariableListener<?> listener, int index) {
    addListenerToMap(variableListeners, eventName, listener, index);
  }

  public void addBuiltInVariableListener(String eventName, VariableListener<?> listener) {
    addBuiltInVariableListener(eventName, listener, -1);
  }

  public void addBuiltInVariableListener(String eventName, VariableListener<?> listener, int index) {
    addListenerToMap(variableListeners, eventName, listener, index);
    addListenerToMap(builtInVariableListeners, eventName, listener, index);
  }

  public Map<String, List<DelegateListener<? extends BaseDelegateExecution>>> getListeners() {
    return listeners;
  }

  public Map<String, List<DelegateListener<? extends BaseDelegateExecution>>> getBuiltInListeners() {
    return builtInListeners;
  }

  public Map<String, List<VariableListener<?>>> getBuiltInVariableListeners() {
    return builtInVariableListeners;
  }

  public Map<String, List<VariableListener<?>>> getVariableListeners() {
    return variableListeners;
  }

}
