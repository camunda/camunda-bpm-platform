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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.persistence.entity.FilterEntity;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Sebastian Menski
 */
public class FilterPropertiesTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected FilterService filterService;
  protected Filter filter;
  protected String nestedJsonObject = "{\"id\":\"nested\"}";
  protected String nestedJsonArray = "[\"a\",\"b\"]";

  @Before
  public void setUp() {
    filterService = engineRule.getFilterService();
    filter = filterService.newTaskFilter("name").setOwner("owner").setProperties(new HashMap<>());
  }

  @After
  public void tearDown() throws Exception {
    for (Filter filter : filterService.createFilterQuery().list()) {
      filterService.deleteFilter(filter.getId());
    }
  }


  @Test
  public void testPropertiesFromNull() {
    filter.setProperties(null);
    assertNull(filter.getProperties());
  }

  @Test
  public void testPropertiesInternalFromNull() {
    // given
    Filter noPropsFilter = filterService.
        newTaskFilter("no props filter")
        .setOwner("demo")
        .setProperties(null);
    filterService.saveFilter(noPropsFilter);

    // when
    FilterEntity noPropsFilterEntity = (FilterEntity) filterService
        .createTaskFilterQuery()
        .filterOwner("demo")
        .singleResult();

    // then
    assertThat(noPropsFilterEntity.getPropertiesInternal(), is("{}"));
  }

  @Test
  public void testPropertiesFromMap() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("color", "#123456");
    properties.put("priority", 42);
    properties.put("userDefined", true);
    properties.put("object", nestedJsonObject);
    properties.put("array", nestedJsonArray);
    filter.setProperties(properties);

    assertTestProperties();
  }

  @Test
  public void testNullProperty() {
    // given
    Map<String, Object> properties = new HashMap<>();
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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
    assertThat(map.get("string"), is("aStringValue"));
    assertThat(map.get("int"), is(47));
    assertThat(map.get("intOutOfRange"), is(Integer.MAX_VALUE + 1L));
    assertThat(map.get("long"), is(Long.MAX_VALUE));
    assertThat(map.get("double"), is(3.14159265359D));
    assertThat(map.get("boolean"), is(true));
    assertThat(map.get("null"), nullValue());
  }

  @Test
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

    assertThat(list.get(0), is("aStringValue"));
    assertThat(list.get(1), is(47));
    assertThat(list.get(2), is(Integer.MAX_VALUE + 1L));
    assertThat(list.get(3), is(Long.MAX_VALUE));
    assertThat(list.get(4), is(3.14159265359D));
    assertThat(list.get(5), is(true));
    assertThat(list.get(6), nullValue());
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
}
