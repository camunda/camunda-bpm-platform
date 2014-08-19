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

import java.util.Date;
import java.util.Map;

import org.camunda.bpm.engine.delegate.SerializedVariableTypes;
import org.camunda.bpm.engine.delegate.SerializedVariableValue;
import org.camunda.bpm.engine.impl.core.variable.SerializedVariableValueImpl;


/**
 * @author Tom Baeyens
 */
public class DateType implements VariableType {

  public String getTypeName() {
    return SerializedVariableTypes.Date.getName();
  }

  public boolean isCachable() {
    return true;
  }

  public boolean isAbleToStore(Object value) {
    if (value==null) {
      return true;
    }
    return Date.class.isAssignableFrom(value.getClass());
  }

  public Object getValue(ValueFields valueFields) {
    Long longValue = valueFields.getLongValue();
    if (longValue!=null) {
      return new Date(longValue);
    }
    return null;
  }

  public void setValue(Object value, ValueFields valueFields) {
    if (value!=null) {
      valueFields.setLongValue(((Date)value).getTime());
    } else {
      valueFields.setLongValue(null);
    }
  }

  public String getTypeNameForValue(Object value) {
    // typename independent of value
    return Date.class.getSimpleName();
  }

  public SerializedVariableValue getSerializedValue(ValueFields valueFields) {
    SerializedVariableValueImpl result = new SerializedVariableValueImpl();
    result.setValue(valueFields.getLongValue());
    return result;
  }

  public void setValueFromSerialized(Object serializedValue, Map<String, Object> configuration, ValueFields valueFields) {
    if (serializedValue != null) {
      valueFields.setLongValue((Long) serializedValue);
    } else {
      valueFields.setLongValue(null);
    }

  }

  public boolean isAbleToStoreSerializedValue(Object value, Map<String, Object> configuration) {
    if (value == null) {
      return true;
    }

    return Long.class.isAssignableFrom(value.getClass());
  }
}
