/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.repository;

import java.util.Collection;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.Rule;

public class DmnModelElementInstanceCmdTest extends PluggableProcessEngineTestCase {

  private final static String DECISION_KEY = "one";

  @Deployment(resources = "org/camunda/bpm/engine/test/repository/one.dmn")
  public void testRepositoryService() {
    String decisionDefinitionId = repositoryService
      .createDecisionDefinitionQuery()
      .decisionDefinitionKey(DECISION_KEY)
      .singleResult()
      .getId();

    DmnModelInstance modelInstance = repositoryService.getDmnModelInstance(decisionDefinitionId);
    assertNotNull(modelInstance);

    Collection<Decision> decisions = modelInstance.getModelElementsByType(Decision.class);
    assertEquals(1, decisions.size());

    Collection<DecisionTable> decisionTables = modelInstance.getModelElementsByType(DecisionTable.class);
    assertEquals(1, decisionTables.size());

    Collection<Input> inputs = modelInstance.getModelElementsByType(Input.class);
    assertEquals(1, inputs.size());

    Collection<Output> outputs = modelInstance.getModelElementsByType(Output.class);
    assertEquals(1, outputs.size());

    Collection<Rule> rules = modelInstance.getModelElementsByType(Rule.class);
    assertEquals(2, rules.size());
  }

}
