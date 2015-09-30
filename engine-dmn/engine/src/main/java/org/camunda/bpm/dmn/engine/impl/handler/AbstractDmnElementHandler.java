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

package org.camunda.bpm.dmn.engine.impl.handler;

import org.camunda.bpm.dmn.engine.handler.DmnElementHandler;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerContext;
import org.camunda.bpm.dmn.engine.impl.DmnElementImpl;
import org.camunda.bpm.model.dmn.instance.DmnElement;

public abstract class AbstractDmnElementHandler<E extends DmnElement, I extends DmnElementImpl> implements DmnElementHandler<E, I> {

  public I handleElement(DmnElementHandlerContext context, E element) {
    I dmnElement = createElement(context, element);
    initElement(context, element, dmnElement);
    return dmnElement;
  }

  protected abstract I createElement(DmnElementHandlerContext context, E element);

  protected void initElement(DmnElementHandlerContext context, E element, I dmnElement) {
    initKey(context, element, dmnElement);
    initName(context, element, dmnElement);
  }

  protected void initKey(DmnElementHandlerContext context, DmnElement element, I dmnElement) {
    dmnElement.setKey(element.getId());
  }

  protected void initName(DmnElementHandlerContext context, DmnElement element, I dmnElement) {
    dmnElement.setName(element.getName());
  }

}
