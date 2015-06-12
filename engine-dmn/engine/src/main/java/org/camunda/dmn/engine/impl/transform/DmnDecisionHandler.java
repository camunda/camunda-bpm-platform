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

import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.dmn.engine.DmnDecision;
import org.camunda.dmn.engine.impl.DmnLogger;
import org.camunda.dmn.engine.impl.DmnParseLogger;
import org.camunda.dmn.engine.transform.DmnElementHandler;
import org.camunda.dmn.engine.transform.DmnTransformContext;

public class DmnDecisionHandler implements DmnElementHandler<Decision, DmnDecision> {

  protected static final DmnParseLogger LOG = DmnLogger.PARSE_LOGGER;

  public DmnDecision handleElement(DmnTransformContext context, Decision decision) {
    Expression expression = decision.getExpression();
    if (expression instanceof DecisionTable) {
      DmnDecision dmnDecision = handleDecisionTable(context, (DecisionTable) expression);
      dmnDecision.setId(decision.getId());
      return dmnDecision;
    }
    else {
      LOG.decisionTypeNotSupported(decision);
      return null;
    }
  }

  protected DmnDecision handleDecisionTable(DmnTransformContext context, DecisionTable decisionTable) {
    DmnElementHandler<DecisionTable, DmnDecision> elementHandler = context.getElementHandler(DecisionTable.class);
    return elementHandler.handleElement(context, decisionTable);
  }

}
