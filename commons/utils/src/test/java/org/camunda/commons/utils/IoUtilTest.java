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

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Sebastian Menski
 */
public class IoUtilTest {

  public final static String TEST_FILE_NAME = "org/camunda/commons/utils/testFile.txt";

  @Test
  public void shouldTransformBetweenInputStreamAndString() {
    InputStream inputStream = IoUtil.stringAsInputStream("test");
    String string = IoUtil.inputStreamAsString(inputStream);
    assertThat(string).isEqualTo("test");
  }
  
  @Test
  public void shouldTransformFromInputStreamToByteArray() {
    String testString = "Test String";
    InputStream inputStream = IoUtil.stringAsInputStream(testString);
    assertThat(IoUtil.inputStreamAsByteArray(inputStream)).isEqualTo(testString.getBytes(IoUtil.ENCODING_CHARSET));
  }  
  
  @Test
  public void shouldTransformFromStringToInputStreamToByteArray() {
    String testString = "Test String";
    InputStream inputStream = IoUtil.stringAsInputStream(testString);
    
    String newString = IoUtil.inputStreamAsString(inputStream);
    assertThat(testString).isEqualTo(newString);   
    
    inputStream = IoUtil.stringAsInputStream(testString);
    byte[] newBytes = newString.getBytes(IoUtil.ENCODING_CHARSET);
    assertThat(IoUtil.inputStreamAsByteArray(inputStream)).isEqualTo(newBytes);
  }  
  
  
  @Test
  public void getFileContentAsString() {
    assertThat(IoUtil.fileAsString(TEST_FILE_NAME)).isEqualTo("This is a Test!");
  }

  @Test
  public void shouldFailGetFileContentAsStringWithGarbageAsFilename() {
    try {
      IoUtil.fileAsString("asd123");
      fail("Expected: IoUtilException");
    } catch (IoUtilException e) {
      // happy way
    }
  }

  @Test
  public void getFileContentAsStream() {
    InputStream stream = IoUtil.fileAsStream(TEST_FILE_NAME);
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    StringBuilder output = new StringBuilder();
    String line;
    try {
      while((line = reader.readLine()) != null) {
        output.append(line);
      }
      assertThat(output.toString()).isEqualTo("This is a Test!");
    } catch(Exception e) {
      fail("Something went wrong while reading the input stream");
    }
  }

  @Test
  public void shouldFailGetFileContentAsStreamWithGarbageAsFilename() {
    try {
      IoUtil.fileAsStream("asd123");
      fail("Expected: IoUtilException");
    } catch(IoUtilException e) {
      // happy path
    }
  }

  @Test
  public void getFileFromClassPath() {
    File file = IoUtil.getClasspathFile(TEST_FILE_NAME);

    assertThat(file).isNotNull();
    assertThat(file.getName()).isEqualTo("testFile.txt");
  }

  @Test
  public void shouldFailGetFileFromClassPathWithGarbage() {
    try {
      IoUtil.getClasspathFile("asd123");
      fail("Expected: IoUtilException");
    } catch(IoUtilException e) {
      // happy way
    }
  }

  @Test
  public void shouldFailGetFileFromClassPathWithNull() {
    try {
      IoUtil.getClasspathFile(null);
      fail("Expected: IoUtilException");
    } catch(IoUtilException e) {
      // happy way
    }
  }

  @Test
  public void shouldUseFallBackWhenCustomClassLoaderIsWrong() {
    File file = IoUtil.getClasspathFile(TEST_FILE_NAME, new ClassLoader() {
      @Override
      public URL getResource(String name) {
        return null;
      }
    });
    assertThat(file).isNotNull();
    assertThat(file.getName()).isEqualTo("testFile.txt");
  }

  @Test
  public void shouldUseFallBackWhenCustomClassLoaderIsNull() {
    File file = IoUtil.getClasspathFile(TEST_FILE_NAME, null);
    assertThat(file).isNotNull();
    assertThat(file.getName()).isEqualTo("testFile.txt");
  }
}
