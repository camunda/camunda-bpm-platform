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
import org.camunda.dmn.engine.impl.DmnDecisionImpl;
import org.camunda.dmn.engine.transform.DmnElementHandler;
import org.camunda.dmn.engine.transform.DmnTransformContext;

public class DmnDecisionHandler implements DmnElementHandler<Decision, DmnDecision> {

  public DmnDecision handleElement(DmnTransformContext context, Decision decision) {
    Expression expression = decision.getExpression();
    if (expression instanceof DecisionTable) {

    }
    DmnDecisionImpl dmnDecision = new DmnDecisionImpl(decision.getId());
    return dmnDecision;
  }

}
