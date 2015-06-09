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

import org.assertj.core.api.Assertions;
import org.camunda.dmn.engine.DmnDecision;
import org.camunda.dmn.engine.DmnOutput;
import org.camunda.dmn.engine.DmnResult;

public class DmnAssertions extends Assertions {

  public static DmnDecisionAssertion assertThat(DmnDecision decision) {
    return new DmnDecisionAssertion(decision);
  }

  public static DmnResultAssertion assertThat(DmnResult result) {
    return new DmnResultAssertion(result);
  }

  public static DmnOutputAssertion assertThat(DmnOutput output) {
    return new DmnOutputAssertion(output);
  }

}
