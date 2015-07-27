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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.AbstractAssert;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.context.DmnContextFactory;
import org.camunda.bpm.dmn.engine.context.DmnDecisionContext;
import org.camunda.bpm.dmn.engine.context.DmnDecisionContextBuilder;
import org.camunda.bpm.dmn.engine.context.DmnScriptContext;
import org.camunda.bpm.dmn.engine.context.DmnVariableContext;
import org.camunda.bpm.dmn.engine.DmnDecision;

public class DmnEngineAssertion extends AbstractAssert<DmnEngineAssertion, DmnEngine> {

  protected DmnDecision decision;
  protected DmnDecisionContext decisionContext;

  protected DmnEngineAssertion(DmnEngine actual) {
    super(actual, DmnEngineAssertion.class);
  }

  public DmnEngineAssertion evaluates(DmnDecision decision) {
    isNotNull();

    if (decision == null) {
      failWithMessage("Expected decision not to be null.");
    }

    this.decision = decision;

    return this;
  }

  public DmnEngineAssertion evaluates(DmnDecisionModel decisionModel) {
    List<DmnDecision> decisions = decisionModel.getDecisions();

    if (decisions.isEmpty()) {
      failWithMessage("Expected at least one decision in model.");
    }

    return evaluates(decisions.get(0));
  }

  public DmnEngineAssertion evaluates(DmnDecisionModel decisionModel, String decisionKey) {
    DmnDecision decision = decisionModel.getDecision(decisionKey);
    return evaluates(decision);
  }

  public DmnEngineAssertionContextBuilder withContext() {
    DmnContextFactory contextFactory = actual.getConfiguration().getDmnContextFactory();
    DmnDecisionContext decisionContext = contextFactory.createDecisionContext();
    return new DmnEngineAssertionContextBuilder(this, decisionContext);
  }

  public DmnEngineAssertion withContext(DmnDecisionContext decisionContext) {
    isNotNull();

    if (decisionContext == null) {
      failWithMessage("Expected decision context not to be null.");
    }

    this.decisionContext = decisionContext;

    return this;
  }

  public DmnEngineAssertion withContext(String name, Object value, Object... additionalVariablePairs) {
    isNotNull();

    if (additionalVariablePairs.length % 2 != 0) {
      throw new DmnAssertionException("Additional context variables have to specified with name and value");
    }

    DmnEngineAssertionContextBuilder contextBuilder = withContext();
    contextBuilder.setVariable(name, value);

    Iterator<Object> variableIterator = Arrays.asList(additionalVariablePairs).iterator();
    while (variableIterator.hasNext()) {
      Object variableName =  variableIterator.next();
      Object variableValue = variableIterator.next();
      if (!(variableName instanceof String)) {
        throw new DmnAssertionException("Additional context variables have to specify a name as String, was " + variableName + ".");
      }
      else {
        contextBuilder.setVariable((String) variableName, variableValue);
      }
    }

    return contextBuilder.build();
  }

  public DmnDecisionResultAssertion hasResult() {
    isNotNull();

    if (decision == null) {
      failWithMessage("Expected decision not to be null.");
    }

    if (decisionContext == null) {
      decisionContext = actual.getConfiguration().getDmnContextFactory().createDecisionContext();
    }

    DmnDecisionResult result = decision.evaluate(decisionContext);

    return new DmnDecisionResultAssertion(result);
  }

  public DmnDecisionOutputAssertion hasResult(Object value) {
    isNotNull();

    if (decision == null) {
      failWithMessage("Expected decision not to be null.");
    }

    if (decisionContext == null) {
      decisionContext = actual.getConfiguration().getDmnContextFactory().createDecisionContext();
    }

    DmnDecisionResult result = decision.evaluate(decisionContext);

    DmnDecisionResultAssertion resultAssertion = new DmnDecisionResultAssertion(result);
    return resultAssertion.hasSingleOutput().hasSingleEntry(value);
  }

  public DmnDecisionOutputAssertion hasResult(String name, String value) {
    isNotNull();

    if (decision == null) {
      failWithMessage("Expected decision not to be null.");
    }

    if (decisionContext == null) {
      decisionContext = actual.getConfiguration().getDmnContextFactory().createDecisionContext();
    }

    DmnDecisionResult result = decision.evaluate(decisionContext);

    DmnDecisionResultAssertion resultAssertion = new DmnDecisionResultAssertion(result);
    return resultAssertion.hasSingleOutput().hasSingleEntry(name, value);
  }

  public DmnDecisionResultAssertion hasEmptyResult() {
    isNotNull();

    return hasResult().isEmpty();
  }


  public class DmnEngineAssertionContextBuilder implements DmnDecisionContextBuilder<DmnEngineAssertion> {

    protected DmnEngineAssertion assertion;
    protected DmnDecisionContext decisionContext;

    public DmnEngineAssertionContextBuilder(DmnEngineAssertion assertion, DmnDecisionContext decisionContext) {
      this.assertion = assertion;
      this.decisionContext = decisionContext;
    }

    public DmnDecisionContextBuilder<DmnEngineAssertion> setVariableContext(DmnVariableContext variableContext) {
      decisionContext.setVariableContext(variableContext);
      return this;
    }

    public DmnDecisionContextBuilder<DmnEngineAssertion> setVariable(String name, Object value) {
      decisionContext.getVariableContextChecked().setVariable(name, value);
      return this;
    }

    public DmnDecisionContextBuilder<DmnEngineAssertion> setVariables(Map<String, Object> variables) {
      decisionContext.getVariableContextChecked().setVariables(variables);
      return this;
    }

    public DmnDecisionContextBuilder<DmnEngineAssertion> setScriptContext(DmnScriptContext scriptContext) {
      decisionContext.setScriptContext(scriptContext);
      return this;
    }

    public DmnDecisionContextBuilder<DmnEngineAssertion> setDefaultScriptLanguage(String defaultScriptLanguage) {
      decisionContext.getScriptContextChecked().setDefaultScriptLanguage(defaultScriptLanguage);
      return this;
    }

    public DmnEngineAssertion build() {
      return assertion.withContext(decisionContext);
    }
  }

}
