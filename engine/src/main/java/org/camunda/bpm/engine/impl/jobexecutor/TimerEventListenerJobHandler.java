package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Roman Smirnov
 * @author Subhro
 */
public class TimerEventListenerJobHandler extends TimerEventJobHandler {

  public static final String TYPE = "timer-event-listener";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void execute(String configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {

  }


}
