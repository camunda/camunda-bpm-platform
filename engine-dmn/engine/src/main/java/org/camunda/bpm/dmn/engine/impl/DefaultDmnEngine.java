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

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionRequirementsGraph;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransformer;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.model.dmn.DmnModelInstance;

public class DefaultDmnEngine implements DmnEngine {

  protected static final DmnEngineLogger LOG = DmnLogger.ENGINE_LOGGER;

  protected DefaultDmnEngineConfiguration dmnEngineConfiguration;
  protected DmnTransformer transformer;

  public DefaultDmnEngine(DefaultDmnEngineConfiguration dmnEngineConfiguration) {
    this.dmnEngineConfiguration = dmnEngineConfiguration;
    this.transformer = dmnEngineConfiguration.getTransformer();
  }

  public DmnEngineConfiguration getConfiguration() {
    return dmnEngineConfiguration;
  }

  public List<DmnDecision> parseDecisions(InputStream inputStream) {
    ensureNotNull("inputStream", inputStream);
    return transformer.createTransform()
      .modelInstance(inputStream)
      .transformDecisions();
  }

  public List<DmnDecision> parseDecisions(DmnModelInstance dmnModelInstance) {
    ensureNotNull("dmnModelInstance", dmnModelInstance);
    return transformer.createTransform()
      .modelInstance(dmnModelInstance)
      .transformDecisions();
  }

  public DmnDecision parseDecision(String decisionKey, InputStream inputStream) {
    ensureNotNull("decisionKey", decisionKey);
    List<DmnDecision> decisions = parseDecisions(inputStream);
    for (DmnDecision decision : decisions) {
      if (decisionKey.equals(decision.getKey())) {
        return decision;
      }
    }
    throw LOG.unableToFindDecisionWithKey(decisionKey);
  }

  public DmnDecision parseDecision(String decisionKey, DmnModelInstance dmnModelInstance) {
    ensureNotNull("decisionKey", decisionKey);
    List<DmnDecision> decisions = parseDecisions(dmnModelInstance);
    for (DmnDecision decision : decisions) {
      if (decisionKey.equals(decision.getKey())) {
        return decision;
      }
    }
    throw LOG.unableToFindDecisionWithKey(decisionKey);
  }

  public DmnDecisionRequirementsGraph parseDecisionRequirementsGraph(InputStream inputStream) {
    ensureNotNull("inputStream", inputStream);
    return transformer.createTransform()
      .modelInstance(inputStream)
      .transformDecisionRequirementsGraph();
  }

  public DmnDecisionRequirementsGraph parseDecisionRequirementsGraph(DmnModelInstance dmnModelInstance) {
    ensureNotNull("dmnModelInstance", dmnModelInstance);
    return transformer.createTransform()
      .modelInstance(dmnModelInstance)
      .transformDecisionRequirementsGraph();
  }

  public DmnDecisionTableResult evaluateDecisionTable(DmnDecision decision, Map<String, Object> variables) {
    ensureNotNull("decision", decision);
    ensureNotNull("variables", variables);
    return evaluateDecisionTable(decision, Variables.fromMap(variables).asVariableContext());
  }

  public DmnDecisionTableResult evaluateDecisionTable(DmnDecision decision, VariableContext variableContext) {
    ensureNotNull("decision", decision);
    ensureNotNull("variableContext", variableContext);

    if (decision instanceof DmnDecisionImpl && decision.isDecisionTable()) {
      DefaultDmnDecisionContext decisionContext = new DefaultDmnDecisionContext(dmnEngineConfiguration);

      DmnDecisionResult decisionResult = decisionContext.evaluateDecision(decision, variableContext);
      return DmnDecisionTableResultImpl.wrap(decisionResult);
    }
    else {
      throw LOG.decisionIsNotADecisionTable(decision);
    }
  }

  public DmnDecisionTableResult evaluateDecisionTable(String decisionKey, InputStream inputStream, Map<String, Object> variables) {
    ensureNotNull("variables", variables);
    return evaluateDecisionTable(decisionKey, inputStream, Variables.fromMap(variables).asVariableContext());
  }

  public DmnDecisionTableResult evaluateDecisionTable(String decisionKey, InputStream inputStream, VariableContext variableContext) {
    ensureNotNull("decisionKey", decisionKey);
    List<DmnDecision> decisions = parseDecisions(inputStream);
    for (DmnDecision decision : decisions) {
      if (decisionKey.equals(decision.getKey())) {
        return evaluateDecisionTable(decision, variableContext);
      }
    }
    throw LOG.unableToFindDecisionWithKey(decisionKey);
  }

  public DmnDecisionTableResult evaluateDecisionTable(String decisionKey, DmnModelInstance dmnModelInstance, Map<String, Object> variables) {
    ensureNotNull("variables", variables);
    return evaluateDecisionTable(decisionKey, dmnModelInstance, Variables.fromMap(variables).asVariableContext());
  }

  public DmnDecisionTableResult evaluateDecisionTable(String decisionKey, DmnModelInstance dmnModelInstance, VariableContext variableContext) {
    ensureNotNull("decisionKey", decisionKey);
    List<DmnDecision> decisions = parseDecisions(dmnModelInstance);
    for (DmnDecision decision : decisions) {
      if (decisionKey.equals(decision.getKey())) {
        return evaluateDecisionTable(decision, variableContext);
      }
    }
    throw LOG.unableToFindDecisionWithKey(decisionKey);
  }

  public DmnDecisionResult evaluateDecision(DmnDecision decision, Map<String, Object> variables) {
    ensureNotNull("decision", decision);
    ensureNotNull("variables", variables);
    return evaluateDecision(decision, Variables.fromMap(variables).asVariableContext());
  }

  public DmnDecisionResult evaluateDecision(DmnDecision decision, VariableContext variableContext) {
    ensureNotNull("decision", decision);
    ensureNotNull("variableContext", variableContext);

    if (decision instanceof DmnDecisionImpl) {
      DefaultDmnDecisionContext decisionContext = new DefaultDmnDecisionContext(dmnEngineConfiguration);
      return decisionContext.evaluateDecision(decision, variableContext);
    }
    else {
      throw LOG.decisionTypeNotSupported(decision);
    }
  }

  public DmnDecisionResult evaluateDecision(String decisionKey, InputStream inputStream, Map<String, Object> variables) {
    ensureNotNull("variables", variables);
    return evaluateDecision(decisionKey, inputStream, Variables.fromMap(variables).asVariableContext());
  }

  public DmnDecisionResult evaluateDecision(String decisionKey, InputStream inputStream, VariableContext variableContext) {
    ensureNotNull("decisionKey", decisionKey);
    List<DmnDecision> decisions = parseDecisions(inputStream);
    for (DmnDecision decision : decisions) {
      if (decisionKey.equals(decision.getKey())) {
        return evaluateDecision(decision, variableContext);
      }
    }
    throw LOG.unableToFindDecisionWithKey(decisionKey);
  }

  public DmnDecisionResult evaluateDecision(String decisionKey, DmnModelInstance dmnModelInstance, Map<String, Object> variables) {
    ensureNotNull("variables", variables);
    return evaluateDecision(decisionKey, dmnModelInstance, Variables.fromMap(variables).asVariableContext());
  }

  public DmnDecisionResult evaluateDecision(String decisionKey, DmnModelInstance dmnModelInstance, VariableContext variableContext) {
    ensureNotNull("decisionKey", decisionKey);
    List<DmnDecision> decisions = parseDecisions(dmnModelInstance);
    for (DmnDecision decision : decisions) {
      if (decisionKey.equals(decision.getKey())) {
        return evaluateDecision(decision, variableContext);
      }
    }
    throw LOG.unableToFindDecisionWithKey(decisionKey);
  }

}
