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
  protected Map<String, Object> properties;
  protected Map<String, List<DelegateListener<? extends BaseDelegateExecution>>> listeners = new HashMap<String, List<DelegateListener<? extends BaseDelegateExecution>>>();

  public CoreModelElement(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setProperty(String name, Object value) {
    if (properties==null) {
      properties = new HashMap<String, Object>();
    }
    properties.put(name, value);
  }

  public Object getProperty(String name) {
    if (properties==null) {
      return null;
    }
    return properties.get(name);
  }

  public Map<String, Object> getProperties() {
    if (properties==null) {
      return Collections.emptyMap();
    }
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
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

  public void addListener(String eventName, DelegateListener<? extends BaseDelegateExecution> listener) {
    addListener(eventName, listener, -1);
  }

  public void addListener(String eventName, DelegateListener<? extends BaseDelegateExecution> listener, int index) {
    List<DelegateListener<? extends BaseDelegateExecution>> listeners = this.listeners.get(eventName);
    if (listeners == null) {
      listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();
      this.listeners.put(eventName, listeners);
    }
    if (index < 0) {
      listeners.add(listener);
    } else {
      listeners.add(index, listener);
    }
  }

  public Map<String, List<DelegateListener<? extends BaseDelegateExecution>>> getListeners() {
    return listeners;
  }

}
