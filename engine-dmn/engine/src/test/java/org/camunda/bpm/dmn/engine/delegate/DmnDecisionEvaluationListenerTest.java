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

package org.camunda.bpm.dmn.engine.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */

public class DmnDecisionEvaluationListenerTest extends DmnEngineTest {

  public static final String DMN_FILE = "org/camunda/bpm/dmn/engine/delegate/DrdDishDecisionExampleWithInsufficientRules.dmn";

  public TestDecisionEvaluationListener listener;

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    return new TestDecisionEvaluationListenerConfiguration();
  }

  @Before
  public void initListener() {
    TestDecisionEvaluationListenerConfiguration configuration = (TestDecisionEvaluationListenerConfiguration) dmnEngine.getConfiguration();
    listener = configuration.testDecisionListener;
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void shouldCallListener() {
    evaluateDecisionTable(20, "Weekend");
    assertThat(listener.getEvaluationEvent()).isNotNull();
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void shouldGetExecutedDecisionElements() {
    evaluateDecisionTable(35, "Weekend");

    DmnDecisionEvaluationEvent evaluationEvent = listener.getEvaluationEvent();
    assertThat(evaluationEvent).isNotNull();
    assertThat(evaluationEvent.getExecutedDecisionElements()).isEqualTo(24L);

    DmnDecisionTableEvaluationEvent dmnDecisionTableEvaluationEvent = getDmnDecisionTable(evaluationEvent.getRequiredDecisionResults(),"Season");
    assertThat(dmnDecisionTableEvaluationEvent).isNotNull();
    assertThat(dmnDecisionTableEvaluationEvent.getExecutedDecisionElements()).isEqualTo(6L);
    
    dmnDecisionTableEvaluationEvent = getDmnDecisionTable(evaluationEvent.getRequiredDecisionResults(),"GuestCount");
    assertThat(dmnDecisionTableEvaluationEvent).isNotNull();
    assertThat(dmnDecisionTableEvaluationEvent.getExecutedDecisionElements()).isEqualTo(6L);
    
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void shouldVerifyRootDecisionResult() {
    evaluateDecisionTable(35, "Weekend");
    
    assertThat(listener.getEvaluationEvent()).isNotNull();
    DmnDecisionTableEvaluationEvent decisionResult = listener.getEvaluationEvent().getDecisionResult();
    assertThat(decisionResult).isNotNull();
    assertThat(decisionResult.getDecisionTable().getKey()).isEqualTo("Dish");
    
    List<DmnEvaluatedInput> inputs = decisionResult.getInputs();
    assertThat(inputs.size()).isEqualTo(2);
    assertThat(inputs.get(0).getName()).isEqualTo("Season");
    assertThat(inputs.get(0).getValue().getValue()).isEqualTo("Summer");
    assertThat(inputs.get(1).getName()).isEqualTo("How many guests");
    assertThat(inputs.get(1).getValue().getValue()).isEqualTo(15);
    
    assertThat(decisionResult.getMatchingRules().size()).isEqualTo(1);
    Map<String, DmnEvaluatedOutput> outputEntries = decisionResult.getMatchingRules().get(0).getOutputEntries();
    assertThat(outputEntries.size()).isEqualTo(1);
    assertThat(outputEntries.containsKey("desiredDish")).isTrue();
    assertThat(outputEntries.get("desiredDish").getValue().getValue()).isEqualTo("Light salad");
    assertThat(decisionResult.getExecutedDecisionElements()).isEqualTo(12L);
    
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void shouldVerifyRootDecisionResultWithNoMatchingOutput() {
    evaluateDecisionTable(20, "Weekend");
    
    assertThat(listener.getEvaluationEvent()).isNotNull();
    DmnDecisionTableEvaluationEvent decisionResult = listener.getEvaluationEvent().getDecisionResult();
    assertThat(decisionResult).isNotNull();
    assertThat(decisionResult.getDecisionTable().getKey()).isEqualTo("Dish");
    assertThat(decisionResult.getMatchingRules().size()).isEqualTo(0);
    assertThat(decisionResult.getExecutedDecisionElements()).isEqualTo(12L);
    
  }

  @Test
  @DecisionResource(resource = DMN_FILE)
  public void shouldVerifyRequiredDecisionResults() {
    evaluateDecisionTable(35, "Weekend");
    
    assertThat(listener.getEvaluationEvent()).isNotNull();
    Collection<DmnDecisionTableEvaluationEvent> requiredDecisions = listener.getEvaluationEvent().getRequiredDecisionResults();
    assertThat(requiredDecisions.size()).isEqualTo(2);
    
    DmnDecisionTableEvaluationEvent dmnDecisionTableEvaluationEvent = getDmnDecisionTable(requiredDecisions,"Season");
    assertThat(dmnDecisionTableEvaluationEvent).isNotNull();
    List<DmnEvaluatedInput> inputs = dmnDecisionTableEvaluationEvent.getInputs();
    assertThat(inputs.size()).isEqualTo(1);
    assertThat(inputs.get(0).getName()).isEqualTo("Weather in Celsius");
    assertThat(inputs.get(0).getValue().getValue()).isEqualTo(35);
    List<DmnEvaluatedDecisionRule> matchingRules = dmnDecisionTableEvaluationEvent.getMatchingRules();
    assertThat(matchingRules.size()).isEqualTo(1);
    assertThat(matchingRules.get(0).getOutputEntries().get("season").getValue().getValue()).isEqualTo("Summer");
    
    dmnDecisionTableEvaluationEvent = getDmnDecisionTable(requiredDecisions,"GuestCount");
    assertThat(dmnDecisionTableEvaluationEvent).isNotNull();
    inputs = dmnDecisionTableEvaluationEvent.getInputs();
    assertThat(inputs.size()).isEqualTo(1);
    assertThat(inputs.get(0).getName()).isEqualTo("Type of day");
    assertThat(inputs.get(0).getValue().getValue()).isEqualTo("Weekend");
    matchingRules = dmnDecisionTableEvaluationEvent.getMatchingRules();
    assertThat(matchingRules.size()).isEqualTo(1);
    assertThat(matchingRules.get(0).getOutputEntries().get("guestCount").getValue().getValue()).isEqualTo(15);

  }

  // helper
  protected DmnDecisionTableEvaluationEvent getDmnDecisionTable(Collection<DmnDecisionTableEvaluationEvent> requiredDecisionEvents, String key) {
    for(DmnDecisionTableEvaluationEvent event: requiredDecisionEvents) {
      if(event.getDecisionTable().getKey().equals(key)) {
        return event;
      }
    }
    return null;
  }

  protected DmnDecisionTableResult evaluateDecisionTable(Object input1, Object input2) {
    variables.put("temperature", input1);
    variables.put("dayType", input2);
    return evaluateDecisionTable();
  }

  public static class TestDecisionEvaluationListenerConfiguration extends DefaultDmnEngineConfiguration {

    public TestDecisionEvaluationListener testDecisionListener = new TestDecisionEvaluationListener();

    public TestDecisionEvaluationListenerConfiguration() {
      customPostDecisionEvaluationListeners.add(testDecisionListener);
    }

  }

  public static class TestDecisionEvaluationListener implements DmnDecisionEvaluationListener {

    public DmnDecisionEvaluationEvent evaluationEvent;

    public void notify(DmnDecisionEvaluationEvent evaluationEvent) {
      this.evaluationEvent = evaluationEvent;
    }

    public DmnDecisionEvaluationEvent getEvaluationEvent() {
      return evaluationEvent;
    }
  }

}
