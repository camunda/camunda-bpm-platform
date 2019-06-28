/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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

import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedDecisionRule;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedOutput;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;

public class DmnHitPolicyLogger extends DmnLogger {

  public DmnHitPolicyException uniqueHitPolicyOnlyAllowsSingleMatchingRule(List<DmnEvaluatedDecisionRule> matchingRules) {
    return new DmnHitPolicyException(exceptionMessage(
      "001",
      "Hit policy '{}' only allows a single rule to match. Actually match rules: '{}'.", HitPolicy.UNIQUE, matchingRules)
    );
  }

  public DmnHitPolicyException anyHitPolicyRequiresThatAllOutputsAreEqual(List<DmnEvaluatedDecisionRule> matchingRules) {
    return new DmnHitPolicyException(exceptionMessage(
      "002",
      "Hit policy '{}' only allows multiple matching rules with equal output. Matching rules: '{}'.", HitPolicy.ANY, matchingRules)
    );
  }

  public DmnHitPolicyException aggregationNotApplicableOnCompoundOutput(BuiltinAggregator aggregator, Map<String, DmnEvaluatedOutput> outputEntries) {
    return new DmnHitPolicyException(exceptionMessage(
      "003",
      "Unable to execute aggregation '{}' on compound decision output '{}'. Only one output entry allowed.", aggregator, outputEntries)
    );
  }

  public DmnHitPolicyException unableToConvertValuesToAggregatableTypes(List<TypedValue> values, Class<?>... targetClasses) {
    return new DmnHitPolicyException(exceptionMessage(
      "004",
      "Unable to convert value '{}' to a support aggregatable type '{}'.", values, targetClasses)
    );
  }

}
