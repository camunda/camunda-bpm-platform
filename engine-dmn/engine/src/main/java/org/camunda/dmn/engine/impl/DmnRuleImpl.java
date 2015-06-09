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

package org.camunda.dmn.engine.impl;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.model.dmn.instance.Clause;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.dmn.engine.DmnDecisionContext;
import org.camunda.dmn.engine.DmnExpression;
import org.camunda.dmn.engine.DmnOutput;
import org.camunda.dmn.engine.DmnOutputComponent;
import org.camunda.dmn.engine.DmnRule;

public class DmnRuleImpl implements DmnRule {

  protected List<DmnExpression> conditions = new ArrayList<DmnExpression>();
  protected List<DmnExpression> conclusions = new ArrayList<DmnExpression>();

  public DmnRuleImpl(Rule rule) {
    parseConditions(rule);
    parseConclusions(rule);
  }

  protected void parseConditions(Rule rule) {
    for (Expression condition : rule.getConditions()) {
      conditions.add(new DmnInputExpressionImpl(condition));
    }
  }

  protected void parseConclusions(Rule rule) {
    for (Expression conclusion : rule.getConclusions()) {
      conclusions.add(new DmnExpressionImpl(conclusion));
    }
  }

  public boolean isApplicable(DmnDecisionContext context) {
    for (DmnExpression condition : conditions) {
      if (!condition.isSatisfied(context)) {
        return false;
      }
    }

    // if all conditions are satisfied the rule is applicable
    return true;
  }

  public DmnOutput getOutput(DmnDecisionContext context) {
    DmnOutputImpl output = new DmnOutputImpl();
    for (DmnExpression conclusion : conclusions) {
      DmnOutputComponent component = outputComponentForConclusion(conclusion, context);
      output.addComponent(component);
    }
    return output;
  }

  protected DmnOutputComponent outputComponentForConclusion(DmnExpression conclusion, DmnDecisionContext context) {
    String name = conclusion.getVariableName();
    String value = conclusion.evaluate(context);
    return new DmnOutputComponentImpl(name, value);
  }

}
