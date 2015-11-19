/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.spin.plugin.variables;

import java.util.ArrayList;
import java.util.List;

public class JsonListSerializable<T> {

  private List<T> listProperty;

  public JsonListSerializable() {
    this.listProperty = new ArrayList<T>();
  }

  public void addElement(T element) {
    this.listProperty.add(element);
  }

  public List<T> getListProperty() {
    return listProperty;
  }

  public void setListProperty(List<T> listProperty) {
    this.listProperty = listProperty;
  }

  public String toExpectedJsonString() {
    StringBuilder jsonBuilder = new StringBuilder();

    jsonBuilder.append("{\"listProperty\":[");
    for (int i = 0; i < listProperty.size(); i++) {
      jsonBuilder.append(listProperty.get(i));
      if (i < listProperty.size() - 1) {
        jsonBuilder.append(",");
      }
    }
    jsonBuilder.append("]}");

    return jsonBuilder.toString();
  }

  public String toString() {
    return toExpectedJsonString();
  }


}
