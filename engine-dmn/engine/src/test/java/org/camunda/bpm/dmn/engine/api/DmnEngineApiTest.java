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
package org.camunda.bpm.dmn.engine.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.camunda.bpm.dmn.engine.test.asserts.DmnEngineTestAssertions.assertThat;
import static org.camunda.bpm.engine.variable.Variables.createVariables;
import static org.camunda.bpm.engine.variable.Variables.emptyVariableContext;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionLogic;
import org.camunda.bpm.dmn.engine.DmnDecisionRequirementsGraph;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnEvaluationException;
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformException;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.commons.utils.IoUtil;
import org.junit.Test;

/**
 * Simple api test making sure the api methods are there and accept the right parameters
 *
 * @author Daniel Meyer
 *
 */
public class DmnEngineApiTest extends DmnEngineTest {

  public static final String ONE_RULE_DMN = "org/camunda/bpm/dmn/engine/api/OneRule.dmn";
  public static final String NOT_A_DMN_FILE = "org/camunda/bpm/dmn/engine/api/NotADmnFile.bpmn";
  public static final String DECISION_LITERAL_EXPRESSION_DMN = "org/camunda/bpm/dmn/engine/api/DecisionWithLiteralExpression.dmn";

  public static final String INPUT_VALUE = "ok";
  public static final String EXPECTED_OUTPUT_VALUE = "ok";
  public static final String DECISION_KEY = "decision";

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    return new DefaultDmnEngineConfiguration()
      .enableFeelLegacyBehavior(true);
  }

  @Test
  public void shouldFailParsingIfInputStreamIsNull() {
    try{
      dmnEngine.parseDecisions((InputStream) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }

    try{
      dmnEngine.parseDecision(DECISION_KEY, (InputStream) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }
  }

  @Test
  public void shouldFailParsingIfInputStreamIsInvalid() {
    try{
      dmnEngine.parseDecisions(createInvalidInputStream());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageContaining("DMN-02003");
    }

    try{
      dmnEngine.parseDecision(DECISION_KEY, createInvalidInputStream());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageContaining("DMN-02003");
    }
  }

  @Test
  public void shouldFailParsingIfModelInstanceIsNull() {
    try{
      dmnEngine.parseDecisions((DmnModelInstance) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }

    try{
      dmnEngine.parseDecision(DECISION_KEY, (DmnModelInstance) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }
  }

  @Test
  public void shouldFailParsingIfDecisionKeyIsNull() {
    try{
      dmnEngine.parseDecision(null, createInputStream());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }

    try{
      dmnEngine.parseDecision(null, createDmnModelInstance());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }
  }

  @Test
  public void shouldFailParsingIfDecisionKeyIsUnknown() {
    try{
      dmnEngine.parseDecision("unknown", createInputStream());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01001")
        .hasMessageContaining("unknown");
    }

    try{
      dmnEngine.parseDecision("unknown", createDmnModelInstance());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01001")
        .hasMessageContaining("unknown");
    }
  }

  @Test
  public void shouldFailParsingDrgIfInputStreamIsNull() {
    try{
      dmnEngine.parseDecisionRequirementsGraph((InputStream) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }
  }

  @Test
  public void shouldFailParsingDrgIfInputStreamIsInvalid() {
    try{
      dmnEngine.parseDecisionRequirementsGraph(createInvalidInputStream());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageContaining("DMN-02003");
    }
  }

  @Test
  public void shouldFailParsingDrgIfModelInstanceIsNull() {
    try{
      dmnEngine.parseDecisionRequirementsGraph((DmnModelInstance) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }
  }

  @Test
  public void shouldFailEvaluatingDecisionTableIfInputStreamIsNull() {
    try{
      dmnEngine.evaluateDecisionTable(DECISION_KEY, (InputStream) null, createVariables());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }

    try{
      dmnEngine.evaluateDecisionTable(DECISION_KEY, (InputStream) null, emptyVariableContext());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }
  }

  @Test
  public void shouldFailEvaluatingDecisionTableIfInputStreamIsInvalid() {
    try{
      dmnEngine.evaluateDecisionTable(DECISION_KEY, createInvalidInputStream(), createVariables());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageContaining("DMN-02003");
    }

    try{
      dmnEngine.evaluateDecisionTable(DECISION_KEY, createInvalidInputStream(), emptyVariableContext());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageContaining("DMN-02003");
    }
  }

  @Test
  public void shouldFailEvaluatingDecisionTableIfModelInstanceIsNull() {
    try{
      dmnEngine.evaluateDecisionTable(DECISION_KEY, (DmnModelInstance) null, createVariables());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }

    try{
      dmnEngine.evaluateDecisionTable(DECISION_KEY, (DmnModelInstance) null, emptyVariableContext());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }
  }

  @Test
  public void shouldFailEvaluatingDecisionTableIfDecisionKeyIsNull() {
    try {
      dmnEngine.evaluateDecisionTable(null, createInputStream(), createVariables());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecisionTable(null, createInputStream(), emptyVariableContext());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecisionTable(null, createDmnModelInstance(), createVariables());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecisionTable(null, createDmnModelInstance(), emptyVariableContext());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }
  }

  @Test
  public void shouldFailEvaluatingDecisionTableIfDecisionKeyIsUnknown() {
    try{
      dmnEngine.evaluateDecisionTable("unknown", createInputStream(), createVariables());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01001")
        .hasMessageContaining("unknown");
    }

    try{
      dmnEngine.evaluateDecisionTable("unknown", createInputStream(), emptyVariableContext());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01001")
        .hasMessageContaining("unknown");
    }

    try{
      dmnEngine.evaluateDecisionTable("unknown", createDmnModelInstance(), createVariables());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01001")
        .hasMessageContaining("unknown");
    }

    try{
      dmnEngine.evaluateDecisionTable("unknown", createDmnModelInstance(), emptyVariableContext());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01001")
        .hasMessageContaining("unknown");
    }
  }

  @Test
  public void shouldFailEvaluatingDecisionTableIfDecisionIsNull() {
    try {
      dmnEngine.evaluateDecisionTable(null, createVariables());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecisionTable(null, emptyVariableContext());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldFailEvaluatingDecisionTableIfVariablesIsNull() {
    try {
      dmnEngine.evaluateDecisionTable(DECISION_KEY, createInputStream(), (Map<String, Object>) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecisionTable(DECISION_KEY, createDmnModelInstance(), (Map<String, Object>) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecisionTable(decision, (Map<String, Object>) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldFailEvaluatingDecisionTableIfVariableContextIsNull() {
    try {
      dmnEngine.evaluateDecisionTable(DECISION_KEY, createInputStream(), (VariableContext) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecisionTable(DECISION_KEY, createDmnModelInstance(), (VariableContext) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecisionTable(decision, (VariableContext) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldFailEvaluatingDecisionTableWithEmptyVariableMap() {
    try {
      dmnEngine.evaluateDecisionTable(decision, createVariables());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(DmnEvaluationException e) {
      assertThat(e).hasMessageStartingWith("DMN-01002");
    }
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldFailEvaluatingDecisionTableWithEmptyVariableContext() {
    try {
      dmnEngine.evaluateDecisionTable(decision, emptyVariableContext());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(DmnEvaluationException e) {
      assertThat(e).hasMessageStartingWith("DMN-01002");
    }
  }

  @Test
  public void shouldFailEvaluatingIfInputStreamIsNull() {
    try{
      dmnEngine.evaluateDecision(DECISION_KEY, (InputStream) null, createVariables());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }

    try{
      dmnEngine.evaluateDecision(DECISION_KEY, (InputStream) null, emptyVariableContext());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }
  }

  @Test
  public void shouldFailEvaluatingIfInputStreamIsInvalid() {
    try{
      dmnEngine.evaluateDecision(DECISION_KEY, createInvalidInputStream(), createVariables());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageContaining("DMN-02003");
    }

    try{
      dmnEngine.evaluateDecision(DECISION_KEY, createInvalidInputStream(), emptyVariableContext());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageContaining("DMN-02003");
    }
  }

  @Test
  public void shouldFailEvaluatingIfModelInstanceIsNull() {
    try{
      dmnEngine.evaluateDecision(DECISION_KEY, (DmnModelInstance) null, createVariables());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }

    try{
      dmnEngine.evaluateDecision(DECISION_KEY, (DmnModelInstance) null, emptyVariableContext());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch (IllegalArgumentException e) {
      assertThat(e)
        .hasMessageContaining("UTILS-02001");
    }
  }

  @Test
  public void shouldFailEvaluatingIfDecisionKeyIsNull() {
    try {
      dmnEngine.evaluateDecision(null, createInputStream(), createVariables());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecision(null, createInputStream(), emptyVariableContext());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecision(null, createDmnModelInstance(), createVariables());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecision(null, createDmnModelInstance(), emptyVariableContext());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }
  }

  @Test
  public void shouldFailEvaluatingIfDecisionKeyIsUnknown() {
    try{
      dmnEngine.evaluateDecision("unknown", createInputStream(), createVariables());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01001")
        .hasMessageContaining("unknown");
    }

    try{
      dmnEngine.evaluateDecision("unknown", createInputStream(), emptyVariableContext());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01001")
        .hasMessageContaining("unknown");
    }

    try{
      dmnEngine.evaluateDecision("unknown", createDmnModelInstance(), createVariables());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01001")
        .hasMessageContaining("unknown");
    }

    try{
      dmnEngine.evaluateDecision("unknown", createDmnModelInstance(), emptyVariableContext());
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageStartingWith("DMN-01001")
        .hasMessageContaining("unknown");
    }
  }

  @Test
  public void shouldFailEvaluatingIfDecisionIsNull() {
    try {
      dmnEngine.evaluateDecision(null, createVariables());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecision(null, emptyVariableContext());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldFailEvaluatingIfVariablesIsNull() {
    try {
      dmnEngine.evaluateDecision(DECISION_KEY, createInputStream(), (Map<String, Object>) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecision(DECISION_KEY, createDmnModelInstance(), (Map<String, Object>) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecision(decision, (Map<String, Object>) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldFailEvaluatingIfVariableContextIsNull() {
    try {
      dmnEngine.evaluateDecision(DECISION_KEY, createInputStream(), (VariableContext) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecision(DECISION_KEY, createDmnModelInstance(), (VariableContext) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }

    try {
      dmnEngine.evaluateDecision(decision, (VariableContext) null);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(IllegalArgumentException e) {
      assertThat(e).hasMessageStartingWith("UTILS-02001");
    }
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldFailEvaluatingDecisionWithEmptyVariableMap() {
    try {
      dmnEngine.evaluateDecision(decision, createVariables());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(DmnEvaluationException e) {
      assertThat(e).hasMessageStartingWith("DMN-01002");
    }
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldFailEvaluatingDecisionWithEmptyVariableContext() {
    try {
      dmnEngine.evaluateDecision(decision, emptyVariableContext());
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(DmnEvaluationException e) {
      assertThat(e).hasMessageStartingWith("DMN-01002");
    }
  }

  @Test
  @DecisionResource(resource = DECISION_LITERAL_EXPRESSION_DMN)
  public void shouldFailEvaluatingDecisionTableIfDecisionIsNotATable() {
    try {
      dmnEngine.evaluateDecisionTable(decision, variables);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(DmnEngineException e) {
      assertThat(e).hasMessageStartingWith("DMN-01013");
    }
  }

  @Test
  public void shouldFailEvaluatingDecisionTableIfDecisionTypeIsNotSupported() {
    try {
      dmnEngine.evaluateDecisionTable(mock(DmnDecision.class), variables);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(DmnEngineException e) {
      assertThat(e).hasMessageStartingWith("DMN-01013");
    }
  }

  @Test
  public void shouldFailEvaluatingDecisionIfDecisionTypeIsNotSupported() {
    try {
      dmnEngine.evaluateDecision(mock(DmnDecision.class), variables);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(DmnEngineException e) {
      assertThat(e).hasMessageStartingWith("DMN-01004");
    }
  }

  @Test
  public void shouldFailEvaluatingDecisionIfDecisionLogicIsNotSupported() {
    DmnDecisionImpl decision = new DmnDecisionImpl();
    decision.setKey("decision");
    decision.setDecisionLogic(mock(DmnDecisionLogic.class));

    try {
      dmnEngine.evaluateDecision(decision, variables);
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    }
    catch(DmnEngineException e) {
      assertThat(e).hasMessageStartingWith("DMN-01012");
    }
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldEvaluateDecisionTableWithVariableMap() {
    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decision, createVariables().putValue("input", INPUT_VALUE));
    assertThat(results)
      .hasSingleResult()
      .hasSingleEntry(EXPECTED_OUTPUT_VALUE);
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldEvaluateDecisionTableWithVariableContext() {
    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decision, createVariables().putValue("input", INPUT_VALUE).asVariableContext());
    assertThat(results)
      .hasSingleResult()
      .hasSingleEntry(EXPECTED_OUTPUT_VALUE);
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldEvaluateDecisionWithVariableMap() {
    DmnDecisionResult results = dmnEngine.evaluateDecision(decision, createVariables().putValue("input", INPUT_VALUE));

    assertThat((String) results.getSingleEntry())
      .isNotNull()
      .isEqualTo(EXPECTED_OUTPUT_VALUE);
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldEvaluateDecisionWithVariableContext() {
    DmnDecisionResult results = dmnEngine.evaluateDecision(decision, createVariables().putValue("input", INPUT_VALUE).asVariableContext());

    assertThat((String) results.getSingleEntry())
      .isNotNull()
      .isEqualTo(EXPECTED_OUTPUT_VALUE);
  }

  @Test
  @DecisionResource(resource = DECISION_LITERAL_EXPRESSION_DMN)
  public void shouldEvaluateDecisionLiteralExpression() {
    DmnDecisionResult results = dmnEngine.evaluateDecision(decision, createVariables().putValue("input", INPUT_VALUE));

    assertThat((String) results.getSingleEntry())
      .isNotNull()
      .isEqualTo(EXPECTED_OUTPUT_VALUE);
  }

  @Test
  public void shouldEvaluateDecisionOfDrg() {
    DmnDecisionRequirementsGraph drd = dmnEngine.parseDecisionRequirementsGraph(createInputStream());
    decision = drd.getDecision("decision");

    DmnDecisionTableResult results = dmnEngine.evaluateDecisionTable(decision, createVariables().putValue("input", INPUT_VALUE));
    assertThat(results)
      .hasSingleResult()
      .hasSingleEntry(EXPECTED_OUTPUT_VALUE);
  }

  // helper ///////////////////////////////////////////////////////////////////

  protected InputStream createInputStream() {
    return IoUtil.fileAsStream(ONE_RULE_DMN);
  }

  protected InputStream createInvalidInputStream() {
    return IoUtil.fileAsStream(NOT_A_DMN_FILE);
  }

  protected DmnModelInstance createDmnModelInstance() {
    return Dmn.readModelFromStream(createInputStream());
  }

}
