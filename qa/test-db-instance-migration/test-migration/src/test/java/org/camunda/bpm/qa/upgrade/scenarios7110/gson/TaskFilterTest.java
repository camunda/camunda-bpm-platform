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
package org.camunda.bpm.qa.upgrade.scenarios7110.gson;

import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.QueryEntityRelationCondition;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

/**
 * @author Tassilo Weidner
 */
@ScenarioUnderTest("TaskFilterScenario")
@Origin("7.11.0")
public class TaskFilterTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  @ScenarioUnderTest("initTaskFilter.1")
  @Test
  public void testTaskFilter() {
    String testString = "test";
    Integer testInteger = 1;
    DelegationState testDelegationState = DelegationState.PENDING;
    Date testDate = new Date(0);
    String[] testActivityInstances = new String[] {"a", "b", "c"};
    String[] testKeys = new String[] {"d", "e"};
    List<String> testCandidateGroups = new ArrayList<>();
    testCandidateGroups.add("group");
    testCandidateGroups.add("anotherGroup");

    String[] variableNames = new String[] {"a", "b", "c", "d", "e", "f"};
    Object[] variableValues = new Object[] {1, 2, "3", "4", 5, 6};

    QueryOperator[] variableOperators = new QueryOperator[] {QueryOperator.EQUALS, QueryOperator.GREATER_THAN_OR_EQUAL, QueryOperator.LESS_THAN, QueryOperator.LIKE, QueryOperator.NOT_EQUALS, QueryOperator.LESS_THAN_OR_EQUAL};
    boolean[] isTaskVariable = new boolean[] {true, true, false, false, false, false};
    boolean[] isProcessVariable = new boolean[] {false, false, true, true, false, false};

    TaskQueryImpl expectedOrderingPropertiesQuery = new TaskQueryImpl();
    expectedOrderingPropertiesQuery.orderByExecutionId().desc();
    expectedOrderingPropertiesQuery.orderByDueDate().asc();
    expectedOrderingPropertiesQuery.orderByProcessVariable("var", ValueType.STRING).desc();

    List<QueryOrderingProperty> expectedOrderingProperties = expectedOrderingPropertiesQuery.getOrderingProperties();

    Filter filter = engineRule.getFilterService().createTaskFilterQuery().filterName("filter").singleResult();

    TaskQueryImpl query = filter.getQuery();
    assertEquals(testString, query.getTaskId());
    assertEquals(testString, query.getName());
    assertEquals(testString, query.getNameNotEqual());
    assertEquals(testString, query.getNameNotLike());
    assertEquals(testString, query.getNameLike());
    assertEquals(testString, query.getDescription());
    assertEquals(testString, query.getDescriptionLike());
    assertEquals(testInteger, query.getPriority());
    assertEquals(testInteger, query.getMinPriority());
    assertEquals(testInteger, query.getMaxPriority());
    assertEquals(testString, query.getAssignee());
    assertEquals(testString, query.getExpressions().get("taskAssignee"));
    assertEquals(testString, query.getAssigneeLike());
    assertEquals(testString, query.getExpressions().get("taskAssigneeLike"));
    assertEquals(testString, query.getInvolvedUser());
    assertEquals(testString, query.getExpressions().get("taskInvolvedUser"));
    assertEquals(testString, query.getOwner());
    assertEquals(testString, query.getExpressions().get("taskOwner"));
    assertTrue(query.isUnassigned());
    assertTrue(query.isAssigned());
    assertEquals(testDelegationState, query.getDelegationState());
    assertEquals(testCandidateGroups, query.getCandidateGroups());
    assertTrue(query.isWithCandidateGroups());
    assertTrue(query.isWithoutCandidateGroups());
    assertTrue(query.isWithCandidateUsers());
    assertTrue(query.isWithoutCandidateUsers());
    assertEquals(testString, query.getExpressions().get("taskCandidateGroupIn"));
    assertEquals(testString, query.getProcessInstanceId());
    assertEquals(testString, query.getExecutionId());
    assertEquals(testActivityInstances.length, query.getActivityInstanceIdIn().length);
    for (int i = 0; i < query.getActivityInstanceIdIn().length; i++) {
      assertEquals(testActivityInstances[i], query.getActivityInstanceIdIn()[i]);
    }
    assertEquals(testDate, query.getCreateTime());
    assertEquals(testString, query.getExpressions().get("taskCreatedOn"));
    assertEquals(testDate, query.getCreateTimeBefore());
    assertEquals(testString, query.getExpressions().get("taskCreatedBefore"));
    assertEquals(testDate, query.getCreateTimeAfter());
    assertEquals(testString, query.getExpressions().get("taskCreatedAfter"));
    assertEquals(testString, query.getKey());
    assertEquals(testKeys.length, query.getKeys().length);
    for (int i = 0; i < query.getKeys().length; i++) {
      assertEquals(testKeys[i], query.getKeys()[i]);
    }
    assertEquals(testString, query.getKeyLike());
    assertEquals(testString, query.getProcessDefinitionKey());
    for (int i = 0; i < query.getProcessDefinitionKeys().length; i++) {
      assertEquals(testKeys[i], query.getProcessDefinitionKeys()[i]);
    }
    assertEquals(testString, query.getProcessDefinitionId());
    assertEquals(testString, query.getProcessDefinitionName());
    assertEquals(testString, query.getProcessDefinitionNameLike());
    assertEquals(testString, query.getProcessInstanceBusinessKey());
    assertEquals(testString, query.getExpressions().get("processInstanceBusinessKey"));
    for (int i = 0; i < query.getProcessInstanceBusinessKeys().length; i++) {
      assertEquals(testKeys[i], query.getProcessInstanceBusinessKeys()[i]);
    }
    assertEquals(testString, query.getProcessInstanceBusinessKeyLike());
    assertEquals(testString, query.getExpressions().get("processInstanceBusinessKeyLike"));

    // variables
    List<TaskQueryVariableValue> variables = query.getVariables();
    for (int i = 0; i < variables.size(); i++) {
      TaskQueryVariableValue variable = variables.get(i);
      assertEquals(variableNames[i], variable.getName());
      assertEquals(variableValues[i], variable.getValue());
      assertEquals(variableOperators[i], variable.getOperator());
      assertEquals(isTaskVariable[i], variable.isLocal());
      assertEquals(isProcessVariable[i], variable.isProcessInstanceVariable());
    }

    assertEquals(testDate, query.getDueDate());
    assertEquals(testString, query.getExpressions().get("dueDate"));
    assertEquals(testDate, query.getDueBefore());
    assertEquals(testString, query.getExpressions().get("dueBefore"));
    assertEquals(testDate, query.getDueAfter());
    assertEquals(testString, query.getExpressions().get("dueAfter"));
    assertEquals(testDate, query.getFollowUpDate());
    assertEquals(testString, query.getExpressions().get("followUpDate"));
    assertEquals(testDate, query.getFollowUpBefore());
    assertEquals(testString, query.getExpressions().get("followUpBefore"));
    assertEquals(testDate, query.getFollowUpAfter());
    assertEquals(testString, query.getExpressions().get("followUpAfter"));
    assertTrue(query.isExcludeSubtasks());
    assertEquals(SuspensionState.SUSPENDED, query.getSuspensionState());
    assertEquals(testString, query.getCaseDefinitionKey());
    assertEquals(testString, query.getCaseDefinitionId());
    assertEquals(testString, query.getCaseDefinitionName());
    assertEquals(testString, query.getCaseDefinitionNameLike());
    assertEquals(testString, query.getCaseInstanceId());
    assertEquals(testString, query.getCaseInstanceBusinessKey());
    assertEquals(testString, query.getCaseInstanceBusinessKeyLike());
    assertEquals(testString, query.getCaseExecutionId());

    // ordering
    verifyOrderingProperties(expectedOrderingProperties, query.getOrderingProperties());
  }

  protected void verifyOrderingProperties(List<QueryOrderingProperty> expectedProperties, List<QueryOrderingProperty> actualProperties) {
    assertEquals(expectedProperties.size(), actualProperties.size());

    for (int i = 0; i < expectedProperties.size(); i++) {
      QueryOrderingProperty expectedProperty = expectedProperties.get(i);
      QueryOrderingProperty actualProperty = actualProperties.get(i);

      assertEquals(expectedProperty.getRelation(), actualProperty.getRelation());
      assertEquals(expectedProperty.getDirection(), actualProperty.getDirection());
      assertEquals(expectedProperty.isContainedProperty(), actualProperty.isContainedProperty());
      assertEquals(expectedProperty.getQueryProperty(), actualProperty.getQueryProperty());

      List<QueryEntityRelationCondition> expectedRelationConditions = expectedProperty.getRelationConditions();
      List<QueryEntityRelationCondition> actualRelationConditions = expectedProperty.getRelationConditions();

      if (expectedRelationConditions != null && actualRelationConditions != null) {
        assertEquals(expectedRelationConditions.size(), actualRelationConditions.size());

        for (QueryEntityRelationCondition expectedFilteringProperty : expectedRelationConditions) {

          assertEquals(expectedFilteringProperty.getProperty(), expectedFilteringProperty.getProperty());
          assertEquals(expectedFilteringProperty.getComparisonProperty(), expectedFilteringProperty.getComparisonProperty());
          assertEquals(expectedFilteringProperty.getScalarValue(), expectedFilteringProperty.getScalarValue());
        }
      } else if (expectedRelationConditions != null || actualRelationConditions != null) {
        fail("Expected filtering properties: " + expectedRelationConditions + ". "
          + "Actual filtering properties: " + actualRelationConditions);
      }
    }
  }

}