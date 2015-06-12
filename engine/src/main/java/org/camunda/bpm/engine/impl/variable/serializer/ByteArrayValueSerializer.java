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
package org.camunda.bpm.engine.impl.variable.serializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.core.variable.value.UntypedValueImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ByteArrayValueSerializer extends PrimitiveValueSerializer<BytesValue> {

  public ByteArrayValueSerializer() {
    super(ValueType.BYTES);
  }

  public BytesValue convertToTypedValue(UntypedValueImpl untypedValue) {
    Object value = untypedValue.getValue();
    if (value instanceof byte[]) {
      return Variables.byteArrayValue((byte[]) value);
    } else {
      byte[] data = readBytesFromStream((InputStream) value);
      return Variables.byteArrayValue(data);
    }
  }

  private byte[] readBytesFromStream(InputStream value) {
    DataInputStream dis = null;
    byte[] data;
    try {
      data = new byte[value.available()];
      dis = new DataInputStream(value);
      dis.readFully(data);
      return data;
    } catch (IOException e) {
      throw new ProcessEngineException(e);
    } finally {
      try {
        if (dis != null) {
          dis.close();
        }
      } catch (IOException e) {
        throw new ProcessEngineException(e);
      }
    }
  }

  public BytesValue readValue(ValueFields valueFields) {
    return Variables.byteArrayValue(getBytes(valueFields));
  }

  public void writeValue(BytesValue variableValue, ValueFields valueFields) {
    setBytes(valueFields, variableValue.getValue());
  }

  @Override
  protected boolean canWriteValue(TypedValue typedValue) {
    return super.canWriteValue(typedValue) || typedValue.getValue() instanceof InputStream;
  }

  public static byte[] getBytes(ValueFields valueFields) {
    byte[] byteArray = null;
    if (valueFields.getByteArrayValue() != null) {
      byteArray = valueFields.getByteArrayValue().getBytes();
    }
    return byteArray;
  }

  public static void setBytes(ValueFields valueFields, byte[] bytes) {
    ByteArrayEntity byteArray = valueFields.getByteArrayValue();

    if (byteArray == null) {
      valueFields.setByteArrayValue(bytes);
    } else {
      byteArray.setBytes(bytes);
    }
  }

}
