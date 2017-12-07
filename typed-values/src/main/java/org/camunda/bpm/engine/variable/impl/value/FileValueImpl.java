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
package org.camunda.bpm.engine.variable.impl.value;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.camunda.bpm.engine.variable.type.FileValueType;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.FileValue;

/**
 * @author Ronny Bräunlich
 * @since 7.4
 *
 */
public class FileValueImpl implements FileValue {

  private static final long serialVersionUID = 1L;
  protected String mimeType;
  protected String filename;
  protected byte[] value;
  protected FileValueType type;
  protected String encoding;
  protected boolean isTransient;

  public FileValueImpl(byte[] value, FileValueType type, String filename, String mimeType, String encoding) {
    this.value = value;
    this.type = type;
    this.filename = filename;
    this.mimeType = mimeType;
    this.encoding = encoding;
  }

  public FileValueImpl(FileValueType type, String filename) {
    this(null, type, filename, null, null);
  }

  @Override
  public String getFilename() {
    return filename;
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public void setValue(byte[] bytes) {
    this.value = bytes;
  }

  @Override
  public InputStream getValue() {
    if (value == null) {
      return null;
    }
    return new ByteArrayInputStream(value);
  }

  @Override
  public ValueType getType() {
    return type;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setEncoding(Charset encoding) {
    this.encoding = encoding.name();
  }

  @Override
  public Charset getEncodingAsCharset() {
    if (encoding == null) {
      return null;
    }
    return Charset.forName(encoding);
  }

  @Override
  public String getEncoding() {
    return encoding;
  }

  /**
   * Get the byte array directly without wrapping it inside a stream to evade
   * not needed wrapping. This method is intended for the internal API, which
   * needs the byte array anyways.
   */
  public byte[] getByteArray() {
    return value;
  }

  @Override
  public String toString() {
    return "FileValueImpl [mimeType=" + mimeType + ", filename=" + filename + ", type=" + type + ", isTransient=" + isTransient + "]";
  }

  @Override
  public boolean isTransient() {
    return isTransient;
  }

  public void setTransient(boolean isTransient) {
    this.isTransient = isTransient;
  }
}
