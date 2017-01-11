/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

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

}
