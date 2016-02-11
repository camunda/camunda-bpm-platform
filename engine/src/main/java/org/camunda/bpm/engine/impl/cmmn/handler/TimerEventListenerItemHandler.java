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
import org.camunda.bpm.engine.impl.cmmn.behavior.EventListenerActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.TimerEventListenerActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.jobexecutor.*;
import org.camunda.bpm.model.cmmn.instance.*;

import java.util.List;
import java.util.logging.Logger;

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
    JobDeclaration timerEventListenerJobDeclaration=parseTimerExpression(element,context);
    activity.setProperty(ItemHandler.PROPERTY_TIMERVEVENTLISTENER_JOBDECLARATION,timerEventListenerJobDeclaration);
  }

  private JobDeclaration parseTimerExpression(CmmnElement element, CmmnHandlerContext context) {
    TimerEventListener elemDef = (TimerEventListener) super.getDefinition(element);

    TimerExpression exp = elemDef.getTimerExpression();
    if(exp!=null) {
      String expText = exp.getText();
      StartTrigger start = elemDef.getTimerStart();

      Expression expression = context.getExpressionManager().createExpression(expText);
      //TODO get the type from the camunda extensions in XML?
      TimerDeclarationType type = determineTimeDeclrType(expText);
      TimerEventListenerJobDeclaration timerDeclaration = null;
      if (type != null) {
        //TODO get the job type handler extending TimerEventJobHandler?
        String jobHandlerType = TimerEventListenerJobHandler.TYPE;
        timerDeclaration = new TimerEventListenerJobDeclaration(expression, type, jobHandlerType);
      }// What to do if type not found?
      return timerDeclaration;
    }

    return null;
  }

  private TimerDeclarationType determineTimeDeclrType(String expText) {
    if(expText!=null && expText.startsWith("R")){
      //TODO more validations, for format and values?
      return TimerDeclarationType.CYCLE;
    }else if(expText!=null && expText.startsWith("P")){
      //TODO more validations.
      return TimerDeclarationType.DURATION;
    }else if(expText!=null){
      //TODO ISO date format validation?
      return TimerDeclarationType.DATE;
    }
    return null;
  }
}
