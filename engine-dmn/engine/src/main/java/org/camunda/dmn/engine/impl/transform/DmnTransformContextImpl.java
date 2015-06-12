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

package org.camunda.dmn.engine.impl.transform;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.model.dmn.instance.DmnModelElementInstance;
import org.camunda.dmn.engine.transform.DmnElementHandler;
import org.camunda.dmn.engine.transform.DmnElementHandlerRegistry;
import org.camunda.dmn.engine.transform.DmnTransformContext;
import org.camunda.dmn.engine.transform.DmnTransformListener;

public class DmnTransformContextImpl implements DmnTransformContext {

  protected List<DmnTransformListener> transformListeners = new ArrayList<DmnTransformListener>();
  protected DmnElementHandlerRegistry elementHandlerRegistry;

  public void setElementHandlerRegistry(DmnElementHandlerRegistry elementHandlerRegistry) {
    this.elementHandlerRegistry = elementHandlerRegistry;
  }

  public DmnElementHandlerRegistry getElementHandlerRegistry() {
    return elementHandlerRegistry;
  }

  public <Source extends DmnModelElementInstance, Target> DmnElementHandler<Source, Target> getElementHandler(Class<Source> elementClass) {
    return elementHandlerRegistry.getElementHandler(elementClass);
  }

  public void setTransformListeners(List<DmnTransformListener> transformListeners) {
    this.transformListeners = transformListeners;
  }

  public List<DmnTransformListener> getTransformListeners() {
    return transformListeners;
  }

  public void addTransformListener(DmnTransformListener transformListener) {
    transformListeners.add(transformListener);
  }

  public void removeTransformListener(DmnTransformListener transformListener) {
    transformListeners.remove(transformListener);
  }

}
