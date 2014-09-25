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
package org.camunda.bpm.engine.impl.spin;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.ProcessEngineVariableType;
import org.camunda.bpm.engine.delegate.SerializedVariableValue;
import org.camunda.bpm.engine.impl.core.variable.SerializedVariableValueImpl;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.variable.ValueFields;
import org.camunda.bpm.engine.impl.variable.VariableType;
import org.camunda.spin.DataFormats;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinFactory;
import org.camunda.spin.SpinRuntimeException;
import org.camunda.spin.spi.DataFormat;

public class SpinSerializationType implements VariableType {

  protected Map<String, DataFormat<?>> availableDataFormats = new HashMap<String, DataFormat<?>>();
  protected DataFormat<?> defaultDataFormat;

  protected static final String CONFIG_DATA_FORMAT = ProcessEngineVariableType.SPIN_TYPE_DATA_FORMAT_ID;
  protected static final String CONFIG_ROOT_TYPE = ProcessEngineVariableType.SPIN_TYPE_CONFIG_ROOT_TYPE;

  public SpinSerializationType(DataFormat<?> defaultDataFormat) {

    for (DataFormat<?> format : DataFormats.getAvailableDataFormats()) {
      availableDataFormats.put(format.getName(), format.newInstance());
    }

    this.defaultDataFormat = defaultDataFormat.newInstance();
  }

  public String getTypeName() {
    return ProcessEngineVariableType.SPIN.getName();
  }

  public boolean isCachable() {
    return true;
  }

  public boolean isAbleToStore(Object value) {
    return true;
  }

  public void setValue(Object value, ValueFields valueFields) {
    try {
      Spin<?> spin = SpinFactory.getInstance().createSpinFromObject(value, defaultDataFormat);

      String serializedVariable = spin.toString();

      setValue(serializedVariable, defaultDataFormat.getName(), defaultDataFormat.getCanonicalTypeName(value), valueFields);
    } catch (SpinRuntimeException e) {
      throw new ProcessEngineException("Cannot serialize object of type " + value.getClass() + ": " + value, e);
    }
  }

  protected void setValue(String serializedValue, String dataFormatId, String valueType, ValueFields valueFields) {
    if (serializedValue == null) {
      valueFields.setTextValue(null);
    } else if (serializedValue.getBytes().length <= DbSqlSessionFactory.ACT_RU_VARIABLE_TEXT_LENGTH) {
      valueFields.setTextValue(serializedValue);
    } else {
      valueFields.setByteArrayValue(serializedValue.getBytes());
    }

    valueFields.setDataFormatId(dataFormatId);
    valueFields.setTextValue2(valueType);
  }

  public Object getValue(ValueFields valueFields) {

    try {
      String dataFormatId = valueFields.getDataFormatId();
      if (!availableDataFormats.containsKey(dataFormatId)) {
        throw new ProcessEngineException("No data format found that"
            + "matches the data format " + dataFormatId + " of variable " + valueFields.getName());
      }

      String variableValue = valueFields.getTextValue();
      if (variableValue == null) {
        ByteArrayEntity byteEntity = valueFields.getByteArrayValue();
        if (byteEntity == null) {
          return null;
        }

        variableValue = new String(valueFields.getByteArrayValue().getBytes());
      }

      Spin<?> spinNode = SpinFactory.getInstance().createSpinFromString(variableValue, availableDataFormats.get(dataFormatId));
      return spinNode.mapTo(valueFields.getTextValue2());

    } catch (SpinRuntimeException e) {
      throw new ProcessEngineException(
          "Cannot deserialize variable '" + valueFields.getName() + "' of format '" +
          valueFields.getDataFormatId() + "' with configuration '" + valueFields.getTextValue2() + "'", e);
    }
  }

  public DataFormat<?> getDefaultDataFormat() {
    return defaultDataFormat;
  }

  public SerializedVariableValue getSerializedValue(ValueFields valueFields) {
    SerializedVariableValueImpl value = new SerializedVariableValueImpl();

    String serializedValue = null;
    if (valueFields.getTextValue() != null) {
      serializedValue = valueFields.getTextValue();
    } else if (valueFields.getByteArrayValue() != null){
      try {
        ByteArrayEntity byteArray = valueFields.getByteArrayValue();
        serializedValue = new String(byteArray.getBytes(), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new ProcessEngineException("UTF-8 is not a supported encoding", e);
      }
    }

    value.setValue(serializedValue);
    value.setConfigValue(CONFIG_DATA_FORMAT, valueFields.getDataFormatId());
    value.setConfigValue(CONFIG_ROOT_TYPE, valueFields.getTextValue2());

    return value;
  }

  public void setValueFromSerialized(Object serializedValue, Map<String, Object> configuration, ValueFields valueFields) {
    String dataFormatName = (String) configuration.get(CONFIG_DATA_FORMAT);
    String rootType = (String) configuration.get(CONFIG_ROOT_TYPE);
    setValue((String) serializedValue, dataFormatName, rootType, valueFields);
  }

  public boolean isAbleToStoreSerializedValue(Object value, Map<String, Object> configuration) {
    return isValidObjectSerializedInput(value, configuration) || isValidNullInput(value, configuration);
  }

  protected boolean isValidObjectSerializedInput(Object value, Map<String, Object> configuration) {
    return value instanceof String
        && configuration != null
        && configuration.get(CONFIG_DATA_FORMAT) instanceof String
        && configuration.get(CONFIG_ROOT_TYPE) instanceof String
        && availableDataFormats.containsKey(configuration.get(CONFIG_DATA_FORMAT));
  }

  protected boolean isValidNullInput(Object value, Map<String, Object> configuration) {
    return value == null
        && configuration != null
        && configuration.get(CONFIG_DATA_FORMAT) instanceof String
        && configuration.get(CONFIG_ROOT_TYPE) instanceof String
        && availableDataFormats.containsKey(configuration.get(CONFIG_DATA_FORMAT));
  }

  public String getTypeNameForValue(ValueFields valueFields) {
    return Object.class.getSimpleName();
  }

  public boolean storesCustomObjects() {
    return true;
  }

}
