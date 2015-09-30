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

import static org.camunda.bpm.dmn.engine.test.asserts.DmnAssertions.assertThat;

import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnDecisionTest;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Test;

public class FeelTest extends DmnDecisionTest {

  public static final String FEEL_TEST_DMN = "FeelTest.dmn";
  protected VariableMap variables;

  @Before
  public void initVariables() {
    variables = Variables.createVariables();
  }

  @Test
  @DecisionResource(resource = FEEL_TEST_DMN)
  public void testStringVariable() {
    variables.putValue("stringInput", "camunda");
    variables.putValue("numberInput", 13.37);
    variables.putValue("booleanInput", true);

    assertThat(engine).evaluates(decision, variables).hasResultValue(true);
  }

}
