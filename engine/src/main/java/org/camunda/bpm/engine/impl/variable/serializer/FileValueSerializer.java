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
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.engine.variable.value.builder.FileValueBuilder;

/**
 * @author Ronny Bräunlich
 * @since 7.4
 */
public class FileValueSerializer extends AbstractTypedValueSerializer<FileValue> {

  public FileValueSerializer() {
    super(ValueType.FILE);
  }

  @Override
  public void writeValue(FileValue value, ValueFields valueFields) {
    byte[] data = readBytes(value);
    valueFields.setByteArrayValue(data);
    valueFields.setTextValue(value.getFilename());
    valueFields.setTextValue2(value.getMimeType());
  }

  protected byte[] readBytes(FileValue value) {
    InputStream is = value.getValue();
    if (is == null) {
      return null;
    }
    DataInputStream dis = null;
    try {
      byte[] data = new byte[is.available()];
      dis = new DataInputStream(is);
      dis.readFully(data);
      dis.close();
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

  @Override
  public FileValue convertToTypedValue(UntypedValueImpl untypedValue) {
    throw new UnsupportedOperationException("Currently no automatic conversation from UntypedValue to FileValue");
  }

  @Override
  public FileValue readValue(ValueFields valueFields, boolean deserializeValue) {
    FileValueBuilder builder = Variables.fileValue(valueFields.getTextValue());
    if (valueFields.getByteArrayValue() != null) {
      builder.file(valueFields.getByteArrayValue().getBytes());
    }
    builder.mimeType(valueFields.getTextValue2());
    return builder.create();
  }

  @Override
  public String getName() {
    return valueType.getName();
  }

  @Override
  protected boolean canWriteValue(TypedValue value) {
    if (value == null || value.getType() == null) {
      // untyped value
      return false;
    }
    return value.getType().getName().equals(getName());
  }

}
