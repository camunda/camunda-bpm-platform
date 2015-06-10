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

package org.camunda.dmn.engine;

import static org.camunda.dmn.engine.test.asserts.DmnAssertions.assertThat;

import org.camunda.dmn.engine.test.DecisionResource;
import org.camunda.dmn.engine.test.DmnDecisionTest;
import org.junit.Test;

public class TestEvaluateDecision extends DmnDecisionTest {

  @Test
  @DecisionResource(resource = NO_INPUT_DMN)
  public void shouldEvaluateRuleWithoutInput() {
    assertThat(decision)
      .hasResult()
      .hasSingleOutput("ok");
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldEvaluateSingleRule() {
    assertThat(decision)
      .withContext("Input", "ok")
      .hasResult("ok");

    assertThat(decision)
      .withContext("Input", "ok")
      .hasResult("Result", "ok");

    assertThat(decision)
      .withContext("Input", "notok")
      .hasEmptyResult();
  }

  @Test
  @DecisionResource(resource = EXAMPLE_DMN)
  public void shouldEvaluateExample() {
    assertThat(decision)
      .withContext()
        .addVariable("CustomerStatus", "bronze")
        .addVariable("OrderSum", 200)
        .build()
      .hasResult("CheckResult", "notok")
      .hasResult("Reason", "work on your status first, as bronze you're not going to get anything");

    assertThat(decision)
      .withContext()
        .addVariable("CustomerStatus", "silver")
        .addVariable("OrderSum", 200)
        .build()
      .hasResult("CheckResult", "ok")
      .hasResult("Reason", "you little fish will get what you want");

    assertThat(decision)
      .withContext()
        .addVariable("CustomerStatus", "silver")
        .addVariable("OrderSum", 1200)
        .build()
      .hasResult("CheckResult", "notok")
      .hasResult("Reason", "you took too much man, you took too much!");

    assertThat(decision)
      .withContext()
        .addVariable("CustomerStatus", "gold")
        .addVariable("OrderSum", 200)
        .build()
      .hasResult("CheckResult", "ok")
      .hasResult("Reason", "you get anything you want");
  }

}
