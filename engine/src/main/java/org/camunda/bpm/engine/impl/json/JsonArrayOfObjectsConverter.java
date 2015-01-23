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
package org.camunda.bpm.engine.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

/**
 * @author Thorben Lindhauer
 */
public class JsonArrayOfObjectsConverter<T> extends JsonArrayConverter<List<T>> {

  protected JsonObjectConverter<T> objectConverter;

  public JsonArrayOfObjectsConverter(JsonObjectConverter<T> objectConverter) {
    this.objectConverter = objectConverter;
  }

  public JSONArray toJsonArray(List<T> objects) {
    JSONArray jsonArray = new JSONArray();

    for (T object : objects) {
      JSONObject jsonObject = objectConverter.toJsonObject(object);
      jsonArray.put(jsonObject);
    }

    return jsonArray;
  }

  public List<T> toObject(JSONArray jsonArray) {
    List<T> result = new ArrayList<T>();
    for (int i = 0; i < jsonArray.length(); i++) {
      T object = objectConverter.toObject(jsonArray.getJSONObject(i));
      result.add(object);
    }

    return result;
  }
}
