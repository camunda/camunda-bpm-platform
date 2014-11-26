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

package org.camunda.bpm.engine.impl.json;

import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

/**
 * @author Sebastian Menski
 */
public class JsonTaskQueryVariableValueConverter extends JsonObjectConverter<TaskQueryVariableValue> {

  public JSONObject toJsonObject(TaskQueryVariableValue variable) {
    JSONObject json = new JSONObject();
    json.put("name", variable.getName());
    json.put("value", variable.getValue());
    json.put("operator", variable.getOperator());
    return json;
  }

  public TaskQueryVariableValue toObject(JSONObject json) {
    String name = json.getString("name");
    Object value = json.get("value");
    QueryOperator operator = QueryOperator.valueOf(json.getString("operator"));
    boolean isTaskVariable = json.getBoolean("taskVariable");
    boolean isProcessVariable = json.getBoolean("processVariable");
    return new TaskQueryVariableValue(name, value, operator, isTaskVariable, isProcessVariable);
  }
}
