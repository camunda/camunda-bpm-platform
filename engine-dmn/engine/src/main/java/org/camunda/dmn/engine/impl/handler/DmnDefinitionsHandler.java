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

package org.camunda.dmn.engine.impl.handler;

import org.camunda.bpm.model.dmn.impl.DmnModelConstants;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.dmn.engine.handler.DmnElementHandlerContext;
import org.camunda.dmn.engine.impl.DmnDecisionModelImpl;

public class DmnDefinitionsHandler extends AbstractDmnElementHandler<Definitions, DmnDecisionModelImpl> {

  protected DmnDecisionModelImpl createElement(DmnElementHandlerContext context, Definitions definitions) {
    return new DmnDecisionModelImpl();
  }

  protected void initElement(DmnElementHandlerContext context, Definitions definitions, DmnDecisionModelImpl dmnDecisionModel) {
    super.initElement(context, definitions, dmnDecisionModel);
    initExpressionLanguage(context, definitions, dmnDecisionModel);
    initTypeLanguage(context, definitions, dmnDecisionModel);
    initNamespace(context, definitions, dmnDecisionModel);
  }

  protected void initNamespace(DmnElementHandlerContext context, Definitions definitions, DmnDecisionModelImpl dmnDecisionModel) {
    dmnDecisionModel.setNamespace(definitions.getNamespace());
  }

  protected void initTypeLanguage(DmnElementHandlerContext context, Definitions definitions, DmnDecisionModelImpl dmnDecisionModel) {
    dmnDecisionModel.setTypeLanguage(definitions.getTypeLanguage());
  }

  protected void initExpressionLanguage(DmnElementHandlerContext context, Definitions definitions, DmnDecisionModelImpl dmnDecisionModel) {
    String expressionLanguage = definitions.getExpressionLanguage();
    // override default feel expression language with JUEL
    if (!DmnModelConstants.FEEL_NS.equals(expressionLanguage)) {
      dmnDecisionModel.setExpressionLanguage(expressionLanguage);
    }
  }

}
