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

package org.camunda.bpm.dmn.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.junit.Test;

public class DmnEngineMetricCollectorTest extends DmnDecisionTest {

  public static final Map<String, Object> VARIABLES = new HashMap<String, Object>();

  static {
    VARIABLES.put("status", "bronze");
    VARIABLES.put("sum", 1000);
  }

  @Test
  public void testInitialExecutedDecisionElementsValue() {
    assertThat(getExecutedDecisionElements()).isEqualTo(0l);
  }

  @Test
  @DecisionResource(resource = EXAMPLE_DMN)
  public void testExecutedDecisionElementsValue() {
    assertThat(getExecutedDecisionElements()).isEqualTo(0l);

    engine.evaluate(decision, VARIABLES);
    assertThat(getExecutedDecisionElements()).isEqualTo(16l);

    engine.evaluate(decision, VARIABLES);
    assertThat(getExecutedDecisionElements()).isEqualTo(32l);

    engine.evaluate(decision, VARIABLES);
    engine.evaluate(decision, VARIABLES);
    assertThat(getExecutedDecisionElements()).isEqualTo(64l);
  }

  @Test
  @DecisionResource(resource = EXAMPLE_DMN)
  public void testClearExecutedDecisionElementsValue() {
    assertThat(getExecutedDecisionElements()).isEqualTo(0l);

    engine.evaluate(decision, VARIABLES);
    assertThat(getExecutedDecisionElements()).isEqualTo(16l);
    assertThat(clearExecutedDecisionElements()).isEqualTo(16l);

    assertThat(getExecutedDecisionElements()).isEqualTo(0l);
  }

  protected long getExecutedDecisionElements() {
    return engine.getConfiguration().getEngineMetricCollector().getExecutedDecisionElements();
  }

  protected long clearExecutedDecisionElements() {
    return engine.getConfiguration().getEngineMetricCollector().clearExecutedDecisionElements();
  }

}
