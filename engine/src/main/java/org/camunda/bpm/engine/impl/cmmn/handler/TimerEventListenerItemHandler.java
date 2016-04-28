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
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationType;
import org.camunda.bpm.engine.impl.jobexecutor.TimerEventListenerJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.TimerEventListenerJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerJobDeclaration;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.TimerEventListener;
import org.camunda.bpm.model.cmmn.instance.TimerExpression;

import java.util.HashMap;

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

  private void initializeTimerEventListenerJobDeclaration(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    TimerEventListenerJobDeclaration timerEventListenerJobDeclaration = parseTimerExpression(element,context);
    timerEventListenerJobDeclaration.setActivity(activity);
    activity.setProperty(ItemHandler.PROPERTY_TIMERVEVENTLISTENER_JOBDECLARATION, timerEventListenerJobDeclaration);
    context.addJobDeclaration(timerEventListenerJobDeclaration);
  }

  private TimerEventListenerJobDeclaration parseTimerExpression(CmmnElement element, CmmnHandlerContext context) {
    TimerEventListener elemDef = (TimerEventListener) super.getDefinition(element);

    TimerExpression exp = elemDef.getTimerExpression();
    if (exp != null) {
      String expText = exp.getText();

      Expression expression = context.getExpressionManager().createExpression(expText);
      TimerDeclarationType type = determineTimeDeclrationType(expText);
      TimerEventListenerJobDeclaration timerDeclaration = null;
      if (type != null) {
        String jobHandlerType = TimerEventListenerJobHandler.TYPE;
        timerDeclaration = new TimerEventListenerJobDeclaration(expression, type, jobHandlerType);
      }
      return timerDeclaration;
    }

    return null;
  }

  private TimerDeclarationType determineTimeDeclrationType(String expText) {
    if (expText != null && expText.startsWith("R")){
      return TimerDeclarationType.CYCLE;
    }
    else if (expText != null && expText.startsWith("P")){
      return TimerDeclarationType.DURATION;
    }
    else if (expText != null){
      return TimerDeclarationType.DATE;
    }
    return null;
  }
}
