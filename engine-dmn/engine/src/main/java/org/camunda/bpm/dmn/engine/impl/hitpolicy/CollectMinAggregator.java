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

package org.camunda.bpm.dmn.engine.impl.hitpolicy;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;

public class CollectMinAggregator extends AbstractDmnHitPolicyNumberAggregator {

  protected DmnDecisionResult aggregateIntegerValues(String name, List<Integer> intValues) {
    Integer min = Collections.min(intValues);
    return createAggregatedDecisionResult(name, min);
  }

  protected DmnDecisionResult aggregateLongValues(String name, List<Long> longValues) {
    Long min = Collections.min(longValues);
    return createAggregatedDecisionResult(name, min);
  }

  protected DmnDecisionResult aggregateDoubleValues(String name, List<Double> doubleValues) {
    Double min = Collections.min(doubleValues);
    return createAggregatedDecisionResult(name, min);
  }

}
