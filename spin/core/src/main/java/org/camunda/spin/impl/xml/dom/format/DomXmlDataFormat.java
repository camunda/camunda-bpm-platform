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

import org.camunda.spin.DataFormats;
import org.camunda.spin.impl.logging.SpinLogger;
import org.camunda.spin.impl.xml.dom.DomXmlAttribute;
import org.camunda.spin.impl.xml.dom.DomXmlElement;
import org.camunda.spin.impl.xml.dom.DomXmlLogger;
import org.camunda.spin.impl.xml.dom.format.spi.DefaultJaxBContextProvider;
import org.camunda.spin.impl.xml.dom.format.spi.JaxBContextProvider;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.xml.SpinXmlAttribute;
import org.camunda.spin.xml.SpinXmlElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

/**
 * @author Daniel Meyer
 *
 */
public class DomXmlDataFormat implements DataFormat<SpinXmlElement> {

  protected static final DomXmlLogger LOG = SpinLogger.XML_DOM_LOGGER;

  /** the DocumentBuilderFactory used by the reader */
  protected DocumentBuilderFactory documentBuilderFactory;

  /** the TransformerFactory instance used by the writer */
  protected TransformerFactory transformerFactory;

  /** the JaxBContextProvider instance used by this writer. */
  protected JaxBContextProvider jaxBContextProvider;

  protected DomXmlDataFormatReader reader;
  protected DomXmlDataFormatWriter writer;
  protected DomXmlDataFormatMapper mapper;

  public DomXmlDataFormat() {
    this(defaultDocumentBuilderFactory());
  }

  public DomXmlDataFormat(JaxBContextProvider contextProvider) {
    this(defaultDocumentBuilderFactory(), contextProvider);
  }

  public DomXmlDataFormat(DocumentBuilderFactory documentBuilderFactory, JaxBContextProvider contextProvider) {
    this(documentBuilderFactory, defaultTransformerFactory(), contextProvider);
  }

  public DomXmlDataFormat(DocumentBuilderFactory documentBuilderFactory) {
    this(documentBuilderFactory, defaultTransformerFactory(), defaultJaxBContextProvider());
  }

  public DomXmlDataFormat(DocumentBuilderFactory documentBuilderFactory, TransformerFactory transformerFactory, JaxBContextProvider contextProvider) {
    this.documentBuilderFactory = documentBuilderFactory;
    LOG.usingDocumentBuilderFactory(documentBuilderFactory.getClass().getName());

    this.transformerFactory = transformerFactory;

    this.jaxBContextProvider = contextProvider;
    init();
  }

  protected void init() {
    this.reader = new DomXmlDataFormatReader(this);
    this.writer = new DomXmlDataFormatWriter(this);
    this.mapper = new DomXmlDataFormatMapper(this);
  }

  public Class<? extends SpinXmlElement> getWrapperType() {
    return DomXmlElement.class;
  }

  public SpinXmlElement createWrapperInstance(Object parameter) {
    return createElementWrapper((Element) parameter);
  }

  public String getName() {
    return DataFormats.XML_DATAFORMAT_NAME;
  }

  public SpinXmlElement createElementWrapper(Element element) {
    return new DomXmlElement(element, this);
  }

  public SpinXmlAttribute createAttributeWrapper(Attr attr) {
    return new DomXmlAttribute(attr, this);
  }

  public DomXmlDataFormatReader getReader() {
    return reader;
  }

  public DomXmlDataFormatWriter getWriter() {
    return writer;
  }

  public DomXmlDataFormatMapper getMapper() {
    return mapper;
  }

  public DocumentBuilderFactory getDocumentBuilderFactory() {
    return documentBuilderFactory;
  }

  public TransformerFactory getTransformerFactory() {
    return transformerFactory;
  }

  public JaxBContextProvider getJaxBContextProvider() {
    return jaxBContextProvider;
  }

  public static TransformerFactory defaultTransformerFactory() {
    return TransformerFactory.newInstance();
  }

  public static DocumentBuilderFactory defaultDocumentBuilderFactory() {

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    documentBuilderFactory.setNamespaceAware(true);
    LOG.documentBuilderFactoryConfiguration("namespaceAware", "true");

    documentBuilderFactory.setValidating(false);
    LOG.documentBuilderFactoryConfiguration("validating", "false");

    documentBuilderFactory.setIgnoringComments(true);
    LOG.documentBuilderFactoryConfiguration("ignoringComments", "true");

    documentBuilderFactory.setIgnoringElementContentWhitespace(false);
    LOG.documentBuilderFactoryConfiguration("ignoringElementContentWhitespace", "false");

    return documentBuilderFactory;
  }

  public static JaxBContextProvider defaultJaxBContextProvider() {
    return new DefaultJaxBContextProvider();
  }

}
