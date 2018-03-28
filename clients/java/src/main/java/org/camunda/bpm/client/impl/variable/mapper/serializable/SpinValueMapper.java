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
package org.camunda.bpm.client.impl.variable.mapper.serializable;

import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.impl.EngineClientLogger;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.impl.variable.mapper.ValueMapper;
import org.camunda.bpm.client.task.impl.dto.TypedValueDto;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Tassilo Weidner
 */
public class SpinValueMapper implements ValueMapper<SerializableValue> {

  protected static final EngineClientLogger LOG = ExternalTaskClientLogger.ENGINE_CLIENT_LOGGER;

  protected Class<?> spinValuesClazz;
  protected String typeName;

  public SpinValueMapper(String typeName) {
    this.typeName = typeName;
    this.spinValuesClazz = reflectSpinValuesClazz();
  }

  @Override
  public String getTypeName() {
    return typeName;
  }

  @Override
  public SerializableValue deserializeTypedValue(TypedValueDto typedValueDto) throws EngineClientException {
    if (spinValuesClazz == null) {
      throw LOG.missingSpinDependencyExceptionInternal(getTypeName());
    }

    String value = (String)typedValueDto.getValue();
    try {

      Object valueBuilder = spinValuesClazz
        .getMethod(getTypeName() + "Value", String.class)
        .invoke(null, value);

      Object serializableValue = valueBuilder.getClass()
        .getMethod("create")
        .invoke(valueBuilder);

      return (SerializableValue) serializableValue;

    }
    catch (InvocationTargetException e) {
      throw LOG.invalidSerializedValueException(value, e.getTargetException().toString());
    }
    catch (NoSuchMethodException | IllegalAccessException e) {
      throw LOG.invalidSerializedValueException(value, e.toString()); // reflection problem
    }
  }

  protected Class<?> reflectSpinValuesClazz() {
    try {
      return Class.forName("org.camunda.spin.plugin.variable.SpinValues");
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  @Override
  public TypedValueDto serializeTypedValue(TypedValue typedValue) {
    TypedValueDto typedValueDto = ValueMapper.super.serializeTypedValue(typedValue);
    typedValueDto.setValue(typedValue.getValue().toString());
    return typedValueDto;
  }

}
