package org.camunda.bpm.engine.impl.jobexecutor;

import java.util.Date;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.calendar.BusinessCalendar;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;

/**
 * @author Roman Smirnov
 * @author Subhro
 */
public class TimerEventListenerJobDeclaration extends TimerJobDeclaration<CaseExecutionEntity> {

  private static final long serialVersionUID = 1L;

  protected CmmnActivity cmmnActivity;

  public void setActivity(CmmnActivity activity) {
    this.cmmnActivity = activity;
  }

  public String getActivityId() {
    if (cmmnActivity != null) {
      return cmmnActivity.getId();
    }
    else {
      return null;
    }
  }

  public TimerEventListenerJobDeclaration(Expression expression, TimerDeclarationType type, String jobHandlerType) {
    super(expression, type, jobHandlerType);
  }

  protected boolean isJobPrioritySupported() {
    return false;
  }

  protected void postInitialize(CaseExecutionEntity context, TimerEntity job) {
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

    VariableScope scopeForExpression = context;
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

    if (type == TimerDeclarationType.CYCLE) {
      String prepared = prepareRepeat(dueDateString);
      job.setRepeat(prepared);
    }
  }

  protected ExecutionEntity resolveExecution(CaseExecutionEntity context) {
    return null;
  }

}
