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
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tassilo Weidner
 */
@ScenarioUnderTest("TaskFilterVariablesScenario")
@Origin("7.11.0")
public class TaskFilterVariablesTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  @ScenarioUnderTest("initTaskFilterVariables.1")
  @Test
  public void testTaskFilterVariables() {
    // boolean filter
    Filter filter = engineRule.getFilterService().createFilterQuery()
      .filterName("filterBooleanVariable")
      .singleResult();

    assertThat(engineRule.getFilterService().count(filter.getId()), is(1L));

    // int filter
    filter = engineRule.getFilterService().createFilterQuery()
      .filterName("filterIntVariable")
      .singleResult();

    assertThat(engineRule.getFilterService().count(filter.getId()), is(1L));

    // int out of range filter
    filter = engineRule.getFilterService().createFilterQuery()
      .filterName("filterIntOutOfRangeVariable")
      .singleResult();

    assertThat(engineRule.getFilterService().count(filter.getId()), is(1L));

    // double filter
    filter = engineRule.getFilterService().createFilterQuery()
      .filterName("filterDoubleVariable")
      .singleResult();

    assertThat(engineRule.getFilterService().count(filter.getId()), is(1L));

    // string filter
    filter = engineRule.getFilterService().createFilterQuery()
      .filterName("filterStringVariable")
      .singleResult();

    assertThat(engineRule.getFilterService().count(filter.getId()), is(1L));

    // null filter
    filter = engineRule.getFilterService().createFilterQuery()
      .filterName("filterNullVariable")
      .singleResult();

    assertThat(engineRule.getFilterService().count(filter.getId()), is(1L));
  }

}