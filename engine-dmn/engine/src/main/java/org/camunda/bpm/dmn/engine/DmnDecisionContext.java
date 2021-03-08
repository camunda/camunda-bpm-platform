package org.camunda.bpm.dmn.engine;

import org.camunda.bpm.engine.variable.context.VariableContext;

/**
 * Worker responsible for evaluating a decision.
 * Created during {@link DmnEngine#evaluateDecision(DmnDecision, VariableContext)}.
 *
 * Which concrete implementation is created can be configured in
 */
public interface DmnDecisionContext {

  /**
   * Evaluate a decision with the given {@link VariableContext}
   *
   * @param decision the decision to evaluate
   * @param variableContext the available variable context
   * @return the result of the decision evaluation
   */
  DmnDecisionResult evaluateDecision(DmnDecision decision, VariableContext variableContext);

}
