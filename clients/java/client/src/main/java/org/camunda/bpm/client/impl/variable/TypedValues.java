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
package org.camunda.bpm.client.impl.variable;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.client.impl.variable.mapper.ValueMapper;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class TypedValues {

  protected ValueMappers serializers;

  public TypedValues(ValueMappers serializers) {
    this.serializers = serializers;
  }

  public Map<String, TypedValueField> serializeVariables(Map<String, Object> variables) {
    Map<String, TypedValueField> result = new HashMap<>();

    if (variables != null) {
      for (String variableName : variables.keySet()) {

        Object variableValue = null;
        if (variables instanceof VariableMap) {
          variableValue = ((VariableMap) variables).getValueTyped(variableName);
        }
        else {
          variableValue = variables.get(variableName);
        }

        TypedValue typedValue = createTypedValue(variableValue);
        TypedValueField typedValueField = toTypedValueField(typedValue);
        result.put(variableName, typedValueField);
      }

    }

    return result;
  }

  public Map<String, VariableValue> deserializeVariables(Map<String, TypedValueField> variables) {
    Map<String, VariableValue> result = new HashMap<>();

    if (variables != null) {
      variables.forEach((variableName, variableValue) -> {

        String typeName = variableValue.getType();
        typeName = Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
        variableValue.setType(typeName);

        VariableValue value = new VariableValue(variableValue, serializers);
        result.put(variableName, value);
      });
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  protected TypedValueField toTypedValueField(TypedValue typedValue) {
    ValueMapper<?> serializer = findSerializer(typedValue);

    if(typedValue instanceof UntypedValueImpl) {
      typedValue = serializer.convertToTypedValue((UntypedValueImpl) typedValue);
    }

    TypedValueField typedValueField = new TypedValueField();

    ((ValueMapper<TypedValue>) serializer).writeValue(typedValue, typedValueField);

    ValueType valueType = typedValue.getType();
    typedValueField.setValueInfo(valueType.getValueInfo(typedValue));

    String typeName = valueType.getName();
    String typeNameCapitalized = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
    typedValueField.setType(typeNameCapitalized);

    return typedValueField;
  }

  protected ValueMapper<?> findSerializer(TypedValue typedValue) {
    return serializers.findMapperForTypedValue(typedValue);
  }

  protected TypedValue createTypedValue(Object value) {
    TypedValue typedValue = null;

    if (value instanceof TypedValue) {
      typedValue = (TypedValue) value;
    }
    else {
      typedValue = Variables.untypedValue(value);
    }

    return typedValue;
  }
}
