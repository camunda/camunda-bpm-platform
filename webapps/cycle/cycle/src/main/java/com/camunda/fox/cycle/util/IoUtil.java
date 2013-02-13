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
package com.camunda.fox.cycle.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import com.camunda.fox.cycle.exception.CycleException;


/**
 * @author Tom Baeyens
 * @author Frederik Heremans
 * @author Joram Barrez
 * 
 * @author nico.rehwaldt
 */
public class IoUtil {

  /**
   * Controls if intermediate results are written to files.
   */
  public static boolean DEBUG;

  /**
   * Directory, into which intermediate results are written.
   */
  public static String DEBUG_DIR;

  private static final int BUFFERSIZE = 4096;

  public static byte[] readInputStream(InputStream inputStream, String inputStreamName) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[16*1024];
    try {
      int bytesRead = inputStream.read(buffer);
      while (bytesRead!=-1) {
        outputStream.write(buffer, 0, bytesRead);
        bytesRead = inputStream.read(buffer);
      }
    } catch (Exception e) {
      throw new CycleException("Couldn't read input stream " + inputStreamName, e);
    }
    return outputStream.toByteArray();
  }

  public static String readFileAsString(String filePath) {
    byte[] buffer = new byte[(int) getFile(filePath).length()];
    BufferedInputStream inputStream = null;
    try {
      inputStream = new BufferedInputStream(new FileInputStream(getFile(filePath)));
      inputStream.read(buffer);
    } catch(Exception e) {
      throw new CycleException("Couldn't read file " + filePath + ": " + e.getMessage());
    } finally {
      IoUtil.closeSilently(inputStream);
    }
    return new String(buffer);
  }
  
  public static InputStream readFileAsInputStream(String absoluteClassPath) {
    InputStream inputStream = null;
    inputStream = IoUtil.class.getClass().getResourceAsStream(absoluteClassPath);
    if (inputStream == null) {
      throw new CycleException("Unable to read " + absoluteClassPath + " as inputstream.");
    }
    return inputStream;
  }

  public static File getFile(String filePath) {
    URL url = IoUtil.class.getClassLoader().getResource(filePath);
    try {
      return new File(url.toURI());
    } catch (Exception e) {
      throw new CycleException("Couldn't get file " + filePath + ": " + e.getMessage());
    }
  }

  public static void writeStringToFile(String content, String filePath) {
    BufferedOutputStream outputStream = null;
    try {
      outputStream = new BufferedOutputStream(new FileOutputStream(getFile(filePath)));
      outputStream.write(content.getBytes());
      outputStream.flush();
    } catch(Exception e) {
      throw new CycleException("Couldn't write file " + filePath, e);
    } finally {
      IoUtil.closeSilently(outputStream);
    }
  }

  public static void writeStringToFileIfDebug(String content, String filename, String suffix) {
    if (DEBUG) {
      String filePath = "";
      if (DEBUG_DIR != null && DEBUG_DIR.length() > 0) {
        filePath = DEBUG_DIR + System.getProperty("file.separator");
        File debugDirectory = new File(filePath);
        if (!debugDirectory.exists()) {
          if (!debugDirectory.mkdirs()) {
            throw new RuntimeException("Unable to create debugDirectory: " + debugDirectory.getAbsolutePath());
          }
        }
        filePath = filePath + filename + "." + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS_").format(new Date()) + suffix;
        try {
          FileWriter writer = new FileWriter(filePath);
          writer.write(content);
          writer.flush();
          writer.close();
        } catch (IOException e) {
          throw new RuntimeException("Unable to write debug file: " + filePath, e);
        }
      }
    }
  }

  /**
   * Closes the given stream. The same as calling {@link InputStream#close()}, but
   * errors while closing are silently ignored.
   */
  public static void closeSilently(InputStream inputStream) {
    try {
      if(inputStream != null) {
        inputStream.close();
      }
    } catch(IOException ignore) {
      // Exception is silently ignored
    }
  }

  public static void closeSilently(InputStream ... streams) {
    for (InputStream is: streams) {
      closeSilently(is);
    }
  }

  /**
   * Closes the given stream. The same as calling {@link OutputStream#close()}, but
   * errors while closing are silently ignored.
   */
  public static void closeSilently(OutputStream outputStream) {
    try {
      if(outputStream != null) {
        outputStream.close();
      }
    } catch(IOException ignore) {
      // Exception is silently ignored
    }
  }

  public static void closeSilently(OutputStream ... streams) {
    for (OutputStream os: streams) {
      closeSilently(os);
    }
  }

  public static int copyBytes(InputStream in, OutputStream out) throws IOException {
    if (in == null || out == null) {
      throw new IllegalArgumentException("In/OutStream cannot be null");
    }

    try {
      int total = 0;
      byte[] buffer = new byte[BUFFERSIZE];
      for (int bytesRead; (bytesRead = in.read(buffer)) != -1;) {
        out.write(buffer, 0, bytesRead);
        total += bytesRead;
      }
      return total;
    } catch (IOException e) {
      throw e;
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }

  public static String toString(InputStream input) throws IOException {
    return IOUtils.toString(input);
  }

  public static String toString(InputStream input, String encoding) throws IOException {
    return IOUtils.toString(input, encoding);
  }

  /**
   * Returns an input stream serving the given argument
   * 
   * @param result
   * @param encoding
   * @return 
   */
  public static InputStream toInputStream(String result, String encoding) {
    try {
      return new ByteArrayInputStream(result.getBytes(encoding));
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("Unsupported encoding", ex);
    }
  }
}
