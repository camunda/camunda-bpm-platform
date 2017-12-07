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
package org.camunda.bpm.engine.variable.impl.value.builder;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.camunda.bpm.engine.variable.impl.value.FileValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.builder.FileValueBuilder;
import org.camunda.commons.utils.EnsureUtil;
import org.camunda.commons.utils.IoUtil;
import org.camunda.commons.utils.IoUtilException;

/**
 * @author Ronny Bräunlich
 * @since 7.4
 *
 */
public class FileValueBuilderImpl implements FileValueBuilder {

  protected FileValueImpl fileValue;

  public FileValueBuilderImpl(String filename) {
    EnsureUtil.ensureNotNull("filename", filename);
    fileValue = new FileValueImpl(PrimitiveValueType.FILE, filename);
  }

  @Override
  public FileValue create() {
    return fileValue;
  }

  @Override
  public FileValueBuilder mimeType(String mimeType) {
    fileValue.setMimeType(mimeType);
    return this;
  }

  @Override
  public FileValueBuilder file(File file) {
    try {
      return file(IoUtil.fileAsByteArray(file));
    } catch(IoUtilException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public FileValueBuilder file(InputStream stream) {
      try {
        return file(IoUtil.inputStreamAsByteArray(stream));
	  } catch(IoUtilException e) {
	  	throw new IllegalArgumentException(e);
	  }
  }

  @Override
  public FileValueBuilder file(byte[] bytes) {
    fileValue.setValue(bytes);
    return this;
  }

  @Override
  public FileValueBuilder encoding(Charset encoding) {
    fileValue.setEncoding(encoding);
    return this;
  }

  @Override
  public FileValueBuilder encoding(String encoding) {
    fileValue.setEncoding(encoding);
    return this;
  }

  @Override
  public FileValueBuilder setTransient(boolean isTransient) {
    fileValue.setTransient(isTransient);
    return this;
  }

}
