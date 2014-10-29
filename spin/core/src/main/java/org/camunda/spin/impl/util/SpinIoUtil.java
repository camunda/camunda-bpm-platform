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
package org.camunda.spin.impl.util;

import org.camunda.commons.utils.IoUtil;
import org.camunda.spin.impl.logging.SpinCoreLogger;
import org.camunda.spin.impl.logging.SpinLogger;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Daniel Meyer
 *
 */
public class SpinIoUtil extends IoUtil {

  public static final Charset ENCODING_CHARSET = Charset.forName("UTF-8");

  private static final SpinCoreLogger LOG = SpinLogger.CORE_LOGGER;

  /**
   * Converts a {@link OutputStream} to an {@link InputStream} by coping the data directly.
   * WARNING: Do not use for large data (>100MB). Only for testing purpose.
   *
   * @param outputStream the {@link OutputStream} to convert
   * @return the resulting {@link InputStream}
   */
  public static InputStream convertOutputStreamToInputStream(OutputStream outputStream) {
    byte[] data = ((ByteArrayOutputStream) outputStream).toByteArray();
    return new ByteArrayInputStream(data);
  }

  /**
   * Convert an {@link InputStream} to a {@link String}
   *
   * @param inputStream the {@link InputStream} to convert
   * @return the resulting {@link String}
   * @throws IOException
   */
  public static String getStringFromInputStream(InputStream inputStream) throws IOException {
    return getStringFromInputStream(inputStream, true);
  }

  /**
   * Convert an {@link InputStream} to a {@link String}
   *
   * @param inputStream the {@link InputStream} to convert
   * @param trim trigger if whitespaces are trimmed in the output
   * @return the resulting {@link String}
   * @throws IOException
   */
  public static String getStringFromInputStream(InputStream inputStream, boolean trim) throws IOException {
    BufferedReader bufferedReader = null;
    StringBuilder stringBuilder = new StringBuilder();
    try {
      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        if (trim) {
          stringBuilder.append(line.trim());
        } else {
          stringBuilder.append(line).append("\n");
        }
      }
    } finally {
      closeSilently(bufferedReader);
    }

    return stringBuilder.toString();
  }

  /**
   * Convert an {@link Reader} to a {@link String}
   *
   * @param reader the {@link Reader} to convert
   * @return the resulting {@link String}
   * @throws IOException
   */
  public static String getStringFromReader(Reader reader) throws IOException {
    return getStringFromReader(reader, true);
  }

  /**
   * Convert an {@link Reader} to a {@link String}
   *
   * @param reader the {@link Reader} to convert
   * @param trim trigger if whitespaces are trimmed in the output
   * @return the resulting {@link String}
   * @throws IOException
   */
  public static String getStringFromReader(Reader reader, boolean trim) throws IOException {
    BufferedReader bufferedReader = null;
    StringBuilder stringBuilder = new StringBuilder();
    try {
      bufferedReader = new BufferedReader(reader);
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        if (trim) {
          stringBuilder.append(line.trim());
        } else {
          stringBuilder.append(line).append("\n");
        }
      }
    } finally {
      closeSilently(bufferedReader);
    }

    return stringBuilder.toString();
  }

  public static Reader classpathResourceAsReader(String fileName) {
    try {
      File classpathFile = getClasspathFile(fileName);
      return new FileReader(classpathFile);
    } catch (FileNotFoundException e) {
      throw LOG.fileNotFoundException(fileName, e);
    }
  }

  public static Reader stringAsReader(String string) {
    return new StringReader(string);
  }

}
