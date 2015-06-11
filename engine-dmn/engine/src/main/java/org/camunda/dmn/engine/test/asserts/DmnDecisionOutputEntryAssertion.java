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

package org.camunda.dmn.engine.test.asserts;

import org.assertj.core.api.AbstractAssert;
import org.camunda.dmn.engine.DmnDecisionOutputEntry;

public class DmnDecisionOutputEntryAssertion extends AbstractAssert<DmnDecisionOutputEntryAssertion, DmnDecisionOutputEntry> {

  protected DmnDecisionOutputEntryAssertion(DmnDecisionOutputEntry actual) {
    super(actual, DmnDecisionOutputEntryAssertion.class);
  }

  public DmnDecisionOutputEntryAssertion hasName(String expectedName) {
    isNotNull();

    String actualName = actual.getName();

    if (actualName == null && expectedName != null) {
      failWithMessage("Expected name to be <%s> but was null", expectedName);
    }
    if (actualName != null && actualName.equals(expectedName)) {
      failWithMessage("Expected name to be <%s> but was <%s>", expectedName, actualName);
    }

    return this;
  }

  public DmnDecisionOutputEntryAssertion hasValue(Object expectedValue) {
    isNotNull();

    Object actualValue = actual.getValue();

    if (actualValue == null && expectedValue != null) {
      failWithMessage("Expected value to be <%s> but was null", expectedValue);
    }
    else if(actualValue != null && !actualValue.equals(expectedValue)) {
      failWithMessage("Expected value to be <%s> but was <%s>", expectedValue, actualValue);
    }

    return this;
  }

}
