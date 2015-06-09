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

import java.util.List;

import org.assertj.core.api.AbstractAssert;
import org.camunda.dmn.engine.DmnOutput;
import org.camunda.dmn.engine.DmnResult;

public class DmnResultAssertion extends AbstractAssert<DmnResultAssertion, DmnResult> {

  protected DmnResultAssertion(DmnResult actual) {
    super(actual, DmnResultAssertion.class);
  }

  public DmnResultAssertion hasSingleOutput() {
    isNotNull();

    int outputCount = actual.getOutputs().size();

    if (outputCount != 1) {
      failWithMessage("Expected result to have exact one output but has <%s>", outputCount);
    }

    return this;
  }

  public DmnResultAssertion hasSingleOutput(Object value) {
    isNotNull();
    hasSingleOutput();

    DmnOutput output = actual.getOutputs().get(0);
    DmnOutputAssertion outputAssertion = new DmnOutputAssertion(output);

    outputAssertion.hasSingleValue(value);

    return this;
  }

  public DmnResultAssertion hasSingleOutput(String name, Object value) {
    isNotNull();
    hasSingleOutput();

    DmnOutput output = actual.getOutputs().get(0);
    DmnOutputAssertion outputAssertion = new DmnOutputAssertion(output);

    outputAssertion.hasSingleValue(name, value);

    return this;
  }

  public DmnResultAssertion isEmpty() {
    isNotNull();

    hasSize(0);

    return this;
  }


  public DmnResultAssertion hasSize(int expectedSize) {
    isNotNull();

    int actualSize = actual.getOutputs().size();

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
}
