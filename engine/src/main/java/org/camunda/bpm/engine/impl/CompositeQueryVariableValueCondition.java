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
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.type.ValueTypeResolver;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Thorben Lindhauer
 *
 */
public class CompositeQueryVariableValueCondition extends AbstractQueryVariableValueCondition {

  protected List<SingleQueryVariableValueCondition> aggregatedValues = new ArrayList<>();

  public CompositeQueryVariableValueCondition(QueryVariableValue variableValue) {
    super(variableValue);
  }

  public void initializeValue(VariableSerializers serializers, String dbType) {
    TypedValue typedValue = wrappedQueryValue.getTypedValue();

    ValueTypeResolver resolver = Context.getProcessEngineConfiguration().getValueTypeResolver();
    Collection<ValueType> concreteTypes = resolver.getSubTypes(typedValue.getType());

    for (ValueType type : concreteTypes) {
      if (type.canConvertFromTypedValue(typedValue)) {
        TypedValue convertedValue = type.convertFromTypedValue(typedValue);
        SingleQueryVariableValueCondition aggregatedValue = new SingleQueryVariableValueCondition(wrappedQueryValue);
        aggregatedValue.initializeValue(serializers, convertedValue, dbType);
        aggregatedValues.add(aggregatedValue);
      }
    }
  }

  public List<SingleQueryVariableValueCondition> getDisjunctiveConditions() {
    return aggregatedValues;
  }

}
