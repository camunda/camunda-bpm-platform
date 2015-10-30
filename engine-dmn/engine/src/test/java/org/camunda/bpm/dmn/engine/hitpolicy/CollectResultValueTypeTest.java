/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.dmn.engine.hitpolicy;

import static org.camunda.bpm.dmn.engine.test.asserts.DmnAssertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * Tests the type of the result value of an evaluated decision table with
 * collect hit policy.
 *
 * @author Philipp Ossler
 */
public class CollectResultValueTypeTest extends DmnDecisionTest {

  public static final String COLLECT_SUM = "HitPolicyTest.collect.sum.single.dmn";
  public static final String COLLECT_MIN = "HitPolicyTest.collect.min.single.dmn";
  public static final String COLLECT_MAX = "HitPolicyTest.collect.max.single.dmn";
  public static final String COLLECT_COUNT = "HitPolicyTest.collect.count.single.dmn";

  @Test
  @DecisionResource(resource = COLLECT_SUM)
  public void collectSumHitPolicy() {
    DmnDecisionResult result = startDecision(10, 20, 50);
    assertThat(result).hasSingleOutput().hasSingleEntry(Variables.integerValue(80));

    result = startDecision(10L, 20L, 50L);
    assertThat(result).hasSingleOutput().hasSingleEntry(Variables.longValue(80L));

    result = startDecision(10.3, 20.5, 50.7);
    assertThat(result).hasSingleOutput().hasSingleEntry(Variables.doubleValue(81.5));
  }

  @Test
  @DecisionResource(resource = COLLECT_MIN)
  public void collectMinHitPolicy() {
    DmnDecisionResult result = startDecision(10, 20, 50);
    assertThat(result).hasSingleOutput().hasSingleEntry(Variables.integerValue(10));

    result = startDecision(10L, 20L, 50L);
    assertThat(result).hasSingleOutput().hasSingleEntry(Variables.longValue(10L));

    result = startDecision(10.3, 20.5, 50.7);
    assertThat(result).hasSingleOutput().hasSingleEntry(Variables.doubleValue(10.3));
  }

  @Test
  @DecisionResource(resource = COLLECT_MAX)
  public void collectMaxHitPolicy() {
    DmnDecisionResult result = startDecision(10, 20, 50);
    assertThat(result).hasSingleOutput().hasSingleEntry(Variables.integerValue(50));

    result = startDecision(10L, 20L, 50L);
    assertThat(result).hasSingleOutput().hasSingleEntry(Variables.longValue(50L));

    result = startDecision(10.3, 20.5, 50.7);
    assertThat(result).hasSingleOutput().hasSingleEntry(Variables.doubleValue(50.7));
  }

  @Test
  @DecisionResource(resource = COLLECT_COUNT)
  public void collectCountHitPolicy() {
    DmnDecisionResult result = startDecision(10, 20, 50);
    assertThat(result).hasSingleOutput().hasSingleEntry(Variables.integerValue(3));

    result = startDecision(10L, 20L, 50L);
    assertThat(result).hasSingleOutput().hasSingleEntry(Variables.integerValue(3));

    result = startDecision(10.3, 20.5, 50.7);
    assertThat(result).hasSingleOutput().hasSingleEntry(Variables.integerValue(3));
  }

  protected DmnDecisionResult startDecision(Object output1, Object output2, Object output3) {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input1", true);
    variables.put("input2", true);
    variables.put("input3", true);
    variables.put("output1", output1);
    variables.put("output2", output2);
    variables.put("output3", output3);

    return engine.evaluate(decision, variables);
  }

}
