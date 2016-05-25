package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Roman Smirnov
 * @author Subhro
 */
public class TimerEventListenerJobHandler extends TimerEventJobHandler {

  public static final String TYPE = "timer-event-listener";

  public String getType() {
    return TYPE;
  }

  public void execute(TimerJobConfiguration configuration, CoreExecution context, CommandContext commandContext, String tenantId) {
    CmmnExecution execution = (CmmnExecution) context;
    execution.occur();

  }

}
