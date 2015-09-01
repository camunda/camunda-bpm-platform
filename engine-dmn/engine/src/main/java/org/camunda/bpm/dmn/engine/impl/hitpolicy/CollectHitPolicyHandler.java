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

import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableRule;
import org.camunda.bpm.dmn.engine.DmnDecisionTableValue;
import org.camunda.bpm.dmn.engine.hitpolicy.DmnHitPolicyAggregator;
import org.camunda.bpm.dmn.engine.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableResultImpl;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;
import org.camunda.bpm.model.dmn.BuiltinAggregator;

public class CollectHitPolicyHandler implements DmnHitPolicyHandler {

  public static final DmnHitPolicyLogger LOG = DmnLogger.HIT_POLICY_LOGGER;

  public static final Map<BuiltinAggregator, DmnHitPolicyAggregator> AGGREGATORS;

  static {
    AGGREGATORS = new HashMap<BuiltinAggregator, DmnHitPolicyAggregator>();
    AGGREGATORS.put(BuiltinAggregator.SUM, new CollectSumAggregator());
    AGGREGATORS.put(BuiltinAggregator.MIN, new CollectMinAggregator());
    AGGREGATORS.put(BuiltinAggregator.MAX, new CollectMaxAggregator());
    AGGREGATORS.put(BuiltinAggregator.COUNT, new CollectCountAggregator());
  }

  public DmnDecisionTableResult apply(DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    BuiltinAggregator aggregation = decisionTable.getAggregation();
    if (aggregation != null) {
      aggregateDecisionTableResult(aggregation, (DmnDecisionTableResultImpl) decisionTableResult);
    }
    return decisionTableResult;
  }

  protected void aggregateDecisionTableResult(BuiltinAggregator aggregation, DmnDecisionTableResultImpl decisionTableResult) {
    DmnHitPolicyAggregator aggregator = AGGREGATORS.get(aggregation);
    if (aggregator != null) {
      List<DmnDecisionTableRule> matchingRules = decisionTableResult.getMatchingRules();
      List<Object> outputValues = collectSingleValues(aggregation, matchingRules);
      String outputName = getDecisionOutputName(matchingRules);
      Object outputValue = aggregator.aggregate(outputValues);
      decisionTableResult.setCollectResultName(outputName);
      decisionTableResult.setCollectResultValue(outputValue);
    }
    else {
      throw LOG.noAggregatorFoundFor(aggregation);
    }
  }

  protected List<Object> collectSingleValues(BuiltinAggregator aggregator, List<DmnDecisionTableRule> matchingRules) {
    List<Object> values = new ArrayList<Object>();
    for (DmnDecisionTableRule matchingRule : matchingRules) {
      Map<String, DmnDecisionTableValue> outputs = matchingRule.getOutputs();
      if (outputs.isEmpty()) {
        continue; // skip empty output
      }
      else if (outputs.size() == 1) {
        values.add(outputs.values().iterator().next().getValue());
      }
      else {
        throw LOG.aggregationNotApplicableOnCompoundOutput(aggregator, outputs);
      }
    }
    return values;
  }

  protected String getDecisionOutputName(List<DmnDecisionTableRule> matchingRules) {
    for (DmnDecisionTableRule matchingRule : matchingRules) {
      Map<String, DmnDecisionTableValue> outputs = matchingRule.getOutputs();
      if (!outputs.isEmpty()) {
        return outputs.values().iterator().next().getOutputName();
      }
    }
    return null;
  }

}
