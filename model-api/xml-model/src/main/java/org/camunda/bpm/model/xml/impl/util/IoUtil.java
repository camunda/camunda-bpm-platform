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
package org.camunda.bpm.model.xml.impl.util;

import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public class IoUtil {

  public static void closeSilently(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (Exception e) {
      // ignored
    }
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
  private static String getStringFromInputStream(InputStream inputStream, boolean trim) throws IOException {
    BufferedReader bufferedReader = null;
    StringBuilder stringBuilder = new StringBuilder();
    try {
      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        if (trim) {
          stringBuilder.append(line.trim());
        }
        else {
          stringBuilder.append(line).append("\n");
        }
      }
    }
    finally {
      closeSilently(bufferedReader);
    }

    return stringBuilder.toString();
  }

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
   * Converts a {@link org.w3c.dom.Document} to its String representation
   *
   * @param document  the XML document to convert
   */
  public static String convertXmlDocumentToString(Document document) {
    StringWriter stringWriter = new StringWriter();
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
    } catch (TransformerConfigurationException e) {
      e.printStackTrace();
    } catch (TransformerException e) {
      e.printStackTrace();
    }
    return stringWriter.toString();
  }
}
