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
package org.camunda.bpm.engine.impl.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;


/**
 * @author Tom Baeyens
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class IoUtil {

  private static final EngineUtilLogger LOG = ProcessEngineLogger.UTIL_LOGGER;

  public static byte[] readInputStream(InputStream inputStream, String inputStreamName) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[16*1024];
    try {
      int bytesRead = inputStream.read(buffer);
      while (bytesRead!=-1) {
        outputStream.write(buffer, 0, bytesRead);
        bytesRead = inputStream.read(buffer);
      }
    }
    catch (Exception e) {
      throw LOG.exceptionWhileReadingStream(inputStreamName, e);
    }
    return outputStream.toByteArray();
  }

  public static String readFileAsString(String filePath) {
    byte[] buffer = new byte[(int) getFile(filePath).length()];
    BufferedInputStream inputStream = null;
    try {
      inputStream = new BufferedInputStream(new FileInputStream(getFile(filePath)));
      inputStream.read(buffer);
    }
    catch(Exception e) {
      throw LOG.exceptionWhileReadingFile(filePath, e);
    }
    finally {
      IoUtil.closeSilently(inputStream);
    }
    return new String(buffer, Charset.forName("UTF-8"));
  }

  public static File getFile(String filePath) {
    URL url = IoUtil.class.getClassLoader().getResource(filePath);
    try {
      return new File(url.toURI());
    }
    catch (Exception e) {
      throw LOG.exceptionWhileGettingFile(filePath, e);
    }
  }

  public static void writeStringToFile(String content, String filePath) {
    BufferedOutputStream outputStream = null;
    try {
      outputStream = new BufferedOutputStream(new FileOutputStream(getFile(filePath)));
      outputStream.write(content.getBytes());
      outputStream.flush();
    }
    catch(Exception e) {
      throw LOG.exceptionWhileWritingToFile(filePath, e);
    }
    finally {
      IoUtil.closeSilently(outputStream);
    }
  }

  /**
   * Closes the given stream. The same as calling {@link Closeable#close()}, but
   * errors while closing are silently ignored.
   */
  public static void closeSilently(Closeable closeable) {
    try {
      if(closeable != null) {
        closeable.close();
      }
    }
    catch(IOException ignore) {
      LOG.debugCloseException(ignore);
    }
  }

  /**
   * Flushes the given object. The same as calling {@link Flushable#flush()}, but
   * errors while flushing are silently ignored.
   */
  public static void flushSilently(Flushable flushable) {
    try {
      if(flushable != null) {
        flushable.flush();
      }
    }
    catch(IOException ignore) {
      LOG.debugCloseException(ignore);
    }
  }
}
