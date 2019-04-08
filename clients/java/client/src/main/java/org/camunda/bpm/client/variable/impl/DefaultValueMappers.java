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
package org.camunda.bpm.client.variable.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DefaultValueMappers<T extends TypedValue> implements Serializable, ValueMappers<T> {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  private static final long serialVersionUID = 1L;

  protected List<ValueMapper<? extends TypedValue>> serializerList = new ArrayList<>();
  protected String defaultSerializationFormat;

  public DefaultValueMappers(String defaultSerializationFormat) {
    this.defaultSerializationFormat = defaultSerializationFormat;
  }

  @SuppressWarnings("unchecked")
  public ValueMapper<T> findMapperForTypedValue(T typedValue) {
    ValueType type = typedValue.getType();

    if (type != null && type.isAbstract()) {
      throw LOG.valueMapperExceptionWhileSerializingAbstractValue(type.getName());
    }

    List<ValueMapper<T>> matchedSerializers = new ArrayList<>();

    for (ValueMapper<?> serializer : serializerList) {
      if(serializer.canHandleTypedValue(typedValue)) {
        matchedSerializers.add((ValueMapper<T>) serializer);
        if(serializer.getType().isPrimitiveValueType()) {
          break;
        }
      }
    }

    if(matchedSerializers.size() == 1) {
      return matchedSerializers.get(0);
    }
    else if (matchedSerializers.size() > 1) {
      // ambiguous match, use default serializer
      return matchedSerializers.stream()
        .filter(serializer -> defaultSerializationFormat.equals(serializer.getSerializationDataformat()))
        .findFirst()
        .orElse(matchedSerializers.get(0));
    }
    else {
      throw LOG.valueMapperExceptionDueToSerializerNotFoundForTypedValue(typedValue);
    }
  }

  @Override
  public ValueMapper<T> findMapperForTypedValueField(TypedValueField typedValueField) {
    ValueMapper<? extends TypedValue> matchedSerializer = serializerList.stream()
      .filter(serializer -> serializer.canHandleTypedValueField(typedValueField))
      .findFirst()
      .orElse(null);

    if (matchedSerializer == null) {
      throw LOG.valueMapperExceptionDueToSerializerNotFoundForTypedValueField(typedValueField.getValue());
    }

    return (ValueMapper<T>)matchedSerializer;
  }

  public ValueMappers addMapper(ValueMapper<T> serializer) {
    serializerList.add(serializer);
    return this;
  }
}
