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

package org.camunda.bpm.engine.impl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

/**
 * @author Sebastian Menski
 */
public final class JsonUtil {

  /**
   * Converts a {@link JSONObject} to a {@link Map}. It supports nested {@link JSONObject}
   * and {@link JSONArray}.
   *
   * @param jsonObject the json object to convert
   * @return the resulting map
   */
  public static Map<String, Object> jsonObjectAsMap(JSONObject jsonObject) {
    if (jsonObject == null) {
      return null;
    }
    else {
      Map<String, Object> map = new HashMap<String, Object>();
      Iterator keys = jsonObject.keys();
      while (keys.hasNext()) {
        String key = (String) keys.next();
        if (jsonObject.optJSONObject(key) != null) {
          map.put(key, jsonObjectAsMap(jsonObject.getJSONObject(key)));
        } else if (jsonObject.optJSONArray(key) != null) {
          map.put(key, jsonArrayAsList(jsonObject.getJSONArray(key)));
        } else {
          map.put(key, jsonObject.get(key));
        }
      }
      return map;
    }
  }

  /**
   * Converts a {@link JSONArray} to a {@link List}. It supports nested {@link JSONObject}
   * and {@link JSONArray}.
   *
   * @param jsonArray the json array to convert
   * @return the resulting map
   */
  public static List<Object> jsonArrayAsList(JSONArray jsonArray) {
    if (jsonArray == null) {
      return null;
    }
    else {
      List<Object> list = new ArrayList<Object>();
      for (int i = 0; i < jsonArray.length(); i++) {
        if (jsonArray.optJSONObject(i) != null) {
          list.add(jsonObjectAsMap(jsonArray.getJSONObject(i)));
        } else if (jsonArray.optJSONArray(i) != null) {
          list.add(jsonArrayAsList(jsonArray.getJSONArray(i)));
        } else {
          list.add(jsonArray.get(i));
        }
      }
      return list;
    }
  }

}
