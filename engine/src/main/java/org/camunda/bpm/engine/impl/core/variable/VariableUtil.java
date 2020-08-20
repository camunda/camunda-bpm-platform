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
package org.camunda.bpm.engine.impl.core.variable;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.CoreLogger;
import org.camunda.bpm.engine.impl.persistence.entity.util.TypedValueField;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializerFactory;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.util.Map;

public class VariableUtil {

  public static CoreLogger CORE_LOGGER = ProcessEngineLogger.CORE_LOGGER;

  /**
   * Checks, if Java serialization will be used and if it is allowed to be used.
   * @param variableName
   * @param value
   */
  public static void checkJavaSerialization(String variableName, TypedValue value) {
    ProcessEngineConfigurationImpl processEngineConfiguration =
        Context.getProcessEngineConfiguration();

    if (value instanceof SerializableValue
        && !processEngineConfiguration.isJavaSerializationFormatEnabled()) {

      SerializableValue serializableValue = (SerializableValue) value;

      // if Java serialization is prohibited
      if (!serializableValue.isDeserialized()) {

        String javaSerializationDataFormat = Variables.SerializationDataFormats.JAVA.getName();
        String requestedDataFormat = serializableValue.getSerializationDataFormat();

        if (requestedDataFormat == null) {
          VariableSerializerFactory fallbackSerializerFactory =
              processEngineConfiguration.getFallbackSerializerFactory();

          // check if Java serializer will be used
          TypedValueSerializer serializerForValue = TypedValueField.getSerializers()
              .findSerializerForValue(serializableValue, fallbackSerializerFactory);
          if (serializerForValue != null) {
            requestedDataFormat = serializerForValue.getSerializationDataformat();
          }
        }

        if (javaSerializationDataFormat.equals(requestedDataFormat)) {
          throw CORE_LOGGER.javaSerializationProhibitedException(variableName);
        }
      }
    }
  }

  public static void setVariables(Map<String, ?> variables,
                                  SetVariableFunction setVariableFunction) {
    if (variables != null) {
      for (String variableName : variables.keySet()) {
        Object value = null;
        if (variables instanceof VariableMap) {
          value = ((VariableMap) variables).getValueTyped(variableName);

        } else {
          value = variables.get(variableName);

        }

        setVariableFunction.apply(variableName, value);
      }
    }
  }

  @FunctionalInterface
  public interface SetVariableFunction {
    void apply(String variableName, Object variableValue);
  }

}
