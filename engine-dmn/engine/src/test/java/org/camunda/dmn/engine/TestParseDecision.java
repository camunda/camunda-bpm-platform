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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.InputStream;

import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.commons.utils.IoUtil;
import org.camunda.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestParseDecision {

  public static final String NO_DECISION_DMN = "org/camunda/dmn/engine/NoDecision.dmn";
  public static final String NO_INPUT_DMN = "org/camunda/dmn/engine/NoInput.dmn";
  public static final String INVOCATION_DECISION_DMN = "org/camunda/dmn/engine/InvocationDecision.dmn";

  protected static DmnEngine dmnEngine;

  @BeforeClass
  public static void createEngine() {
    dmnEngine = new DmnEngineConfigurationImpl().buildEngine();
  }

  @Test
  public void shouldParseDecisionFromFile() {
    DmnDecision decision = dmnEngine.parseDecision(NO_INPUT_DMN);
    assertDecision(decision);

    decision = dmnEngine.parseDecision(NO_INPUT_DMN, "decision");
    assertDecision(decision);
  }

  @Test
  public void shouldParseDecisionFromInputStream() {
    InputStream inputStream = IoUtil.fileAsStream(NO_INPUT_DMN);

    DmnDecision decision = dmnEngine.parseDecision(inputStream);
    assertDecision(decision);

    inputStream = IoUtil.fileAsStream(NO_INPUT_DMN);
    decision = dmnEngine.parseDecision(inputStream, "decision");
    assertDecision(decision);
  }

  @Test
  public void shouldParseDecisionFromModelInstance() {
    InputStream inputStream = IoUtil.fileAsStream(NO_INPUT_DMN);
    DmnModelInstance modelInstance = Dmn.readModelFromStream(inputStream);

    DmnDecision decision = dmnEngine.parseDecision(modelInstance);
    assertDecision(decision);

    decision = dmnEngine.parseDecision(modelInstance, "decision");
    assertDecision(decision);
  }

  @Test
  public void shouldFailIfModelDoesNotContainDecision() {
    try {
      dmnEngine.parseDecision(NO_DECISION_DMN);
      fail("Exception expected");
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageContaining("Unable to find any decision")
        .hasMessageContaining("NoDecision.dmn");
    }
  }

  @Test
  public void shouldFailIfDecisionKeyIsUnknown() {
    try {
      dmnEngine.parseDecision(NO_INPUT_DMN, "unknownDecision");
      fail("Exception expected");
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageContaining("Unable to find decision")
        .hasMessageContaining("unknownDecision")
        .hasMessageContaining("NoInput.dmn");
    }
  }

  @Test
  public void shouldFailIfNoSupportedDecisionIsFound() {
    try {
      dmnEngine.parseDecision(INVOCATION_DECISION_DMN);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasMessageContaining("Unable to find any decision")
        .hasMessageContaining("InvocationDecision.dmn");
    }
  }

  protected void assertDecision(DmnDecision decision) {
    assertThat(decision).isNotNull();
    assertThat(decision.getKey()).isEqualTo("decision");
  }

}
