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
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DmnDecisionOutputAssertion extends AbstractAssert<DmnDecisionOutputAssertion, DmnDecisionOutput> {

  public DmnDecisionOutputAssertion(DmnDecisionOutput actual) {
    super(actual, DmnDecisionOutputAssertion.class);
  }

  public DmnDecisionOutputAssertion hasSingleEntry() {
    isNotNull();

    int entriesCount = actual.size();
    if (entriesCount != 1) {
      failWithMessage("Expected output to have exact one entry but has <%s>", entriesCount);
    }

    return this;
  }

  public DmnDecisionOutputAssertion hasSingleEntryValue(Object expectedValue) {
    hasSingleEntry();

    assertEquals(expectedValue, actual.getSingleValue());

    return this;
  }

  public DmnDecisionOutputAssertion hasSingleEntry(TypedValue expectedValue) {
    hasSingleEntry();

    assertEquals(expectedValue, actual.getSingleValue());

    return this;
  }

  private void assertEquals(Object expectedValue, Object actualValue) {
    if (actualValue == null && expectedValue != null) {
      failWithMessage("Expected output value to be <%s> but was null", expectedValue);
    }
    else if (actualValue != null && !actualValue.equals(expectedValue)) {
      failWithMessage("Expected output value <%s> to be equal to <%s>", actualValue, expectedValue);
    }
  }

  public DmnDecisionOutputAssertion hasEntryWithValue(String name, Object expectedValue) {
    isNotNull();

    assertEquals(expectedValue, actual.get(name));

    return this;
  }

}
