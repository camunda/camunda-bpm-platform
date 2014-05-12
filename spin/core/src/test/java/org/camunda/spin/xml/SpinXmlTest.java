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

package org.camunda.spin.xml;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.DataFormats.xml;
import static org.camunda.spin.Spin.S;
import static org.camunda.spin.Spin.XML;

/**
 * @author Sebastian Menski
 */
public class SpinXmlTest {

  public final static String TEST_STRING = "<customers><customer id=\"customer1\" /><customer id=\"customer2\" /></customers>";
  public final static InputStream TEST_INPUT_STREAM = new ByteArrayInputStream(TEST_STRING.getBytes(Charset.forName("UTF-8")));

  @Test
  public void shouldWrapXmlString() {
    SpinXml xml = XML(TEST_STRING);
    assertThat(xml).isNotNull();

    xml = S(TEST_STRING, xml());
    assertThat(xml).isNotNull();

    xml = S(TEST_STRING).as(xml());
    assertThat(xml).isNotNull();
  }

  @Test
  public void shouldWrapXmlInputStream() {
    SpinXml xml = XML(TEST_INPUT_STREAM);
    assertThat(xml).isNotNull();

    xml = S(TEST_INPUT_STREAM, xml());
    assertThat(xml).isNotNull();

    xml = S(TEST_INPUT_STREAM).as(xml());
    assertThat(xml).isNotNull();
  }

}
