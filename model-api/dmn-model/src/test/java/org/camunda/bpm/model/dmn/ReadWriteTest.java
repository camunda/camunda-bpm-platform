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

package org.camunda.bpm.model.dmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.model.dmn.DecisionTableOrientation.CrossTable;
import static org.camunda.bpm.model.dmn.DecisionTableOrientation.Rule_as_Column;
import static org.camunda.bpm.model.dmn.DecisionTableOrientation.Rule_as_Row;
import static org.camunda.bpm.model.dmn.HitPolicy.ANY;
import static org.camunda.bpm.model.dmn.HitPolicy.COLLECT;
import static org.camunda.bpm.model.dmn.HitPolicy.FIRST;
import static org.camunda.bpm.model.dmn.HitPolicy.OUTPUT_ORDER;
import static org.camunda.bpm.model.dmn.HitPolicy.PRIORITY;
import static org.camunda.bpm.model.dmn.HitPolicy.RULE_ORDER;
import static org.camunda.bpm.model.dmn.HitPolicy.UNIQUE;

import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.util.DmnModelResource;
import org.junit.Test;

public class ReadWriteTest extends DmnModelTest {

  public static final String DECISION_TABLE_ORIENTATION_DMN = "org/camunda/bpm/model/dmn/ReadWriteTest.decisionTableOrientation.dmn";
  public static final String HIT_POLICY_DMN = "org/camunda/bpm/model/dmn/ReadWriteTest.hitPolicy.dmn";

  @Test
  @DmnModelResource(resource = DECISION_TABLE_ORIENTATION_DMN)
  public void shouldReadDecisionTableOrientation() {
    // Default
    DecisionTable decisionTable = dmnModelInstance.getModelElementById("decisionTable1");
    assertThat(decisionTable.getPreferedOrientation()).isEqualTo(Rule_as_Row);

    // Rule-as-Row
    decisionTable = dmnModelInstance.getModelElementById("decisionTable2");
    assertThat(decisionTable.getPreferedOrientation()).isEqualTo(Rule_as_Row);

    // Rule-as-Column
    decisionTable = dmnModelInstance.getModelElementById("decisionTable3");
    assertThat(decisionTable.getPreferedOrientation()).isEqualTo(Rule_as_Column);

    // CrossTable
    decisionTable = dmnModelInstance.getModelElementById("decisionTable4");
    assertThat(decisionTable.getPreferedOrientation()).isEqualTo(CrossTable);
  }

  @Test
  public void shouldWriteDecisionTableOrientation() throws Exception {
    dmnModelInstance = Dmn.createEmptyModel();
    Definitions definitions = generateElement(Definitions.class);
    definitions.setNamespace(TEST_URI);
    dmnModelInstance.setDocumentElement(definitions);

    // Default
    Decision decision = generateElement(Decision.class, 1);
    DecisionTable decisionTable = generateElement(DecisionTable.class, 1);
    decision.setExpression(decisionTable);
    definitions.addChildElement(decision);

    // Rule-as-Row
    decision = generateElement(Decision.class, 2);
    decisionTable = generateElement(DecisionTable.class, 2);
    decisionTable.setPreferedOrientation(Rule_as_Row);
    decision.setExpression(decisionTable);
    definitions.addChildElement(decision);

    // Rule-as-Column
    decision = generateElement(Decision.class, 3);
    decisionTable = generateElement(DecisionTable.class, 3);
    decisionTable.setPreferedOrientation(Rule_as_Column);
    decision.setExpression(decisionTable);
    definitions.addChildElement(decision);

    // CrossTable
    decision = generateElement(Decision.class, 4);
    decisionTable = generateElement(DecisionTable.class, 4);
    decisionTable.setPreferedOrientation(CrossTable);
    decision.setExpression(decisionTable);
    definitions.addChildElement(decision);

    assertModelEqualsFile(DECISION_TABLE_ORIENTATION_DMN);
  }

  @Test
  @DmnModelResource(resource = HIT_POLICY_DMN)
  public void shouldReadHitPolicy() {
    // Default
    DecisionTable decisionTable = dmnModelInstance.getModelElementById("decisionTable1");
    assertThat(decisionTable.getHitPolicy()).isEqualTo(UNIQUE);

    // UNIQUE
    decisionTable = dmnModelInstance.getModelElementById("decisionTable2");
    assertThat(decisionTable.getHitPolicy()).isEqualTo(UNIQUE);

    // FIRST
    decisionTable = dmnModelInstance.getModelElementById("decisionTable3");
    assertThat(decisionTable.getHitPolicy()).isEqualTo(FIRST);

    // PRIORITY
    decisionTable = dmnModelInstance.getModelElementById("decisionTable4");
    assertThat(decisionTable.getHitPolicy()).isEqualTo(PRIORITY);

    // ANY
    decisionTable = dmnModelInstance.getModelElementById("decisionTable5");
    assertThat(decisionTable.getHitPolicy()).isEqualTo(ANY);

    // COLLECT
    decisionTable = dmnModelInstance.getModelElementById("decisionTable6");
    assertThat(decisionTable.getHitPolicy()).isEqualTo(COLLECT);

    // RULE ORDER
    decisionTable = dmnModelInstance.getModelElementById("decisionTable7");
    assertThat(decisionTable.getHitPolicy()).isEqualTo(RULE_ORDER);

    // OUTPUT ORDER
    decisionTable = dmnModelInstance.getModelElementById("decisionTable8");
    assertThat(decisionTable.getHitPolicy()).isEqualTo(OUTPUT_ORDER);

  }

  @Test
  public void shouldWriteHitPolicy() throws Exception {
    dmnModelInstance = Dmn.createEmptyModel();
    Definitions definitions = generateElement(Definitions.class);
    definitions.setNamespace(TEST_URI);
    dmnModelInstance.setDocumentElement(definitions);

    // Default
    Decision decision = generateElement(Decision.class, 1);
    DecisionTable decisionTable = generateElement(DecisionTable.class, 1);
    decision.setExpression(decisionTable);
    definitions.addChildElement(decision);

    // UNIQUE
    decision = generateElement(Decision.class, 2);
    decisionTable = generateElement(DecisionTable.class, 2);
    decisionTable.setHitPolicy(UNIQUE);
    decision.setExpression(decisionTable);
    definitions.addChildElement(decision);

    // FIRST
    decision = generateElement(Decision.class, 3);
    decisionTable = generateElement(DecisionTable.class, 3);
    decisionTable.setHitPolicy(FIRST);
    decision.setExpression(decisionTable);
    definitions.addChildElement(decision);

    // PRIORITY
    decision = generateElement(Decision.class, 4);
    decisionTable = generateElement(DecisionTable.class, 4);
    decisionTable.setHitPolicy(PRIORITY);
    decision.setExpression(decisionTable);
    definitions.addChildElement(decision);

    // ANY
    decision = generateElement(Decision.class, 5);
    decisionTable = generateElement(DecisionTable.class, 5);
    decisionTable.setHitPolicy(ANY);
    decision.setExpression(decisionTable);
    definitions.addChildElement(decision);

    // COLLECT
    decision = generateElement(Decision.class, 6);
    decisionTable = generateElement(DecisionTable.class, 6);
    decisionTable.setHitPolicy(COLLECT);
    decision.setExpression(decisionTable);
    definitions.addChildElement(decision);

    // RULE ORDER
    decision = generateElement(Decision.class, 7);
    decisionTable = generateElement(DecisionTable.class, 7);
    decisionTable.setHitPolicy(RULE_ORDER);
    decision.setExpression(decisionTable);
    definitions.addChildElement(decision);

    // OUTPUT ORDER
    decision = generateElement(Decision.class, 8);
    decisionTable = generateElement(DecisionTable.class, 8);
    decisionTable.setHitPolicy(OUTPUT_ORDER);
    decision.setExpression(decisionTable);
    definitions.addChildElement(decision);

    assertModelEqualsFile(HIT_POLICY_DMN);

  }

}
