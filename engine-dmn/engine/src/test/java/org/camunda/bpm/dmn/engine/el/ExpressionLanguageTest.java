/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.camunda.bpm.dmn.engine.util.DmnExampleVerifier.assertExample;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionImpl;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
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
import org.junit.Test;

public class ExpressionLanguageTest extends DmnEngineTest {

  public static final String GROOVY_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.groovy.dmn";
  public static final String SCRIPT_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.script.dmn";
  public static final String EMPTY_EXPRESSIONS_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.emptyExpressions.dmn";

  protected DefaultScriptEngineResolver scriptEngineResolver;
  protected JuelElProvider elProvider;

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();

    configuration.setScriptEngineResolver(createScriptEngineResolver());
    configuration.setElProvider(createElProvider());

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
  @DecisionResource(resource = GROOVY_DMN)
  public void testGlobalExpressionLanguage() {
    DmnDecisionImpl decisionEntity  = (DmnDecisionImpl) decision;
    DmnDecisionTableImpl decisionTable = decisionEntity.getDecisionTable();
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
    verify(scriptEngineResolver, never()).getScriptEngineForLanguage("juel");
  }

  @Test
  public void testExecuteDefaultDmnEngineConfiguration() {
    assertExample(dmnEngine);

    verify(elProvider, atLeastOnce()).createExpression(anyString());
  }

  @Test
  @DecisionResource(resource = SCRIPT_DMN)
  public void testExecuteJuelDmnEngineConfiguration() {
    DmnEngine juelEngine = createEngineWithDefaultExpressionLanguage("juel");
    assertExample(juelEngine, decision);

    verify(elProvider, atLeastOnce()).createExpression(anyString());
  }

  @Test
  @DecisionResource(resource = SCRIPT_DMN)
  public void testExecuteGroovyDmnEngineConfiguration() {
    DmnEngine groovyEngine = createEngineWithDefaultExpressionLanguage("groovy");
    assertExample(groovyEngine, decision);

    verify(scriptEngineResolver, atLeastOnce()).getScriptEngineForLanguage("groovy");
    verify(scriptEngineResolver, never()).getScriptEngineForLanguage("juel");
  }

  @Test
  @DecisionResource(resource = SCRIPT_DMN)
  public void testExecuteJavascriptDmnEngineConfiguration() {
    DmnEngine javascriptEngine = createEngineWithDefaultExpressionLanguage("javascript");
    assertExample(javascriptEngine, decision);

    verify(scriptEngineResolver, atLeastOnce()).getScriptEngineForLanguage("javascript");
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
    dmnEngine = createEngineWithDefaultExpressionLanguage("juel");
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

  protected DmnEngine createEngineWithDefaultExpressionLanguage(String expressionLanguage) {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();

    configuration.setDefaultInputExpressionExpressionLanguage(expressionLanguage);
    configuration.setDefaultInputEntryExpressionLanguage(expressionLanguage);
    configuration.setDefaultOutputEntryExpressionLanguage(expressionLanguage);

    return configuration.buildEngine();
  }

}
