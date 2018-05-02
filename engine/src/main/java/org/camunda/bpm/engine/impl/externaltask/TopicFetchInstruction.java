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
package org.camunda.bpm.engine.impl.externaltask;

import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.QueryVariableValue;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Thorben Lindhauer
 *
 */
public class TopicFetchInstruction implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String topicName;
  protected String businessKey;
  protected List<String> variablesToFetch;

  protected List<QueryVariableValue> filterVariables;
  protected long lockDuration;
  protected boolean deserializeVariables = false;
  protected boolean localVariables = false;

  public TopicFetchInstruction(String topicName, long lockDuration) {
    this.topicName = topicName;
    this.lockDuration = lockDuration;
    this.filterVariables = new ArrayList<QueryVariableValue>();
  }

  public List<String> getVariablesToFetch() {
    return variablesToFetch;
  }

  public void setVariablesToFetch(List<String> variablesToFetch) {
    this.variablesToFetch = variablesToFetch;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public List<QueryVariableValue> getFilterVariables() {
    return filterVariables;
  }

  public void setFilterVariables(Map<String, Object> filterVariables) {
    QueryVariableValue variableValue;
    for (Map.Entry<String, Object> filter : filterVariables.entrySet()) {
      variableValue = new QueryVariableValue(filter.getKey(), filter.getValue(), null, false);
      this.filterVariables.add(variableValue);
    }
  }

  public void addFilterVariable(String name, Object value) {
    QueryVariableValue variableValue = new QueryVariableValue(name, value, QueryOperator.EQUALS, true);
    this.filterVariables.add(variableValue);
  }

  public Long getLockDuration() {
    return lockDuration;
  }

  public String getTopicName() {
    return topicName;
  }

  public boolean isDeserializeVariables() {
    return deserializeVariables;
  }

  public void setDeserializeVariables(boolean deserializeVariables) {
    this.deserializeVariables = deserializeVariables;
  }

  public void ensureVariablesInitialized() {
    if (!filterVariables.isEmpty()) {
      VariableSerializers variableSerializers = Context
          .getProcessEngineConfiguration()
          .getVariableSerializers();
      for(QueryVariableValue queryVariableValue : filterVariables) {
        queryVariableValue.initialize(variableSerializers);
      }
    }
  }

  public boolean isLocalVariables() {
	return localVariables;
  }

  public void setLocalVariables(boolean localVariables) {
	this.localVariables = localVariables;
  }

}
