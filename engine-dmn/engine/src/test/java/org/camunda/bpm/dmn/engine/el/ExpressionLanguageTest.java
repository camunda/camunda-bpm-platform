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

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.*;
import static org.camunda.bpm.dmn.engine.test.asserts.DmnAssertions.assertThat;
import static org.camunda.bpm.dmn.engine.util.DmnExampleVerifier.assertExample;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.dmn.engine.DmnClause;
import org.camunda.bpm.dmn.engine.DmnClauseEntry;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.DmnExpression;
import org.camunda.bpm.dmn.engine.DmnScriptEngineResolver;
import org.camunda.bpm.dmn.engine.impl.DefaultScriptEngineResolver;
import org.camunda.bpm.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.camunda.bpm.dmn.engine.impl.el.JuelElProvider;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.camunda.bpm.dmn.feel.FeelException;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

public class ExpressionLanguageTest extends DmnDecisionTest {

  public static final String GROOVY_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.groovy.dmn";
  public static final String SCRIPT_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.script.dmn";
  public static final String EMPTY_EXPRESSIONS_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.emptyExpressions.dmn";

  public static final int NUMBER_OF_INPUT_EXPRESSIONS = 2;
  public static final int NUMBER_OF_INPUT_ENTRIES = 5;
  public static final int NUMBER_OF_MATCHING_OUTPUT_ENTRIES = 2;
  public static final int NUMBER_OF_EXAMPLE_EXECUTIONS = 4;
  public static final int NUMBER_OF_EXPRESSIONS = (NUMBER_OF_INPUT_EXPRESSIONS + NUMBER_OF_INPUT_ENTRIES + NUMBER_OF_MATCHING_OUTPUT_ENTRIES) * NUMBER_OF_EXAMPLE_EXECUTIONS;
  public static final int NUMBER_OF_FEEL_EXPRESSION = NUMBER_OF_INPUT_ENTRIES * NUMBER_OF_EXAMPLE_EXECUTIONS;

  protected DefaultScriptEngineResolver scriptEngineResolver;
  protected JuelElProvider elProvider;

  public DmnEngineConfiguration createDmnEngineConfiguration() {
    DmnEngineConfigurationImpl dmnEngineConfiguration = new DmnEngineConfigurationImpl();

    dmnEngineConfiguration.setScriptEngineResolver(createScriptEngineResolver());
    dmnEngineConfiguration.setElProvider(createElProvider());

    return dmnEngineConfiguration;
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
  public void testGlobalExpressionLanguage() {
    DmnDecisionModel decisionModel = engine.parseDecisionModel(GROOVY_DMN);
    assertThat(decisionModel.getExpressionLanguage()).isEqualTo("groovy");

    DmnDecisionTable decision = decisionModel.getDecision("decision");
    for (DmnClause dmnClause : decision.getClauses()) {
      if (dmnClause.isInputClause()) {
        assertThat(dmnClause.getInputExpression().getExpressionLanguage()).isEqualTo("groovy");
        for (DmnClauseEntry dmnClauseEntry : dmnClause.getInputEntries()) {
          assertThat(dmnClauseEntry.getExpressionLanguage()).isEqualTo("groovy");
        }
      }
      else {
        for (DmnExpression dmnExpression : dmnClause.getOutputEntries()) {
          assertThat(dmnExpression.getExpressionLanguage()).isEqualTo("groovy");
        }
      }
    }

    assertExample(engine, decision);
    verify(scriptEngineResolver, times(NUMBER_OF_EXPRESSIONS)).getScriptEngineForLanguage("groovy");
    verify(scriptEngineResolver, never()).getScriptEngineForLanguage("juel");
  }

  @Test
  public void testExecuteDefaultDmnEngineConfiguration() {
    assertExample(engine);

    verify(elProvider, times(NUMBER_OF_EXPRESSIONS - NUMBER_OF_FEEL_EXPRESSION)).createExpression(anyString());
  }

  @Test
  @DecisionResource(resource = SCRIPT_DMN)
  public void testExecuteJuelDmnEngineConfiguration() {
    DmnEngine juelEngine = createEngineWithDefaultExpressionLanguage("juel");
    assertExample(juelEngine, decision);

    verify(elProvider, times(NUMBER_OF_EXPRESSIONS)).createExpression(anyString());
  }

  @Test
  @DecisionResource(resource = SCRIPT_DMN)
  public void testExecuteGroovyDmnEngineConfiguration() {
    DmnEngine groovyEngine = createEngineWithDefaultExpressionLanguage("groovy");
    assertExample(groovyEngine, decision);

    verify(scriptEngineResolver, times(NUMBER_OF_EXPRESSIONS)).getScriptEngineForLanguage("groovy");
    verify(scriptEngineResolver, never()).getScriptEngineForLanguage("juel");
  }

  @Test
  @DecisionResource(resource = SCRIPT_DMN)
  public void testExecuteJavascriptDmnEngineConfiguration() {
    DmnEngine javascriptEngine = createEngineWithDefaultExpressionLanguage("javascript");
    assertExample(javascriptEngine, decision);

    verify(scriptEngineResolver, times(NUMBER_OF_EXPRESSIONS)).getScriptEngineForLanguage("javascript");
    verify(scriptEngineResolver, never()).getScriptEngineForLanguage("juel");
  }

  @Test
  @DecisionResource(resource = EMPTY_EXPRESSIONS_DMN)
  public void testDefaultEmptyExpressions() {
    assertThat(engine).evaluates(decision, Variables.createVariables()).hasResultValue(true);
    verify(elProvider).createExpression(anyString());
  }

  @Test
  @DecisionResource(resource = EMPTY_EXPRESSIONS_DMN)
  public void testJuelEmptyExpressions() {
    DmnEngine juelEngine = createEngineWithDefaultExpressionLanguage("juel");
    assertThat(juelEngine).evaluates(decision, Variables.createVariables()).hasResultValue(true);

    verify(elProvider).createExpression(anyString());
  }

  @Test
  @DecisionResource(resource = EMPTY_EXPRESSIONS_DMN)
  public void testGroovyEmptyExpressions() {
    DmnEngine groovyEngine = createEngineWithDefaultExpressionLanguage("groovy");
    assertThat(groovyEngine).evaluates(decision, Variables.createVariables()).hasResultValue(true);

    verify(scriptEngineResolver).getScriptEngineForLanguage("groovy");
  }

  @Test
  @DecisionResource(resource = EMPTY_EXPRESSIONS_DMN)
  public void testJavascriptEmptyExpressions() {
    DmnEngine javascriptEngine = createEngineWithDefaultExpressionLanguage("javascript");
    assertThat(javascriptEngine).evaluates(decision, Variables.createVariables()).hasResultValue(true);

    verify(scriptEngineResolver).getScriptEngineForLanguage("javascript");
  }

  @Test
  @DecisionResource(resource = EMPTY_EXPRESSIONS_DMN, decisionKey = "decision2")
  public void testFailFeelUseOfEmptyInputExpression() {
    try {
      engine.evaluate(decision, Variables.createVariables());
      fail("Exception expected as the input expression is empty");
    }
    catch (FeelException e) {
      assertThat(e).hasMessageStartingWith("FEEL-01017");
      assertThat(e).hasMessageContaining("'10'");
      assertThat(e.getMessage()).doesNotContain("cellInput");
    }
  }

  protected DmnEngine createEngineWithDefaultExpressionLanguage(String expressionLanguage) {
    DmnEngineConfigurationImpl engineConfiguration = (DmnEngineConfigurationImpl) createDmnEngineConfiguration();
    engineConfiguration.setDefaultAllowedValueExpressionLanguage(expressionLanguage);
    engineConfiguration.setDefaultInputExpressionExpressionLanguage(expressionLanguage);
    engineConfiguration.setDefaultInputEntryExpressionLanguage(expressionLanguage);
    engineConfiguration.setDefaultOutputEntryExpressionLanguage(expressionLanguage);

    return engineConfiguration.buildEngine();
  }

}
