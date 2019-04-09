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
package org.camunda.bpm.qa.upgrade.gson;

import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class TaskFilterPropertiesScenario {

  @DescribesScenario("initTaskFilterProperties")
  public static ScenarioSetup initTaskFilterProperties() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        FilterService filterService = engine.getFilterService();

        Filter filterOne = filterService
          .newTaskFilter("taskFilterOne");

        Map<String, Object> primitivesMap = new HashMap<>();
        primitivesMap.put("string", "aStringValue");
        primitivesMap.put("int", 47);
        primitivesMap.put("intOutOfRange", Integer.MAX_VALUE + 1L);
        primitivesMap.put("long", Long.MAX_VALUE);
        primitivesMap.put("double", 3.14159265359D);
        primitivesMap.put("boolean", true);
        primitivesMap.put("null", null);

        filterOne.setProperties(Collections.<String, Object>singletonMap("foo", Collections.singletonList(primitivesMap)));
        filterService.saveFilter(filterOne);

        Filter filterTwo = engine.getFilterService()
          .newTaskFilter("taskFilterTwo");

        List<Object> primitivesList = new ArrayList<>();
        primitivesList.add("aStringValue");
        primitivesList.add(47);
        primitivesList.add(Integer.MAX_VALUE + 1L);
        primitivesList.add(Long.MAX_VALUE);
        primitivesList.add(3.14159265359D);
        primitivesList.add(true);
        primitivesList.add(null);

        filterTwo.setProperties(Collections.<String, Object>singletonMap("foo", Collections.singletonMap("bar", primitivesList)));
        filterService.saveFilter(filterTwo);
      }
    };
  }
}
