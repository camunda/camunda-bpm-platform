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
package org.camunda.spin.impl.xml.dom;

import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.spi.DataFormatReader;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Daniel Meyer
 *
 */
public class XmlDomDataFormatReader implements DataFormatReader {

  private static XmlDomLogger LOG = SpinLogger.XML_DOM_LOGGER;

  public boolean canRead(InputStream input) {
    // TODO: check whether this reader can actually read the input
    return true;
  }

  public Element readInput(InputStream input) {

    DocumentBuilder documentBuilder = createDocumentBuilder();
    try {
      LOG.parsingInput();
      return documentBuilder.parse(input).getDocumentElement();

    } catch (SAXException e) {
      throw LOG.unableToParseInput(e);

    } catch (IOException e) {
      throw LOG.unableToParseInput(e);

    }
  }

  /**
   * @return the DocumentBuilder used by this reader
   */
  protected DocumentBuilder createDocumentBuilder() {
    DocumentBuilderFactory documentBuilderFactory = createDocumentBuilderFactory();
    LOG.usingDocumentBuilderFactory(documentBuilderFactory.getClass().getName());

    // configure the factory
    configureDocumentBuilderFactory(documentBuilderFactory);

    try {
      DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
      LOG.createdDocumentBuilder();
      return docBuilder;

    } catch (ParserConfigurationException e) {
      throw LOG.unableToCreateParser(e);

    }
  }

  /**
   * @return the DocumentBuilderFactory used by this reader
   */
  protected DocumentBuilderFactory createDocumentBuilderFactory() {
    return DocumentBuilderFactory.newInstance();
  }

  /**
   * Configure the document builder factory.
   *
   * @param documentBuilderFactory
   */
  protected void configureDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory) {

    documentBuilderFactory.setNamespaceAware(true);
    LOG.documentBuilderFactoryConfiguration("namespaceAware", "true");

    documentBuilderFactory.setValidating(false);
    LOG.documentBuilderFactoryConfiguration("validating", "false");

    documentBuilderFactory.setIgnoringComments(true);
    LOG.documentBuilderFactoryConfiguration("ignoringComments", "true");

    documentBuilderFactory.setIgnoringElementContentWhitespace(false);
    LOG.documentBuilderFactoryConfiguration("ignoringElementContentWhitespace", "false");

  }

}
