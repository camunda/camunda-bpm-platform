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
package org.camunda.bpm.engine.impl.core.handler;

import org.camunda.bpm.engine.impl.core.model.CoreActivity;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * <p>A {@link ModelElementHandler} handles an instance of a {@link ModelElementInstance modelElement}
 * to create a new {@link CoreActivity activity.}</p>
 *
 * @author Roman Smirnov
 *
 */
public interface ModelElementHandler<T extends ModelElementInstance, V extends HandlerContext, E> {

  /**
   * <p>This method handles a element to create a new element.</p>
   *
   * @param element the {@link ModelElementInstance} to be handled.
   * @param context the {@link HandlerContext} which holds necessary information.
   *
   * @return a new element.
   */
  E handleElement(T element, V context);

}
