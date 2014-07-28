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
package org.camunda.bpm.engine.impl.variable;

import java.io.UnsupportedEncodingException;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinFactory;
import org.camunda.spin.SpinRuntimeException;
import org.camunda.spin.spi.DataFormat;

public class DefaultSerializationFormatType implements VariableType {

  public static final String TYPE_NAME = "defaultSerialization";

  // TODO: is this the best place for this constant?
  protected static final int TEXT_FIELD_LENGTH = 4000;

  protected DataFormat<?> dataFormat;

  public DefaultSerializationFormatType(DataFormat<?> dataFormat) {
    this.dataFormat = dataFormat;
  }

  public String getTypeName() {
    return TYPE_NAME;
  }

  public String getTypeNameForValue(Object value) {
    // TODO What's supposed to be here?
    return null;
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

      if (serializedVariable.getBytes().length <= 4000) {
        valueFields.setTextValue(spin.toString());
      } else {
        valueFields.setByteArrayValue(serializedVariable.getBytes());
      }

      valueFields.setDataFormatId(dataFormat.getName());
      valueFields.setConfiguration(dataFormat.getCanonicalTypeName(value));
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
      Object value = spinNode.mapTo(valueFields.getConfiguration());
      return value;

    } catch (SpinRuntimeException e) {
      throw new ProcessEngineException(
          "Cannot deserialize variable '" + valueFields.getName() + "' of format '" +
          valueFields.getDataFormatId() + "' with configuration '" + valueFields.getConfiguration() + "'");
    }
  }

  public Object getRawValue(ValueFields valueFields) {
    String value = null;

    if (valueFields.getTextValue() != null) {
      value = valueFields.getTextValue();
    } else if (valueFields.getByteArrayValue() != null){
      try {
        ByteArrayEntity byteArray = valueFields.getByteArrayValue();
        value = new String(byteArray.getBytes(), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new ProcessEngineException("UTF-8 is not a supported encoding");
      }
    }

    return value;
  }

}
