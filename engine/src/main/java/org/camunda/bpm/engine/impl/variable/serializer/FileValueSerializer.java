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

import java.util.Arrays;

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.FileValueImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.engine.variable.value.builder.FileValueBuilder;

/**
 * @author Ronny Bräunlich
 * @since 7.4
 */
public class FileValueSerializer extends AbstractTypedValueSerializer<FileValue> {

  /**
   * The numbers values we encoded in textfield two.
   */
  protected static final int NR_OF_VALUES_IN_TEXTFIELD2 = 2;

  /**
   * The separator to be able to store encoding and mimetype inside the same
   * text field. Please be aware that the separator only works when it is a
   * character that is not allowed in the first component.
   */
  protected static final String MIMETYPE_ENCODING_SEPARATOR = "#";

  public FileValueSerializer() {
    super(ValueType.FILE);
  }

  @Override
  public void writeValue(FileValue value, ValueFields valueFields) {
    byte[] data = ((FileValueImpl) value).getByteArray();
    valueFields.setByteArrayValue(data);
    valueFields.setTextValue(value.getFilename());
    if (value.getMimeType() == null && value.getEncoding() != null) {
      valueFields.setTextValue2(MIMETYPE_ENCODING_SEPARATOR + value.getEncoding());
    } else if (value.getMimeType() != null && value.getEncoding() == null) {
      valueFields.setTextValue2(value.getMimeType() + MIMETYPE_ENCODING_SEPARATOR);
    } else if (value.getMimeType() != null && value.getEncoding() != null) {
      valueFields.setTextValue2(value.getMimeType() + MIMETYPE_ENCODING_SEPARATOR + value.getEncoding());
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
      builder.file(valueFields.getByteArrayValue());
    }
    // to ensure the same array size all the time
    if (valueFields.getTextValue2() != null) {
      String[] split = Arrays.copyOf(valueFields.getTextValue2().split(MIMETYPE_ENCODING_SEPARATOR, NR_OF_VALUES_IN_TEXTFIELD2), NR_OF_VALUES_IN_TEXTFIELD2);

      String mimeType = returnNullIfEmptyString(split[0]);
      String encoding = returnNullIfEmptyString(split[1]);

      builder.mimeType(mimeType);
      builder.encoding(encoding);
    }
    return builder.create();
  }

  protected String returnNullIfEmptyString(String s) {
    if (s.isEmpty()) {
      return null;
    }
    return s;
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
