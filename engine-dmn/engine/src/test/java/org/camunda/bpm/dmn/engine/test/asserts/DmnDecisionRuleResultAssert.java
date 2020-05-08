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
package org.camunda.bpm.dmn.engine.test.asserts;

import org.assertj.core.api.AbstractMapAssert;
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DmnDecisionRuleResultAssert extends AbstractMapAssert<DmnDecisionRuleResultAssert, DmnDecisionRuleResult, String, Object> {

  public DmnDecisionRuleResultAssert(DmnDecisionRuleResult decisionRuleResult) {
    super(decisionRuleResult, DmnDecisionRuleResultAssert.class);
  }

  public DmnDecisionRuleResultAssert hasSingleEntry(Object value) {
    hasSize(1);
    containsValue(value);

    return this;
  }

  public DmnDecisionRuleResultAssert hasSingleEntryTyped(TypedValue value) {
    hasSize(1);

    TypedValue actualValue = actual.getSingleEntryTyped();
    failIfTypedValuesAreNotEqual(value, actualValue);

    return this;
  }

  protected void failIfTypedValuesAreNotEqual(TypedValue expectedValue, TypedValue actualValue) {
    if (actualValue == null && expectedValue != null) {
      failWithMessage("Expected value to be '%s' but was null", expectedValue);
    }
    else if (actualValue != null && !actualValue.equals(expectedValue)) {
      failWithMessage("Expected typed value to be '%s' but was '%s'", expectedValue, actualValue);
    }
  }

}
