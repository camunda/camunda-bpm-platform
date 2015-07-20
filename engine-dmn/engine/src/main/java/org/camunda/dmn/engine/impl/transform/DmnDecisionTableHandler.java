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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.camunda.bpm.model.dmn.impl.DmnModelConstants;
import org.camunda.bpm.model.dmn.instance.Clause;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.commons.utils.StringUtil;
import org.camunda.dmn.engine.DmnDecision;
import org.camunda.dmn.engine.DmnExpression;
import org.camunda.dmn.engine.impl.DmnDecisionImpl;
import org.camunda.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.dmn.engine.impl.DmnLogger;
import org.camunda.dmn.engine.impl.DmnParseLogger;
import org.camunda.dmn.engine.impl.DmnRuleImpl;
import org.camunda.dmn.engine.transform.DmnElementHandler;
import org.camunda.dmn.engine.transform.DmnTransformContext;
import org.camunda.dmn.juel.JuelScriptEngineFactory;

public class DmnDecisionTableHandler implements DmnElementHandler<DecisionTable, DmnDecision> {

  protected static final DmnParseLogger LOG = DmnLogger.PARSE_LOGGER;

  protected DmnTransformContext context;
  protected DmnDecisionImpl decision;

  public DmnDecision handleElement(DmnTransformContext context, DecisionTable decisionTable) {
    this.context = context;
    decision = new DmnDecisionImpl();

    transformClauses(decisionTable.getClauses());
    transformRules(decisionTable.getRules());
    return decision;
  }

  protected void transformClauses(Collection<Clause> clauses) {
    for (Clause clause : clauses) {
      transformClause(clause);
    }
  }

  protected void transformClause(Clause clause) {
    if (isInputClause(clause)) {
      transformInputClause(clause);
    }
    else if (isOutputClause(clause)) {
      transformOutputClause(clause);
    }
    else {
      LOG.ignoringClause(clause);
    }
  }

  protected boolean isInputClause(Clause clause) {
    return clause.getInputExpression() != null || !clause.getInputEntries().isEmpty();
  }

  protected boolean isOutputClause(Clause clause) {
    return clause.getOutputDefinition() != null || !clause.getOutputEntries().isEmpty();
  }

  protected void transformInputClause(Clause clause) {
    String variableName = getVariableNameForClause(clause);
    if (variableName == null) {
      variableName = DmnExpressionImpl.DEFAULT_INPUT_VARIABLE_NAME;
    }

    DmnElementHandler<LiteralExpression, DmnExpression> elementHandler = context.getElementHandler(LiteralExpression.class);

    DmnExpression inputExpression = elementHandler.handleElement(context, clause.getInputExpression());
    inputExpression.setVariableName(variableName);
    postProcessInputExpression(inputExpression);
    decision.addInputExpression(clause.getId(), inputExpression);

    for (InputEntry inputEntry : clause.getInputEntries()) {
      DmnExpression expression = elementHandler.handleElement(context, inputEntry);
      postProcessInputEntry(expression, variableName);
      decision.addInputEntry(expression);
    }
  }

  protected void postProcessInputExpression(DmnExpression inputExpression) {
    if (hasJuelExpressionLanguage(inputExpression)) {
      String expression = inputExpression.getExpression();
      if (!StringUtil.isExpression(expression)) {
        inputExpression.setExpression("${" + expression + "}");
      }
    }
  }

  protected void postProcessInputEntry(DmnExpression inputEntry, String variableName) {
    if (hasJuelExpressionLanguage(inputEntry)) {
      String expression = inputEntry.getExpression();
      if (!StringUtil.isExpression(expression)) {
        if (startsWithOperator(expression)) {
          inputEntry.setExpression("${" + variableName + expression + "}");
        }
        else if (isNumber(expression)) {
          inputEntry.setExpression("${" + variableName + "==" + expression + "}");
        }
        else {
          inputEntry.setExpression("${" + variableName + "=='" + expression + "'}");
        }
      }
    }
  }

  protected boolean hasJuelExpressionLanguage(DmnExpression expression) {
    String expressionLanguage = expression.getExpressionLanguage();
    // TODO: assumes JUEL is default expression language
    return expressionLanguage ==  null || expressionLanguage.equalsIgnoreCase(JuelScriptEngineFactory.NAME);
  }

  protected boolean isNumber(String text) {
    try {
      Double.parseDouble(text);
      return true;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }

  protected boolean startsWithOperator(String text) {
    char firstChar = text.trim().charAt(0);
    return !Character.isLetterOrDigit(firstChar);
  }

  protected void transformOutputClause(Clause clause) {
    String variableName = getVariableNameForClause(clause);

    DmnElementHandler<LiteralExpression, DmnExpression> elementHandler = context.getElementHandler(LiteralExpression.class);

    for (OutputEntry outputEntry : clause.getOutputEntries()) {
      DmnExpression expression = elementHandler.handleElement(context, outputEntry);
      expression.setVariableName(variableName);
      decision.addOutputEntry(expression);
    }
  }

  private String getVariableNameForClause(Clause clause) {
    return clause.getAttributeValueNs(DmnModelConstants.CAMUNDA_NS, "output");
  }

  protected void transformRules(Collection<Rule> rules) {
    for (Rule rule : rules) {
      transformRule(rule);
    }
  }

  protected void transformRule(Rule rule) {
    DmnRuleImpl dmnRule = new DmnRuleImpl();
    dmnRule.setId(rule.getId());

    Map<String, List<DmnExpression>> conditions = new TreeMap<String, List<DmnExpression>>();
    for (Expression condition : rule.getConditions()) {
      String clauseId = ((Clause) condition.getParentElement()).getId();
      List<DmnExpression> clauseConditions = conditions.get(clauseId);
      if (clauseConditions == null) {
        clauseConditions = new ArrayList<DmnExpression>();
        conditions.put(clauseId, clauseConditions);
      }

      DmnExpression inputEntry = decision.getInputEntry(condition.getId());
      clauseConditions.add(inputEntry);
      DmnExpression inputExpression = decision.getInputExpressions().get(clauseId);
      if (inputExpression != null) {
        dmnRule.addInputExpression(clauseId, inputExpression);
      }
    }
    dmnRule.setConditions(conditions);

    List<DmnExpression> conclusions = new ArrayList<DmnExpression>();
    for (Expression conclusion : rule.getConclusions()) {
      DmnExpression outputEntry = decision.getOutputEntry(conclusion.getId());
      conclusions.add(outputEntry);
    }
    dmnRule.setConclusions(conclusions);

    decision.addRule(dmnRule);
  }

}
