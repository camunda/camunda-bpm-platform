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
package org.camunda.bpm.engine.impl.json;

import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import com.google.gson.JsonObject;
import org.camunda.bpm.engine.impl.util.JsonUtil;

/**
 * @author Sebastian Menski
 */
public class JsonTaskQueryVariableValueConverter extends JsonObjectConverter<TaskQueryVariableValue> {

  public JsonObject toJsonObject(TaskQueryVariableValue variable) {
    JsonObject jsonObject = JsonUtil.createObject();
    JsonUtil.addField(jsonObject, "name", variable.getName());
    JsonUtil.addFieldRawValue(jsonObject, "value", variable.getValue());
    JsonUtil.addField(jsonObject, "operator", variable.getOperator().name());
    return jsonObject;
  }

  public TaskQueryVariableValue toObject(JsonObject json) {
    String name = JsonUtil.getString(json, "name");
    Object value = JsonUtil.getRawObject(json, "value");
    QueryOperator operator = QueryOperator.valueOf(JsonUtil.getString(json, "operator"));
    boolean isTaskVariable = JsonUtil.getBoolean(json, "taskVariable");
    boolean isProcessVariable = JsonUtil.getBoolean(json, "processVariable");
    return new TaskQueryVariableValue(name, value, operator, isTaskVariable, isProcessVariable);
  }
}
