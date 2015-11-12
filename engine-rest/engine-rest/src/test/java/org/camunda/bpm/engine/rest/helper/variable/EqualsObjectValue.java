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
package org.camunda.bpm.engine.rest.helper.variable;

import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.hamcrest.Description;


/**
 * @author Thorben Lindhauer
 *
 */
public class EqualsObjectValue extends EqualsTypedValue<EqualsObjectValue> {

  protected String serializationFormat;
  protected String objectTypeName;
  protected String serializedValue;
  protected Object value;
  protected boolean isDeserialized = false;

  public EqualsObjectValue() {
    this.type = ValueType.OBJECT;
  }

  public EqualsObjectValue serializationFormat(String serializationFormat) {
    this.serializationFormat = serializationFormat;
    return this;
  }

  public EqualsObjectValue objectTypeName(String objectTypeName) {
    this.objectTypeName = objectTypeName;
    return this;
  }

  public EqualsObjectValue value(Object value) {
    this.value = value;
    return this;
  }

  public EqualsObjectValue isDeserialized() {
    this.isDeserialized = true;
    return this;
  }

  public EqualsObjectValue serializedValue(String serializedValue) {
    this.serializedValue = serializedValue;
    return this;
  }

  public boolean matches(Object argument) {
    if (!super.matches(argument)) {
      return false;
    }

    if (!ObjectValue.class.isAssignableFrom(argument.getClass())) {
      return false;
    }

    ObjectValue objectValue = (ObjectValue) argument;

    if (isDeserialized) {
      if (!objectValue.isDeserialized()) {
        return false;
      }

      if (value == null) {
        if (objectValue.getValue() != null) {
          return false;
        }
      } else {
        if (!value.equals(objectValue.getValue())) {
          return false;
        }
      }

    } else {
      if (objectValue.isDeserialized()) {
        return false;
      }


      if (serializationFormat == null) {
        if (objectValue.getSerializationDataFormat() != null) {
          return false;
        }
      } else {
        if (!serializationFormat.equals(objectValue.getSerializationDataFormat())) {
          return false;
        }
      }

      if (objectTypeName == null) {
        if (objectValue.getObjectTypeName() != null) {
          return false;
        }
      } else {
        if (!objectTypeName.equals(objectValue.getObjectTypeName())) {
          return false;
        }
      }

      if (serializedValue == null) {
        if (objectValue.getValueSerialized() != null) {
          return false;
        }
      } else {
        if (!serializedValue.equals(objectValue.getValueSerialized())) {
          return false;
        }
      }
    }


    return true;
  }

  public static EqualsObjectValue objectValueMatcher() {
    return new EqualsObjectValue();
  }

  public void describeTo(Description description) {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getSimpleName());
    sb.append(": ");
    sb.append("serializedValue=");
    sb.append(serializedValue);
    sb.append(", objectTypeName=");
    sb.append(objectTypeName);
    sb.append(", serializationFormat=");
    sb.append(serializationFormat);
    sb.append(", isDeserialized=false");

    description.appendText(sb.toString());
  }

}
