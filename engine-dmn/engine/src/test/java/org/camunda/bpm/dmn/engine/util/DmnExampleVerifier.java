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

package org.camunda.bpm.dmn.engine.util;

import static org.camunda.bpm.dmn.engine.test.asserts.DmnAssertions.assertThat;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

public final class DmnExampleVerifier {

  public static final String EXAMPLE_DMN = "org/camunda/bpm/dmn/engine/Example.dmn";

  public static void assertExample(DmnEngine engine) {
    DmnDecision decision = engine.parseDecision(EXAMPLE_DMN);
    assertExample(engine, decision);
  }

  public static void assertExample(DmnEngine engine, DmnDecision decision) {
    VariableMap variables = Variables.createVariables();
    variables.put("status", "bronze");
    variables.put("sum", 200);

    assertThat(engine)
      .evaluates(decision, variables)
      .hasResult()
      .hasSingleOutput()
      .hasEntryWithValue("result", "notok")
      .hasEntryWithValue("reason", "work on your status first, as bronze you're not going to get anything");

    variables.put("status", "silver");

    assertThat(engine)
      .evaluates(decision, variables)
      .hasResult()
      .hasSingleOutput()
      .hasEntryWithValue("result", "ok")
      .hasEntryWithValue("reason", "you little fish will get what you want");

    variables.put("sum", 1200);

    assertThat(engine)
      .evaluates(decision, variables)
      .hasResult()
      .hasSingleOutput()
      .hasEntryWithValue("result", "notok")
      .hasEntryWithValue("reason", "you took too much man, you took too much!");

    variables.put("status", "gold");
    variables.put("sum", 200);

    assertThat(engine)
      .evaluates(decision, variables)
      .hasResult()
      .hasSingleOutput()
      .hasEntryWithValue("result", "ok")
      .hasEntryWithValue("reason", "you get anything you want");
  }

}
