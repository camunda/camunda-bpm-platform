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
package org.camunda.bpm.engine.rest.helper;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.variable.VariableMap;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.ArgumentMatcher;

public class EqualsVariableMap extends ArgumentMatcher<Map<String, Object>> {

  protected Map<String, Matcher<?>> matchers;

  public EqualsVariableMap() {
  }

  @Override
  public boolean matches(Object argument) {
    return matchesMatchers(argument);
  }

  protected boolean matchesMatchers(Object argument) {
    if (argument == null) {
      return false;
    }

    VariableMap argumentMap = (VariableMap) argument;

    boolean containSameKeys = matchers.keySet().containsAll(argumentMap.keySet()) &&
        argumentMap.keySet().containsAll(matchers.keySet());
    if (!containSameKeys) {
      return false;
    }

    for (String key : argumentMap.keySet()) {
      Matcher<?> matcher = matchers.get(key);
      if (!matcher.matches(argumentMap.getValueTyped(key))) {
        return false;
      }
    }

    return true;
  }

  public static EqualsVariableMap matchesExactly(Map<String, Matcher<?>> matchers) {
    EqualsVariableMap matcher = new EqualsVariableMap();
    matcher.matchers = matchers;
    return matcher;
  }

  public static EqualsVariableMap matches() {
    return new EqualsVariableMap();
  }

  public EqualsVariableMap matcher(String key, Matcher<?> matcher) {
    if (matchers == null) {
      this.matchers = new HashMap<String, Matcher<?>>();
    }

    matchers.put(key, matcher);
    return this;
  }

  public void describeTo(Description description) {
    description.appendText("EqualsVariableMap: {");
    for (Map.Entry<String, Matcher<?>> matcher : matchers.entrySet()) {
      description.appendText(matcher.getKey() + " => ");
      description.appendDescriptionOf(matcher.getValue());
    }
    description.appendText("}");

  }

}
