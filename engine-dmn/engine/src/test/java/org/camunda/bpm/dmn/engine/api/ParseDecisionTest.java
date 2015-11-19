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
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformException;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.commons.utils.IoUtil;
import org.junit.Test;

public class ParseDecisionTest extends DmnEngineTest {

  public static final String NO_DECISION_DMN = "org/camunda/bpm/dmn/engine/api/NoDecision.dmn";
  public static final String NO_INPUT_DMN = "org/camunda/bpm/dmn/engine/api/NoInput.dmn";
  public static final String INVOCATION_DECISION_DMN = "org/camunda/bpm/dmn/engine/api/InvocationDecision.dmn";
  public static final String MISSING_DECISION_ID_DMN = "org/camunda/bpm/dmn/engine/api/MissingIds.missingDecisionId.dmn";
  public static final String MISSING_INPUT_ID_DMN = "org/camunda/bpm/dmn/engine/api/MissingIds.missingInputId.dmn";
  public static final String MISSING_OUTPUT_ID_DMN = "org/camunda/bpm/dmn/engine/api/MissingIds.missingOutputId.dmn";
  public static final String MISSING_RULE_ID_DMN = "org/camunda/bpm/dmn/engine/api/MissingIds.missingRuleId.dmn";
  public static final String MISSING_COMPOUND_OUTPUT_NAME_DMN = "org/camunda/bpm/dmn/engine/api/CompoundOutputs.noName.dmn";
  public static final String DUPLICATE_COMPOUND_OUTPUT_NAME_DMN = "org/camunda/bpm/dmn/engine/api/CompoundOutputs.duplicateName.dmn";

  @Test
  public void shouldParseDecisionFromInputStream() {
    InputStream inputStream = IoUtil.fileAsStream(NO_INPUT_DMN);
    decision = dmnEngine.parseDecision("decision", inputStream);
    assertDecision(decision);
  }

  @Test
  public void shouldParseDecisionFromModelInstance() {
    InputStream inputStream = IoUtil.fileAsStream(NO_INPUT_DMN);
    DmnModelInstance modelInstance = Dmn.readModelFromStream(inputStream);

    decision = dmnEngine.parseDecision("decision", modelInstance);
    assertDecision(decision);
  }

  @Test
  public void shouldFailIfDecisionKeyIsUnknown() {
    try {
      parseDecisionFromFile("unknownDecision", NO_INPUT_DMN);
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      Assertions.assertThat(e)
        .hasMessageStartingWith("DMN-01001")
        .hasMessageContaining("Unable to find decision")
        .hasMessageContaining("unknownDecision");
    }
  }

  @Test
  public void shouldFailIfDecisionIdIsMissing() {
    try {
      parseDecisionsFromFile(MISSING_DECISION_ID_DMN);
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasCauseExactlyInstanceOf(DmnTransformException.class)
        .hasMessageStartingWith("DMN-02004")
        .hasMessageContaining("DMN-02010")
        .hasMessageContaining("Decision With Missing Id");
    }
  }

  @Test
  public void shouldFailIfInputIdIsMissing() {
    try {
      parseDecisionsFromFile(MISSING_INPUT_ID_DMN);
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasCauseExactlyInstanceOf(DmnTransformException.class)
        .hasMessageStartingWith("DMN-02004")
        .hasMessageContaining("DMN-02011")
        .hasMessageContaining("Decision With Missing Input Id");
    }
  }

  @Test
  public void shouldFailIfOutputIdIsMissing() {
    try {
      parseDecisionsFromFile(MISSING_OUTPUT_ID_DMN);
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasCauseExactlyInstanceOf(DmnTransformException.class)
        .hasMessageStartingWith("DMN-02004")
        .hasMessageContaining("DMN-02012")
        .hasMessageContaining("Decision With Missing Output Id");
    }
  }

  @Test
  public void shouldFailIfRuleIdIsMissing() {
    try {
      parseDecisionsFromFile(MISSING_RULE_ID_DMN);
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasCauseExactlyInstanceOf(DmnTransformException.class)
        .hasMessageStartingWith("DMN-02004")
        .hasMessageContaining("DMN-02013")
        .hasMessageContaining("Decision With Missing Rule Id");
    }
  }

  @Test
  public void shouldFailIfCompoundOutputsNameIsMissing() {
    try {
      parseDecisionsFromFile(MISSING_COMPOUND_OUTPUT_NAME_DMN);
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasCauseExactlyInstanceOf(DmnTransformException.class)
        .hasMessageStartingWith("DMN-02004")
        .hasMessageContaining("DMN-02008")
        .hasMessageContaining("does not have an output name");
    }
  }

  @Test
  public void shouldFailIfCompoundOutputsHaveDuplicateName() {
    try {
      parseDecisionsFromFile(DUPLICATE_COMPOUND_OUTPUT_NAME_DMN);
      failBecauseExceptionWasNotThrown(DmnTransformException.class);
    }
    catch (DmnTransformException e) {
      assertThat(e)
        .hasCauseExactlyInstanceOf(DmnTransformException.class)
        .hasMessageStartingWith("DMN-02004")
        .hasMessageContaining("DMN-02009")
        .hasMessageContaining("has a compound output but name of output")
        .hasMessageContaining("is duplicate");
    }
  }

  protected void assertDecision(DmnDecision decision) {
    assertThat(decision).isNotNull();
    assertThat(decision.getKey()).isEqualTo("decision");
  }

}
