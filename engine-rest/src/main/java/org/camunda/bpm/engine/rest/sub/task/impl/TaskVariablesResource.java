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
package org.camunda.bpm.engine.rest.sub.task.impl;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.rest.sub.impl.AbstractVariablesResource;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Daniel Meyer
 *
 */
public class TaskVariablesResource extends AbstractVariablesResource {

  public TaskVariablesResource(ProcessEngine engine, String resourceId, ObjectMapper objectMapper) {
    super(engine, resourceId, objectMapper);
  }

  protected String getResourceTypeName() {
    return "task";
  }

  protected TypedValue getVariableEntity(String variableKey, boolean deserializeValue) {
    return engine.getTaskService().getVariableTyped(resourceId, variableKey, deserializeValue);
  }

  protected VariableMap getVariableEntities(boolean deserializeValues) {
    return engine.getTaskService().getVariablesTyped(resourceId, deserializeValues);
  }

  protected void removeVariableEntity(String variableKey) {
    engine.getTaskService().removeVariable(resourceId, variableKey);
  }

  protected void updateVariableEntities(VariableMap modifications, List<String> deletions) {
    TaskServiceImpl taskService = (TaskServiceImpl) engine.getTaskService();
    taskService.updateVariables(resourceId, modifications, deletions);
  }

  protected void setVariableEntity(String variableKey, TypedValue variableValue) {
    engine.getTaskService().setVariable(resourceId, variableKey, variableValue);
  }

}
