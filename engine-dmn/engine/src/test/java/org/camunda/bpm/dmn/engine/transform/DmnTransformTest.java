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

package org.camunda.bpm.dmn.engine.transform;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnClause;
import org.camunda.bpm.dmn.engine.DmnClauseEntry;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnExpression;
import org.camunda.bpm.dmn.engine.DmnItemDefinition;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.DmnTypeDefinition;
import org.camunda.bpm.dmn.engine.impl.DefaultItemDefinition;
import org.camunda.bpm.dmn.engine.impl.DefaultTypeDefinition;
import org.camunda.bpm.dmn.engine.impl.DmnClauseImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.commons.utils.IoUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class DmnTransformTest {

  protected static DmnEngine engine;
  protected static DmnDecisionModel decisionModel;

  @BeforeClass
  public static void readModelFromFile() {
    InputStream inputStream = IoUtil.fileAsStream("org/camunda/bpm/dmn/engine/transform/DmnTransformTest.dmn");
    DmnEngineConfigurationImpl engineConfiguration = new DmnEngineConfigurationImpl();
    engine = engineConfiguration.buildEngine();
    decisionModel = engine.parseDecisionModel(inputStream);
  }

  @Test
  public void shouldTransformDefinitions() {
    assertThat(decisionModel.getKey()).isEqualTo("definitions");
    assertThat(decisionModel.getName()).isEqualTo("camunda");
    assertThat(decisionModel.getNamespace()).isEqualTo("http://camunda.org/dmn");
    assertThat(decisionModel.getExpressionLanguage()).isEqualTo(null);
  }

  @Test
  public void shouldTransformItemDefinitions() {
    assertThat(decisionModel.getItemDefinitions()).hasSize(2);

    DmnItemDefinition itemDefinition = decisionModel.getItemDefinition("itemDefinition1");
    assertThat(itemDefinition).isNotNull();
    assertThat(itemDefinition.getName()).isEqualTo("camunda");

    itemDefinition = decisionModel.getItemDefinition("itemDefinition2");
    assertThat(itemDefinition).isNotNull();
    assertThat(itemDefinition.getName()).isNull();
  }

  @Test
  public void shouldTransformTypeDefinitions() {
    DmnTypeDefinition typeDefinition = decisionModel.getItemDefinition("itemDefinition1").getTypeDefinition();
    assertThat(typeDefinition).isEqualTo(new DefaultTypeDefinition());

    typeDefinition = decisionModel.getItemDefinition("itemDefinition2").getTypeDefinition();
    assertThat(typeDefinition).isNotNull();
    assertThat(typeDefinition.getTypeName()).isEqualTo("string");
  }

  @Test
  public void shouldTransformAllowedValues() {
    List<DmnExpression> allowedValues = decisionModel.getItemDefinition("itemDefinition1").getAllowedValues();
    assertThat(allowedValues).isEmpty();

    allowedValues = decisionModel.getItemDefinition("itemDefinition2").getAllowedValues();
    assertThat(allowedValues).hasSize(2);

    DmnExpression allowedValue = allowedValues.get(0);
    assertThat(allowedValue.getKey()).isEqualTo("allowedValue1");
    assertThat(allowedValue.getName()).isEqualTo("a");
    assertThat(allowedValue.getExpressionLanguage()).isNull();
    assertThat(allowedValue.getExpression()).isEqualTo("camunda");

    allowedValue = allowedValues.get(1);
    assertThat(allowedValue.getKey()).isEqualTo("allowedValue2");
    assertThat(allowedValue.getName()).isEqualTo("b");
    assertThat(allowedValue.getExpressionLanguage()).isNull();
    assertThat(allowedValue.getExpression()).isEqualTo("camunda");
  }

  @Test
  public void shouldTransformDecisions() {
    assertThat(decisionModel.getDecisions()).hasSize(2);

    DmnDecision decision = decisionModel.getDecision("decision1");
    assertThat(decision).isNotNull();
    assertThat(decision.getName()).isEqualTo("camunda");

    // decision2 should be ignored as it isn't supported by the DMN engine
    decision = decisionModel.getDecision("decision2");
    assertThat(decision).isNull();

    decision = decisionModel.getDecision("decision3");
    assertThat(decision).isNotNull();
    assertThat(decision.getName()).isEqualTo("camunda");
  }

  @Test
  public void shouldTransformDecisionTables() {
    DmnDecision decision = decisionModel.getDecision("decision1");
    assertThat(decision).isInstanceOf(DmnDecisionTable.class);

    DmnDecisionTable decisionTable = (DmnDecisionTable) decision;
    assertThat(decisionTable.getHitPolicy()).isEqualTo(DmnDecisionTableImpl.DEFAULT_HIT_POLICY);

    decision = decisionModel.getDecision("decision3");
    assertThat(decision).isInstanceOf(DmnDecisionTable.class);

    decisionTable = (DmnDecisionTable) decision;
    assertThat(decisionTable.getHitPolicy()).isEqualTo(HitPolicy.FIRST);
  }

  @Test
  public void shouldTransformClauses() {
    List<DmnClause> clauses = getClausesForDecision("decision1");
    // clause7 should be ignored as it has no input or output entries
    assertThat(clauses).hasSize(6);

    DmnClause clause = clauses.get(0);
    assertThat(clause.getKey()).isEqualTo("clause1");
    assertThat(clause.getName()).isEqualTo("camunda");
    assertThat(clause.getOutputName()).isEqualTo("camunda");
    assertThat(clause.isOrdered()).isFalse();

    clause = clauses.get(1);
    assertThat(clause.getKey()).isEqualTo("clause2");
    assertThat(clause.getName()).isNull();
    assertThat(clause.getOutputName()).isEqualTo(DmnClauseImpl.DEFAULT_INPUT_VARIABLE_NAME);
    assertThat(clause.isOrdered()).isFalse();

    clause = clauses.get(2);
    assertThat(clause.getKey()).isEqualTo("clause3");
    assertThat(clause.getName()).isNull();
  assertThat(clause.getOutputName()).isEqualTo(DmnClauseImpl.DEFAULT_INPUT_VARIABLE_NAME);
  assertThat(clause.isOrdered()).isFalse();

    clause = clauses.get(3);
    assertThat(clause.getKey()).isEqualTo("clause4");
    assertThat(clause.getName()).isNull();
    assertThat(clause.getOutputName()).isEqualTo(DmnClauseImpl.DEFAULT_INPUT_VARIABLE_NAME);
    assertThat(clause.isOrdered()).isTrue();

    clause = clauses.get(4);
    assertThat(clause.getKey()).isEqualTo("clause5");
    assertThat(clause.getName()).isNull();
    assertThat(clause.getOutputName()).isNull();
    assertThat(clause.isOrdered()).isFalse();

    clause = clauses.get(5);
    assertThat(clause.getKey()).isEqualTo("clause6");
    assertThat(clause.getName()).isEqualTo("camunda");
    assertThat(clause.getOutputName()).isNull();
    assertThat(clause.isOrdered()).isTrue();
  }

  @Test
  public void shouldTransformInputExpressions() {
    List<DmnClause> clauses = getClausesForDecision("decision1");

    DmnExpression inputExpression = clauses.get(0).getInputExpression();
    assertThat(inputExpression).isNotNull();
    assertThat(inputExpression.getKey()).isEqualTo("inputExpression1");
    assertThat(inputExpression.getName()).isNull();
    assertThat(inputExpression.getExpressionLanguage()).isNull();
    assertThat(inputExpression.getExpression()).isEqualTo("camunda");
    assertThat(inputExpression.getItemDefinition()).isNotNull();
    assertThat(inputExpression.getItemDefinition().getTypeDefinition()).isEqualTo(new DefaultTypeDefinition());

    inputExpression = clauses.get(1).getInputExpression();
    assertThat(inputExpression).isNotNull();
    assertThat(inputExpression.getKey()).isEqualTo("inputExpression2");
    assertThat(inputExpression.getName()).isEqualTo("camunda");
    assertThat(inputExpression.getExpressionLanguage()).isEqualTo("camunda");
    assertThat(inputExpression.getExpression()).isNull();
    assertThat(inputExpression.getItemDefinition()).isNotNull();
    assertThat(inputExpression.getItemDefinition().getTypeDefinition()).isNotNull();
    assertThat(inputExpression.getItemDefinition().getTypeDefinition().getTypeName()).isEqualTo("string");

    inputExpression = clauses.get(2).getInputExpression();
    assertThat(inputExpression).isNotNull();
    assertThat(inputExpression.getKey()).isEqualTo("inputExpression3");
    assertThat(inputExpression.getName()).isNull();
    assertThat(inputExpression.getExpressionLanguage()).isNull();
    assertThat(inputExpression.getExpression()).isNull();
    assertThat(inputExpression.getItemDefinition()).isEqualTo(new DefaultItemDefinition());

    inputExpression = clauses.get(3).getInputExpression();
    assertThat(inputExpression).isNull();
  }

  @Test
  public void shouldTransformInputEntries() {
    List<DmnClause> clauses = getClausesForDecision("decision1");

    assertThat(clauses.get(0).getInputEntries()).isEmpty();
    assertThat(clauses.get(4).getInputEntries()).isEmpty();
    assertThat(clauses.get(5).getInputEntries()).isEmpty();

    List<DmnClauseEntry> inputEntries = clauses.get(3).getInputEntries();
    assertThat(inputEntries).hasSize(3);

    DmnClauseEntry inputEntry = inputEntries.get(0);
    assertThat(inputEntry.getKey()).isEqualTo("inputEntry1");
    assertThat(inputEntry.getName()).isEqualTo("camunda");
    assertThat(inputEntry.getExpressionLanguage()).isNull();
    assertThat(inputEntry.getExpression()).isEqualTo("camunda");
    assertThat(inputEntry.getClause().getKey()).isEqualTo("clause4");

    inputEntry = inputEntries.get(1);
    assertThat(inputEntry.getKey()).isEqualTo("inputEntry2");
    assertThat(inputEntry.getName()).isEqualTo("camunda");
    assertThat(inputEntry.getExpressionLanguage()).isEqualTo("camunda");
    assertThat(inputEntry.getExpression()).isNull();
    assertThat(inputEntry.getClause().getKey()).isEqualTo("clause4");

    inputEntry = inputEntries.get(2);
    assertThat(inputEntry.getKey()).isEqualTo("inputEntry3");
    assertThat(inputEntry.getName()).isNull();
    assertThat(inputEntry.getExpressionLanguage()).isNull();
    assertThat(inputEntry.getExpression()).isNull();
    assertThat(inputEntry.getClause().getKey()).isEqualTo("clause4");
  }

  @Test
  public void shouldTransformOutputDefinition() {
    List<DmnClause> clauses = getClausesForDecision("decision1");

    assertThat(clauses.get(0).getOutputDefinition()).isEqualTo(new DefaultItemDefinition());

    DmnItemDefinition outputDefinition = clauses.get(4).getOutputDefinition();
    assertThat(outputDefinition).isNotNull();
    assertThat(outputDefinition.getTypeDefinition()).isNotNull();
    assertThat(outputDefinition.getTypeDefinition().getTypeName()).isEqualTo("string");
  }

  @Test
  public void shouldTransformOutputEntries() {
    List<DmnClause> clauses = getClausesForDecision("decision1");

    assertThat(clauses.get(0).getOutputEntries()).isEmpty();
    assertThat(clauses.get(3).getOutputEntries()).isEmpty();
    assertThat(clauses.get(4).getOutputEntries()).isEmpty();

    List<DmnExpression> outputEntries = clauses.get(5).getOutputEntries();
    assertThat(outputEntries).hasSize(3);

    DmnExpression outputEntry = outputEntries.get(0);
    assertThat(outputEntry.getKey()).isEqualTo("outputEntry1");
    assertThat(outputEntry.getName()).isEqualTo("camunda");
    assertThat(outputEntry.getExpressionLanguage()).isNull();
    assertThat(outputEntry.getExpression()).isEqualTo("camunda");

    outputEntry = outputEntries.get(1);
    assertThat(outputEntry.getKey()).isEqualTo("outputEntry2");
    assertThat(outputEntry.getName()).isEqualTo("camunda");
    assertThat(outputEntry.getExpressionLanguage()).isEqualTo("camunda");
    assertThat(outputEntry.getExpression()).isNull();

    outputEntry = outputEntries.get(2);
    assertThat(outputEntry.getKey()).isEqualTo("outputEntry3");
    assertThat(outputEntry.getName()).isNull();
    assertThat(outputEntry.getExpressionLanguage()).isNull();
    assertThat(outputEntry.getExpression()).isNull();
  }

  @Test
  public void shouldTransformRules() {
    List<DmnRule> rules = getRulesForDecision("decision1");

    assertThat(rules).hasSize(5);

    DmnRule rule = rules.get(0);
    assertThat(rule.getKey()).isEqualTo("rule1");
    assertThat(rule.getName()).isEqualTo("camunda");

    rule = rules.get(1);
    assertThat(rule.getKey()).isEqualTo("rule2");
    assertThat(rule.getName()).isNull();

    rule = rules.get(2);
    assertThat(rule.getKey()).isEqualTo("rule3");
    assertThat(rule.getName()).isNull();

    rule = rules.get(3);
    assertThat(rule.getKey()).isEqualTo("rule4");
    assertThat(rule.getName()).isNull();

    rule = rules.get(4);
    assertThat(rule.getKey()).isEqualTo("rule5");
    assertThat(rule.getName()).isNull();
  }

  @Test
  public void shouldTransformConditions() {
    List<DmnRule> rules = getRulesForDecision("decision1");

    List<DmnClauseEntry> conditions = rules.get(0).getConditions();
    assertThat(conditions).isEmpty();

    conditions = rules.get(1).getConditions();
    assertThat(conditions).hasSize(1);
    assertThat(conditions.get(0).getKey()).isEqualTo("inputEntry1");

    conditions = rules.get(2).getConditions();
    assertThat(conditions).hasSize(2);
    assertThat(conditions.get(0).getKey()).isEqualTo("inputEntry1");
    assertThat(conditions.get(1).getKey()).isEqualTo("inputEntry2");

    conditions = rules.get(3).getConditions();
    assertThat(conditions).hasSize(2);
    assertThat(conditions.get(0).getKey()).isEqualTo("inputEntry1");
    assertThat(conditions.get(1).getKey()).isEqualTo("inputEntry3");

    conditions = rules.get(4).getConditions();
    assertThat(conditions).isEmpty();
  }

  @Test
  public void shouldTransformConclusions() {
    List<DmnRule> rules = getRulesForDecision("decision1");

    List<DmnClauseEntry> conclusions = rules.get(0).getConclusions();
    assertThat(conclusions).isEmpty();

    conclusions = rules.get(1).getConclusions();
    assertThat(conclusions).isEmpty();

    conclusions = rules.get(2).getConclusions();
    assertThat(conclusions).hasSize(1);
    assertThat(conclusions.get(0).getKey()).isEqualTo("outputEntry1");

    conclusions = rules.get(3).getConclusions();
    assertThat(conclusions).hasSize(2);
    assertThat(conclusions.get(0).getKey()).isEqualTo("outputEntry1");
    assertThat(conclusions.get(1).getKey()).isEqualTo("outputEntry2");

    conclusions = rules.get(4).getConclusions();
    assertThat(conclusions).hasSize(2);
    assertThat(conclusions.get(0).getKey()).isEqualTo("outputEntry1");
    assertThat(conclusions.get(1).getKey()).isEqualTo("outputEntry3");
  }

  // helper ///////////////////////////////////////////////////////////////////////////////////////////////////////

  public List<DmnClause> getClausesForDecision(String decisionKey) {
    DmnDecisionTable decisionTable = decisionModel.getDecision(decisionKey);
    return decisionTable.getClauses();
  }

  public List<DmnRule> getRulesForDecision(String decisionKey) {
    DmnDecisionTable decisionTable = decisionModel.getDecision(decisionKey);
    return decisionTable.getRules();
  }

}
