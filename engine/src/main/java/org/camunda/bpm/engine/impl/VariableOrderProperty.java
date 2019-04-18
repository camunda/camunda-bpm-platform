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

import java.util.ArrayList;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.query.QueryProperty;
import org.camunda.bpm.engine.variable.type.ValueType;

/**
 * @author Thorben Lindhauer
 *
 */
public class VariableOrderProperty extends QueryOrderingProperty {

  private static final long serialVersionUID = 1L;

  public VariableOrderProperty(String name, ValueType valueType) {
    super(QueryOrderingProperty.RELATION_VARIABLE, typeToQueryProperty(valueType));
    this.relationConditions = new ArrayList<QueryEntityRelationCondition>();
    relationConditions.add(new QueryEntityRelationCondition(VariableInstanceQueryProperty.VARIABLE_NAME, name));

    // works only for primitive types
    relationConditions.add(new QueryEntityRelationCondition(VariableInstanceQueryProperty.VARIABLE_TYPE, valueType.getName()));
  }

  public VariableOrderProperty() {
  }

  public static VariableOrderProperty forProcessInstanceVariable(String variableName, ValueType valueType) {
    VariableOrderProperty orderingProperty = new VariableOrderProperty(variableName, valueType);
    orderingProperty.relationConditions.add(
        new QueryEntityRelationCondition(VariableInstanceQueryProperty.EXECUTION_ID, TaskQueryProperty.PROCESS_INSTANCE_ID));

    return orderingProperty;
  }

  public static VariableOrderProperty forExecutionVariable(String variableName, ValueType valueType) {
    VariableOrderProperty orderingProperty = new VariableOrderProperty(variableName, valueType);
    orderingProperty.relationConditions.add(
        new QueryEntityRelationCondition(VariableInstanceQueryProperty.EXECUTION_ID, TaskQueryProperty.EXECUTION_ID));

    return orderingProperty;
  }

  public static VariableOrderProperty forTaskVariable(String variableName, ValueType valueType) {
    VariableOrderProperty orderingProperty = new VariableOrderProperty(variableName, valueType);
    orderingProperty.relationConditions.add(
        new QueryEntityRelationCondition(VariableInstanceQueryProperty.TASK_ID, TaskQueryProperty.TASK_ID));

    return orderingProperty;
  }

  public static VariableOrderProperty forCaseInstanceVariable(String variableName, ValueType valueType) {
    VariableOrderProperty orderingProperty = new VariableOrderProperty(variableName, valueType);
    orderingProperty.relationConditions.add(
        new QueryEntityRelationCondition(VariableInstanceQueryProperty.CASE_EXECUTION_ID, TaskQueryProperty.CASE_INSTANCE_ID));

    return orderingProperty;
  }

  public static VariableOrderProperty forCaseExecutionVariable(String variableName, ValueType valueType) {
    VariableOrderProperty orderingProperty = new VariableOrderProperty(variableName, valueType);
    orderingProperty.relationConditions.add(
        new QueryEntityRelationCondition(VariableInstanceQueryProperty.CASE_EXECUTION_ID, TaskQueryProperty.CASE_EXECUTION_ID));

    return orderingProperty;
  }

  public static QueryProperty typeToQueryProperty(ValueType type) {
    if (ValueType.STRING.equals(type)) {
      return VariableInstanceQueryProperty.TEXT_AS_LOWER;
    } else if (ValueType.INTEGER.equals(type)) {
      return VariableInstanceQueryProperty.LONG;
    } else if (ValueType.SHORT.equals(type)) {
      return VariableInstanceQueryProperty.LONG;
    } else if (ValueType.DATE.equals(type)) {
      return VariableInstanceQueryProperty.LONG;
    } else if (ValueType.BOOLEAN.equals(type)) {
      return VariableInstanceQueryProperty.LONG;
    } else if (ValueType.LONG.equals(type)) {
      return VariableInstanceQueryProperty.LONG;
    } else if (ValueType.DOUBLE.equals(type)) {
      return VariableInstanceQueryProperty.DOUBLE;
    } else {
      throw new ProcessEngineException("Cannot order by variables of type " + type.getName());
    }
  }

}
