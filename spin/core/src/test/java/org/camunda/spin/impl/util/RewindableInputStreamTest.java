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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.After;
import org.junit.Test;

public class RewindableInputStreamTest {

  private static final String EXAMPLE_INPUT_STRING = "a long string with content";
  private static final int DEFAULT_BUFFER_SIZE = 10;
  
  protected RewindableInputStream stream;
  
  @Test
  public void shouldRead() throws IOException {
    // read(byte[])
    stream = newStreamInstance(EXAMPLE_INPUT_STRING, DEFAULT_BUFFER_SIZE);
    assertThat(stream.getRewindBufferSize()).isEqualTo(DEFAULT_BUFFER_SIZE);
    assertThat(stream.getCurrentRewindableCapacity()).isEqualTo(DEFAULT_BUFFER_SIZE);
    
    byte[] buffer = new byte[5];
    int bytesRead = stream.read(buffer);
    
    String bufferAsString = new String(buffer, Charset.forName("UTF-8"));
    
    assertThat(bytesRead).isEqualTo(5);
    assertThat(stream.getCurrentRewindableCapacity()).isEqualTo(DEFAULT_BUFFER_SIZE - 5);
    
    assertThat(bufferAsString).isEqualTo(EXAMPLE_INPUT_STRING.substring(0, 5));
    
    stream.close();
    
    // read(byte[], off, len)
    stream = newStreamInstance(EXAMPLE_INPUT_STRING, DEFAULT_BUFFER_SIZE);
    
    buffer = new byte[5];
    bytesRead = stream.read(buffer, 2, 3);
    
    assertThat(bytesRead).isEqualTo(3);
    
    assertThat(buffer[0]).isEqualTo((byte) 0);
    assertThat(buffer[1]).isEqualTo((byte) 0);
    
    bufferAsString = new String(Arrays.copyOfRange(buffer, 2, 4));
    assertThat(bufferAsString).isEqualTo(EXAMPLE_INPUT_STRING.substring(0, 2));
    
    // read()
    stream = newStreamInstance(EXAMPLE_INPUT_STRING, DEFAULT_BUFFER_SIZE);
    
    byte byteRead = (byte) stream.read();
    assertThat(byteRead).isEqualTo((byte) 'a');
    
    stream.close();
  }
  
  @Test
  public void shouldRewind() throws IOException {
    stream = newStreamInstance(EXAMPLE_INPUT_STRING, DEFAULT_BUFFER_SIZE);
    
    byte[] buffer = new byte[5];
    stream.read(buffer);
    
    stream.rewind();
    
    assertThat(IoUtil.inputStreamAsString(stream)).isEqualTo(EXAMPLE_INPUT_STRING);
  }
  
  @Test
  public void shouldRewindAfterRepeatedRead() throws IOException {
    stream = newStreamInstance(EXAMPLE_INPUT_STRING, DEFAULT_BUFFER_SIZE);
    
    byte[] buffer = new byte[5];
    stream.read(buffer);
    stream.read(buffer);
    
    stream.rewind();
    
    assertThat(IoUtil.inputStreamAsString(stream)).isEqualTo(EXAMPLE_INPUT_STRING);
  }
  
  @Test
  public void shouldReadAndRewindWhenEndOfStreamIsReached() throws IOException {
    String input = EXAMPLE_INPUT_STRING.substring(0, 5);
    
    stream = newStreamInstance(input, DEFAULT_BUFFER_SIZE);
    
    byte[] buffer = new byte[10];
    int bytesRead = stream.read(buffer);
    String bufferAsString = 
        new String(Arrays.copyOfRange(buffer, 0, bytesRead), Charset.forName("UTF-8"));
    
    assertThat(bytesRead).isEqualTo(5);
    assertThat(bufferAsString).isEqualTo(input);
    
    bytesRead = stream.read(buffer);
    assertThat(bytesRead).isEqualTo(-1);
    
    stream.rewind();
    
    bytesRead = stream.read(buffer);
    bufferAsString = 
        new String(Arrays.copyOfRange(buffer, 0, bytesRead), Charset.forName("UTF-8"));
    
    assertThat(bytesRead).isEqualTo(5);
    assertThat(bufferAsString).isEqualTo(input);
  }
  
  @Test
  public void shouldReadRemainder() throws IOException {
    stream = newStreamInstance(EXAMPLE_INPUT_STRING, DEFAULT_BUFFER_SIZE);
    
    byte[] buffer = new byte[5];
    stream.read(buffer);
    
    assertThat(IoUtil.inputStreamAsString(stream)).isEqualTo(EXAMPLE_INPUT_STRING.substring(5));
  }

  /**
   * When reading more bytes than fits into the stream's buffer
   * @throws IOException 
   */
  @Test
  public void shouldFailWhenRewindLimitExceeded() throws IOException {
    // exceeding with read(byte[])
    stream = newStreamInstance(EXAMPLE_INPUT_STRING, DEFAULT_BUFFER_SIZE);
    
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE + 5];
    stream.read(buffer);
    
    assertThat(IoUtil.inputStreamAsString(stream))
      .isEqualTo(EXAMPLE_INPUT_STRING.substring(DEFAULT_BUFFER_SIZE + 5));
    
    try {
      stream.rewind();
      fail("IOException expected");
    } catch (IOException e) {
      // happy path
    }
    
    // exceeding with read()
    stream = newStreamInstance(EXAMPLE_INPUT_STRING, DEFAULT_BUFFER_SIZE);
    
    buffer = new byte[DEFAULT_BUFFER_SIZE];
    stream.read(buffer);
    stream.read(); 
    
    try {
      stream.rewind();
      fail("IOException expected");
    } catch (IOException e) {
      // happy path
    }
    
    // repeated read(byte[])
    stream = newStreamInstance(EXAMPLE_INPUT_STRING, DEFAULT_BUFFER_SIZE);
    
    buffer = new byte[DEFAULT_BUFFER_SIZE];
    stream.read(buffer);
    stream.read(buffer);
    
    try {
      stream.rewind();
      fail("IOException expected");
    } catch (IOException e) {
      // happy path
    }
  }
  
  @Test
  public void shouldRewindWhenNothingWasRead() throws IOException {
    stream = newStreamInstance("", DEFAULT_BUFFER_SIZE);
    
    int byteRead = stream.read();
    assertThat(byteRead).isEqualTo(-1);
    
    stream.rewind();
    
    byteRead = stream.read();
    assertThat(byteRead).isEqualTo(-1);
  }

  @After
  public void closeStream() {
    if (stream != null) {
      IoUtil.closeSilently(stream);
    }
  }
  
  protected RewindableInputStream newStreamInstance(String input, int bufferSize) {
    InputStream inputStream = IoUtil.stringAsInputStream(input);
    return new RewindableInputStream(inputStream, bufferSize);
  }
}
