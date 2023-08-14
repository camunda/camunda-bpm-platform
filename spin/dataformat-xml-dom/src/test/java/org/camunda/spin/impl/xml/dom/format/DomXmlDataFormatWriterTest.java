/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.spin.impl.xml.dom.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import org.camunda.spin.DataFormats;
import org.camunda.spin.SpinFactory;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.xml.JdkUtil;
import org.camunda.spin.xml.SpinXmlElement;
import org.junit.Test;

/**
 * Test xml transformation in DomXmlDataFormatWriter
 */
public class DomXmlDataFormatWriterTest {

  private final String newLine = System.getProperty("line.separator");
  private final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><order><product>Milk</product><product>Coffee</product><product> </product></order>";

  private final String formattedXmlIbmJDK = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><order>" + newLine
      + "  <product>Milk</product>" + newLine
      + "  <product>Coffee</product>" + newLine
      + "  <product/>" + newLine
      + "</order>";

  private final String formattedXml = formattedXmlIbmJDK + newLine;

  private final String formattedXmlWithWhitespaceInProductIbmJDK = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><order>" + newLine
      + "  <product>Milk</product>" + newLine
      + "  <product>Coffee</product>" + newLine
      + "  <product> </product>" + newLine
      + "</order>";

  private final String formattedXmlWithWhitespaceInProduct = formattedXmlWithWhitespaceInProductIbmJDK + newLine;


  // this is what execution.setVariable("test", spinXml); does
  // see https://github.com/camunda/camunda-bpm-platform/blob/master/engine-plugins/spin-plugin/src/main/java/org/camunda/spin/plugin/impl/SpinValueSerializer.java
  private byte[] serializeValue(SpinXmlElement spinXml) throws UnsupportedEncodingException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OutputStreamWriter outWriter = new OutputStreamWriter(out, "UTF-8");
    BufferedWriter bufferedWriter = new BufferedWriter(outWriter);

    spinXml.writeToWriter(bufferedWriter);
    return out.toByteArray();
  }

  public SpinXmlElement deserializeValue(byte[] serialized, DataFormat<SpinXmlElement> dataFormat)
      throws UnsupportedEncodingException {
    ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
    InputStreamReader inReader = new InputStreamReader(bais, "UTF-8");
    BufferedReader bufferedReader = new BufferedReader(inReader);

    Object wrapper = dataFormat.getReader().readInput(bufferedReader);
    return dataFormat.createWrapperInstance(wrapper);
  }

  /**
   * IBM JDK does not generate a new line character at the end
   * of an XSLT-transformed XML document. See CAM-14806.
   */
  private String getExpectedFormattedXML(boolean withWhitespaceInElement) {
    if (JdkUtil.runsOnIbmJDK()) {
      return withWhitespaceInElement ? formattedXmlWithWhitespaceInProductIbmJDK : formattedXmlIbmJDK;
    } else {
      return withWhitespaceInElement ? formattedXmlWithWhitespaceInProduct : formattedXml;
    }
  }

  private String getExpectedFormattedXML() {
    return getExpectedFormattedXML(false);
  }

  /**
   * standard behaviour: an unformatted XML will be formatted stored into a SPIN variable and also returned formatted.
   */
  @Test
  public void testStandardFormatter() throws Exception {
    // given
    DataFormat<SpinXmlElement> dataFormat = new DomXmlDataFormat(DataFormats.XML_DATAFORMAT_NAME);

    SpinXmlElement spinXml = SpinFactory.INSTANCE.createSpin(xml, dataFormat);

    // when
    byte[] serializedValue = serializeValue(spinXml);

    // then
    // assert that there are now new lines in the serialized value:
    assertThat(new String(serializedValue, "UTF-8")).isEqualTo(getExpectedFormattedXML());

    // when
    // this is what execution.getVariable("test"); does
    SpinXmlElement spinXmlElement = deserializeValue(serializedValue, dataFormat);

    // then
    assertThat(spinXmlElement.toString()).isEqualTo(getExpectedFormattedXML());
  }

  /**
   * behaviour fixed by CAM-13699: an already formatted XML will be formatted stored into a SPIN variable and also
   * returned formatted but no additional blank lines are inserted into the XML.
   */
  @Test
  public void testAlreadyFormattedXml() throws Exception {
    // given
    DataFormat<SpinXmlElement> dataFormat = new DomXmlDataFormat(DataFormats.XML_DATAFORMAT_NAME);

    SpinXmlElement spinXml = SpinFactory.INSTANCE.createSpin(formattedXml, dataFormat);

    // when
    byte[] serializedValue = serializeValue(spinXml);

    // then
    // assert that there are no new lines in the serialized value:
    assertThat(new String(serializedValue, "UTF-8")).isEqualTo(getExpectedFormattedXML());

    // when
    // this is what execution.getVariable("test"); does
    SpinXmlElement spinXmlElement = deserializeValue(serializedValue, dataFormat);

    // then
    assertThat(spinXmlElement.toString()).isEqualTo(getExpectedFormattedXML());
  }

  /**
   * new feature provided by CAM-13699 - pretty print feature disabled. The XML is stored and returned as is.
   */
  @Test
  public void testDisabledPrettyPrintUnformatted() throws Exception {
    // given
    DataFormat<SpinXmlElement> dataFormat = new DomXmlDataFormat(DataFormats.XML_DATAFORMAT_NAME);
    ((DomXmlDataFormat) dataFormat).setPrettyPrint(false);

    SpinXmlElement spinXml = SpinFactory.INSTANCE.createSpin(xml, dataFormat);

    // when
    byte[] serializedValue = serializeValue(spinXml);

    // then
    // assert that xml has not been formatted
    assertThat(new String(serializedValue, "UTF-8")).isEqualTo(xml);

    // when
    // this is what execution.getVariable("test"); does
    SpinXmlElement spinXmlElement = deserializeValue(serializedValue, dataFormat);

    // then
    assertThat(spinXmlElement.toString()).isEqualTo(xml);
  }

  /**
   * new feature provided by CAM-13699 - pretty print feature disabled. The XML is stored and returned as is.
   */
  @Test
  public void testDisabledPrettyPrintFormatted() throws Exception {

    // given
    String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><order>" + newLine
        + "  <product>Milk</product>" + newLine
        + "  <product>Coffee</product>" + newLine
        + "  <product> </product>" + newLine
        + "</order>";

    DataFormat<SpinXmlElement> dataFormat = new DomXmlDataFormat(DataFormats.XML_DATAFORMAT_NAME);
    ((DomXmlDataFormat) dataFormat).setPrettyPrint(false);

    SpinXmlElement spinXml = SpinFactory.INSTANCE.createSpin(formattedXmlWithWhitespaceInProduct, dataFormat);

    // when
    byte[] serializedValue = serializeValue(spinXml);

    // then
    // assert that xml has not been formatted
    assertThat(new String(serializedValue, "UTF-8")).isEqualTo(expectedXml);

    // when
    // this is what execution.getVariable("test"); does
    SpinXmlElement spinXmlElement = deserializeValue(serializedValue, dataFormat);

    // then
    assertThat(spinXmlElement.toString()).isEqualTo(expectedXml);
  }

  /**
   * new feature provided by https://github.com/camunda/camunda-bpm-platform/issues/3633: custom formatting
   * configuration to preserve-space.
   */
  @Test
  public void testCustomStripSpaceXSL() throws Exception {
    final DataFormat<SpinXmlElement> dataFormat = new DomXmlDataFormat(DataFormats.XML_DATAFORMAT_NAME);

    try (final InputStream inputStream = DomXmlDataFormatWriterTest.class.getClassLoader()
        .getResourceAsStream("org/camunda/spin/strip-space-preserve-space.xsl")) {
      ((DomXmlDataFormat) dataFormat).setFormattingConfiguration(inputStream);
    }

    final SpinXmlElement spinXml = SpinFactory.INSTANCE.createSpin(this.xml, dataFormat);

    // when
    final byte[] serializedValue = serializeValue(spinXml);

    // then
    // assert that xml has not been formatted
    assertThat(new String(serializedValue, "UTF-8")).isEqualTo(getExpectedFormattedXML(true));

    // when
    // this is what execution.getVariable("test"); does
    final SpinXmlElement spinXmlElement = deserializeValue(serializedValue, dataFormat);

    // then
    assertThat(spinXmlElement.toString()).isEqualTo(getExpectedFormattedXML(true));
  }
}
