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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.assertj.core.api.AbstractAssert;
import org.camunda.dmn.engine.DmnDecision;
import org.camunda.dmn.engine.DmnDecisionContext;
import org.camunda.dmn.engine.DmnDecisionContextBuilder;
import org.camunda.dmn.engine.DmnResult;
import org.camunda.dmn.engine.impl.DecisionContextImpl;

public class DmnDecisionAssertion extends AbstractAssert<DmnDecisionAssertion, DmnDecision> {

  protected DmnDecisionContext context = new DecisionContextImpl();

  protected DmnDecisionAssertion(DmnDecision actual) {
    super(actual, DmnDecisionAssertion.class);
  }

  public DmnDecisionAssertion withContext(DmnDecisionContext context) {
    this.context = context;
    return this;
  }

  public DmnDecisionAssertion withContext(Map<String, Object> variables) {
    context = new DecisionContextImpl();
    context.setVariables(variables);
    return this;
  }

  public DmnDecisionAssertion withContext(String name, Object value, Object... additionalVariablePairs) {
    context = new DecisionContextImpl();
    context.addVariable(name, value);
    if (additionalVariablePairs.length % 2 != 0) {
      throw new DmnAssertionException("Additional context variables have to specified with name and value");
    }

    Iterator<Object> variableIterator = Arrays.asList(additionalVariablePairs).iterator();
    while (variableIterator.hasNext()) {
      Object variableName =  variableIterator.next();
      Object variableValue = variableIterator.next();
      if (!(variableName instanceof String)) {
        throw new DmnAssertionException("Additional context variables have to specify a name as String, was " + variableName + ".");
      }
      else {
        context.addVariable((String) variableName, variableValue);
      }
    }

    return this;
  }

  public DmnDecisionContextBuilder<DmnDecisionAssertion> withContext() {
    return new DmnDecisionAssertionContextBuilder(this);
  }

  public DmnDecisionAssertion hasEmptyResult() {
    isNotNull();

    DmnResultAssertion resultAssertion = getResultAssertion();

    resultAssertion.isEmpty();

    return this;
  }

  public DmnResultAssertion hasResult() {
    isNotNull();

    DmnResult result = evaluate();

    return new DmnResultAssertion(result);
  }

  public DmnDecisionAssertion hasResult(Object value) {
    hasResult().hasSingleOutput(value);
    return this;
  }

  public DmnDecisionAssertion hasResult(String name, Object value) {
    hasResult().hasSingleOutput(name, value);
    return this;
  }

  protected DmnResult evaluate() {
    return actual.evaluate(context);
  }

  protected DmnResultAssertion getResultAssertion() {
    DmnResult result = evaluate();
    return new DmnResultAssertion(result);
  }

  protected class DmnDecisionAssertionContextBuilder implements DmnDecisionContextBuilder<DmnDecisionAssertion> {

    protected DmnDecisionAssertion assertion;
    protected DmnDecisionContext context;

    public DmnDecisionAssertionContextBuilder(DmnDecisionAssertion assertion) {
      this.assertion = assertion;
      this.context = new DecisionContextImpl();
    }

    public DmnDecisionContextBuilder<DmnDecisionAssertion> addVariable(String name, Object value) {
      context.addVariable(name, value);
      return this;
    }

    public DmnDecisionAssertion build() {
      return assertion.withContext(context);
    }

  }

}
