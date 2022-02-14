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
package org.camunda.bpm.dmn.engine.el;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionLiteralExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableInputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;
import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.el.DefaultScriptEngineResolver;
import org.camunda.bpm.dmn.engine.impl.el.JuelElProvider;
import org.camunda.bpm.dmn.engine.impl.spi.el.DmnScriptEngineResolver;
import org.camunda.bpm.dmn.engine.impl.spi.el.ElProvider;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.dmn.feel.impl.FeelException;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.camunda.bpm.dmn.engine.util.DmnExampleVerifier.assertExample;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ExpressionLanguageTest extends DmnEngineTest {

  public static final String GROOVY_DECISION_TABLE_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.groovy.decisionTable.dmn";
  public static final String GROOVY_DECISION_LITERAL_EXPRESSION_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.groovy.decisionLiteralExpression.dmn";
  public static final String SCRIPT_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.script.dmn";
  public static final String EMPTY_EXPRESSIONS_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.emptyExpressions.dmn";
  public static final String DECISION_WITH_LITERAL_EXPRESSION_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.decisionLiteralExpression.dmn";
  public static final String CAPITAL_JUEL_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.JUEL.dmn";
  public static final String JUEL_EXPRESSIONS_WITH_PROPERTIES_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.JUEL.expressionsWithProperties.dmn";
  public static final String JUEL = "juel";

  protected DefaultScriptEngineResolver scriptEngineResolver;
  protected JuelElProvider elProvider;

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();

    configuration.setScriptEngineResolver(createScriptEngineResolver());
    configuration.setElProvider(createElProvider());
    configuration.enableFeelLegacyBehavior(true);

    return configuration;
  }

  protected ElProvider createElProvider() {
    elProvider = spy(new JuelElProvider());
    return elProvider;
  }

  protected DmnScriptEngineResolver createScriptEngineResolver() {
    scriptEngineResolver = spy(new DefaultScriptEngineResolver());
    return scriptEngineResolver;
  }

  @Test
  @DecisionResource(resource = GROOVY_DECISION_TABLE_DMN)
  public void testGlobalExpressionLanguageDecisionTable() {
    DmnDecisionTableImpl decisionTable = (DmnDecisionTableImpl) decision.getDecisionLogic();
    for (DmnDecisionTableInputImpl dmnInput : decisionTable.getInputs()) {
      assertThat(dmnInput.getExpression().getExpressionLanguage()).isEqualTo("groovy");
    }

    for (DmnDecisionTableRuleImpl dmnRule : decisionTable.getRules()) {
      for (DmnExpressionImpl condition : dmnRule.getConditions()) {
        assertThat(condition.getExpressionLanguage()).isEqualTo("groovy");
      }
      for (DmnExpressionImpl conclusion : dmnRule.getConclusions()) {
        assertThat(conclusion.getExpressionLanguage()).isEqualTo("groovy");
      }
    }

    assertExample(dmnEngine, decision);
    verify(scriptEngineResolver, atLeastOnce()).getScriptEngineForLanguage("groovy");
    verify(scriptEngineResolver, never()).getScriptEngineForLanguage(JUEL);
  }

  @Test
  @DecisionResource(resource = GROOVY_DECISION_LITERAL_EXPRESSION_DMN)
  public void testGlobalExpressionLanguageDecisionLiteralExpression() {
    DmnDecisionLiteralExpressionImpl decisionLiteralExpression = (DmnDecisionLiteralExpressionImpl) decision.getDecisionLogic();

    assertThat(decisionLiteralExpression.getExpression().getExpressionLanguage()).isEqualTo("groovy");

    dmnEngine.evaluateDecision(decision,
        Variables.createVariables().putValue("a", 2).putValue("b", 3));

    verify(scriptEngineResolver, atLeastOnce()).getScriptEngineForLanguage("groovy");
    verify(scriptEngineResolver, never()).getScriptEngineForLanguage(JUEL);
  }

  @Test
  public void testExecuteDefaultDmnEngineConfiguration() {
    assertExample(dmnEngine);

    verify(elProvider, atLeastOnce()).createExpression(anyString());
  }

  @Test
  @DecisionResource(resource = SCRIPT_DMN)
  public void testExecuteJuelDmnEngineConfiguration() {
    DmnEngine juelEngine = createEngineWithDefaultExpressionLanguage(JUEL);
    assertExample(juelEngine, decision);

    verify(elProvider, atLeastOnce()).createExpression(anyString());
  }

  @Test
  @DecisionResource(resource = SCRIPT_DMN)
  public void testExecuteGroovyDmnEngineConfiguration() {
    DmnEngine groovyEngine = createEngineWithDefaultExpressionLanguage("groovy");
    assertExample(groovyEngine, decision);

    verify(scriptEngineResolver, atLeastOnce()).getScriptEngineForLanguage("groovy");
    verify(scriptEngineResolver, never()).getScriptEngineForLanguage(JUEL);
  }

  @Test
  @DecisionResource(resource = SCRIPT_DMN)
  public void testExecuteJavascriptDmnEngineConfiguration() {
    DmnEngine javascriptEngine = createEngineWithDefaultExpressionLanguage("javascript");
    assertExample(javascriptEngine, decision);

    verify(scriptEngineResolver, atLeastOnce()).getScriptEngineForLanguage("javascript");
    verify(scriptEngineResolver, never()).getScriptEngineForLanguage(JUEL);
  }

  @Test
  @DecisionResource(resource = DECISION_WITH_LITERAL_EXPRESSION_DMN)
  public void testExecuteLiteralExpressionWithDefaultDmnEngineConfiguration() {
    dmnEngine.evaluateDecision(decision,
        Variables.createVariables().putValue("a", 1).putValue("b", 2));

    verify(elProvider, atLeastOnce()).createExpression(anyString());
  }

  @Test
  @DecisionResource(resource = DECISION_WITH_LITERAL_EXPRESSION_DMN)
  public void testExecuteLiteralExpressionWithGroovyDmnEngineConfiguration() {
    DmnEngine juelEngine = createEngineWithDefaultExpressionLanguage("groovy");

    juelEngine.evaluateDecision(decision,
        Variables.createVariables().putValue("a", 1).putValue("b", 2));

    verify(scriptEngineResolver, atLeastOnce()).getScriptEngineForLanguage("groovy");
    verify(scriptEngineResolver, never()).getScriptEngineForLanguage("juel");
  }

  @Test
  @DecisionResource(resource = EMPTY_EXPRESSIONS_DMN)
  public void testDefaultEmptyExpressions() {
    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);

    verify(elProvider).createExpression(anyString());
  }

  @Test
  @DecisionResource(resource = EMPTY_EXPRESSIONS_DMN)
  public void testJuelEmptyExpressions() {
    dmnEngine = createEngineWithDefaultExpressionLanguage(JUEL);
    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);

    verify(elProvider).createExpression(anyString());
  }

  @Test
  @DecisionResource(resource = EMPTY_EXPRESSIONS_DMN)
  public void testGroovyEmptyExpressions() {
    dmnEngine = createEngineWithDefaultExpressionLanguage("groovy");
    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);

    verify(scriptEngineResolver).getScriptEngineForLanguage("groovy");
  }

  @Test
  @DecisionResource(resource = EMPTY_EXPRESSIONS_DMN)
  public void testJavascriptEmptyExpressions() {
    dmnEngine = createEngineWithDefaultExpressionLanguage("javascript");
    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);

    verify(scriptEngineResolver).getScriptEngineForLanguage("javascript");
  }

  @Test
  @DecisionResource(resource = EMPTY_EXPRESSIONS_DMN, decisionKey = "decision2")
  public void testFailFeelUseOfEmptyInputExpression() {
    try {
      evaluateDecisionTable();
      failBecauseExceptionWasNotThrown(FeelException.class);
    }
    catch (FeelException e) {
      assertThat(e).hasMessageStartingWith("FEEL-01017");
      assertThat(e).hasMessageContaining("'10'");
      assertThat(e.getMessage()).doesNotContain("cellInput");
    }
  }

  @Test
  @DecisionResource(resource = CAPITAL_JUEL_DMN)
  public void testElResolution () throws Exception {
    DmnEngine juelEngine = createEngineWithDefaultExpressionLanguage(JUEL);
    assertExample(juelEngine, decision);

    verify(elProvider, atLeastOnce()).createExpression(anyString());
    verify(scriptEngineResolver, never()).getScriptEngineForLanguage(JUEL.toUpperCase());
  }

  @Test
  @DecisionResource(resource = JUEL_EXPRESSIONS_WITH_PROPERTIES_DMN)
  public void testJuelDoesNotShadowInnerProperty() {
    VariableMap inputs = Variables.createVariables();
    inputs.putValue("testExpr", "TestProperty");

    Map<String, Object> mapVar = new HashMap<>(1);
    mapVar.put("b", "B_FROM_MAP");
    inputs.putValue("a", mapVar);
    inputs.putValue("b", "B_FROM_CONTEXT");

    DmnDecisionResult result = dmnEngine.evaluateDecision(decision, inputs.asVariableContext());

    assertThat((String) result.getSingleEntry()).isEqualTo("B_FROM_MAP");
  }

  @Test
  @DecisionResource(resource = JUEL_EXPRESSIONS_WITH_PROPERTIES_DMN)
  public void testJuelResolvesListIndex() {
    VariableMap inputs = Variables.createVariables();
    inputs.putValue("testExpr", "TestListIndex");

    List<String> listVar = new ArrayList<>(1);
    listVar.add("0_FROM_LIST");
    inputs.putValue("a", listVar);

    DmnDecisionResult result = dmnEngine.evaluateDecision(decision, inputs.asVariableContext());

    assertThat((String) result.getSingleEntry()).isEqualTo("0_FROM_LIST");
  }

  protected DmnEngine createEngineWithDefaultExpressionLanguage(String expressionLanguage) {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();

    configuration.setDefaultInputExpressionExpressionLanguage(expressionLanguage);
    configuration.setDefaultInputEntryExpressionLanguage(expressionLanguage);
    configuration.setDefaultOutputEntryExpressionLanguage(expressionLanguage);
    configuration.setDefaultLiteralExpressionLanguage(expressionLanguage);

    return configuration.buildEngine();
  }

}
