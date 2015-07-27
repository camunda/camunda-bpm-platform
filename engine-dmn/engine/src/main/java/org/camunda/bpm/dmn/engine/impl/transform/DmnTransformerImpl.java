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

package org.camunda.bpm.dmn.engine.impl.transform;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerRegistry;
import org.camunda.bpm.dmn.engine.transform.DmnTransform;
import org.camunda.bpm.dmn.engine.transform.DmnTransformFactory;
import org.camunda.bpm.dmn.engine.transform.DmnTransformListener;
import org.camunda.bpm.dmn.engine.transform.DmnTransformer;

public class DmnTransformerImpl implements DmnTransformer {

  protected DmnTransformFactory factory;
  protected DmnElementHandlerRegistry elementHandlerRegistry;
  protected List<DmnTransformListener> transformListeners = new ArrayList<DmnTransformListener>();

  public DmnTransformerImpl(DmnTransformFactory factory, DmnElementHandlerRegistry elementHandlerRegistry) {
    this.factory = factory;
    this.elementHandlerRegistry = elementHandlerRegistry;
  }

  public DmnTransformerImpl(DmnTransformFactory factory, DmnElementHandlerRegistry elementHandlerRegistry, List<DmnTransformListener> transformListeners) {
    this.factory = factory;
    this.elementHandlerRegistry = elementHandlerRegistry;
    this.transformListeners = transformListeners;
  }

  public DmnTransformFactory getFactory() {
    return factory;
  }

  public void setFactory(DmnTransformFactory factory) {
    this.factory = factory;
  }

  public DmnElementHandlerRegistry getElementHandlerRegistry() {
    return elementHandlerRegistry;
  }

  public void setElementHandlerRegistry(DmnElementHandlerRegistry elementHandlerRegistry) {
    this.elementHandlerRegistry = elementHandlerRegistry;
  }

  public List<DmnTransformListener> getTransformListeners() {
    return transformListeners;
  }

  public void setTransformListeners(List<DmnTransformListener> transformListeners) {
    this.transformListeners = transformListeners;
  }

  public DmnTransform createTransform() {
    return factory.createTransform(this);
  }

}
