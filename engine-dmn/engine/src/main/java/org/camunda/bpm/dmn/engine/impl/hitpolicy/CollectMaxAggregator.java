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

public class CollectMaxAggregator extends AbstractDmnHitPolicyNumberAggregator {

  protected Object aggregateIntegerValues(List<Integer> intValues) {
    return Collections.max(intValues);
  }

  protected Object aggregateLongValues(List<Long> longValues) {
    return Collections.max(longValues);
  }

  protected Object aggregateDoubleValues(List<Double> doubleValues) {
    return Collections.max(doubleValues);
  }

}
