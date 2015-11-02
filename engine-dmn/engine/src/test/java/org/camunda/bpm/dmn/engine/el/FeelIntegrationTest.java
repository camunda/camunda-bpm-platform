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
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.dmn.engine.util.DmnExampleVerifier.assertExample;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.camunda.bpm.dmn.feel.FeelEngine;
import org.camunda.bpm.dmn.feel.FeelEngineProvider;
import org.camunda.bpm.dmn.feel.FeelException;
import org.camunda.bpm.dmn.feel.impl.FeelEngineProviderImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.junit.Test;

public class FeelIntegrationTest extends DmnDecisionTest {

  protected FeelEngine feelEngine;

  @Override
  public DmnEngineConfiguration createDmnEngineConfiguration() {
    DmnEngineConfigurationImpl dmnEngineConfiguration = new DmnEngineConfigurationImpl();
    dmnEngineConfiguration.setFeelEngineProvider(new TestFeelEngineProvider());
    return dmnEngineConfiguration;
  }

  @Test
  public void testDefaultEngineFeelInvocation() {
    int numberOfNotEmptyInputEntries = 6;
    int numberOfExampleInvocations = 4;
    assertExample(engine);

    verify(feelEngine, times(numberOfNotEmptyInputEntries * numberOfExampleInvocations))
      .evaluateSimpleUnaryTests(anyString(), anyString(), any(VariableContext.class));
  }

  @Test
  public void testFeelAlternativeName() {
    int numberOfNotEmptyInputEntries = 6;
    int numberOfExampleInvocations = 4;

    DmnEngineConfigurationImpl dmnEngineConfiguration = (DmnEngineConfigurationImpl) createDmnEngineConfiguration();
    dmnEngineConfiguration.setDefaultInputEntryExpressionLanguage("feel");
    DmnEngine dmnEngine = dmnEngineConfiguration.buildEngine();

    assertExample(dmnEngine);

    verify(feelEngine, times(numberOfNotEmptyInputEntries * numberOfExampleInvocations)).evaluateSimpleUnaryTests(anyString(), anyString(), any(VariableContext.class));
  }

  @Test
  public void testFeelInputExpressions() {
    DmnEngineConfigurationImpl configuration = (DmnEngineConfigurationImpl) createDmnEngineConfiguration();
    configuration.setDefaultInputExpressionExpressionLanguage(DmnEngineConfigurationImpl.FEEL_EXPRESSION_LANGUAGE);
    DmnEngine engine = configuration.buildEngine();

    try {
      assertExample(engine);
      fail("Expression expected as FEEL input expressions are not supported.");
    }
    catch (UnsupportedOperationException e) {
      assertThat(e).hasMessageStartingWith("FEEL-01016");
      verify(feelEngine).evaluateSimpleExpression(anyString(), any(VariableContext.class));
    }
  }

  @Test
  public void testFeelOutputEntry() {
    DmnEngineConfigurationImpl configuration = (DmnEngineConfigurationImpl) createDmnEngineConfiguration();
    configuration.setDefaultOutputEntryExpressionLanguage(DmnEngineConfigurationImpl.FEEL_EXPRESSION_LANGUAGE);
    DmnEngine engine = configuration.buildEngine();

    try {
      assertExample(engine);
      fail("Exception expected as FEEL output entries are not supported.");
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
      assertExample(engine, decision);
      fail("Exception expected as invalid FEEL is used.");
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

    VariableMap variables = Variables.createVariables()
      .putValue("dateString", format.format(testDate));

    DmnDecisionResult result = engine.evaluate(decision, variables);
    assertThat(result).hasSize(1);

    DmnDecisionOutput output = result.getSingleOutput();
    assertThat(output.size()).isEqualTo(1);

    DateValue dateResult = output.getSingleValueTyped();
    DateValue expectedResult = Variables.dateValue(testDate);
    assertThat(dateResult).isEqualTo(expectedResult);
  }

  public class TestFeelEngineProvider implements FeelEngineProvider {

    public TestFeelEngineProvider() {
      FeelEngineProviderImpl feelEngineProvider = new FeelEngineProviderImpl();
      feelEngine = spy(feelEngineProvider.createInstance());
    }

    public FeelEngine createInstance() {
      return feelEngine;
    }

  }

}
