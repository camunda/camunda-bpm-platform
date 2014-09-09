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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.TaskQuery;

/**
 * @author Sebastian Menski
 */
public class JsonTaskQueryConverter extends JsonObjectConverter<TaskQuery> {

  protected static JsonTaskQueryVariableValueConverter variableValueConverter = new JsonTaskQueryVariableValueConverter();

  public JSONObject toJsonObject(TaskQuery taskQuery) {
    JSONObject json = new JSONObject();
    TaskQueryImpl query = (TaskQueryImpl) taskQuery;

    addField(json, "taskId", query.getTaskId());
    addField(json, "name", query.getName());
    addField(json, "nameLike", query.getNameLike());
    addField(json, "description", query.getDescription());
    addField(json, "descriptionLike", query.getDescriptionLike());
    addField(json, "priority", query.getPriority());
    addField(json, "minPriority", query.getMinPriority());
    addField(json, "maxPriority", query.getMaxPriority());
    addField(json, "assignee", query.getAssignee());
    addField(json, "assigneeLike", query.getAssigneeLike());
    addField(json, "involvedUser", query.getInvolvedUser());
    addField(json, "owner", query.getOwner());
    addDefaultField(json, "unassigned", false, query.isUnassigned());
    addField(json, "delegationState", query.getDelegationStateString());
    addField(json, "candidateUser", query.getCandidateUser());
    addField(json, "candidateGroup", query.getCandidateGroup());
    if (query.getCandidateUser() == null && query.getCandidateGroup() == null) {
      addListField(json, "candidateGroups", query.getCandidateGroups());
    }
    addField(json, "processInstanceId", query.getProcessInstanceId());
    addField(json, "executionId", query.getExecutionId());
    addArrayField(json, "activityInstanceIdIn", query.getActivityInstanceIdIn());
    addDateField(json, "created", query.getCreateTime());
    addDateField(json, "createdBefore", query.getCreateTimeBefore());
    addDateField(json, "createdAfter", query.getCreateTimeAfter());
    addField(json, "key", query.getKey());
    addField(json, "keyLike", query.getKeyLike());
    addField(json, "processDefinitionKey", query.getProcessDefinitionKey());
    addField(json, "processDefinitionId", query.getProcessDefinitionId());
    addField(json, "processDefinitionName", query.getProcessDefinitionName());
    addField(json, "processDefinitionNameLike", query.getProcessDefinitionNameLike());
    addField(json, "processInstanceBusinessKey", query.getProcessInstanceBusinessKey());
    addField(json, "processInstanceBusinessKeyLike", query.getProcessInstanceBusinessKeyLike());
    addVariablesFields(json, query.getVariables());
    addDateField(json, "due", query.getDueDate());
    addDateField(json, "dueBefore", query.getDueBefore());
    addDateField(json, "dueAfter", query.getDueAfter());
    addDateField(json, "followUp", query.getFollowUpDate());
    addDateField(json, "followUpBefore", query.getFollowUpBefore());
    addDateField(json, "followUpAfter", query.getFollowUpAfter());
    addDefaultField(json, "excludeSubtasks", false, query.isExcludeSubtasks());
    addSuspensionState(json, query.getSuspensionState());
    addField(json, "caseDefinitionKey", query.getCaseDefinitionKey());
    addField(json, "caseDefinitionId", query.getCaseDefinitionId());
    addField(json, "caseDefinitionName", query.getCaseDefinitionName());
    addField(json, "caseDefinitionNameLike", query.getCaseDefinitionNameLike());
    addField(json, "caseInstanceId", query.getCaseInstanceId());
    addField(json, "caseInstanceBusinessKey", query.getCaseInstanceBusinessKey());
    addField(json, "caseInstanceBusinessKeyLike", query.getCaseInstanceBusinessKeyLike());
    addField(json, "caseExecutionId", query.getCaseExecutionId());
    addDefaultField(json, "orderBy", ListQueryParameterObject.DEFAULT_ORDER_BY, query.getOrderBy());

    return json;
  }

  protected void addField(JSONObject json, String name, Object value) {
    if (value != null) {
      json.put(name, value);
    }
  }

  protected void addDefaultField(JSONObject json, String name, Object defaultValue, Object value) {
    if (value != null && !value.equals(defaultValue)) {
      json.put(name, value);
    }
  }

  protected void addListField(JSONObject json, String name, List<String> list) {
    if (list != null) {
      json.put(name, new JSONArray(list));
    }
  }

  protected void addArrayField(JSONObject json, String name, String[] array) {
    if (array != null) {
      addListField(json, name, Arrays.asList(array));
    }
  }

  protected void addDateField(JSONObject json, String name, Date date) {
    if (date != null) {
      json.put(name, date.getTime());
    }
  }

  private void addSuspensionState(JSONObject json, SuspensionState suspensionState) {
    if (suspensionState != null) {
      if (suspensionState.equals(SuspensionState.ACTIVE)) {
        json.put("active", true);
      }
      else if (suspensionState.equals(SuspensionState.SUSPENDED)) {
        json.put("suspended", true);
      }
    }
  }

  protected void addVariablesFields(JSONObject json, List<TaskQueryVariableValue> variables) {
    for (TaskQueryVariableValue variable : variables) {
      if (variable.isProcessInstanceVariable()) {
        addVariable(json, "processVariables", variable);
      }
      else if(variable.isLocal()) {
        addVariable(json, "taskVariables", variable);
      }
      else {
        addVariable(json, "caseInstanceVariables", variable);
      }
    }
  }

  protected void addVariable(JSONObject json, String variableType, TaskQueryVariableValue variable) {
    JSONArray array = json.optJSONArray(variableType);
    if (array == null) {
      array = new JSONArray();
    }
    addVariable(array, variable);
    json.put(variableType, array);
  }

  protected void addVariable(JSONArray array, TaskQueryVariableValue variable) {
    array.put(variableValueConverter.toJsonObject(variable));
  }

  public TaskQuery toObject(Reader reader) {
    throw new ProcessEngineException("not implemented");
  }

  public TaskQuery toObject(String jsonString) {
    JSONObject json = new JSONObject(jsonString);
    TaskQueryImpl query = new TaskQueryImpl();

    if (json.has("taskId")) {
      query.taskId(json.getString("taskId"));
    }
    if (json.has("name")) {
      query.taskName(json.getString("name"));
    }
    if (json.has("nameLike")) {
      query.taskNameLike(json.getString("nameLike"));
    }
    if (json.has("description")) {
      query.taskDescription(json.getString("description"));
    }
    if (json.has("descriptionLike")) {
      query.taskDescriptionLike(json.getString("descriptionLike"));
    }
    if (json.has("priority")) {
      query.taskPriority(json.getInt("priority"));
    }
    if (json.has("minPriority")) {
      query.taskMinPriority(json.getInt("minPriority"));
    }
    if (json.has("maxPriority")) {
      query.taskMaxPriority(json.getInt("maxPriority"));
    }
    if (json.has("assignee")) {
      query.taskAssignee(json.getString("assignee"));
    }
    if (json.has("assigneeLike")) {
      query.taskAssigneeLike(json.getString("assigneeLike"));
    }
    if (json.has("involvedUser")) {
      query.taskInvolvedUser(json.getString("involvedUser"));
    }
    if (json.has("owner")) {
      query.taskOwner(json.getString("owner"));
    }
    if (json.has("unassigned") && json.getBoolean("unassigned")) {
        query.taskUnassigned();
    }
    if (json.has("delegationState")) {
      query.taskDelegationState(DelegationState.valueOf(json.getString("delegationState")));
    }
    if (json.has("candidateUser")) {
      query.taskCandidateUser(json.getString("candidateUser"));
    }
    if (json.has("candidateGroup")) {
      query.taskCandidateGroup(json.getString("candidateGroup"));
    }
    if (json.has("candidateGroups") && !json.has("candidateUser") && !json.has("candidateGroup")) {
      query.taskCandidateGroupIn(getList(json.getJSONArray("candidateGroups")));
    }
    if (json.has("processInstanceId")) {
      query.processInstanceId(json.getString("processInstanceId"));
    }
    if (json.has("executionId")) {
      query.executionId(json.getString("executionId"));
    }
    if (json.has("activityInstanceIdIn")) {
      query.activityInstanceIdIn(getArray(json.getJSONArray("activityInstanceIdIn")));
    }
    if (json.has("created")) {
      query.taskCreatedOn(new Date(json.getLong("created")));
    }
    if (json.has("createdBefore")) {
      query.taskCreatedBefore(new Date(json.getLong("createdBefore")));
    }
    if (json.has("createdAfter")) {
      query.taskCreatedAfter(new Date(json.getLong("createdAfter")));
    }
    if (json.has("key")) {
      query.taskDefinitionKey(json.getString("key"));
    }
    if (json.has("keyLike")) {
      query.taskDefinitionKeyLike(json.getString("keyLike"));
    }
    if (json.has("processDefinitionKey")) {
      query.processDefinitionKey(json.getString("processDefinitionKey"));
    }
    if (json.has("processDefinitionId")) {
      query.processDefinitionId(json.getString("processDefinitionId"));
    }
    if (json.has("processDefinitionName")) {
      query.processDefinitionName(json.getString("processDefinitionName"));
    }
    if (json.has("processDefinitionNameLike")) {
      query.processDefinitionNameLike(json.getString("processDefinitionNameLike"));
    }
    if (json.has("processInstanceBusinessKey")) {
      query.processInstanceBusinessKey(json.getString("processInstanceBusinessKey"));
    }
    if (json.has("processInstanceBusinessKeyLike")) {
      query.processInstanceBusinessKeyLike(json.getString("processInstanceBusinessKeyLike"));
    }
    if (json.has("taskVariables")) {
      addVariables(query, json.getJSONArray("taskVariables"), true, false);
    }
    if (json.has("processVariables")) {
      addVariables(query, json.getJSONArray("processVariables"), false, true);
    }
    if (json.has("caseInstanceVariables")) {
      addVariables(query, json.getJSONArray("caseInstanceVariables"), false, false);
    }
    if (json.has("due")) {
      query.dueDate(new Date(json.getLong("due")));
    }
    if (json.has("dueBefore")) {
      query.dueBefore(new Date(json.getLong("dueBefore")));
    }
    if (json.has("dueAfter")) {
      query.dueAfter(new Date(json.getLong("dueAfter")));
    }
    if (json.has("followUp")) {
      query.followUpDate(new Date(json.getLong("followUp")));
    }
    if (json.has("followUpBefore")) {
      query.followUpBefore(new Date(json.getLong("followUpBefore")));
    }
    if (json.has("followUpAfter")) {
      query.followUpAfter(new Date(json.getLong("followUpAfter")));
    }
    if (json.has("excludeSubtasks") && json.getBoolean("excludeSubtasks")) {
      query.excludeSubtasks();
    }
    if (json.has("suspended") && json.getBoolean("suspended")) {
      query.suspended();
    }
    if (json.has("active") && json.getBoolean("active")) {
      query.active();
    }
    if (json.has("caseDefinitionKey")) {
      query.caseDefinitionKey(json.getString("caseDefinitionKey"));
    }
    if (json.has("caseDefinitionId")) {
      query.caseDefinitionId(json.getString("caseDefinitionId"));
    }
    if (json.has("caseDefinitionName")) {
      query.caseDefinitionName(json.getString("caseDefinitionName"));
    }
    if (json.has("caseDefinitionNameLike")) {
      query.caseDefinitionNameLike(json.getString("caseDefinitionNameLike"));
    }
    if (json.has("caseInstanceId")) {
      query.caseInstanceId(json.getString("caseInstanceId"));
    }
    if (json.has("caseInstanceBusinessKey")) {
      query.caseInstanceBusinessKey(json.getString("caseInstanceBusinessKey"));
    }
    if (json.has("caseInstanceBusinessKeyLike")) {
      query.caseInstanceBusinessKeyLike(json.getString("caseInstanceBusinessKeyLike"));
    }
    if (json.has("caseExecutionId")) {
      query.caseExecutionId(json.getString("caseExecutionId"));
    }
    if (json.has("orderBy")) {
      query.setOrderBy(json.getString("orderBy"));
    }

    return query;
  }

  private String[] getArray(JSONArray array) {
    return getList(array).toArray(new String[array.length()]);
  }

  protected List<String> getList(JSONArray array) {
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < array.length(); i++) {
      list.add(array.getString(i));
    }
    return list;
  }

  private void addVariables(TaskQueryImpl query, JSONArray variables, boolean isTaskVariable, boolean isProcessVariable) {
    for (int i = 0; i < variables.length(); i++) {
      JSONObject variable = variables.getJSONObject(i);
      String name = variable.getString("name");
      Object value = variable.get("value");
      QueryOperator operator = QueryOperator.valueOf(variable.getString("operator"));
      query.addVariable(name, value, operator, isTaskVariable, isProcessVariable);
    }
  }

}
