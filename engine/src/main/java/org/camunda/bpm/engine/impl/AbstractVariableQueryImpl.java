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

package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.query.Query;


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

  protected void addVariable(String name, Object value, QueryOperator operator, boolean processInstanceScope) {
    ensureNotNull(NotValidException.class, "name", name);
    if(value == null || isBoolean(value)) {
      // Null-values and booleans can only be used in EQUALS and NOT_EQUALS
      switch(operator) {
      case GREATER_THAN:
        throw new NotValidException("Booleans and null cannot be used in 'greater than' condition");
      case LESS_THAN:
        throw new NotValidException("Booleans and null cannot be used in 'less than' condition");
      case GREATER_THAN_OR_EQUAL:
        throw new NotValidException("Booleans and null cannot be used in 'greater than or equal' condition");
      case LESS_THAN_OR_EQUAL:
        throw new NotValidException("Booleans and null cannot be used in 'less than or equal' condition");
      case LIKE:
        throw new NotValidException("Booleans and null cannot be used in 'like' condition");
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
      VariableSerializers variableSerializers = Context
              .getProcessEngineConfiguration()
              .getVariableSerializers();
      for(QueryVariableValue queryVariableValue : queryVariableValues) {
        queryVariableValue.initialize(variableSerializers);
      }
    }
  }

  public List<QueryVariableValue> getQueryVariableValues() {
    return queryVariableValues;
  }


}
