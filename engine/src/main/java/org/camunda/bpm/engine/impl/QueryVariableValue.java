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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * Represents a variable value used in queries.
 *
 * @author Frederik Heremans
 */
public class QueryVariableValue implements Serializable {
  protected static final long serialVersionUID = 1L;
  protected String name;
  protected TypedValue value;
  protected QueryOperator operator;
  protected boolean local;

  protected AbstractQueryVariableValueCondition valueCondition;

  protected boolean variableNameIgnoreCase;
  protected boolean variableValueIgnoreCase;

  public QueryVariableValue(String name, Object value, QueryOperator operator, boolean local) {
    this(name, value, operator, local, false, false);
  }

  public QueryVariableValue(String name, Object value, QueryOperator operator, boolean local, boolean variableNameIgnoreCase, boolean variableValueIgnoreCase) {
    this.name = name;
    this.value = Variables.untypedValue(value);
    this.operator = operator;
    this.local = local;
    this.variableNameIgnoreCase = variableNameIgnoreCase;
    this.variableValueIgnoreCase = variableValueIgnoreCase;
  }

  public void initialize(VariableSerializers serializers, String dbType) {
    if (value.getType() != null && value.getType().isAbstract()) {
      valueCondition = new CompositeQueryVariableValueCondition(this);
    } else {
      valueCondition = new SingleQueryVariableValueCondition(this);
    }

    valueCondition.initializeValue(serializers, dbType);
  }

  public List<SingleQueryVariableValueCondition> getValueConditions() {
    return valueCondition.getDisjunctiveConditions();
  }

  public String getName() {
    return name;
  }

  public QueryOperator getOperator() {
    if(operator != null) {
      return operator;
    }
    return QueryOperator.EQUALS;
  }

  public String getOperatorName() {
    return getOperator().toString();
  }

  public Object getValue() {
    return value.getValue();
  }

  public TypedValue getTypedValue() {
    return value;
  }

  public boolean isLocal() {
    return local;
  }


  public boolean isVariableNameIgnoreCase() {
    return variableNameIgnoreCase;
  }

  public void setVariableNameIgnoreCase(boolean variableNameIgnoreCase) {
    this.variableNameIgnoreCase = variableNameIgnoreCase;
  }

  public boolean isVariableValueIgnoreCase() {
    return variableValueIgnoreCase;
  }

  public void setVariableValueIgnoreCase(boolean variableValueIgnoreCase) {
    this.variableValueIgnoreCase = variableValueIgnoreCase;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    QueryVariableValue that = (QueryVariableValue) o;
    return local == that.local && variableNameIgnoreCase == that.variableNameIgnoreCase
        && variableValueIgnoreCase == that.variableValueIgnoreCase && name.equals(that.name) && value.equals(that.value)
        && operator == that.operator && Objects.equals(valueCondition, that.valueCondition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value, operator, local, valueCondition, variableNameIgnoreCase, variableValueIgnoreCase);
  }

}
