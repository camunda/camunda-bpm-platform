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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.hitpolicy.DmnHitPolicyAggregator;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;

public class CollectHitPolicyHandler extends AbstractDmnHitPolicyHandler {

  public static final HitPolicy HIT_POLICY = HitPolicy.COLLECT;

  public static final Map<BuiltinAggregator, DmnHitPolicyAggregator> AGGREGATORS;

  static {
    AGGREGATORS = new HashMap<BuiltinAggregator, DmnHitPolicyAggregator>();
    AGGREGATORS.put(BuiltinAggregator.SUM, new CollectSumAggregator());
    AGGREGATORS.put(BuiltinAggregator.MIN, new CollectMinAggregator());
    AGGREGATORS.put(BuiltinAggregator.MAX, new CollectMaxAggregator());
    AGGREGATORS.put(BuiltinAggregator.COUNT, new CollectCountAggregator());
  }

  public HitPolicy getHandledHitPolicy() {
    return HIT_POLICY;
  }

  public boolean handlesHitPolicy(HitPolicy hitPolicy) {
    return HIT_POLICY.equals(hitPolicy);
  }

  public List<DmnRule> filterMatchingRules(DmnDecisionTable decisionTable, List<DmnRule> matchingRules) {
    return matchingRules;
  }

  public DmnDecisionResult getDecisionResult(DmnDecisionTable decisionTable, List<DmnDecisionOutput> decisionOutputs) {
    BuiltinAggregator aggregation = decisionTable.getAggregation();
    if (aggregation != null) {
      return getAggregatedDecisionResult(decisionTable, aggregation, decisionOutputs);
    }
    else {
      return super.getDecisionResult(decisionTable, decisionOutputs);
    }
  }

  protected DmnDecisionResult getAggregatedDecisionResult(DmnDecisionTable decisionTable, BuiltinAggregator aggregation, List<DmnDecisionOutput> decisionOutputs) {
    DmnHitPolicyAggregator aggregator = AGGREGATORS.get(aggregation);
    List<Object> outputValues = collectSingleValues(decisionOutputs);
    String outputName = getDecisionOutputName(decisionOutputs);
    if (aggregator != null) {
      return aggregator.aggregate(outputName, outputValues);
    }
    else {
      throw LOG.noAggregatorFoundFor(aggregation);
    }
  }

  protected List<Object> collectSingleValues(List<DmnDecisionOutput> decisionOutputs) {
    List<Object> values = new ArrayList<Object>();
    for (DmnDecisionOutput decisionOutput : decisionOutputs) {
      if (decisionOutput.isEmpty()) {
        continue; // skip empty output
      }
      else if (decisionOutput.size() == 1) {
        values.add(decisionOutput.getValue());
      }
      else {
        throw LOG.countAggregationNotApplicableOnCompoundOutput(decisionOutput);
      }
    }

    return values;
  }

  protected String getDecisionOutputName(List<DmnDecisionOutput> decisionOutputs) {
    for (DmnDecisionOutput decisionOutput : decisionOutputs) {
      if (!decisionOutput.isEmpty()) {
        return decisionOutput.keySet().iterator().next();
      }
    }
    return null;
  }

}
