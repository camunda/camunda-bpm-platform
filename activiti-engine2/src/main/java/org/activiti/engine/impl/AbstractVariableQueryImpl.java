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

package org.activiti.engine.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.variable.VariableTypes;
import org.activiti.engine.query.Query;
import org.activiti.engine.runtime.ExecutionQuery;


/**
 * Abstract query class that adds methods to query for variable values.
 * 
 * @author Frederik Heremans
 */
public abstract class AbstractVariableQueryImpl<T extends Query<?,?>, U> extends AbstractQuery<T, U> {

  private static final long serialVersionUID = 1L;
  
  protected List<QueryVariableValue> queryVariableValues = new ArrayList<QueryVariableValue>();
  
  public AbstractVariableQueryImpl() {
  }

  public AbstractVariableQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public AbstractVariableQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public abstract long executeCount(CommandContext commandContext) ;

  @Override
  public abstract List<U> executeList(CommandContext commandContext, Page page);
  
  
  @SuppressWarnings("unchecked")
  public T variableValueEquals(String name, Object value) {
    addVariable(name, value, QueryOperator.EQUALS, true);
    return (T) this;
  }
  
  @SuppressWarnings("unchecked")
  public T variableValueNotEquals(String name, Object value) {
    addVariable(name, value, QueryOperator.NOT_EQUALS, true);
    return (T) this;
  }
  
  @SuppressWarnings("unchecked")
  public T variableValueGreaterThan(String name, Object value) {
    addVariable(name, value, QueryOperator.GREATER_THAN, true);
    return (T) this;
  } 
  
  @SuppressWarnings("unchecked")
  public T variableValueGreaterThanOrEqual(String name, Object value) {
    addVariable(name, value, QueryOperator.GREATER_THAN_OR_EQUAL, true);
    return (T) this;
  }
  
  @SuppressWarnings("unchecked")
  public T variableValueLessThan(String name, Object value) {
    addVariable(name, value, QueryOperator.LESS_THAN, true);
    return (T) this;
  }
  
  @SuppressWarnings("unchecked")
  public T variableValueLessThanOrEqual(String name, Object value) {
    addVariable(name, value, QueryOperator.LESS_THAN_OR_EQUAL, true);
    return (T) this;
  }
  
  @SuppressWarnings("unchecked")
  public T variableValueLike(String name, String value) {
    addVariable(name, value, QueryOperator.LIKE, true);
    return (T)this;
  }
  
  @SuppressWarnings("unchecked")
  public T processVariableValueEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.EQUALS, false);
    return (T)this;
  }

  @SuppressWarnings("unchecked")
  public T processVariableValueNotEquals(String variableName, Object variableValue) {
    addVariable(variableName, variableValue, QueryOperator.NOT_EQUALS, false);
    return (T)this;
  }

  
  private void addVariable(String name, Object value, QueryOperator operator, boolean processInstanceScope) {
    if(name == null) {
      throw new ActivitiException("name is null");
    }
    if(value == null || isBoolean(value)) {
      // Null-values and booleans can only be used in EQUALS and NOT_EQUALS
      switch(operator) {
      case GREATER_THAN:
        throw new ActivitiException("Booleans and null cannot be used in 'greater than' condition");
      case LESS_THAN:
        throw new ActivitiException("Booleans and null cannot be used in 'less than' condition");
      case GREATER_THAN_OR_EQUAL:
        throw new ActivitiException("Booleans and null cannot be used in 'greater than or equal' condition");
      case LESS_THAN_OR_EQUAL:
        throw new ActivitiException("Booleans and null cannot be used in 'less than or equal' condition");
      case LIKE:
        throw new ActivitiException("Booleans and null cannot be used in 'like' condition");
      }
    }
    queryVariableValues.add(new QueryVariableValue(name, value, operator, processInstanceScope));
  }
  
  private boolean isBoolean(Object value) {
    if (value == null) {
      return false;
    }
    return Boolean.class.isAssignableFrom(value.getClass()) || boolean.class.isAssignableFrom(value.getClass());
  }

  protected void ensureVariablesInitialized() {
    if (!queryVariableValues.isEmpty()) {
      VariableTypes variableTypes = Context
              .getProcessEngineConfiguration()
              .getVariableTypes();
      for(QueryVariableValue queryVariableValue : queryVariableValues) {
        queryVariableValue.initialize(variableTypes);
      }
    }
  }

  public List<QueryVariableValue> getQueryVariableValues() {
    return queryVariableValues;
  }

  
}
