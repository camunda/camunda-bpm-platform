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
package org.camunda.bpm.qa.upgrade.scenarios7150.variable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("EmptyStringVariableScenario")
@Origin("7.14.0")
public class EmptyStringVariableTest {

  @Rule
  public UpgradeTestRule engineRule = new UpgradeTestRule();

  RuntimeService runtimeService;

  @Before
  public void assignServices() {
    runtimeService = engineRule.getRuntimeService();
  }

  @Test
  @ScenarioUnderTest("emptyStringVariableScenario.1")
  public void shouldFindEmptyStringVariable() {
    // given
    VariableInstanceQuery variableQuery = runtimeService.createVariableInstanceQuery().variableName("myStringVar");

    // when
    VariableInstance variable = variableQuery.singleResult();

    // then
    assertNotNull(variable);
    if (DbSqlSessionFactory.ORACLE.equals(engineRule.getProcessEngineConfiguration().getDatabaseType())) {
      assertNull(variable.getValue());
    } else {
      assertEquals("", variable.getValue());
    }
  }

}
