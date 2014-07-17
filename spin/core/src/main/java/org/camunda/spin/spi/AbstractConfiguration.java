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
package org.camunda.spin.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thorben Lindhauer
 */
public abstract class AbstractConfiguration<R extends Configurable<R>> implements Configurable<R> {

  protected Map<String, Object> configuration;
  
  public AbstractConfiguration() {
    this.configuration = Collections.synchronizedMap(new HashMap<String, Object>());
  }
  
  public AbstractConfiguration(AbstractConfiguration<R> other) {
    this.configuration = Collections.synchronizedMap(
        new HashMap<String, Object>(other.configuration));
  }
  
  public R config(String key, Object value) {
    configuration.put(key, value);
    return thisConfiguration();
  }

  public R config(Map<String, Object> config) {
    if (config != null) {
      configuration.putAll(config);
    }
    
    return thisConfiguration();
  }

  public Object getValue(String key) {
    return configuration.get(key);
  }

  public Object getValue(String key, Object defaultValue) {
    Object value = configuration.get(key);
    
    if (value == null) {
      return defaultValue;
    }
    
    return value;
  }

  public Map<String, Object> getConfiguration() {
    return configuration;
  }
  
  protected abstract R thisConfiguration();

}
