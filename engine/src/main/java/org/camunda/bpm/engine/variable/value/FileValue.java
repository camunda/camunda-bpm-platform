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
package org.camunda.bpm.engine.variable.value;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author Ronny Bräunlich
 * @since 7.4
 *
 */
public interface FileValue extends TypedValue {

  String getFilename();

  String getMimeType();

  /**
   * Convenience method to save the transformation. This method will perform no
   * check if the saved encoding is known to the JVM and therefore could throw
   * every exception that {@link Charset#forName(String)} lists.
   * <p>
   * If no encoding has been saved it will return null.
   *
   */
  Charset getEncodingAsCharset();

  /**
   * @return the saved encoding or null if none has been saved
   */
  String getEncoding();

  @Override
  InputStream getValue();

}
