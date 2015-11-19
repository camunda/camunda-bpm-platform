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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.dmn.feel.FeelEngine;
import org.camunda.bpm.dmn.feel.FeelEngineFactory;
import org.camunda.bpm.dmn.feel.FeelException;
import org.camunda.bpm.dmn.feel.impl.FeelEngineFactoryImpl;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.junit.Test;

public class FeelIntegrationTest extends DmnEngineTest {

  protected FeelEngine feelEngine;

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();
    configuration.setFeelEngineFactory(new TestFeelEngineFactory());
    return configuration;
  }

  @Test
  public void testDefaultEngineFeelInvocation() {
    assertExample(dmnEngine);

    verify(feelEngine, atLeastOnce())
      .evaluateSimpleUnaryTests(anyString(), anyString(), any(VariableContext.class));
  }

  @Test
  public void testFeelAlternativeName() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultInputEntryExpressionLanguage("feel");
    DmnEngine dmnEngine = configuration.buildEngine();

    assertExample(dmnEngine);

    verify(feelEngine, atLeastOnce()).evaluateSimpleUnaryTests(anyString(), anyString(), any(VariableContext.class));
  }

  @Test
  public void testFeelInputExpressions() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultInputExpressionExpressionLanguage(DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE);
    DmnEngine engine = configuration.buildEngine();

    try {
      assertExample(engine);
      failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
    }
    catch (UnsupportedOperationException e) {
      assertThat(e).hasMessageStartingWith("FEEL-01016");
      verify(feelEngine).evaluateSimpleExpression(anyString(), any(VariableContext.class));
    }
  }

  @Test
  public void testFeelOutputEntry() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultOutputEntryExpressionLanguage(DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE);
    DmnEngine engine = configuration.buildEngine();

    try {
      assertExample(engine);
      failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
    }
    catch (UnsupportedOperationException e) {
      assertThat(e).hasMessageStartingWith("FEEL-01016");
      verify(feelEngine).evaluateSimpleExpression(anyString(), any(VariableContext.class));
    }
  }

  @Test
  @DecisionResource(resource = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.script.dmn")
  public void testFeelExceptionDoesNotContainJuel() {
    try {
      assertExample(dmnEngine, decision);
      failBecauseExceptionWasNotThrown(FeelException.class);
    }
    catch (FeelException e) {
      assertThat(e).hasMessageStartingWith("FEEL-01015");
      assertThat(e.getMessage()).doesNotContain("${");
    }
  }

  @Test
  @DecisionResource()
  public void testDateAndTimeIntegration() {
    Date testDate = new Date(1445526087000L);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    variables.putValue("dateString", format.format(testDate));

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.dateValue(testDate));

  }

  public class TestFeelEngineFactory implements FeelEngineFactory {

    public TestFeelEngineFactory() {
      FeelEngineFactoryImpl feelEngineFactory = new FeelEngineFactoryImpl();
      feelEngine = spy(feelEngineFactory.createInstance());
    }

    public FeelEngine createInstance() {
      return feelEngine;
    }

  }

}
