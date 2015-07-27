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

package org.camunda.bpm.engine.impl.dmn.handler;

import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerContext;
import org.camunda.bpm.dmn.engine.impl.handler.AbstractDmnDecisionTableHandler;

public class DecisionDefinitionHandler extends AbstractDmnDecisionTableHandler<DecisionDefinitionEntity> {

  protected DecisionDefinitionEntity createElement(DmnElementHandlerContext context, DecisionTable decisionTable) {
    return new DecisionDefinitionEntity();
  }

  protected void initElement(DmnElementHandlerContext context, DecisionTable decisionTable, DecisionDefinitionEntity decisionDefinition) {
    super.initElement(context, decisionTable, decisionDefinition);
    initCategory(context, decisionTable, decisionDefinition);
  }

  protected void initCategory(DmnElementHandlerContext context, DecisionTable decisionTable, DecisionDefinitionEntity decisionDefinition) {
    DmnDecisionModel decisionModel = context.getDecisionModel();
    decisionDefinition.setCategory(decisionModel.getNamespace());
  }

}
