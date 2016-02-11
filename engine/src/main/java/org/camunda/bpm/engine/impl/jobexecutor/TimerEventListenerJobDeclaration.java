package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.delegate.Expression;

/**
 * @author Roman Smirnov
 * @author Subhro
 */
public class TimerEventListenerJobDeclaration extends TimerDeclarationImpl {
  public TimerEventListenerJobDeclaration(Expression expression, TimerDeclarationType type, String jobHandlerType) {
    super(expression, type, jobHandlerType);
  }
}
