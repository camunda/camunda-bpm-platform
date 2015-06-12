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

import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.dmn.engine.DmnDecisionModel;
import org.camunda.dmn.engine.transform.DmnElementHandlerRegistry;
import org.camunda.dmn.engine.transform.DmnTransformContext;
import org.camunda.dmn.engine.transform.DmnTransformListener;
import org.camunda.dmn.engine.transform.DmnTransformer;

public abstract class AbstractDmnTransformer implements DmnTransformer {

  protected DmnTransformContext transformContext;

  public AbstractDmnTransformer(DmnElementHandlerRegistry elementHandlerRegistry) {
    transformContext = new DmnTransformContextImpl();
    transformContext.setElementHandlerRegistry(elementHandlerRegistry);
  }

  public void setTransformContext(DmnTransformContext transformContext) {
    this.transformContext = transformContext;
  }

  public DmnTransformContext getTransformContext() {
    return transformContext;
  }

  public void setElementHandlerRegistry(DmnElementHandlerRegistry elementHandlerRegistry) {
    transformContext.setElementHandlerRegistry(elementHandlerRegistry);
  }

  public DmnElementHandlerRegistry getElementHandlerRegistry() {
    return transformContext.getElementHandlerRegistry();
  }

  public DmnDecisionModel transform(DmnModelInstance modelInstance) {
    Definitions definitions = modelInstance.getDefinitions();
    DmnDecisionModel decisionModel = performTransform(definitions);
    notifyTransformListeners(definitions, decisionModel);
    return decisionModel;
  }

  protected abstract DmnDecisionModel performTransform(Definitions definitions);

  protected void notifyTransformListeners(Definitions definitions, DmnDecisionModel decisionModel) {
    for (DmnTransformListener dmnTransformListener : transformContext.getTransformListeners()) {
      dmnTransformListener.transformDefinitions(definitions, decisionModel);
    }
  }

}
