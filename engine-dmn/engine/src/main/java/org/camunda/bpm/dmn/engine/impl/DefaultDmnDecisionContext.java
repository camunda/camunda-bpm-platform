/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.dmn.engine.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionLogic;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionEvaluationListener;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionLogicEvaluationEvent;
import org.camunda.bpm.dmn.engine.impl.delegate.DmnDecisionEvaluationEventImpl;
import org.camunda.bpm.dmn.engine.impl.evaluation.DecisionLiteralExpressionEvaluationHandler;
import org.camunda.bpm.dmn.engine.impl.evaluation.DecisionTableEvaluationHandler;
import org.camunda.bpm.dmn.engine.impl.evaluation.DmnDecisionLogicEvaluationHandler;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.HitPolicyEntry;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.model.dmn.HitPolicy;

/**
 * Context which evaluates a decision on a given input
 */
public class DefaultDmnDecisionContext {

  protected static final DmnEngineLogger LOG = DmnEngineLogger.ENGINE_LOGGER;

  protected static final HitPolicyEntry COLLECT_HIT_POLICY = new HitPolicyEntry(HitPolicy.COLLECT, null);
  protected static final HitPolicyEntry RULE_ORDER_HIT_POLICY = new HitPolicyEntry(HitPolicy.RULE_ORDER, null);

  protected final List<DmnDecisionEvaluationListener> evaluationListeners;

  protected final Map<Class<? extends DmnDecisionLogic>, DmnDecisionLogicEvaluationHandler> evaluationHandlers;

  public DefaultDmnDecisionContext(DefaultDmnEngineConfiguration configuration) {
    evaluationListeners = configuration.getDecisionEvaluationListeners();

    evaluationHandlers = new HashMap<Class<? extends DmnDecisionLogic>, DmnDecisionLogicEvaluationHandler>();
    evaluationHandlers.put(DmnDecisionTableImpl.class, new DecisionTableEvaluationHandler(configuration));
    evaluationHandlers.put(DmnDecisionLiteralExpressionImpl.class, new DecisionLiteralExpressionEvaluationHandler(configuration));
  }

  /**
   * Evaluate a decision with the given {@link VariableContext}
   *
   * @param decision the decision to evaluate
   * @param variableContext the available variable context
   * @return the result of the decision evaluation
   */
  public DmnDecisionResult evaluateDecision(DmnDecision decision, VariableContext variableContext) {

    if(decision.getKey() == null) {
      throw LOG.unableToFindAnyDecisionTable();
    }
    VariableMap variableMap = buildVariableMapFromVariableContext(variableContext);

    List<DmnDecision> requiredDecisions = new ArrayList<DmnDecision>();
    buildDecisionTree(decision, requiredDecisions);

    List<DmnDecisionLogicEvaluationEvent> evaluatedEvents = new ArrayList<DmnDecisionLogicEvaluationEvent>();
    DmnDecisionResult evaluatedResult = null;

    for (DmnDecision evaluateDecision : requiredDecisions) {
      DmnDecisionLogicEvaluationHandler handler = getDecisionEvaluationHandler(evaluateDecision);
      DmnDecisionLogicEvaluationEvent evaluatedEvent = handler.evaluate(evaluateDecision, variableMap.asVariableContext());
      evaluatedEvents.add(evaluatedEvent);

      evaluatedResult = handler.generateDecisionResult(evaluatedEvent);
      if(decision != evaluateDecision) {
        addResultToVariableContext(evaluatedResult, variableMap, evaluateDecision);
      }
    }

    generateDecisionEvaluationEvent(evaluatedEvents);
    return evaluatedResult;
  }

  protected VariableMap buildVariableMapFromVariableContext(VariableContext variableContext) {

    VariableMap variableMap = Variables.createVariables();

    Set<String> variables = variableContext.keySet();
    for(String variable: variables) {
      variableMap.put(variable, variableContext.resolve(variable));
    }

    return variableMap;
  }

  protected void buildDecisionTree(DmnDecision decision, List<DmnDecision> requiredDecisions) {
    if (requiredDecisions.contains(decision)) {
      return;
    }

    for(DmnDecision dmnDecision : decision.getRequiredDecisions()){
      buildDecisionTree(dmnDecision, requiredDecisions);
    }

    requiredDecisions.add(decision);
  }

  protected DmnDecisionLogicEvaluationHandler getDecisionEvaluationHandler(DmnDecision decision) {
    Class<? extends DmnDecisionLogic> key = decision.getDecisionLogic().getClass();

    if (evaluationHandlers.containsKey(key)) {
      return evaluationHandlers.get(key);
    } else {
      throw LOG.decisionLogicTypeNotSupported(decision.getDecisionLogic());
    }
  }

  protected void addResultToVariableContext(DmnDecisionResult evaluatedResult, VariableMap variableMap, DmnDecision evaluatedDecision) {
    List<Map<String, Object>> resultList = evaluatedResult.getResultList();

    if (resultList.isEmpty()) {
      return;
    } else if (resultList.size() == 1 && !isDecisionTableWithCollectOrRuleOrderHitPolicy(evaluatedDecision)) {
      variableMap.putAll(evaluatedResult.getSingleResult());
    } else {
      Set<String> outputs = new HashSet<String>();

      for (Map<String, Object> resultMap : resultList) {
        outputs.addAll(resultMap.keySet());
      }

      for (String output : outputs) {
        List<Object> values = evaluatedResult.collectEntries(output);
        variableMap.put(output, values);
      }
    }
  }

  protected boolean isDecisionTableWithCollectOrRuleOrderHitPolicy(DmnDecision evaluatedDecision) {
    boolean isDecisionTableWithCollectHitPolicy = false;

    if (evaluatedDecision.isDecisionTable()) {
      DmnDecisionTableImpl decisionTable = (DmnDecisionTableImpl) evaluatedDecision.getDecisionLogic();
      isDecisionTableWithCollectHitPolicy = COLLECT_HIT_POLICY.equals(decisionTable.getHitPolicyHandler().getHitPolicyEntry())
        || RULE_ORDER_HIT_POLICY.equals(decisionTable.getHitPolicyHandler().getHitPolicyEntry());
    }

    return isDecisionTableWithCollectHitPolicy;
  }

  protected void generateDecisionEvaluationEvent(List<DmnDecisionLogicEvaluationEvent> evaluatedEvents) {

    DmnDecisionLogicEvaluationEvent rootEvaluatedEvent = null;
    DmnDecisionEvaluationEventImpl decisionEvaluationEvent = new DmnDecisionEvaluationEventImpl();
    long executedDecisionElements = 0L;

    for(DmnDecisionLogicEvaluationEvent evaluatedEvent: evaluatedEvents) {
      executedDecisionElements += evaluatedEvent.getExecutedDecisionElements();
      rootEvaluatedEvent = evaluatedEvent;
    }

    decisionEvaluationEvent.setDecisionResult(rootEvaluatedEvent);
    decisionEvaluationEvent.setExecutedDecisionInstances(evaluatedEvents.size());
    decisionEvaluationEvent.setExecutedDecisionElements(executedDecisionElements);

    evaluatedEvents.remove(rootEvaluatedEvent);
    decisionEvaluationEvent.setRequiredDecisionResults(evaluatedEvents);

    for (DmnDecisionEvaluationListener evaluationListener : evaluationListeners) {
      evaluationListener.notify(decisionEvaluationEvent);
    }
  }

}
