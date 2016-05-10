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

import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.TimerEventListenerActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationType;
import org.camunda.bpm.engine.impl.jobexecutor.TimerEventListenerJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.TimerEventListenerJobHandler;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.TimerEventListener;
import org.camunda.bpm.model.cmmn.instance.TimerExpression;

/**
 *  @author Roman Smirnov
 *  @author Subhro
 */
public class TimerEventListenerItemHandler extends EventListenerItemHandler {

  @Override
  protected CmmnActivityBehavior getActivityBehavior() {
    return new TimerEventListenerActivityBehavior();
  }

  @Override
  protected void initializeActivity(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    super.initializeActivity(element, activity, context);
    initializeTimerEventListenerJobDeclaration(element, activity, context);
  }

  protected void initializeTimerEventListenerJobDeclaration(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {

    ExpressionManager expressionManager = context.getExpressionManager();

    TimerExpression timerExpression = getTimerExpression(element);
    Expression expression = initializeTimerExpression(timerExpression, expressionManager);

    TimerDeclarationType timerType = initializeTimerDeclrationType(timerExpression);

    String jobHandlerType = TimerEventListenerJobHandler.TYPE;

    TimerEventListenerJobDeclaration declaration = new TimerEventListenerJobDeclaration(expression, timerType, jobHandlerType);

    declaration.setActivity(activity);
    activity.setProperty(ItemHandler.PROPERTY_TIMERVEVENTLISTENER_JOBDECLARATION, declaration);
    declaration.setRawJobHandlerConfiguration(activity.getId());

    context.addJobDeclaration(declaration);
  }

  protected TimerDeclarationType initializeTimerDeclrationType(TimerExpression timerExpression) {
    if (timerExpression != null) {

      String expression = timerExpression.getText();
      if (expression != null) {

        if (expression.startsWith("R")){
          return TimerDeclarationType.CYCLE;
        }
        else if (expression.startsWith("P")){
          return TimerDeclarationType.DURATION;
        }
        else {
          return TimerDeclarationType.DATE;
        }

      }

    }

    return null;
  }

  protected Expression initializeTimerExpression(TimerExpression timerExpression, ExpressionManager expressionManager) {
    if (timerExpression != null) {
      return expressionManager.createExpression(timerExpression.getText());
    }
    return null;
  }

  protected TimerEventListener getDefinition(CmmnElement element) {
    return (TimerEventListener) super.getDefinition(element);
  }

  protected TimerExpression getTimerExpression(CmmnElement element) {
    TimerEventListener definition = getDefinition(element);

    if (definition != null) {
      return definition.getTimerExpression();
    }

    return null;
  }

}
