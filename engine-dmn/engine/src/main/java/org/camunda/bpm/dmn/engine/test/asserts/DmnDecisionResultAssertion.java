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

package org.camunda.bpm.dmn.engine.test.asserts;

import org.assertj.core.api.AbstractAssert;
import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;

public class DmnDecisionResultAssertion extends AbstractAssert<DmnDecisionResultAssertion, DmnDecisionResult> {

  protected DmnDecisionResultAssertion(DmnDecisionResult actual) {
    super(actual, DmnDecisionResultAssertion.class);
  }

  public DmnDecisionResultAssertion hasSize(int expectedSize) {
    isNotNull();

    int actualSize = actual.size();

    if (actualSize != expectedSize) {
      if (expectedSize == 0) {
        failWithMessage("Expected result have no outputs but has <%s>", actualSize);
      }
      else {
        failWithMessage("Expected result to have <%s> outputs but has <%s>", expectedSize, actualSize);
      }
    }

    return this;
  }

  public DmnDecisionResultAssertion isEmpty() {
    isNotNull();
    return hasSize(0);
  }


  public DmnDecisionOutputAssertion hasSingleOutput() {
    isNotNull();
    hasSize(1);

    DmnDecisionOutput decisionOutput = actual.get(0);

    return new DmnDecisionOutputAssertion(decisionOutput);
  }

}
