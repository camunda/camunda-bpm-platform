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
import org.camunda.dmn.engine.DmnEngineException;
import org.camunda.dmn.engine.DmnOutput;

public class DmnOutputAssertion extends AbstractAssert<DmnOutputAssertion, DmnOutput> {

  public DmnOutputAssertion(DmnOutput actual) {
    super(actual, DmnOutputAssertion.class);
  }

  public DmnOutputAssertion hasSingleValue() {
    isNotNull();

    int componentsCount = actual.getComponents().size();
    if (componentsCount != 1) {
      failWithMessage("Expected output to have exact one component but has <%s>", componentsCount);
    }

    return this;
  }

  public DmnOutputAssertion hasSingleValue(Object expectedValue) {
    hasSingleValue();

    Object actualValue = actual.getValue();

    if (actualValue == null && expectedValue != null) {
      failWithMessage("Expected output value to be <%s> but was null", expectedValue);
    }
    else if (actualValue != null && !actualValue.equals(expectedValue)) {
      failWithMessage("Expected output value <%s> to be equal to <%s>", actualValue, expectedValue);
    }

    return this;
  }

  public DmnOutputAssertion hasSingleValue(String name, Object expectedValue) {
    isNotNull();

    Object actualValue = null;

    try {
      actualValue = actual.getValue(name);
    }
    catch (DmnEngineException e) {
      failWithMessage("Expected result to have an output with name <%s> but has not", name);
    }

    if (actualValue == null && expectedValue != null) {
      failWithMessage("Expected output value to be <%s> but was null", expectedValue);
    }
    else if (actualValue != null && !actualValue.equals(expectedValue)) {
      failWithMessage("Expected output value <%s> to be equal to <%s>", actualValue, expectedValue);
    }

    return this;
  }


}
