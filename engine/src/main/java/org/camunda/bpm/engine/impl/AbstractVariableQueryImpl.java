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
package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
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

  protected List<QueryVariableValue> queryVariableValues = new ArrayList<>();

  protected Boolean variableNamesIgnoreCase;
  protected Boolean variableValuesIgnoreCase;

  public AbstractVariableQueryImpl() {
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
  public T variableValueNotLike(String name, String value) {
    addVariable(name, value, QueryOperator.NOT_LIKE, true);
    return (T)this;
  }

  @SuppressWarnings("unchecked")
  public T matchVariableNamesIgnoreCase() {
    this.variableNamesIgnoreCase = true;
    for (QueryVariableValue variable : getQueryVariableValues()) {
      variable.setVariableNameIgnoreCase(true);
    }
    return (T)this;
  }

  @SuppressWarnings("unchecked")
  public T matchVariableValuesIgnoreCase() {
    this.variableValuesIgnoreCase = true;
    for (QueryVariableValue variable : getQueryVariableValues()) {
      variable.setVariableValueIgnoreCase(true);
    }
    return (T)this;
  }

  protected void addVariable(String name, Object value, QueryOperator operator, boolean processInstanceScope) {
    QueryVariableValue queryVariableValue = createQueryVariableValue(name, value, operator, processInstanceScope);
    getQueryVariableValues().add(queryVariableValue);
  }

  protected QueryVariableValue createQueryVariableValue(String name, Object value, QueryOperator operator, boolean processInstanceScope) {
    validateVariable(name, value, operator);

    boolean shouldMatchVariableValuesIgnoreCase = Boolean.TRUE.equals(variableValuesIgnoreCase) && value != null && String.class.isAssignableFrom(value.getClass());
    boolean shouldMatchVariableNamesIgnoreCase = Boolean.TRUE.equals(variableNamesIgnoreCase);

    return new QueryVariableValue(name, value, operator, processInstanceScope, shouldMatchVariableNamesIgnoreCase, shouldMatchVariableValuesIgnoreCase);
  }

  protected void validateVariable(String name, Object value, QueryOperator operator) {
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
        case NOT_LIKE:
          throw new NotValidException("Booleans and null cannot be used in 'not like' condition");
        default:
          break;
      }
    }
  }

  private boolean isBoolean(Object value) {
    if (value == null) {
      return false;
    }
    return Boolean.class.isAssignableFrom(value.getClass()) || boolean.class.isAssignableFrom(value.getClass());
  }

  protected void ensureVariablesInitialized() {
    if (!getQueryVariableValues().isEmpty()) {
      ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      VariableSerializers variableSerializers = processEngineConfiguration.getVariableSerializers();
      String dbType = processEngineConfiguration.getDatabaseType();
      for(QueryVariableValue queryVariableValue : getQueryVariableValues()) {
        queryVariableValue.initialize(variableSerializers, dbType);
      }
    }
  }

  public List<QueryVariableValue> getQueryVariableValues() {
    return queryVariableValues;
  }

  public Boolean isVariableNamesIgnoreCase() {
    return variableNamesIgnoreCase;
  }

  public Boolean isVariableValuesIgnoreCase() {
    return variableValuesIgnoreCase;
  }

}
