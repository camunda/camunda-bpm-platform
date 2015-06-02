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
package org.camunda.bpm.model.dmn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.camunda.bpm.model.dmn.instance.AllowedValue;
import org.camunda.bpm.model.dmn.instance.BusinessContextElement;
import org.camunda.bpm.model.dmn.instance.Clause;
import org.camunda.bpm.model.dmn.instance.Conclusion;
import org.camunda.bpm.model.dmn.instance.Condition;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.DrgElement;
import org.camunda.bpm.model.dmn.instance.ElementCollection;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.Import;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.dmn.instance.Text;
import org.camunda.bpm.model.dmn.instance.TypeDefinition;
import org.camunda.bpm.model.dmn.util.DmnModelResource;
import org.junit.Test;

public class ExampleTest extends DmnModelTest {

  public static final String EXAMPLE_DMN = "org/camunda/bpm/model/dmn/Example.dmn";

  @Test
  @DmnModelResource(resource = EXAMPLE_DMN)
  public void shouldGetElements() {

    // Definitions
    Definitions definitions = dmnModelInstance.getDefinitions();
    assertThat(definitions).isNotNull();
    assertThat(definitions.getId()).isEqualTo("definitions");
    assertThat(definitions.getName()).isEqualTo("camunda");
    assertThat(definitions.getNamespace()).isEqualTo(TEST_URI);
    assertElementIsEqualToId(definitions, "definitions");

    // Imports
    Collection<Import> imports = definitions.getImports();
    assertThat(imports).isEmpty();

    // Item definitions
    Collection<ItemDefinition> itemDefinitions = definitions.getItemDefinitions();
    assertThat(itemDefinitions).isNotEmpty().hasSize(4);

    Iterator<ItemDefinition> itemDefinitionIterator = itemDefinitions.iterator();

    // 1. Item definition
    ItemDefinition itemDefinition = itemDefinitionIterator.next();
    assertThat(itemDefinition.getId()).isEqualTo("itemDefinition1");
    assertThat(itemDefinition.getName()).isEqualTo("CustomerStatusType");
    assertThat(itemDefinition.getTypeDefinition().getTextContent()).isEqualTo("string");
    assertElementIsEqualToId(itemDefinition, "itemDefinition1");
    Collection<AllowedValue> allowedValues = itemDefinition.getAllowedValues();
    Iterator<AllowedValue> allowedValueIterator = allowedValues.iterator();
    AllowedValue allowedValue = allowedValueIterator.next();
    assertThat(allowedValue.getId()).isEqualTo("allowedValue1");
    assertThat(allowedValue.getText().getTextContent()).isEqualTo("gold");
    allowedValue = allowedValueIterator.next();
    assertThat(allowedValue.getId()).isEqualTo("allowedValue2");
    assertThat(allowedValue.getText().getTextContent()).isEqualTo("silver");
    allowedValue = allowedValueIterator.next();
    assertThat(allowedValue.getId()).isEqualTo("allowedValue3");
    assertThat(allowedValue.getText().getTextContent()).isEqualTo("bronze");

    // 2. Item definition
    itemDefinition = itemDefinitionIterator.next();
    assertThat(itemDefinition.getId()).isEqualTo("itemDefinition2");
    assertThat(itemDefinition.getName()).isEqualTo("OrderSumType");
    assertThat(itemDefinition.getTypeDefinition().getTextContent()).isEqualTo("number");
    assertThat(itemDefinition.getAllowedValues()).isEmpty();
    assertElementIsEqualToId(itemDefinition, "itemDefinition2");

    // 3. Item definition
    itemDefinition = itemDefinitionIterator.next();
    assertThat(itemDefinition.getId()).isEqualTo("itemDefinition3");
    assertThat(itemDefinition.getName()).isEqualTo("CheckResultType");
    assertThat(itemDefinition.getTypeDefinition().getTextContent()).isEqualTo("string");
    assertElementIsEqualToId(itemDefinition, "itemDefinition3");
    allowedValues = itemDefinition.getAllowedValues();
    allowedValueIterator = allowedValues.iterator();
    allowedValue = allowedValueIterator.next();
    assertThat(allowedValue.getId()).isEqualTo("allowedValue4");
    assertThat(allowedValue.getText().getTextContent()).isEqualTo("ok");
    allowedValue = allowedValueIterator.next();
    assertThat(allowedValue.getId()).isEqualTo("allowedValue5");
    assertThat(allowedValue.getText().getTextContent()).isEqualTo("notok");

    // 4. Item definition
    itemDefinition = itemDefinitionIterator.next();
    assertThat(itemDefinition.getId()).isEqualTo("itemDefinition4");
    assertThat(itemDefinition.getName()).isEqualTo("ReasonType");
    assertThat(itemDefinition.getTypeDefinition().getTextContent()).isEqualTo("string");
    assertThat(itemDefinition.getAllowedValues()).isEmpty();
    assertElementIsEqualToId(itemDefinition, "itemDefinition4");

    // DRG elements
    Collection<DrgElement> drgElements = definitions.getDrgElements();
    assertThat(drgElements).isNotEmpty().hasSize(1);

    // Decision
    Decision decision = (Decision) drgElements.iterator().next();
    assertThat(decision).isNotNull();
    assertThat(decision.getId()).isEqualTo("decision");
    assertThat(decision.getName()).isEqualTo("CheckOrder");
    assertElementIsEqualToId(decision, "decision");

    // Decision table
    DecisionTable decisionTable = (DecisionTable) decision.getExpression();
    assertThat(decisionTable).isNotNull();
    assertThat(decisionTable.getId()).isEqualTo("decisionTable");
    assertThat(decisionTable.getName()).isEqualTo("CheckOrder");
    assertThat(decisionTable.isComplete()).isTrue();
    assertThat(decisionTable.isConsistent()).isTrue();
    assertElementIsEqualToId(decisionTable, "decisionTable");

    Collection<Clause> clauses = decisionTable.getClauses();
    assertThat(clauses).isNotEmpty().hasSize(4);
    Iterator<Clause> clauseIterator = clauses.iterator();

    // 1. Clause
    Clause clause = clauseIterator.next();
    assertThat(clause.getName()).isEqualTo("Customer Status");
    InputExpression inputExpression = clause.getInputExpression();
    assertThat(inputExpression.getId()).isEqualTo("inputExpression1");
    assertThat(inputExpression.getName()).isEqualTo("Status");
    assertElementIsEqualToId(inputExpression.getItemDefinition(), "itemDefinition1");
    Collection<InputEntry> inputEntries = clause.getInputEntries();
    Iterator<InputEntry> inputEntryIterator = inputEntries.iterator();
    InputEntry inputEntry = inputEntryIterator.next();
    assertThat(inputEntry.getId()).isEqualTo("inputEntry1");
    assertThat(inputEntry.getText().getTextContent()).isEqualTo("bronze");
    inputEntry = inputEntryIterator.next();
    assertThat(inputEntry.getId()).isEqualTo("inputEntry2");
    assertThat(inputEntry.getText().getTextContent()).isEqualTo("silver");
    inputEntry = inputEntryIterator.next();
    assertThat(inputEntry.getId()).isEqualTo("inputEntry3");
    assertThat(inputEntry.getText().getTextContent()).isEqualTo("gold");

    // 2. Clause
    clause = clauseIterator.next();
    assertThat(clause.getName()).isEqualTo("Order Sum");
    inputExpression = clause.getInputExpression();
    assertThat(inputExpression.getId()).isEqualTo("inputExpression2");
    assertThat(inputExpression.getName()).isEqualTo("Sum");
    assertElementIsEqualToId(inputExpression.getItemDefinition(), "itemDefinition2");
    inputEntries = clause.getInputEntries();
    inputEntryIterator = inputEntries.iterator();
    inputEntry = inputEntryIterator.next();
    assertThat(inputEntry.getId()).isEqualTo("inputEntry4");
    assertThat(inputEntry.getText().getTextContent()).isEqualTo("< 1000");
    inputEntry = inputEntryIterator.next();
    assertThat(inputEntry.getId()).isEqualTo("inputEntry5");
    assertThat(inputEntry.getText().getTextContent()).isEqualTo(">= 1000");

    // 3. Clause
    clause = clauseIterator.next();
    assertThat(clause.getName()).isEqualTo("Check Result");
    assertThat(clause.getInputExpression()).isNull();
    assertThat(clause.getInputEntries()).isEmpty();
    assertElementIsEqualToId(clause.getOutputDefinition(), "itemDefinition3");
    Collection<OutputEntry> outputEntries = clause.getOutputEntries();
    Iterator<OutputEntry> outputEntryIterator = outputEntries.iterator();
    OutputEntry outputEntry = outputEntryIterator.next();
    assertThat(outputEntry.getId()).isEqualTo("outputEntry1");
    assertThat(outputEntry.getText().getTextContent()).isEqualTo("notok");
    outputEntry = outputEntryIterator.next();
    assertThat(outputEntry.getId()).isEqualTo("outputEntry2");
    assertThat(outputEntry.getText().getTextContent()).isEqualTo("ok");

    // 4. Clause
    clause = clauseIterator.next();
    assertThat(clause.getName()).isEqualTo("Reason");
    assertThat(clause.getInputExpression()).isNull();
    assertThat(clause.getInputEntries()).isEmpty();
    assertElementIsEqualToId(clause.getOutputDefinition(), "itemDefinition4");
    outputEntries = clause.getOutputEntries();
    outputEntryIterator = outputEntries.iterator();
    outputEntry = outputEntryIterator.next();
    assertThat(outputEntry.getId()).isEqualTo("outputEntry3");
    assertThat(outputEntry.getText().getTextContent()).isEqualTo("work on your status first, as bronze you're not going to get anything");
    outputEntry = outputEntryIterator.next();
    assertThat(outputEntry.getId()).isEqualTo("outputEntry4");
    assertThat(outputEntry.getText().getTextContent()).isEqualTo("you little fish will get what you want");
    outputEntry = outputEntryIterator.next();
    assertThat(outputEntry.getId()).isEqualTo("outputEntry5");
    assertThat(outputEntry.getText().getTextContent()).isEqualTo("you took too much man, you took too much!");
    outputEntry = outputEntryIterator.next();
    assertThat(outputEntry.getId()).isEqualTo("outputEntry6");
    assertThat(outputEntry.getText().getTextContent()).isEqualTo("you get anything you want");

    // Rules
    Collection<Rule> rules = decisionTable.getRules();
    Iterator<Rule> ruleIterator = rules.iterator();

    // 1. Rule
    Rule rule = ruleIterator.next();
    Collection<Expression> conditions = rule.getConditions();
    assertThat(conditions).hasSize(1);
    Iterator<Expression> conditionsIterator = conditions.iterator();
    Expression condition = conditionsIterator.next();
    assertElementIsEqualToId(condition, "inputEntry1");
    Collection<Expression> conclusions = rule.getConclusions();
    assertThat(conclusions).hasSize(2);
    Iterator<Expression> conclusionIterator = conclusions.iterator();
    Expression conclusion = conclusionIterator.next();
    assertElementIsEqualToId(conclusion, "outputEntry1");
    conclusion = conclusionIterator.next();
    assertElementIsEqualToId(conclusion, "outputEntry3");

    // 2. Rule
    rule = ruleIterator.next();
    conditions = rule.getConditions();
    assertThat(conditions).hasSize(2);
    conditionsIterator = conditions.iterator();
    condition = conditionsIterator.next();
    assertElementIsEqualToId(condition, "inputEntry2");
    condition = conditionsIterator.next();
    assertElementIsEqualToId(condition, "inputEntry4");
    conclusions = rule.getConclusions();
    assertThat(conclusions).hasSize(2);
    conclusionIterator = conclusions.iterator();
    conclusion = conclusionIterator.next();
    assertElementIsEqualToId(conclusion, "outputEntry2");
    conclusion = conclusionIterator.next();
    assertElementIsEqualToId(conclusion, "outputEntry4");

    // 3. Rule
    rule = ruleIterator.next();
    conditions = rule.getConditions();
    assertThat(conditions).hasSize(2);
    conditionsIterator = conditions.iterator();
    condition = conditionsIterator.next();
    assertElementIsEqualToId(condition, "inputEntry2");
    condition = conditionsIterator.next();
    assertElementIsEqualToId(condition, "inputEntry5");
    conclusions = rule.getConclusions();
    assertThat(conclusions).hasSize(2);
    conclusionIterator = conclusions.iterator();
    conclusion = conclusionIterator.next();
    assertElementIsEqualToId(conclusion, "outputEntry1");
    conclusion = conclusionIterator.next();
    assertElementIsEqualToId(conclusion, "outputEntry5");

    // 4. Rule
    rule = ruleIterator.next();
    conditions = rule.getConditions();
    assertThat(conditions).hasSize(1);
    conditionsIterator = conditions.iterator();
    condition = conditionsIterator.next();
    assertElementIsEqualToId(condition, "inputEntry3");
    conclusions = rule.getConclusions();
    assertThat(conclusions).hasSize(2);
    conclusionIterator = conclusions.iterator();
    conclusion = conclusionIterator.next();
    assertElementIsEqualToId(conclusion, "outputEntry2");
    conclusion = conclusionIterator.next();
    assertElementIsEqualToId(conclusion, "outputEntry6");

    // Element collections
    Collection<ElementCollection> elementCollections = definitions.getElementCollections();
    assertThat(elementCollections).isEmpty();

    // Business contexts
    Collection<BusinessContextElement> businessContextElements = definitions.getBusinessContextElements();
    assertThat(businessContextElements).isEmpty();

  }

  @Test
  public void shouldWriteElements() throws Exception {
    dmnModelInstance = Dmn.createEmptyModel();

    // Definitions
    Definitions definitions = generateElement(Definitions.class);
    definitions.setName("camunda");
    definitions.setNamespace(TEST_URI);
    dmnModelInstance.setDocumentElement(definitions);

    // 1. Item definition
    ItemDefinition itemDefinition1 = generateElement(ItemDefinition.class, 1);
    itemDefinition1.setName("CustomerStatusType");
    TypeDefinition typeDefinition = generateElement(TypeDefinition.class);
    typeDefinition.setTextContent("string");
    itemDefinition1.setTypeDefinition(typeDefinition);
    AllowedValue allowedValue = generateElement(AllowedValue.class, 1);
    Text text = generateElement(Text.class);
    text.setTextContent("gold");
    allowedValue.setText(text);
    itemDefinition1.getAllowedValues().add(allowedValue);
    allowedValue = generateElement(AllowedValue.class, 2);
    text = generateElement(Text.class);
    text.setTextContent("silver");
    allowedValue.setText(text);
    itemDefinition1.getAllowedValues().add(allowedValue);
    allowedValue = generateElement(AllowedValue.class, 3);
    text = generateElement(Text.class);
    text.setTextContent("bronze");
    allowedValue.setText(text);
    itemDefinition1.getAllowedValues().add(allowedValue);
    definitions.addChildElement(itemDefinition1);

    // 2. Item Definition
    ItemDefinition itemDefinition2 = generateElement(ItemDefinition.class, 2);
    itemDefinition2.setName("OrderSumType");
    typeDefinition = generateElement(TypeDefinition.class);
    typeDefinition.setTextContent("number");
    itemDefinition2.setTypeDefinition(typeDefinition);
    definitions.addChildElement(itemDefinition2);

    // 3. Item definition
    ItemDefinition itemDefinition3 = generateElement(ItemDefinition.class, 3);
    itemDefinition3.setName("CheckResultType");
    typeDefinition = generateElement(TypeDefinition.class);
    typeDefinition.setTextContent("string");
    itemDefinition3.setTypeDefinition(typeDefinition);
    allowedValue = generateElement(AllowedValue.class, 4);
    text = generateElement(Text.class);
    text.setTextContent("ok");
    allowedValue.setText(text);
    itemDefinition3.getAllowedValues().add(allowedValue);
    allowedValue = generateElement(AllowedValue.class, 5);
    text = generateElement(Text.class);
    text.setTextContent("notok");
    allowedValue.setText(text);
    itemDefinition3.getAllowedValues().add(allowedValue);
    definitions.addChildElement(itemDefinition3);

    // 4. Item Definition
    ItemDefinition itemDefinition4 = generateElement(ItemDefinition.class, 4);
    itemDefinition4.setName("ReasonType");
    typeDefinition = generateElement(TypeDefinition.class);
    typeDefinition.setTextContent("string");
    itemDefinition4.setTypeDefinition(typeDefinition);
    definitions.addChildElement(itemDefinition4);

    // Decision
    Decision decision = generateElement(Decision.class);
    decision.setName("CheckOrder");
    definitions.addChildElement(decision);

    // Decision table
    DecisionTable decisionTable = generateElement(DecisionTable.class);
    decisionTable.setName("CheckOrder");
    decisionTable.setComplete(true);
    decisionTable.setConsistent(true);
    decision.addChildElement(decisionTable);

    // 1. Clause
    Clause clause = generateElement(Clause.class);
    clause.setName("Customer Status");
    InputExpression inputExpression = generateElement(InputExpression.class, 1);
    inputExpression.setName("Status");
    inputExpression.setItemDefinition(itemDefinition1);
    clause.setInputExpression(inputExpression);
    InputEntry inputEntry1 = generateElement(InputEntry.class, 1);
    text = generateElement(Text.class);
    text.setTextContent("bronze");
    inputEntry1.setText(text);
    clause.getInputEntries().add(inputEntry1);
    InputEntry inputEntry2 = generateElement(InputEntry.class, 2);
    text = generateElement(Text.class);
    text.setTextContent("silver");
    inputEntry2.setText(text);
    clause.getInputEntries().add(inputEntry2);
    InputEntry inputEntry3 = generateElement(InputEntry.class, 3);
    text = generateElement(Text.class);
    text.setTextContent("gold");
    inputEntry3.setText(text);
    clause.getInputEntries().add(inputEntry3);
    decisionTable.getClauses().add(clause);

    // 2. Clause
    clause = generateElement(Clause.class);
    clause.setName("Order Sum");
    inputExpression = generateElement(InputExpression.class, 2);
    inputExpression.setName("Sum");
    inputExpression.setItemDefinition(itemDefinition2);
    clause.setInputExpression(inputExpression);
    InputEntry inputEntry4 = generateElement(InputEntry.class, 4);
    text = generateElement(Text.class);
    text.getDomElement().addCDataSection("< 1000");
    inputEntry4.setText(text);
    clause.getInputEntries().add(inputEntry4);
    InputEntry inputEntry5 = generateElement(InputEntry.class, 5);
    text = generateElement(Text.class);
    text.getDomElement().addCDataSection(">= 1000");
    inputEntry5.setText(text);
    clause.getInputEntries().add(inputEntry5);
    decisionTable.getClauses().add(clause);

    // 3. Clause
    clause = generateElement(Clause.class);
    clause.setName("Check Result");
    clause.setOutputDefinition(itemDefinition3);
    OutputEntry outputEntry1 = generateElement(OutputEntry.class, 1);
    text = generateElement(Text.class);
    text.setTextContent("notok");
    outputEntry1.setText(text);
    clause.getOutputEntries().add(outputEntry1);
    OutputEntry outputEntry2 = generateElement(OutputEntry.class, 2);
    text = generateElement(Text.class);
    text.setTextContent("ok");
    outputEntry2.setText(text);
    clause.getOutputEntries().add(outputEntry2);
    decisionTable.getClauses().add(clause);

    // 4. Clause
    clause = generateElement(Clause.class);
    clause.setName("Reason");
    clause.setOutputDefinition(itemDefinition4);
    OutputEntry outputEntry3 = generateElement(OutputEntry.class, 3);
    text = generateElement(Text.class);
    text.getDomElement().addCDataSection("work on your status first, as bronze you're not going to get anything");
    outputEntry3.setText(text);
    clause.getOutputEntries().add(outputEntry3);
    OutputEntry outputEntry4 = generateElement(OutputEntry.class, 4);
    text = generateElement(Text.class);
    text.setTextContent("you little fish will get what you want");
    outputEntry4.setText(text);
    clause.getOutputEntries().add(outputEntry4);
    decisionTable.getClauses().add(clause);
    OutputEntry outputEntry5 = generateElement(OutputEntry.class, 5);
    text = generateElement(Text.class);
    text.setTextContent("you took too much man, you took too much!");
    outputEntry5.setText(text);
    clause.getOutputEntries().add(outputEntry5);
    decisionTable.getClauses().add(clause);
    OutputEntry outputEntry6 = generateElement(OutputEntry.class, 6);
    text = generateElement(Text.class);
    text.setTextContent("you get anything you want");
    outputEntry6.setText(text);
    clause.getOutputEntries().add(outputEntry6);
    decisionTable.getClauses().add(clause);

    // 1. Rule
    Rule rule = generateElement(Rule.class);
    rule.getConditions().add(inputEntry1);
    rule.getConclusions().addAll(Arrays.asList(outputEntry1, outputEntry3));
    decisionTable.getRules().add(rule);

    // 2. Rule
    rule = generateElement(Rule.class);
    rule.getConditions().add(inputEntry2);
    rule.getConditions().add(inputEntry4);
    rule.getConclusions().add(outputEntry2);
    rule.getConclusions().add(outputEntry4);
    decisionTable.getRules().add(rule);

    // 3. Rule
    rule = generateElement(Rule.class);
    Condition condition = generateElement(Condition.class);
    condition.setTextContent(inputEntry2.getId() + " " + inputEntry5.getId());
    rule.addChildElement(condition);
    Conclusion conclusion = generateElement(Conclusion.class);
    conclusion.setTextContent(outputEntry1.getId() + " " + outputEntry5.getId());
    rule.addChildElement(conclusion);
    decisionTable.getRules().add(rule);

    // 4. Rule
    rule = generateElement(Rule.class);
    rule.getConditions().add(inputEntry3);
    rule.getConclusions().add(outputEntry2);
    rule.getConclusions().add(outputEntry6);
    decisionTable.getRules().add(rule);

    assertModelEqualsFile(EXAMPLE_DMN);
  }


}
