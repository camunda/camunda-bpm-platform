/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.commons.utils;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Sebastian Menski
 */
public final class IoUtil {

  private static final IoUtilLogger LOG = UtilsLogger.IO_UTIL_LOGGER;

  /**
   * Returns the input stream as {@link String}.
   *
   * @param inputStream the input stream
   */
  public static String inputStreamAsString(InputStream inputStream) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      byte[] buffer = new byte[16 * 1024];
      int read = 0;
      while((read = inputStream.read(buffer)) > 0) {
        os.write(buffer, 0, read);
      }
      return os.toString("utf-8");
    } catch (IOException e) {
      throw LOG.unableToReadInputStream(e);
    }
    finally {
      closeSilently(inputStream);
    }
  }

  /**
   * Returns the {@link String} as {@link InputStream}.
   *
   * @param string the {@link String} to convert
   * @return the {@link InputStream} containing the {@link String}
   */
  public static InputStream stringAsInputStream(String string) {
    return new ByteArrayInputStream(string.getBytes(Charset.forName("UTF-8")));
  }

  /**
   * Close a closable ignoring any IO exception.
   *
   * @param closeable the closable to close
   */
  public static void closeSilently(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException e) {
      // ignore
    }
  }

}
