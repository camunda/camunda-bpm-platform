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
package org.camunda.spin.impl.xml.dom.format;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.camunda.spin.impl.xml.dom.DomXmlLogger;
import org.camunda.spin.spi.TextBasedDataFormatReader;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Daniel Meyer
 *
 */
public class DomXmlDataFormatReader extends TextBasedDataFormatReader {

  private static final DomXmlLogger LOG = DomXmlLogger.XML_DOM_LOGGER;
  private static final Pattern INPUT_MATCHING_PATTERN = Pattern.compile("\\A(\\s)*<");

  protected DomXmlDataFormat dataFormat;

  public DomXmlDataFormatReader(DomXmlDataFormat dataFormat) {
    this.dataFormat = dataFormat;
  }

  public Element readInput(Reader input) {

    DocumentBuilder documentBuilder = getDocumentBuilder();
    try {
      LOG.parsingInput();
      return documentBuilder.parse(new InputSource(input)).getDocumentElement();

    } catch (SAXException e) {
      throw LOG.unableToParseInput(e);

    } catch (IOException e) {
      throw LOG.unableToParseInput(e);

    }
  }

  /**
   * @return the DocumentBuilder used by this reader
   */
  protected DocumentBuilder getDocumentBuilder() {
    try {
      DocumentBuilder docBuilder = dataFormat.getDocumentBuilderFactory().newDocumentBuilder();
      LOG.createdDocumentBuilder();
      return docBuilder;

    } catch (ParserConfigurationException e) {
      throw LOG.unableToCreateParser(e);

    }
  }

  protected Pattern getInputDetectionPattern() {
    return INPUT_MATCHING_PATTERN;
  }

}
