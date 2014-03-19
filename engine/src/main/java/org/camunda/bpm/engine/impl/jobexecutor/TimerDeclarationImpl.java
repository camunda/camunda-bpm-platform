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

import java.text.SimpleDateFormat;
import java.util.Date;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.camunda.bpm.engine.impl.calendar.BusinessCalendar;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.StartProcessVariableScope;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class TimerDeclarationImpl extends JobDeclaration<TimerEntity> {

  private static final long serialVersionUID = 1L;

  protected Expression description;
  protected TimerDeclarationType type;

  protected String repeat;
  protected boolean isInterruptingTimer; // For boundary timers
  protected String eventScopeActivityId = null;
  protected Boolean isParallelMultiInstance;

  public TimerDeclarationImpl(Expression expression, TimerDeclarationType type, String jobHandlerType) {
    super(jobHandlerType);
    this.description = expression;
    this.type= type;
  }

  public boolean isInterruptingTimer() {
    return isInterruptingTimer;
  }

  public void setInterruptingTimer(boolean isInterruptingTimer) {
    this.isInterruptingTimer = isInterruptingTimer;
  }

  public String getRepeat() {
    return repeat;
  }

  public void setEventScopeActivityId(String eventScopeActivityId) {
    this.eventScopeActivityId = eventScopeActivityId;
  }

  public String getEventScopeActivityId() {
    return eventScopeActivityId;
  }

  protected TimerEntity newJobInstance(ExecutionEntity execution) {

    BusinessCalendar businessCalendar = Context
        .getProcessEngineConfiguration()
        .getBusinessCalendarManager()
        .getBusinessCalendar(type.calendarName);

    if (description==null) {
      // Prefent NPE from happening in the next line
      throw new ProcessEngineException("Timer '"+execution.getActivityId()+"' was not configured with a valid duration/time");
    }

    String dueDateString = null;
    Date duedate = null;

    // ACT-1415: timer-declaration on start-event may contain expressions NOT
    // evaluating variables but other context, evaluating should happen nevertheless
    VariableScope scopeForExpression = execution;
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
      throw new ProcessEngineException("Timer '"+execution.getActivityId()+"' was not configured with a valid duration/time, either hand in a java.util.Date or a String in format 'yyyy-MM-dd'T'hh:mm:ss'");
    }

    if (duedate==null) {
      duedate = businessCalendar.resolveDuedate(dueDateString);
    }

    TimerEntity timer = new TimerEntity(this);
    timer.setDuedate(duedate);
    if (execution != null) {
      timer.setExecution(execution);
    }

    if (type == TimerDeclarationType.CYCLE) {

      // See ACT-1427: A boundary timer with a cancelActivity='true', doesn't need to repeat itself
      if (!isInterruptingTimer) {
        String prepared = prepareRepeat(dueDateString);
        timer.setRepeat(prepared);
      }
    }

    return timer;
  }

  protected String prepareRepeat(String dueDate) {
    if (dueDate.startsWith("R") && dueDate.split("/").length==2) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      return dueDate.replace("/","/"+sdf.format(ClockUtil.getCurrentTime())+"/");
    }
    return dueDate;
  }

  private boolean isParallelMultiInstance(ExecutionEntity execution) {
    if (isParallelMultiInstance == null) { // cache result
      if (eventScopeActivityId == null) {
        isParallelMultiInstance = false;
      } else {
        ActivityImpl activity = execution.getProcessDefinition().findActivity(eventScopeActivityId);
        isParallelMultiInstance = activity.getActivityBehavior() instanceof ParallelMultiInstanceBehavior;
      }
    }
    return isParallelMultiInstance;
  }

  public TimerEntity createTimerInstance(ExecutionEntity execution) {
    if (isParallelMultiInstance(execution)) {
      return null;
    } else {
      return createTimer(execution);
    }
  }

  public TimerEntity createTimerInstanceForParallelMultiInstance(ExecutionEntity execution) {
    if (isParallelMultiInstance(execution)) {
      return createTimer(execution);
    } else {
      return null;
    }
  }

  public TimerEntity createTimer(ExecutionEntity execution) {
    TimerEntity timer = super.createJobInstance(execution);
    Context
    .getCommandContext()
    .getJobManager()
    .schedule(timer);
    return timer;
  }

}
