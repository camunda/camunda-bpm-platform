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
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.CoreLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.util.TypedValueField;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializerFactory;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class VariableUtil {

  public static final CommandLogger CMD_LOGGER = ProcessEngineLogger.CMD_LOGGER;
  public static final CoreLogger CORE_LOGGER = ProcessEngineLogger.CORE_LOGGER;

  public static final String ERROR_MSG = "Cannot set variable with name {0}. Java serialization format is prohibited";

  /**
   * Checks, if Java serialization will be used and if it is allowed to be used.
   * @param value
   */
  public static boolean isJavaSerializationProhibited(TypedValue value) {
    ProcessEngineConfigurationImpl processEngineConfiguration =
        Context.getProcessEngineConfiguration();

    if (value instanceof SerializableValue
        && !processEngineConfiguration.isJavaSerializationFormatEnabled()) {

      SerializableValue serializableValue = (SerializableValue) value;

      // if Java serialization is prohibited
      if (!serializableValue.isDeserialized()) {
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

        return Variables.SerializationDataFormats.JAVA.getName().equals(requestedDataFormat);
      }
    }
    return false;
  }

  public static void checkJavaSerialization(String variableName, TypedValue value) {
    if (isJavaSerializationProhibited(value)) {
      throw CORE_LOGGER.javaSerializationProhibitedException(variableName);
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

  public static void setVariablesByBatchId(Map<String, ?> variables, String batchId) {
    setVariables(variables, (name, value) -> setVariableByBatchId(batchId, name, value));
  }

  public static void setVariableByBatchId(String batchId, String variableName, Object variableValue) {
    TypedValue variableTypedValue = Variables.untypedValue(variableValue);

    boolean isTransient = variableTypedValue.isTransient();
    if (isTransient) {
      throw CMD_LOGGER.exceptionSettingTransientVariablesAsyncNotSupported(variableName);
    }

    checkJavaSerialization(variableName, variableTypedValue);

    VariableInstanceEntity variableInstance =
        VariableInstanceEntity.createAndInsert(variableName, variableTypedValue);

    variableInstance.setVariableScopeId(batchId);
    variableInstance.setBatchId(batchId);
  }

  public static Map<String, ?> findBatchVariablesSerialized(String batchId, CommandContext commandContext) {
    List<VariableInstanceEntity> variableInstances = commandContext.getVariableInstanceManager()
        .findVariableInstancesByBatchId(batchId);
    return variableInstances.stream().collect(variablesCollector());
  }

  protected static Collector<VariableInstanceEntity, ?, Map<String, TypedValue>> variablesCollector() {
    return Collectors.toMap(VariableInstanceEntity::getName, VariableUtil::getSerializedValue);
  }

  protected static TypedValue getSerializedValue(VariableInstanceEntity variableInstanceEntity) {
    return variableInstanceEntity.getTypedValue(false);
  }

  @FunctionalInterface
  public interface SetVariableFunction {
    void apply(String variableName, Object variableValue);
  }

}
