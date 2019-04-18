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

import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Tassilo Weidner
 */
@ScenarioUnderTest("TaskFilterPropertiesScenario")
@Origin("7.11.0")
public class TaskFilterPropertiesTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  @ScenarioUnderTest("initTaskFilterProperties.1")
  @Test
  public void testMapContainingListContainingMapProperty_DeserializePrimitives() {
    FilterService filterService = engineRule.getFilterService();

    // when
    Filter filterOne = filterService.createFilterQuery().filterName("taskFilterOne").singleResult();

    Map deserialisedProperties = filterOne.getProperties();

    List list = (List) deserialisedProperties.get("foo");
    Map map = (Map) list.get(0);

    // then
    assertThat(deserialisedProperties.size(), is(1));
    assertThat((String) map.get("string"), is("aStringValue"));
    assertThat((int) map.get("int"), is(47));
    assertThat((long) map.get("intOutOfRange"), is(Integer.MAX_VALUE + 1L));
    assertThat((long) map.get("long"), is(Long.MAX_VALUE));
    assertThat((double) map.get("double"), is(3.14159265359D));
    assertThat((boolean) map.get("boolean"), is(true));
    assertThat(map.get("null"), nullValue());
  }

  @ScenarioUnderTest("initTaskFilterProperties.1")
  @Test
  public void testMapContainingMapContainingListProperty_DeserializePrimitives() {
    FilterService filterService = engineRule.getFilterService();

    // when
    Filter filterTwo = filterService.createFilterQuery().filterName("taskFilterTwo").singleResult();

    Map deserialisedProperties = filterTwo.getProperties();

    List list = (List) ((Map) deserialisedProperties.get("foo")).get("bar");

    // then
    assertThat(deserialisedProperties.size(), is(1));

    assertThat((String) list.get(0), is("aStringValue"));
    assertThat((int) list.get(1), is(47));
    assertThat((long) list.get(2), is(Integer.MAX_VALUE + 1L));
    assertThat((long) list.get(3), is(Long.MAX_VALUE));
    assertThat((double) list.get(4), is(3.14159265359D));
    assertThat((boolean) list.get(5), is(true));
    assertThat(list.get(6), nullValue());
  }

}