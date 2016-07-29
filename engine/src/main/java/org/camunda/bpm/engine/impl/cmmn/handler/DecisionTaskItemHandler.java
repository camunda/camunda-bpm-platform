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

import static org.camunda.bpm.engine.impl.util.DecisionEvaluationUtil.getDecisionResultMapperForName;

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.DmnDecisionTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.dmn.result.DecisionResultMapper;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.DecisionRefExpression;
import org.camunda.bpm.model.cmmn.instance.DecisionTask;


/**
 * @author Roman Smirnov
 *
 */
public class DecisionTaskItemHandler extends CallingTaskItemHandler {

  protected void initializeActivity(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    super.initializeActivity(element, activity, context);

    initializeResultVariable(element, activity, context);

    initializeDecisionTableResultMapper(element, activity, context);
  }

  protected void initializeResultVariable(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    DecisionTask decisionTask = getDefinition(element);
    DmnDecisionTaskActivityBehavior behavior = getActivityBehavior(activity);
    String resultVariable = decisionTask.getCamundaResultVariable();
    behavior.setResultVariable(resultVariable);
  }

  protected void initializeDecisionTableResultMapper(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    DecisionTask decisionTask = getDefinition(element);
    DmnDecisionTaskActivityBehavior behavior = getActivityBehavior(activity);
    String mapper = decisionTask.getCamundaMapDecisionResult();
    DecisionResultMapper decisionResultMapper = getDecisionResultMapperForName(mapper);
    behavior.setDecisionTableResultMapper(decisionResultMapper);
  }

  protected BaseCallableElement createCallableElement() {
    return new BaseCallableElement();
  }

  protected CmmnActivityBehavior getActivityBehavior() {
    return new DmnDecisionTaskActivityBehavior();
  }

  protected DmnDecisionTaskActivityBehavior getActivityBehavior(CmmnActivity activity) {
    return (DmnDecisionTaskActivityBehavior) activity.getActivityBehavior();
  }

  protected String getDefinitionKey(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    DecisionTask definition = getDefinition(element);
    String decision = definition.getDecision();

    if (decision == null) {
      DecisionRefExpression decisionExpression = definition.getDecisionExpression();
      if (decisionExpression != null) {
        decision = decisionExpression.getText();
      }
    }

    return decision;
  }

  protected String getBinding(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    DecisionTask definition = getDefinition(element);
    return definition.getCamundaDecisionBinding();
  }

  protected String getVersion(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    DecisionTask definition = getDefinition(element);
    return definition.getCamundaDecisionVersion();
  }

  protected String getTenantId(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    DecisionTask definition = getDefinition(element);
    return definition.getCamundaDecisionTenantId();
  }


  protected DecisionTask getDefinition(CmmnElement element) {
    return (DecisionTask) super.getDefinition(element);
  }

}
