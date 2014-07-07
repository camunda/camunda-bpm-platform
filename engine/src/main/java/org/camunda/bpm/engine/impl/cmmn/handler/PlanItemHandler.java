/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.cmmn.handler;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.cmmn.CaseControlRule;
import org.camunda.bpm.engine.impl.cmmn.behavior.CaseControlRuleImpl;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.model.cmmn.instance.ManualActivationRule;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemControl;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;
import org.camunda.bpm.model.cmmn.instance.RepetitionRule;
import org.camunda.bpm.model.cmmn.instance.RequiredRule;

/**
 * @author Roman Smirnov
 *
 */
public abstract class PlanItemHandler extends CmmnElementHandler<PlanItem> {

  public CmmnActivity handleElement(PlanItem planItem, CmmnHandlerContext context) {
    // create a new activity
    CmmnActivity newActivity = createActivity(planItem, context);

    // initialize activity
    initializeActivity(planItem, newActivity, context);

    return newActivity;
  }

  protected void initializeActivity(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    String name = planItem.getName();
    if (name == null) {
      PlanItemDefinition definition = planItem.getDefinition();
      name = definition.getName();
    }
    activity.setName(name);

    // requiredRule
    initializeRequiredRule(planItem, activity, context);

    // manualActivation
    initializeManualActivationRule(planItem, activity, context);

    // repetitionRule
    initializeRepetitionRule(planItem, activity, context);

  }

  protected void initializeRequiredRule(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemDefinition definition = planItem.getDefinition();

    PlanItemControl itemControl = planItem.getItemControl();
    PlanItemControl defaultControl = definition.getDefaultControl();

    ExpressionManager expressionManager = context.getExpressionManager();

    RequiredRule requiredRule = null;
    if (itemControl != null) {
      requiredRule = itemControl.getRequiredRule();
    }
    if (requiredRule == null && defaultControl != null) {
      requiredRule = defaultControl.getRequiredRule();
    }

    if (requiredRule != null) {
      String rule = requiredRule.getCondition().getBody();
      Expression requiredRuleExpression = expressionManager.createExpression(rule);
      CaseControlRule caseRule = new CaseControlRuleImpl(requiredRuleExpression);
      activity.setProperty("requiredRule", caseRule);
    }

  }

  protected void initializeManualActivationRule(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemDefinition definition = planItem.getDefinition();

    PlanItemControl itemControl = planItem.getItemControl();
    PlanItemControl defaultControl = definition.getDefaultControl();

    ExpressionManager expressionManager = context.getExpressionManager();

    ManualActivationRule manualActivationRule = null;
    if (itemControl != null) {
      manualActivationRule = itemControl.getManualActivationRule();
    }
    if (manualActivationRule == null && defaultControl != null) {
      manualActivationRule = defaultControl.getManualActivationRule();
    }

    if (manualActivationRule != null) {
      String rule = manualActivationRule.getCondition().getBody();
      Expression requiredRuleExpression = expressionManager.createExpression(rule);
      CaseControlRule caseRule = new CaseControlRuleImpl(requiredRuleExpression);
      activity.setProperty("manualActivationRule", caseRule);
    }

  }

  protected void initializeRepetitionRule(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemDefinition definition = planItem.getDefinition();

    PlanItemControl itemControl = planItem.getItemControl();
    PlanItemControl defaultControl = definition.getDefaultControl();

    ExpressionManager expressionManager = context.getExpressionManager();

    RepetitionRule repetitionRule = null;
    if (itemControl != null) {
      repetitionRule = itemControl.getRepetitionRule();
    }
    if (repetitionRule == null && defaultControl != null) {
      repetitionRule = defaultControl.getRepetitionRule();
    }

    if (repetitionRule != null) {
      String rule = repetitionRule.getCondition().getBody();
      Expression requiredRuleExpression = expressionManager.createExpression(rule);
      CaseControlRule caseRule = new CaseControlRuleImpl(requiredRuleExpression);
      activity.setProperty("repetitionRule", caseRule);
    }

  }

  protected PlanItemDefinition getDefinition(PlanItem planItem) {
    return planItem.getDefinition();
  }

}
