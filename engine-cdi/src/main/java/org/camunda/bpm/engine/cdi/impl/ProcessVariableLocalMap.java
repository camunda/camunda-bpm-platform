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
package org.camunda.bpm.engine.cdi.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.camunda.bpm.engine.cdi.BusinessProcess;

/**
 * Allows to expose the local process variables of the current business process as a
 * java.util.Map<String,Object>
 * <p/>
 * The map delegates changes to
 * {@link BusinessProcess#setVariableLocal(String, Object)} and
 * {@link BusinessProcess#getVariableLocal(String)}, so that they are not flushed
 * prematurely.
 * 
 * @author Michael Scholz
 */
public class ProcessVariableLocalMap implements Map<String, Object> {
  
  @Inject private BusinessProcess businessProcess;
  
  @Override
  public Object get(Object key) {
    if(key == null) {
      throw new IllegalArgumentException("This map does not support 'null' keys.");
    }
    return businessProcess.getVariableLocal(key.toString());
  }

  @Override
  public Object put(String key, Object value) {
    if(key == null) {
      throw new IllegalArgumentException("This map does not support 'null' keys.");
    }
    Object variableBefore = businessProcess.getVariableLocal(key);
    businessProcess.setVariableLocal(key, value);
    return variableBefore;
  }
  
  @Override
  public void putAll(Map< ? extends String, ? extends Object> m) {
    for (java.util.Map.Entry< ? extends String, ? extends Object> newEntry : m.entrySet()) {
      businessProcess.setVariableLocal(newEntry.getKey(), newEntry.getValue());      
    }
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException(ProcessVariableLocalMap.class.getName()+".size() is not supported.");
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException(ProcessVariableLocalMap.class.getName()+".isEmpty() is not supported.");
  }

  @Override
  public boolean containsKey(Object key) {
    throw new UnsupportedOperationException(ProcessVariableLocalMap.class.getName()+".containsKey() is not supported.");
  }

  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException(ProcessVariableLocalMap.class.getName()+".containsValue() is not supported.");
  }

  @Override
  public Object remove(Object key) {
    throw new UnsupportedOperationException(ProcessVariableLocalMap.class.getName() + ".remove() is unsupported. Use ProcessVariableMap.put(key, null)");    
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException(ProcessVariableLocalMap.class.getName()+".clear() is not supported.");
  }

  @Override
  public Set<String> keySet() {
    throw new UnsupportedOperationException(ProcessVariableLocalMap.class.getName()+".keySet() is not supported.");
  }

  @Override
  public Collection<Object> values() {
    throw new UnsupportedOperationException(ProcessVariableLocalMap.class.getName()+".values() is not supported.");
  }

  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    throw new UnsupportedOperationException(ProcessVariableLocalMap.class.getName()+".entrySet() is not supported.");
  }

}
