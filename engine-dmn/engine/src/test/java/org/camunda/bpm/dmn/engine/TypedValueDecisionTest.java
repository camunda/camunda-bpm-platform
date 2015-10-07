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
package org.camunda.bpm.dmn.engine;

import static org.camunda.bpm.dmn.engine.test.asserts.DmnAssertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.junit.Test;

/**
 * @author Philipp Ossler
 */
public class TypedValueDecisionTest extends DmnDecisionTest {

  public static final String DMN_FILE = "org/camunda/bpm/dmn/engine/TypedValue.dmn";

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void decisionWithUntypedValueSatisfied() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("type", "untyped");
    variables.put("integer", 84);

    assertThat(engine).evaluates(decision, variables).hasResultValue(true);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void decisionWithUntypedValueNotSatisfied() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("type", "untyped");
    variables.put("integer", 21);

    assertThat(engine).evaluates(decision, variables).hasResultValue(false);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void decisionWithTypedValueSatisfied() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("type", "typed");
    variables.put("integer", 73);

    assertThat(engine).evaluates(decision, variables).hasResultValue(true);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void decisionWithTypedValueNotSatisfied() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("type", "typed");
    variables.put("integer", 41);

    assertThat(engine).evaluates(decision, variables).hasResultValue(false);
  }

}
