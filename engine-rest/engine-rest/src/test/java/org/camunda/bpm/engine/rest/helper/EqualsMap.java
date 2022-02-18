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
package org.camunda.bpm.engine.rest.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.camunda.bpm.engine.variable.VariableMap;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class EqualsMap extends BaseMatcher<Map<String, Object>> {

  protected Map<String, Object> mapToCompare;
  protected Map<String, Matcher<?>> matchers;

  public EqualsMap() {
  }

  public EqualsMap(Map<String, Object> mapToCompare) {
    this.mapToCompare = mapToCompare;
  }

  @Override
  public boolean matches(Object argument) {
    if (mapToCompare != null) {
      return matchesExactly(argument);
    } else if (matchers != null) {
      return matchesMatchers(argument);
    } else {
      return argument == null;
    }
  }

  protected boolean matchesExactly(Object argument) {
    if (argument == null) {
      return false;
    }

    Map<String, Object> argumentMap = (Map<String, Object>) argument;

    Set<Entry<String, Object>> setToCompare = mapToCompare.entrySet();
    Set<Entry<String, Object>> argumentSet = argumentMap.entrySet();

    return setToCompare.equals(argumentSet);
  }

  protected boolean matchesMatchers(Object argument) {
    if (argument == null) {
      return false;
    }

    Map<String, Object> argumentMap = (Map<String, Object>) argument;

    boolean containSameKeys = matchers.keySet().containsAll(argumentMap.keySet()) &&
        argumentMap.keySet().containsAll(matchers.keySet());
    if (!containSameKeys) {
      return false;
    }

    for (String key : argumentMap.keySet()) {
      Matcher<?> matcher = matchers.get(key);
      Object value = null;
      if (argumentMap instanceof VariableMap) {
        VariableMap varMap = (VariableMap) argumentMap;
        value = varMap.getValueTyped(key);
      }
      else {
        value = argumentMap.get(key);
      }
      if (!matcher.matches(value)) {
        return false;
      }
    }


    return true;
  }

  public static EqualsMap containsExactly(Map<String, Object> map) {
    EqualsMap matcher = new EqualsMap();
    matcher.mapToCompare = map;
    return matcher;
  }

  public static EqualsMap matchesExactly(Map<String, Matcher<?>> matchers) {
    EqualsMap matcher = new EqualsMap();
    matcher.matchers = matchers;
    return matcher;
  }

  public EqualsMap matcher(String key, Matcher<?> matcher) {
    if (matchers == null) {
      this.matchers = new HashMap<String, Matcher<?>>();
    }

    matchers.put(key, matcher);
    return this;
  }

  public void describeTo(Description description) {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getSimpleName());
    sb.append(": ");
    sb.append("mapToCompare=");
    sb.append(mapToCompare);
    sb.append(" ");
    sb.append("matchers=");
    sb.append(matchers);

    description.appendText(sb.toString());
  }

}
