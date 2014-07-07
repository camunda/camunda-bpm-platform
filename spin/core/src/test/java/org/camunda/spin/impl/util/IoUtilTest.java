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

import java.io.InputStream;

import org.junit.Test;

public class IoUtilTest {

  private static final int INPUT_STREAM_BYTE_LIMIT = 16;
  
  @Test
  public void testReadFirstBytes() {
    String input = "a string with some content";
    InputStream inputStream = IoUtil.stringAsInputStream(input);
    
    byte[] firstBytes = IoUtil.readFirstBytes(inputStream, INPUT_STREAM_BYTE_LIMIT);
    assertThat(new String(firstBytes, IoUtil.ENCODING_CHARSET)).isEqualTo(input.substring(0, INPUT_STREAM_BYTE_LIMIT));
  }
  
  @Test
  public void testReadFirstBytesUtf8() {
    String input = "ä string with some content";
    InputStream inputStream = IoUtil.stringAsInputStream(input);
    
    byte[] firstBytes = IoUtil.readFirstBytes(inputStream, INPUT_STREAM_BYTE_LIMIT);
    
    // ä is two bytes in utf-8
    assertThat(new String(firstBytes, IoUtil.ENCODING_CHARSET)).isEqualTo(input.substring(0, INPUT_STREAM_BYTE_LIMIT - 1));
  }
  
}
