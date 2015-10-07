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

package org.camunda.bpm.dmn.engine.test.asserts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;

public class DmnEngineAssertion extends AbstractAssert<DmnEngineAssertion, DmnEngine> {

  protected DmnDecision decision;
  protected Map<String, Object> variables = new HashMap<String, Object>();

  protected DmnEngineAssertion(DmnEngine actual) {
    super(actual, DmnEngineAssertion.class);
  }

  public DmnEngineAssertion evaluates(DmnDecision decision, Map<String, Object> variables) {
    isNotNull();

    if (decision == null) {
      failWithMessage("Expected decision not to be null.");
    }

    this.decision = decision;
    this.variables = variables;

    return this;
  }

  public DmnEngineAssertion evaluates(DmnDecisionModel decisionModel, Map<String, Object> variables) {
    List<DmnDecision> decisions = decisionModel.getDecisions();

    if (decisions.isEmpty()) {
      failWithMessage("Expected at least one decision in model.");
    }

    return evaluates(decisions.get(0), variables);
  }

  public DmnEngineAssertion evaluates(DmnDecisionModel decisionModel, String decisionKey, Map<String, Object> variables) {
    DmnDecision decision = decisionModel.getDecision(decisionKey);
    return evaluates(decision, variables);
  }

  public DmnDecisionResultAssertion hasResult() {
    isNotNull();

    if (decision == null) {
      failWithMessage("Expected decision not to be null.");
    }

    DmnDecisionResult result = actual.evaluate(decision, variables);

    return new DmnDecisionResultAssertion(result);
  }

  public DmnDecisionOutputAssertion hasResultValue(Object value) {
    isNotNull();

    if (decision == null) {
      failWithMessage("Expected decision not to be null.");
    }

    DmnDecisionResult result = actual.evaluate(decision, variables);

    DmnDecisionResultAssertion resultAssertion = new DmnDecisionResultAssertion(result);
    return resultAssertion.hasSingleOutput().hasSingleEntryValue(value);
  }

  public DmnDecisionResultAssertion hasEmptyResult() {
    isNotNull();

    return hasResult().isEmpty();
  }

  public <T extends Exception> ThrowableAssert thrown(Class<T> expectedException) {
    isNotNull();

    if (decision == null) {
      failWithMessage("Expected decision not to be null.");
    }

    try {
      actual.evaluate(decision, variables);
      failWithMessage("<%s> expected but no exception was thrown",expectedException.getName());
    } catch (Exception e) {
      if(expectedException.isInstance(e)){
        return (ThrowableAssert) Assertions.assertThat(e);
      } else {
        failWithMessage("<%s> expected but was <%s>", expectedException, e.getClass());
      }
    }
    return null;
  }

}
