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
package org.camunda.bpm.engine.test.api.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Sebastian Menski
 */
public class FilterPropertiesTest extends PluggableProcessEngineTestCase {

  protected Filter filter;
  protected String nestedJsonObject = "{\"id\":\"nested\"}";
  protected String nestedJsonArray = "[\"a\",\"b\"]";


  public void setUp() {
    filter = filterService.newTaskFilter("name").setOwner("owner").setProperties(new HashMap<String, Object>());
  }

  protected void tearDown() throws Exception {
    if (filter.getId() != null)
    {
      filterService.deleteFilter(filter.getId());
    }
  }


  public void testPropertiesFromNull() {
    filter.setProperties(null);
    assertNull(filter.getProperties());

    filter.setProperties((Map<String, Object>) null);
    assertNull(filter.getProperties());
  }

  public void testPropertiesFromMap() {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("color", "#123456");
    properties.put("priority", 42);
    properties.put("userDefined", true);
    properties.put("object", nestedJsonObject);
    properties.put("array", nestedJsonArray);
    filter.setProperties(properties);

    assertTestProperties();
  }

  protected void assertTestProperties() {
    filterService.saveFilter(filter);
    filter = filterService.getFilter(filter.getId());

    Map<String, Object> properties = filter.getProperties();
    assertEquals(5, properties.size());
    assertEquals("#123456", properties.get("color"));
    assertEquals(42, properties.get("priority"));
    assertEquals(true, properties.get("userDefined"));
    assertEquals(nestedJsonObject, properties.get("object"));
    assertEquals(nestedJsonArray, properties.get("array"));
  }

  public void testNullProperty() {
    // given
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("null", null);
    filter.setProperties(properties);
    filterService.saveFilter(filter);

    // when
    filter = filterService.getFilter(filter.getId());

    // then
    Map<String, Object> persistentProperties = filter.getProperties();
    assertEquals(1, persistentProperties.size());
    assertTrue(persistentProperties.containsKey("null"));
    assertNull(persistentProperties.get("null"));

  }

  public void testMapContainingListProperty() {
    // given
    Map properties = Collections.singletonMap("foo", Collections.singletonList("bar"));

    filter.setProperties(properties);
    filterService.saveFilter(filter);

    // when
    filter = filterService.getFilter(filter.getId());

    Map deserialisedProperties = filter.getProperties();
    List list = (List) deserialisedProperties.get("foo");
    Object string = list.get(0);

    // then
    assertThat(deserialisedProperties.size(), is(1));
    assertThat(string, instanceOf(String.class));
    assertThat(string.toString(), is("bar"));
  }

  public void testMapContainingMapProperty() {
    // given
    Map properties = Collections.singletonMap("foo", Collections.singletonMap("bar", "foo"));

    filter.setProperties(properties);
    filterService.saveFilter(filter);

    // when
    filter = filterService.getFilter(filter.getId());

    Map deserialisedProperties = filter.getProperties();

    Map map = (Map) deserialisedProperties.get("foo");
    Object string = map.get("bar");

    // then
    assertThat(deserialisedProperties.size(), is(1));
    assertThat(string.toString(), is("foo"));
  }

  public void testMapContainingMapContainingListProperty() {
    // given
    Map properties = Collections.singletonMap("foo", Collections.singletonMap("bar", Collections.singletonList("foo")));

    filter.setProperties(properties);
    filterService.saveFilter(filter);

    // when
    filter = filterService.getFilter(filter.getId());

    Map deserialisedProperties = filter.getProperties();

    Map map = (Map) deserialisedProperties.get("foo");
    List list = (List) map.get("bar");
    Object string = list.get(0);

    // then
    assertThat(deserialisedProperties.size(), is(1));
    assertThat(string.toString(), is("foo"));
  }

  public void testMapContainingListContainingMapProperty_DeserializePrimitives() {
    // given
    Map<String, Object> primitives = new HashMap<>();
    primitives.put("string", "aStringValue");
    primitives.put("int", 47);
    primitives.put("intOutOfRange", Integer.MAX_VALUE + 1L);
    primitives.put("long", Long.MAX_VALUE);
    primitives.put("double", 3.14159265359D);
    primitives.put("boolean", true);
    primitives.put("null", null);

    Map properties = Collections.singletonMap("foo", Collections.singletonList(primitives));

    filter.setProperties(properties);
    filterService.saveFilter(filter);

    // when
    filter = filterService.getFilter(filter.getId());

    Map deserialisedProperties = filter.getProperties();

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

  public void testMapContainingMapContainingListProperty_DeserializePrimitives() {
    // given
    List<Object> primitives = new ArrayList<>();
    primitives.add("aStringValue");
    primitives.add(47);
    primitives.add(Integer.MAX_VALUE + 1L);
    primitives.add(Long.MAX_VALUE);
    primitives.add(3.14159265359D);
    primitives.add(true);
    primitives.add(null);

    Map properties = Collections.singletonMap("foo", Collections.singletonMap("bar", primitives));

    filter.setProperties(properties);
    filterService.saveFilter(filter);

    // when
    filter = filterService.getFilter(filter.getId());

    Map deserialisedProperties = filter.getProperties();

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
