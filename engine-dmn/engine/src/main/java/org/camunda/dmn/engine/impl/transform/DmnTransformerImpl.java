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

import java.util.Collection;

import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.dmn.engine.DmnDecision;
import org.camunda.dmn.engine.DmnDecisionModel;
import org.camunda.dmn.engine.impl.DmnDecisionModelImpl;
import org.camunda.dmn.engine.transform.DmnElementHandler;
import org.camunda.dmn.engine.transform.DmnElementHandlerRegistry;

public class DmnTransformerImpl extends AbstractDmnTransformer {

  public DmnTransformerImpl(DmnElementHandlerRegistry elementHandlerRegistry) {
    super(elementHandlerRegistry);
  }

  protected DmnDecisionModel performTransform(Definitions definitions) {
    Collection<Decision> decisions = definitions.getChildElementsByType(Decision.class);

    DmnElementHandler<Decision, DmnDecision> decisionHandler = transformContext.getElementHandler(Decision.class);

    DmnDecisionModelImpl decisionModel = new DmnDecisionModelImpl();

    for (Decision decision : decisions) {
      DmnDecision dmnDecision = decisionHandler.handleElement(transformContext, decision);
      if (dmnDecision != null) {
        decisionModel.addDecision(dmnDecision);
      }
    }

    return decisionModel;
  }

}
