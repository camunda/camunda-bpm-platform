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

package org.camunda.bpm.dmn.engine.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.variable.Variables.createVariables;

import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.spi.DmnEngineMetricCollector;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.commons.utils.IoUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DmnEngineMetricCollectorTest extends DmnEngineTest {

  public static final String EXAMPLE_DMN = "org/camunda/bpm/dmn/engine/api/Example.dmn";
  public static final String DISH_EXAMPLE_DMN = "org/camunda/bpm/dmn/engine/api/DrdDishDecisionExample.dmn";

  protected DmnEngineMetricCollector metricCollector;

  @Before
  public void getEngineMetricCollector() {
    metricCollector = dmnEngine.getConfiguration().getEngineMetricCollector();
  }

  @Before
  public void setTestVariables() {
    variables.putValue("status", "bronze");
    variables.putValue("sum", 100);
  }

  @After
  public void clearEngineMetrics() {
    metricCollector.clearExecutedDecisionElements();
  }

  @Test
  public void testInitialExecutedDecisionElementsValue() {
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(0L);
  }

  @Test
  @DecisionResource(resource = EXAMPLE_DMN)
  public void testExecutedDecisionElementsValue() {
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(0L);

    dmnEngine.evaluateDecisionTable(decision, variables);
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(16L);

    dmnEngine.evaluateDecisionTable(decision, variables);
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(32L);

    dmnEngine.evaluateDecisionTable(decision, variables);
    dmnEngine.evaluateDecisionTable(decision, variables);
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(64L);
  }

  @Test
  public void testDrdExecutedDecisionElementsValue() {
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(0L);
    List<DmnDecision> decisions = parseDecisionsFromFile(DISH_EXAMPLE_DMN);
    VariableMap variableMap = createVariables()
      .putValue("temperature", 20)
      .putValue("dayType", "Weekend");
    
    dmnEngine.evaluateDecisionTable(decisions.get(0), variableMap);
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(30L);

    dmnEngine.evaluateDecisionTable(decisions.get(0), variableMap);
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(60L);

    dmnEngine.evaluateDecisionTable(decisions.get(0), variableMap);
    dmnEngine.evaluateDecisionTable(decisions.get(0), variableMap);
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(120L);
  }

  @Test
  @DecisionResource(resource = EXAMPLE_DMN)
  public void testClearExecutedDecisionElementsValue() {
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(0L);

    dmnEngine.evaluateDecisionTable(decision, variables);
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(16L);
    assertThat(metricCollector.clearExecutedDecisionElements()).isEqualTo(16L);

    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(0L);
  }

  @Test
  public void testDrdDishDecisionExample() {
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(0L);

    dmnEngine.evaluateDecisionTable("Dish", IoUtil.fileAsStream(DISH_EXAMPLE_DMN), createVariables()
      .putValue("temperature", 20)
      .putValue("dayType", "Weekend"));
    
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(30L);
    assertThat(metricCollector.clearExecutedDecisionElements()).isEqualTo(30L);

    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(0L);
  }

}
