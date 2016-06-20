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

package org.camunda.bpm.engine.test.standalone.history;

import java.util.List;

import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

public class DecisionInstanceHistoryTest extends ResourceProcessEngineTestCase {

  public static final String DECISION_SINGLE_OUTPUT_DMN = "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml";

  public DecisionInstanceHistoryTest() {
    super("org/camunda/bpm/engine/test/standalone/history/decisionInstanceHistory.camunda.cfg.xml");
  }

  @Deployment(resources = DECISION_SINGLE_OUTPUT_DMN)
  public void testDecisionDefinitionPassedToHistoryLevel() {
    RecordHistoryLevel historyLevel = (RecordHistoryLevel) processEngineConfiguration.getHistoryLevel();
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey("testDecision").singleResult();

    VariableMap variables = Variables.createVariables().putValue("input1", true);
    decisionService.evaluateDecisionTableByKey("testDecision", variables);

    List<RecordHistoryLevel.ProducedHistoryEvent> producedHistoryEvents = historyLevel.getProducedHistoryEvents();
    assertEquals(1, producedHistoryEvents.size());

    RecordHistoryLevel.ProducedHistoryEvent producedHistoryEvent = producedHistoryEvents.get(0);
    assertEquals(HistoryEventTypes.DMN_DECISION_EVALUATE, producedHistoryEvent.eventType);

    DecisionDefinition entity = (DecisionDefinition) producedHistoryEvent.entity;
    assertNotNull(entity);
    assertEquals(decisionDefinition.getId(), entity.getId());
  }

}
