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
package org.camunda.bpm.engine.variable.impl.type;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.FileValueType;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.engine.variable.value.builder.FileValueBuilder;

/**
 * Valuetype to save files from byte arrays, inputstreams or just files as
 * process variables and retrieve them via an {@link InputStream}.
 *
 * @author Ronny Bräunlich
 * @since 7.4
 *
 */
public class FileValueTypeImpl extends AbstractValueTypeImpl implements FileValueType {

  private static final long serialVersionUID = 1L;

  public FileValueTypeImpl() {
    super("file");
  }

  @Override
  public TypedValue createValue(Object value, Map<String, Object> valueInfo) {
    Object filename = valueInfo.get(VALUE_INFO_FILE_NAME);
    if (filename == null) {
      throw new IllegalArgumentException("Cannot create file without filename! Please set a name into ValueInfo with key " + VALUE_INFO_FILE_NAME);
    }
    FileValueBuilder builder = Variables.fileValue(filename.toString());
    if (value instanceof File) {
      builder.file((File) value);
    } else if (value instanceof InputStream) {
      builder.file((InputStream) value);
    } else if (value instanceof byte[]) {
      builder.file((byte[]) value);
    } else {
      throw new IllegalArgumentException("Provided value is not of File, InputStream or byte[] type.");
    }

    if (valueInfo.containsKey(VALUE_INFO_FILE_MIME_TYPE)) {
      Object mimeType = valueInfo.get(VALUE_INFO_FILE_MIME_TYPE);

      if (mimeType == null) {
        throw new IllegalArgumentException("The provided mime type is null. Set a non-null value info property with key '" + VALUE_INFO_FILE_NAME + "'");
      }

      builder.mimeType(mimeType.toString());
    }
    if (valueInfo.containsKey(VALUE_INFO_FILE_ENCODING)) {
      Object encoding = valueInfo.get(VALUE_INFO_FILE_ENCODING);

      if (encoding == null) {
        throw new IllegalArgumentException("The provided encoding is null. Set a non-null value info property with key '" + VALUE_INFO_FILE_ENCODING + "'");
      }

      builder.encoding(encoding.toString());
    }

    builder.setTransient(isTransient(valueInfo));
    return builder.create();
  }

  @Override
  public Map<String, Object> getValueInfo(TypedValue typedValue) {
    if (!(typedValue instanceof FileValue)) {
      throw new IllegalArgumentException("Value not of type FileValue");
    }
    FileValue fileValue = (FileValue) typedValue;
    Map<String, Object> result = new HashMap<String, Object>(2);
    result.put(VALUE_INFO_FILE_NAME, fileValue.getFilename());
    if (fileValue.getMimeType() != null) {
      result.put(VALUE_INFO_FILE_MIME_TYPE, fileValue.getMimeType());
    }
    if (fileValue.getEncoding() != null) {
      result.put(VALUE_INFO_FILE_ENCODING, fileValue.getEncoding());
    }
    if (fileValue.isTransient()) {
      result.put(VALUE_INFO_TRANSIENT, fileValue.isTransient());
    }
    return result;
  }

  @Override
  public boolean isPrimitiveValueType() {
    return true;
  }

}
