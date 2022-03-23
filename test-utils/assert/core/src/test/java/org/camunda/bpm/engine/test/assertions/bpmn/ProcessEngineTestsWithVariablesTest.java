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
package org.camunda.bpm.engine.test.assertions.bpmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class ProcessEngineTestsWithVariablesTest {

  List<Object> keys;
  List<Object> values;
  Map<String, Object> expectedMap;

  public ProcessEngineTestsWithVariablesTest(String key1, Object value1, String key2, Object value2, String key3, Object value3) {

    expectedMap = new HashMap<>();
    if (key1 != null)
      expectedMap.put(key1, value1);
    if (key2 != null)
      expectedMap.put(key2, value2);
    if (key3 != null)
      expectedMap.put(key3, value3);

    keys = new ArrayList<>();
    if (key1 != null)
    keys.add(key1);
    if (key2 != null)
    keys.add(key2);
    if (key3 != null)
    keys.add(key3);

    values = new ArrayList<>();
    if (key1 != null)
    values.add(value1);
    if (key2 != null)
    values.add(value2);
    if (key3 != null)
    values.add(value3);

  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    Object[][] data = new Object[][] {
      { "key1", 1   , null  , null, null  , null },
      { "key1", 1   , "key2", 2   , null  , null },
      { null  , null, "key2", 2   , null  , null },
      { "key1", 1   , "key2", 2   , "key3", 3    }
    };
    return Arrays.asList(data);
  }

  @Test
  public void testWithVariables() {
    // When we simply constuct the map with the given data
    Map<String, Object> returnedMap = returnedMap(keys, values);
    // Then we expect to find the expected Map
    assertThat(returnedMap).isEqualTo(expectedMap);
  }

  @Test
  public void testWithVariables_NoStringKeys() {
    // Given we replace the last key with its integer value
    keys.set(keys.size() - 1, values.get(values.size() - 1));
    // When we construct the variables map
    try {
      returnedMap(keys, values);
    // Then we expect an exception to be thrown
    } catch (Throwable t) {
      assertThat(t).isInstanceOfAny(ClassCastException.class, IllegalArgumentException.class, AssertionError.class);
      return;
    }
    fail("IllegalArgumentException or AssertionError expected!");
  }

  @Test
  public void testWithVariables_NullKeys() {
    // Given we replace the last key with a null pointer
    keys.set(keys.size() - 1, null);
    // When we construct the variables map
    try {
      returnedMap(keys, values);
    // Then we expect an exception to be thrown
    } catch (Throwable t) {
      assertThat(t).isInstanceOfAny(IllegalArgumentException.class, AssertionError.class);
      return;
    }
    fail("IllegalArgumentException expected!");
  }

  @Test
  public void testWithVariables_NullValues() {
    // Given we replace all values with a null pointer
    int idx = values.size();
    while (idx > 0)
      values.set(--idx, null);
    // When we construct the variables map
    Map<String, Object> returnedMap = returnedMap(keys, values);
    // Then we expect the keys to match the expectedMap keys
    assertThat(returnedMap.keySet()).isEqualTo(expectedMap.keySet());
    // And we expect all the values to be null
    assertThat(returnedMap.values()).containsOnly((Object) null);
  }

  private static Map<String, Object> returnedMap(List<Object> keys, List<Object> values) {
    Map<String, Object> returnedMap;
    if (keys.size() > 2)
      returnedMap = withVariables((String) keys.get(0), values.get(0), keys.get(1), values.get(1), keys.get(2), values.get(2));
    else if (keys.size() > 1)
      returnedMap = withVariables((String) keys.get(0), values.get(0), keys.get(1), values.get(1));
    else
      returnedMap = withVariables((String) keys.get(0), values.get(0));
    return returnedMap;
  }

}
