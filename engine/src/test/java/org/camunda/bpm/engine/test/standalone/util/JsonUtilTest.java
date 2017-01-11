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

package org.camunda.bpm.engine.test.standalone.util;

import static org.camunda.bpm.engine.impl.util.JsonUtil.jsonArrayAsList;
import static org.camunda.bpm.engine.impl.util.JsonUtil.jsonObjectAsMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.junit.Test;

/**
 * @author Sebastian Menski
 */
public class JsonUtilTest {

  @Test
  @SuppressWarnings("unchecked")
  public void testJsonObjectToMap() {
    assertNull(jsonObjectAsMap(null));

    JSONObject jsonObject = new JSONObject();

    Map<String, Object> map = jsonObjectAsMap(jsonObject);
    assertTrue(map.isEmpty());

    jsonObject.put("boolean", true);
    jsonObject.put("int", 12);
    jsonObject.put("double", 11.1);
    jsonObject.put("long", 13l);
    jsonObject.put("string", "test");
    jsonObject.put("list", Collections.singletonList("test"));
    jsonObject.put("map", Collections.singletonMap("test", "test"));
    jsonObject.put("date", new Date(0));
    jsonObject.put("null", JSONObject.NULL);

    map = jsonObjectAsMap(jsonObject);
    assertEquals(9, map.size());

    assertEquals(true, map.get("boolean"));
    assertEquals(12, map.get("int"));
    assertEquals(11.1, map.get("double"));
    assertEquals(13l, map.get("long"));
    assertEquals("test", map.get("string"));

    List<Object> embeddedList = (List<Object>) map.get("list");
    assertEquals(1, embeddedList.size());
    assertEquals("test", embeddedList.get(0));

    Map<String, Object> embeddedMap = (Map<String, Object>) map.get("map");
    assertEquals(1, embeddedMap.size());
    assertTrue(embeddedMap.containsKey("test"));
    assertEquals("test", embeddedMap.get("test"));

    Date embeddedDate = (Date) map.get("date");
    assertEquals(new Date(0), embeddedDate);

    assertTrue(map.containsKey("null"));
    assertNull(map.get("null"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testJsonArrayToList() {
    assertNull(jsonArrayAsList(null));

    JSONArray jsonArray = new JSONArray();

    List<Object> list = jsonArrayAsList(jsonArray);
    assertTrue(list.isEmpty());

    jsonArray.put(true);
    jsonArray.put(12);
    jsonArray.put(11.1);
    jsonArray.put(13l);
    jsonArray.put("test");
    jsonArray.put(Collections.singletonList("test"));
    jsonArray.put(Collections.singletonMap("test", "test"));
    jsonArray.put(new Date(0));
    jsonArray.put(JSONObject.NULL);

    list = jsonArrayAsList(jsonArray);
    assertEquals(9, list.size());

    assertEquals(true, list.get(0));
    assertEquals(12, list.get(1));
    assertEquals(11.1, list.get(2));
    assertEquals(13l, list.get(3));
    assertEquals("test", list.get(4));

    List<Object> embeddedList = (List<Object>) list.get(5);
    assertEquals(1, embeddedList.size());
    assertEquals("test", embeddedList.get(0));

    Map<String, Object> embeddedMap = (Map<String, Object>) list.get(6);
    assertEquals(1, embeddedMap.size());
    assertTrue(embeddedMap.containsKey("test"));
    assertEquals("test", embeddedMap.get("test"));

    Date embeddedDate = (Date) list.get(7);
    assertEquals(new Date(0), embeddedDate);

    Object null_ = list.get(8);
    assertNull(null_);
  }

  @Test
  public void testJsonObjectNullRetained() {
    String json = "{\"key\":null}";

    JSONObject object = new JSONObject(json);
    assertTrue(object.has("key"));

    Map<String, Object> map = jsonObjectAsMap(object);
    assertTrue(map.containsKey("key"));
    assertNull(map.get("key"));
  }

  @Test
  public void testJsonArrayNullRetained() {
    String json = "[null]";

    JSONArray array = new JSONArray(json);
    assertEquals(1, array.length());

    List<Object> list = jsonArrayAsList(array);
    assertEquals(1, list.size());
    assertNull(list.get(0));
  }

}
