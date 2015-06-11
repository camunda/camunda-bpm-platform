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

package org.camunda.dmn.engine.transform;

import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.dmn.engine.DmnDecisionModel;

public interface DmnTransformer {

  void setTransformContext(DmnTransformContext transformContext);

  DmnTransformContext getTransformContext();

  void setElementHandlerRegistry(DmnElementHandlerRegistry elementHandlerRegistry);

  DmnElementHandlerRegistry getElementHandlerRegistry();

  DmnDecisionModel transform(DmnModelInstance modelInstance);

}
