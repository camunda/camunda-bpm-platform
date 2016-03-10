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
package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.calendar.BusinessCalendar;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.StartProcessVariableScope;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class TimerDeclarationImpl extends TimerJobDeclaration<ExecutionEntity> {

  private static final long serialVersionUID = 1L;

  protected boolean isInterruptingTimer; // For boundary timers
  protected String eventScopeActivityId = null;
  protected Boolean isParallelMultiInstance;

  public TimerDeclarationImpl(Expression expression, TimerDeclarationType type, String jobHandlerType) {
    super(expression, type, jobHandlerType);
  }

  public boolean isInterruptingTimer() {
    return isInterruptingTimer;
  }

  public void setInterruptingTimer(boolean isInterruptingTimer) {
    this.isInterruptingTimer = isInterruptingTimer;
  }

  public void setEventScopeActivityId(String eventScopeActivityId) {
    this.eventScopeActivityId = eventScopeActivityId;
  }

  public String getEventScopeActivityId() {
    return eventScopeActivityId;
  }

  protected void postInitialize(ExecutionEntity context, TimerEntity job) {
    BusinessCalendar businessCalendar = Context
        .getProcessEngineConfiguration()
        .getBusinessCalendarManager()
        .getBusinessCalendar(type.calendarName);

    if (description==null) {
      // Prevent NPE from happening in the next line
      throw new ProcessEngineException("Timer '"+context.getActivityId()+"' was not configured with a valid duration/time");
    }

    String dueDateString = null;
    Date duedate = null;

    // ACT-1415: timer-declaration on start-event may contain expressions NOT
    // evaluating variables but other context, evaluating should happen nevertheless
    VariableScope scopeForExpression = context;
    if(scopeForExpression == null) {
      scopeForExpression = StartProcessVariableScope.getSharedInstance();
    }

    Object dueDateValue = description.getValue(scopeForExpression);
    if (dueDateValue instanceof String) {
      dueDateString = (String)dueDateValue;
    }
    else if (dueDateValue instanceof Date) {
      duedate = (Date)dueDateValue;
    }
    else {
      throw new ProcessEngineException("Timer '"+context.getActivityId()+"' was not configured with a valid duration/time, either hand in a java.util.Date or a String in format 'yyyy-MM-dd'T'hh:mm:ss'");
    }

    if (duedate==null) {
      duedate = businessCalendar.resolveDuedate(dueDateString);
    }

    job.setDuedate(duedate);

    if (type == TimerDeclarationType.CYCLE && jobHandlerType != TimerCatchIntermediateEventJobHandler.TYPE) {

      // See ACT-1427: A boundary timer with a cancelActivity='true', doesn't need to repeat itself
      if (!isInterruptingTimer) {
        String prepared = prepareRepeat(dueDateString);
        job.setRepeat(prepared);
      }
    }
  }

  protected ExecutionEntity resolveExecution(ExecutionEntity context) {
    return context;
  }

  public TimerEntity createStartTimerInstance(String deploymentId) {
    return createTimer(deploymentId);
  }

  public static List<TimerDeclarationImpl> getDeclarationsForScope(PvmScope scope) {
    List<TimerDeclarationImpl> result = scope.getProperties().get(BpmnProperties.TIMER_DECLARATIONS);
    if (result != null) {
      return result;
    }
    else {
      return Collections.emptyList();
    }
  }

}
