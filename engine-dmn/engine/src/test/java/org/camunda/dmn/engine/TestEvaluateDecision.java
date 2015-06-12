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
    assertThat(engine)
      .evaluates(decision)
      .hasResult("ok");
  }

  @Test
  @DecisionResource(resource = ONE_RULE_DMN)
  public void shouldEvaluateSingleRule() {
    assertThat(engine)
      .evaluates(decision)
      .withContext("input", "ok")
      .hasResult("ok");

    assertThat(engine)
      .evaluates(decision)
      .withContext("input", "ok")
      .hasResult(null, "ok");

    assertThat(engine)
      .evaluates(decision)
      .withContext("input", "notok")
      .hasEmptyResult();
  }

  @Test
  @DecisionResource(resource = EXAMPLE_DMN)
  public void shouldEvaluateExample() {
    assertThat(engine)
      .evaluates(decision)
      .withContext()
        .setVariable("status", "bronze")
        .setVariable("sum", 200)
        .build()
      .hasResult()
        .hasSingleOutput()
          .hasEntry("result", "notok")
          .hasEntry("reason", "work on your status first, as bronze you're not going to get anything");

    assertThat(engine)
      .evaluates(decision)
      .withContext()
        .setVariable("status", "silver")
        .setVariable("sum", 200)
        .build()
      .hasResult()
        .hasSingleOutput()
          .hasEntry("result", "ok")
          .hasEntry("reason", "you little fish will get what you want");

    assertThat(engine)
      .evaluates(decision)
      .withContext()
        .setVariable("status", "silver")
        .setVariable("sum", 1200)
        .build()
      .hasResult()
        .hasSingleOutput()
          .hasEntry("result", "notok")
          .hasEntry("reason", "you took too much man, you took too much!");

    assertThat(engine)
      .evaluates(decision)
      .withContext()
        .setVariable("status", "gold")
        .setVariable("sum", 200)
        .build()
      .hasResult()
        .hasSingleOutput()
          .hasEntry("result", "ok")
          .hasEntry("reason", "you get anything you want");
  }

}
