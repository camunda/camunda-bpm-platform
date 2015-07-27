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

import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerContext;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;

public abstract class AbstractDmnDecisionTableHandler<E extends DmnDecisionTableImpl> extends AbstractDmnElementHandler<DecisionTable, E> {

  protected void initElement(DmnElementHandlerContext context, DecisionTable decisionTable, E dmnElement) {
    // id and name is used from parent decision
    Decision decision = (Decision) decisionTable.getParentElement();
    initKey(context, decision, dmnElement);
    initName(context, decision, dmnElement);
    initHitPolicy(context, decisionTable, dmnElement);
  }

  protected void initHitPolicy(DmnElementHandlerContext context, DecisionTable decisionTable, E dmnElement) {
    dmnElement.setHitPolicy(decisionTable.getHitPolicy());
  }

}
