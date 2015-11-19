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

import org.camunda.bpm.dmn.engine.spi.DmnEngineMetricCollector;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DmnEngineMetricCollectorTest extends DmnEngineTest {

  public static final String EXAMPLE_DMN = "org/camunda/bpm/dmn/engine/api/Example.dmn";

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
  @DecisionResource(resource = EXAMPLE_DMN)
  public void testClearExecutedDecisionElementsValue() {
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(0L);

    dmnEngine.evaluateDecisionTable(decision, variables);
    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(16L);
    assertThat(metricCollector.clearExecutedDecisionElements()).isEqualTo(16L);

    assertThat(metricCollector.getExecutedDecisionElements()).isEqualTo(0L);
  }

}
