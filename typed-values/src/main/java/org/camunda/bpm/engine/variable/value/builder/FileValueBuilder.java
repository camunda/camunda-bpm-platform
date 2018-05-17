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
package org.camunda.bpm.engine.variable.value.builder;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.camunda.bpm.engine.variable.value.FileValue;

/**
 * @author Ronny Bräunlich
 * @since 7.4
 *
 */
public interface FileValueBuilder extends TypedValueBuilder<FileValue> {

  /**
   * Saves the MIME type of a file in the value infos.
   *
   * @param mimeType
   *          the MIME type as string
   */
  FileValueBuilder mimeType(String mimeType);

  /**
   * Sets the value to the specified {@link File}.
   *
   * @see #file(byte[])
   * @see #file(InputStream)
   */
  FileValueBuilder file(File file);

  /**
   * Sets the value to the specified {@link InputStream}.
   *
   * @see #file(byte[])
   * @see #file(File)
   */
  FileValueBuilder file(InputStream stream);

  /**
   * Sets the value to the specified {@link Byte} array
   *
   * @see #file(File)
   * @see #file(InputStream)
   */
  FileValueBuilder file(byte[] bytes);

  /**
   * Sets the encoding for the file in the value infos (optional).
   *
   * @param encoding
   * @return
   */
  FileValueBuilder encoding(Charset encoding);

  /**
   * Sets the encoding for the file in the value infos (optional).
   *
   * @param encoding
   * @return
   */
  FileValueBuilder encoding(String encoding);

}
