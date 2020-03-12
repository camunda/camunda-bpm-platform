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
package org.camunda.bpm.dmn.engine.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.DefaultHitPolicyHandlerRegistry;
import org.camunda.bpm.dmn.engine.impl.spi.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.spi.hitpolicy.DmnHitPolicyHandlerRegistry;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DmnDecisionTableEvaluationListenerTest extends DmnEngineTest {

  public static final String DMN_FILE = "org/camunda/bpm/dmn/engine/delegate/DmnDecisionTableListenerTest.test.dmn";

  public static DmnHitPolicyHandlerRegistry hitPolicyHandlerRegistry;
  public TestDecisionTableEvaluationListener listener;

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    return new TestDecisionTableEvaluationListenerConfiguration()
      .enableFeelLegacyBehavior(true);
  }

  @BeforeClass
  public static void initHitPolicyHandlerRegistry() {
    hitPolicyHandlerRegistry = new DefaultHitPolicyHandlerRegistry();
  }

  @Before
  public void initListener() {
    TestDecisionTableEvaluationListenerConfiguration configuration = (TestDecisionTableEvaluationListenerConfiguration) dmnEngine.getConfiguration();
    listener = configuration.testDecisionTableListener;
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void testListenerIsCalled() {
    evaluateDecisionTable(true, "foo", "hello", "hello");
    assertThat(listener.evaluationEvent).isNotNull();
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void testExecutedDecisionElements() {
    // the number should be independent from input and result
    evaluateDecisionTable(true, "foo", false, "hello");
    assertThat(listener.evaluationEvent.getExecutedDecisionElements()).isEqualTo(36);

    evaluateDecisionTable(false, "bar", true, "hello");
    assertThat(listener.evaluationEvent.getExecutedDecisionElements()).isEqualTo(36);

    evaluateDecisionTable(false, "false", true, "hello");
    assertThat(listener.evaluationEvent.getExecutedDecisionElements()).isEqualTo(36);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void testInputValues() {
    evaluateDecisionTable(true, "foo", "test", "hello");
    List<DmnEvaluatedInput> inputs = listener.evaluationEvent.getInputs();
    assertThat(inputs).hasSize(3);

    DmnEvaluatedInput input1 = inputs.get(0);
    assertThat(input1.getId()).isEqualTo("input1");
    assertThat(input1.getName()).isEqualTo("Input");
    assertThat(input1.getInputVariable()).isEqualTo("cellInput");
    assertThat(input1.getValue()).isEqualTo(Variables.untypedValue(true));

    DmnEvaluatedInput input2 = inputs.get(1);
    assertThat(input2.getId()).isEqualTo("input2");
    assertThat(input2.getName()).isNull();
    assertThat(input2.getInputVariable()).isEqualTo("x");
    assertThat(input2.getValue()).isEqualTo(Variables.untypedValue("foo"));

    DmnEvaluatedInput input3 = inputs.get(2);
    assertThat(input3.getId()).isEqualTo("input3");
    assertThat(input3.getName()).isNull();
    assertThat(input3.getInputVariable()).isEqualTo("cellInput");
    assertThat(input3.getValue()).isEqualTo(Variables.untypedNullValue());
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void testMatchingRules() {
    evaluateDecisionTable(true, "foo", "test", "hello");
    List<DmnEvaluatedDecisionRule> matchingRules = listener.evaluationEvent.getMatchingRules();
    assertThat(matchingRules).hasSize(1);

    DmnEvaluatedDecisionRule matchedRule = matchingRules.get(0);
    assertThat(matchedRule.getId()).isEqualTo("rule1");
    assertThat(matchedRule.getOutputEntries()).hasSize(2);

    evaluateDecisionTable(true, "bar", "test", "hello");
    matchingRules = listener.evaluationEvent.getMatchingRules();
    assertThat(matchingRules).hasSize(0);

    evaluateDecisionTable(false, "bar", "test", "hello");
    matchingRules = listener.evaluationEvent.getMatchingRules();
    assertThat(matchingRules).hasSize(1);

    matchedRule = matchingRules.get(0);
    assertThat(matchedRule.getId()).isEqualTo("rule2");
    assertThat(matchedRule.getOutputEntries()).hasSize(1);

    evaluateDecisionTable(false, "bar", true, "hello");
    matchingRules = listener.evaluationEvent.getMatchingRules();
    assertThat(matchingRules).hasSize(5);

    matchedRule = matchingRules.get(0);
    assertThat(matchedRule.getId()).isEqualTo("rule2");
    assertThat(matchedRule.getOutputEntries()).hasSize(1);
    matchedRule = matchingRules.get(1);
    assertThat(matchedRule.getId()).isEqualTo("rule3");
    assertThat(matchedRule.getOutputEntries()).hasSize(0);
    matchedRule = matchingRules.get(2);
    assertThat(matchedRule.getId()).isEqualTo("rule4");
    assertThat(matchedRule.getOutputEntries()).hasSize(1);
    matchedRule = matchingRules.get(3);
    assertThat(matchedRule.getId()).isEqualTo("rule5");
    assertThat(matchedRule.getOutputEntries()).hasSize(1);
    matchedRule = matchingRules.get(4);
    assertThat(matchedRule.getId()).isEqualTo("rule6");
    assertThat(matchedRule.getOutputEntries()).hasSize(1);
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void testOutputs() {
    evaluateDecisionTable(true, "foo", "test", "hello");
    List<DmnEvaluatedDecisionRule> matchingRules = listener.evaluationEvent.getMatchingRules();
    Map<String, DmnEvaluatedOutput> outputs = matchingRules.get(0).getOutputEntries();
    assertThat(outputs).hasSize(2)
      .containsKeys("out1", "out2");

    DmnEvaluatedOutput output1 = outputs.get("out1");
    assertThat(output1.getId()).isEqualTo("output1");
    assertThat(output1.getName()).isEqualTo("Output 1");
    assertThat(output1.getOutputName()).isEqualTo("out1");
    assertThat(output1.getValue()).isEqualTo(Variables.untypedValue("hello"));

    DmnEvaluatedOutput output2 = outputs.get("out2");
    assertThat(output2.getId()).isEqualTo("output2");
    assertThat(output2.getName()).isNull();
    assertThat(output2.getOutputName()).isEqualTo("out2");
    assertThat(output2.getValue()).isEqualTo(Variables.untypedValue("camunda"));

    evaluateDecisionTable(false, "bar", "test", "hello");
    matchingRules = listener.evaluationEvent.getMatchingRules();
    outputs = matchingRules.get(0).getOutputEntries();
    assertThat(outputs).hasSize(1)
      .containsKeys("out2");

    output2 = outputs.get("out2");
    assertThat(output2.getId()).isEqualTo("output2");
    assertThat(output2.getName()).isNull();
    assertThat(output2.getOutputName()).isEqualTo("out2");
    assertThat(output2.getValue()).isEqualTo(Variables.untypedValue("camunda"));
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void testCollectResult() {
    setDecisionTableHitPolicy(HitPolicy.COLLECT, null);
    evaluateDecisionTable(true, "bar", true, "hello");
    assertThat(listener.evaluationEvent.getCollectResultName()).isNull();
    assertThat(listener.evaluationEvent.getCollectResultValue()).isNull();
    List<DmnEvaluatedDecisionRule> matchingRules = listener.evaluationEvent.getMatchingRules();
    assertThat(matchingRules).hasSize(4);
    assertThat(matchingRules.get(0).getId()).isEqualTo("rule3");
    assertThat(matchingRules.get(1).getId()).isEqualTo("rule4");
    assertThat(matchingRules.get(2).getId()).isEqualTo("rule5");
    assertThat(matchingRules.get(3).getId()).isEqualTo("rule6");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void testCollectCountResult() {
    setDecisionTableHitPolicy(HitPolicy.COLLECT, BuiltinAggregator.COUNT);
    evaluateDecisionTable(true, "bar", true, "hello");
    assertThat(listener.evaluationEvent.getCollectResultName()).isEqualTo("collectMe");
    assertThat(listener.evaluationEvent.getCollectResultValue()).isEqualTo(Variables.integerValue(3));
    List<DmnEvaluatedDecisionRule> matchingRules = listener.evaluationEvent.getMatchingRules();
    assertThat(matchingRules).hasSize(4);
    assertThat(matchingRules.get(0).getId()).isEqualTo("rule3");
    assertThat(matchingRules.get(1).getId()).isEqualTo("rule4");
    assertThat(matchingRules.get(2).getId()).isEqualTo("rule5");
    assertThat(matchingRules.get(3).getId()).isEqualTo("rule6");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void testCollectSumResult() {
    setDecisionTableHitPolicy(HitPolicy.COLLECT, BuiltinAggregator.SUM);
    evaluateDecisionTable(true, "bar", true, "hello");
    assertThat(listener.evaluationEvent.getCollectResultName()).isEqualTo("collectMe");
    assertThat(listener.evaluationEvent.getCollectResultValue()).isEqualTo(Variables.integerValue(90));
    List<DmnEvaluatedDecisionRule> matchingRules = listener.evaluationEvent.getMatchingRules();
    assertThat(matchingRules).hasSize(4);
    assertThat(matchingRules.get(0).getId()).isEqualTo("rule3");
    assertThat(matchingRules.get(1).getId()).isEqualTo("rule4");
    assertThat(matchingRules.get(2).getId()).isEqualTo("rule5");
    assertThat(matchingRules.get(3).getId()).isEqualTo("rule6");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void testCollectMaxResult() {
    setDecisionTableHitPolicy(HitPolicy.COLLECT, BuiltinAggregator.MAX);
    evaluateDecisionTable(true, "bar", true, "hello");
    assertThat(listener.evaluationEvent.getCollectResultName()).isEqualTo("collectMe");
    assertThat(listener.evaluationEvent.getCollectResultValue()).isEqualTo(Variables.integerValue(50));
    List<DmnEvaluatedDecisionRule> matchingRules = listener.evaluationEvent.getMatchingRules();
    assertThat(matchingRules).hasSize(4);
    assertThat(matchingRules.get(0).getId()).isEqualTo("rule3");
    assertThat(matchingRules.get(1).getId()).isEqualTo("rule4");
    assertThat(matchingRules.get(2).getId()).isEqualTo("rule5");
    assertThat(matchingRules.get(3).getId()).isEqualTo("rule6");
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void testCollectMinResult() {
    setDecisionTableHitPolicy(HitPolicy.COLLECT, BuiltinAggregator.MIN);
    evaluateDecisionTable(true, "bar", true, "hello");
    assertThat(listener.evaluationEvent.getCollectResultName()).isEqualTo("collectMe");
    assertThat(listener.evaluationEvent.getCollectResultValue()).isEqualTo(Variables.integerValue(10));
    List<DmnEvaluatedDecisionRule> matchingRules = listener.evaluationEvent.getMatchingRules();
    assertThat(matchingRules).hasSize(4);
    assertThat(matchingRules.get(0).getId()).isEqualTo("rule3");
    assertThat(matchingRules.get(1).getId()).isEqualTo("rule4");
    assertThat(matchingRules.get(2).getId()).isEqualTo("rule5");
    assertThat(matchingRules.get(3).getId()).isEqualTo("rule6");
  }

  // helper

  public void setDecisionTableHitPolicy(HitPolicy hitPolicy, BuiltinAggregator aggregator) {
    DmnHitPolicyHandler handler = hitPolicyHandlerRegistry.getHandler(hitPolicy, aggregator);
    assertThat(handler).isNotNull();
    DmnDecisionTableImpl decisionTable = (DmnDecisionTableImpl) this.decision.getDecisionLogic();
    decisionTable.setHitPolicyHandler(handler);
  }

  public DmnDecisionTableResult evaluateDecisionTable(Object input1, Object input2, Object input3, Object output1) {
    variables.put("input1", input1);
    variables.put("input2", input2);
    variables.put("input3", input3);
    variables.put("output1", output1);

    return evaluateDecisionTable();
  }

  public static class TestDecisionTableEvaluationListenerConfiguration extends DefaultDmnEngineConfiguration {

    public TestDecisionTableEvaluationListener testDecisionTableListener = new TestDecisionTableEvaluationListener();

    public TestDecisionTableEvaluationListenerConfiguration() {
      customPostDecisionTableEvaluationListeners.add(testDecisionTableListener);
    }

  }

  public static class TestDecisionTableEvaluationListener implements DmnDecisionTableEvaluationListener {

    public DmnDecisionTableEvaluationEvent evaluationEvent;

    public void notify(DmnDecisionTableEvaluationEvent evaluationEvent) {
      this.evaluationEvent = evaluationEvent;
    }

  }

}
