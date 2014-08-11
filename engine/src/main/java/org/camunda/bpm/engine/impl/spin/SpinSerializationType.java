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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.runtime.SerializedVariableValueImpl;
import org.camunda.bpm.engine.impl.variable.ValueFields;
import org.camunda.bpm.engine.impl.variable.VariableType;
import org.camunda.bpm.engine.runtime.SerializedVariableValue;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinFactory;
import org.camunda.spin.SpinRuntimeException;
import org.camunda.spin.spi.DataFormat;

public class SpinSerializationType implements VariableType {

  public static final String TYPE_NAME = "SpinSerialization";

  public static final String CONFIG_DATA_FORMAT_ID = "dataFormatId";
  public static final String CONFIG_TYPE = "configType";

  protected DataFormat<?> dataFormat;

  public SpinSerializationType(DataFormat<?> dataFormat) {
    this.dataFormat = dataFormat;
  }

  public String getTypeName() {
    return TYPE_NAME;
  }

  public String getTypeNameForValue(Object value) {
    return "Object";
  }

  public boolean isCachable() {
    return true;
  }

  public boolean isAbleToStore(Object value) {
    return true;
  }

  public void setValue(Object value, ValueFields valueFields) {
    try {
      Spin<?> spin = SpinFactory.getInstance().createSpinFromObject(value, dataFormat);

      String serializedVariable = spin.toString();

      if (serializedVariable.getBytes().length <= DbSqlSessionFactory.ACT_RU_VARIABLE_TEXT_LENGTH) {
        valueFields.setTextValue(spin.toString());
      } else {
        valueFields.setByteArrayValue(serializedVariable.getBytes());
      }

      valueFields.setDataFormatId(dataFormat.getName());
      valueFields.setTextValue2(dataFormat.getCanonicalTypeName(value));
    } catch (SpinRuntimeException e) {
      throw new ProcessEngineException("Cannot serialize object of type " + value.getClass() + ": " + value, e);
    }
  }

  public Object getValue(ValueFields valueFields) {

    try {
      String variableValue = valueFields.getTextValue();
      if (variableValue == null) {
        ByteArrayEntity byteEntity = valueFields.getByteArrayValue();
        if (byteEntity == null) {
          return null;
        }

        variableValue = new String(valueFields.getByteArrayValue().getBytes());
      }

      Spin<?> spinNode = SpinFactory.getInstance().createSpinFromString(variableValue, dataFormat);
      Object value = spinNode.mapTo(valueFields.getTextValue2());
      return value;

    } catch (SpinRuntimeException e) {
      throw new ProcessEngineException(
          "Cannot deserialize variable '" + valueFields.getName() + "' of format '" +
          valueFields.getDataFormatId() + "' with configuration '" + valueFields.getTextValue2() + "'");
    }
  }

  public DataFormat<?> getDataFormat() {
    return dataFormat;
  }

  public SerializedVariableValue getSerializedValue(ValueFields valueFields) {
    SerializedVariableValueImpl value = new SerializedVariableValueImpl();;

    String serializedValue = null;
    if (valueFields.getTextValue() != null) {
      serializedValue = valueFields.getTextValue();
    } else if (valueFields.getByteArrayValue() != null){
      try {
        ByteArrayEntity byteArray = valueFields.getByteArrayValue();
        serializedValue = new String(byteArray.getBytes(), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new ProcessEngineException("UTF-8 is not a supported encoding");
      }
    }

    value.setValue(serializedValue);
    value.setConfigValue(CONFIG_DATA_FORMAT_ID, valueFields.getDataFormatId());
    value.setConfigValue(CONFIG_TYPE, valueFields.getTextValue2());

    return value;
  }

}
