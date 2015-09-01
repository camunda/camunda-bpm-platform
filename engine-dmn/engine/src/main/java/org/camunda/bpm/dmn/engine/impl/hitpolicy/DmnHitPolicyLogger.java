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

import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableRule;
import org.camunda.bpm.dmn.engine.DmnDecisionTableValue;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;

public class DmnHitPolicyLogger extends DmnLogger {

  public DmnHitPolicyException uniqueHitPolicyOnlyAllowsSingleMatchingRule(List<DmnDecisionTableRule> matchingRules) {
    return new DmnHitPolicyException(exceptionMessage("001", "Hit policy '{}' only allows a single rule to match. Actually match rules: '{}'.", HitPolicy.UNIQUE, matchingRules));
  }

  public DmnHitPolicyException anyHitPolicyRequiresThatAllOutputsAreEqual(List<DmnDecisionTableRule> matchingRules) {
    return new DmnHitPolicyException(exceptionMessage("002", "Hit policy '{}' only allows multiple matching rules with equal output. Matching rules: '{}'.", HitPolicy.ANY, matchingRules));
  }

  public DmnHitPolicyException noAggregatorFoundFor(BuiltinAggregator aggregation) {
    return new DmnHitPolicyException(exceptionMessage("003", "Unable to find hit policy aggregator '{}'.", aggregation));
  }

  public DmnHitPolicyException aggregationNotApplicableOnCompoundOutput(BuiltinAggregator aggregator, Map<String, DmnDecisionTableValue> outputs) {
    return new DmnHitPolicyException(exceptionMessage("004", "Unable to execute aggregation '{}' on compound decision output '{}'. Only one output value allowed.", aggregator, outputs));
  }

  public DmnHitPolicyException unableToConvertValueTo(Class<?> targetClass, Object value, NumberFormatException cause) {
    return new DmnHitPolicyException(exceptionMessage("005", "Unable to convert '' to ''.", value, targetClass), cause);
  }

  public DmnHitPolicyException unableToConvertValuesToAggregatableTypes(List<Object> values, Class<?>... targetClasses) {
    return new DmnHitPolicyException(exceptionMessage("006", "Unable to convert value '{}' to a support aggregatable type '{}'.", values, targetClasses));
  }
}
