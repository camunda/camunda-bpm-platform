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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.camunda.spin.SpinFileNotFoundException;
import org.camunda.spin.SpinRuntimeException;
import org.camunda.spin.logging.SpinCoreLogger;
import org.camunda.spin.logging.SpinLogger;

/**
 * @author Daniel Meyer
 *
 */
public class IoUtil {
  
  public final static Charset ENCODING_CHARSET = Charset.forName("UTF-8");

  private final static SpinCoreLogger LOG = SpinLogger.CORE_LOGGER;

  public static void closeSilently(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (Exception e) {
      // ignored
    }
  }

  public static String fileAsString(File file) {
    try {
      return inputStreamAsString(new FileInputStream(file));

    } catch (FileNotFoundException e) {
      throw LOG.fileNotFoundException(file.getAbsolutePath(), e);
    }
  }

  /**
   * Returns the input stream as {@link String}.
   *
   * @param inputStream the input stream
   * @throws SpinRuntimeException in case the input stream cannot be read
   */
  public static String inputStreamAsString(InputStream inputStream) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      byte[] buffer = new byte[16 * 1024];
      int read = 0;
      while((read = inputStream.read(buffer)) > 0) {
        os.write(buffer, 0, read);
      }
      return os.toString(ENCODING_CHARSET.name());
    } catch (IOException e) {
      throw LOG.unableToReadInputStream(e);
    }
    finally {
      closeSilently(inputStream);
    }
  }

  public static InputStream stringAsInputStream(String string) {
    return new ByteArrayInputStream(string.getBytes(ENCODING_CHARSET));
  }


  /**
   * Returns the input stream of a file.
   *
   * @param file the {@link File} to load
   * @return the file content as input stream
   * @throws SpinFileNotFoundException if the file cannot be loaded
   */
  public static InputStream fileAsStream(File file) {
    try {
      return new BufferedInputStream(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      throw LOG.fileNotFoundException(file.getAbsolutePath());
    }
  }

  /**
   * Returns the {@link File} for a filename.
   *
   * @param filename the filename to load
   * @param classLoader the classLoader to load file with
   * @return the file object
   * @throws SpinFileNotFoundException if the file cannot be loaded
   */
  public static File getClasspathFile(String filename, ClassLoader classLoader) {
    URL fileUrl = classLoader.getResource(filename);
    if (fileUrl == null) {
      throw LOG.fileNotFoundException(filename);
    }

    try {
      return new File(fileUrl.toURI());
    } catch (URISyntaxException e) {
      throw LOG.fileNotFoundException(filename, e);
    }
  }

  public static File getClasspathFile(String filename) {
    return getClasspathFile(filename,  IoUtil.class.getClassLoader());
  }

  public static String readFileAsString(String filename) {
    File classpathFile = getClasspathFile(filename);
    return fileAsString(classpathFile);
  }

  public static InputStream getFileAsStream(String filename) {
    File classpathFile = getClasspathFile(filename);
    return fileAsStream(classpathFile);
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
  
  public static byte[] readFirstBytes(InputStream stream, int limit) {
    try {
      byte[] result = new byte[limit];
      int bytesRead = stream.read(result);
      
      if (bytesRead == -1) {
        result = new byte[0];
      } else if (bytesRead < result.length) {
        result = Arrays.copyOfRange(result, 0, bytesRead);
      }
      
      return result;
    } catch(IOException e) {
      throw LOG.unableToReadInputStream(e);
    }
    
    
  }

}
