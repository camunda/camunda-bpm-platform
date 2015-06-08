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

package org.camunda.dmn.engine.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.dmn.engine.DmnOutput;
import org.camunda.dmn.engine.DmnOutputComponent;

public class DmnOutputImpl implements DmnOutput {

  protected static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  protected Map<String, DmnOutputComponent> components = new LinkedHashMap<String, DmnOutputComponent>();

  public DmnOutputImpl() {

  }

  public void setComponents(List<DmnOutputComponent> components) {
    this.components = new LinkedHashMap<String, DmnOutputComponent>();
    for (DmnOutputComponent component : components) {
      addComponent(component);
    }
  }

  public void addComponent(DmnOutputComponent component) {
    components.put(component.getName(), component);
  }

  public List<DmnOutputComponent> getComponents() {
    return new ArrayList<DmnOutputComponent>(components.values());
  }

  public <T> T getValue() {
    Set<String> keys = components.keySet();
    if (!keys.isEmpty()) {
      String key = keys.iterator().next();
      return getValue(key);
    }
    else {
      throw LOG.outputDoesNotContainAnyComponent();
    }
  }

  public <T> T getValue(String componentName) {
    DmnOutputComponent component = components.get(componentName);
    if (component != null) {
      return component.getValue();
    }
    else {
      throw LOG.unableToFindOutputComponentWithName(componentName);
    }
  }

}
