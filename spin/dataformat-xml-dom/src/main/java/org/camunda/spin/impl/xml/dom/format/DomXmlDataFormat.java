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

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
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

/**
 * @author Daniel Meyer
 */
public class DomXmlDataFormat implements DataFormat<SpinXmlElement> {

  protected static final DomXmlLogger LOG = DomXmlLogger.XML_DOM_LOGGER;

  protected static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
  protected static final String DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
  protected static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
  protected static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
  protected static final String JAXP_ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";
  protected static final String JAXP_ACCESS_EXTERNAL_SCHEMA_SYSTEM_PROPERTY = "javax.xml.accessExternalSchema";
  protected static final String JAXP_ACCESS_EXTERNAL_SCHEMA_ALL = "all";

  public static final String XXE_PROPERTY = "xxe-processing";
  public static final String SP_PROPERTY = "secure-processing";

  /**
   * the DocumentBuilderFactory used by the reader
   */
  protected DocumentBuilderFactory documentBuilderFactory;

  /**
   * the TransformerFactory instance used by the writer
   */
  protected TransformerFactory transformerFactory;

  /**
   * the JaxBContextProvider instance used by this writer.
   */
  protected JaxBContextProvider jaxBContextProvider;

  protected DomXmlDataFormatReader reader;
  protected DomXmlDataFormatWriter writer;
  protected DomXmlDataFormatMapper mapper;

  protected final String name;

  protected boolean prettyPrint;

  protected InputStream formattingConfiguration;

  public DomXmlDataFormat(String name) {
    this(name, defaultDocumentBuilderFactory());
  }

  public DomXmlDataFormat(String name, Map<String, Object> configurationProperties) {
    this(name, configurableDocumentBuilderFactory(configurationProperties));
  }

  public DomXmlDataFormat(String name, JaxBContextProvider contextProvider) {
    this(name, defaultDocumentBuilderFactory(), contextProvider);
  }

  public DomXmlDataFormat(String name,
                          DocumentBuilderFactory documentBuilderFactory,
                          JaxBContextProvider contextProvider) {
    this(name, documentBuilderFactory, defaultTransformerFactory(), contextProvider);
  }

  public DomXmlDataFormat(String name, DocumentBuilderFactory documentBuilderFactory) {
    this(name, documentBuilderFactory, defaultTransformerFactory(), defaultJaxBContextProvider());
  }

  public DomXmlDataFormat(String name,
                          DocumentBuilderFactory documentBuilderFactory,
                          TransformerFactory transformerFactory,
                          JaxBContextProvider contextProvider) {
    this.name = name;
    this.documentBuilderFactory = documentBuilderFactory;
    this.prettyPrint = true;
    this.formattingConfiguration = null;

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

  @Override
  public Class<? extends SpinXmlElement> getWrapperType() {
    return DomXmlElement.class;
  }

  @Override
  public SpinXmlElement createWrapperInstance(Object parameter) {
    return createElementWrapper((Element) parameter);
  }

  @Override
  public String getName() {
    return name;
  }

  public SpinXmlElement createElementWrapper(Element element) {
    return new DomXmlElement(element, this);
  }

  public SpinXmlAttribute createAttributeWrapper(Attr attr) {
    return new DomXmlAttribute(attr, this);
  }

  @Override
  public DomXmlDataFormatReader getReader() {
    return reader;
  }

  @Override
  public DomXmlDataFormatWriter getWriter() {
    return writer;
  }

  @Override
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

  public void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory) {
    this.documentBuilderFactory = documentBuilderFactory;
  }

  public void setTransformerFactory(TransformerFactory transformerFactory) {
    this.transformerFactory = transformerFactory;
    this.writer.reloadFormattingTemplates();
  }

  public void setJaxBContextProvider(JaxBContextProvider jaxBContextProvider) {
    this.jaxBContextProvider = jaxBContextProvider;
  }

  public boolean isPrettyPrint() {
    return prettyPrint;
  }

  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  public InputStream getFormattingConfiguration() {
    return this.formattingConfiguration;
  }

  public void setFormattingConfiguration(InputStream formattingConfiguration) {
    this.formattingConfiguration = formattingConfiguration;
    //writer need a new formattingTemplate with the new formattingConfiguration
    this.writer.setFormattingTemplates(this.writer.reloadFormattingTemplates());
  }

  public static TransformerFactory defaultTransformerFactory() {
    return TransformerFactory.newInstance();
  }

  public static DocumentBuilderFactory defaultDocumentBuilderFactory() {
    return configurableDocumentBuilderFactory(Collections.emptyMap());
  }

  public static DocumentBuilderFactory configurableDocumentBuilderFactory(Map<String, Object> configurationProperties) {

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    documentBuilderFactory.setNamespaceAware(true);
    LOG.documentBuilderFactoryConfiguration("namespaceAware", "true");

    documentBuilderFactory.setValidating(false);
    LOG.documentBuilderFactoryConfiguration("validating", "false");

    documentBuilderFactory.setIgnoringComments(true);
    LOG.documentBuilderFactoryConfiguration("ignoringComments", "true");

    documentBuilderFactory.setIgnoringElementContentWhitespace(false);
    LOG.documentBuilderFactoryConfiguration("ignoringElementContentWhitespace", "false");

    if ((boolean) configurationProperties.getOrDefault(XXE_PROPERTY, false) == false) {
      disableXxeProcessing(documentBuilderFactory);
    }

    if ((boolean) configurationProperties.getOrDefault(SP_PROPERTY, true) == true) {
      enableSecureProcessing(documentBuilderFactory);
    }

    return documentBuilderFactory;
  }

  public static JaxBContextProvider defaultJaxBContextProvider() {
    return new DefaultJaxBContextProvider();
  }

  /*
   * Configures the DocumentBuilderFactory in a way, that it is protected against
   * XML External Entity Attacks. If the implementing parser does not support one or
   * multiple features, the failed feature is ignored. The parser might not be protected,
   * if the feature assignment fails.
   *
   * @see <a href="https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet">OWASP Information of XXE attacks</a>
   *
   * @param dbf The factory to configure.
   */
  protected static void disableXxeProcessing(DocumentBuilderFactory dbf) {
    try {
      dbf.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
      dbf.setFeature(DISALLOW_DOCTYPE_DECL, true);
      dbf.setFeature(LOAD_EXTERNAL_DTD, false);
      dbf.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
    } catch (ParserConfigurationException ignored) {
      // ignore
    }
    dbf.setXIncludeAware(false);
    dbf.setExpandEntityReferences(false);
  }

  /*
   * Configures the DocumentBuilderFactory to process XML securely.
   * If the implementing parser does not support one or multiple features,
   * the failed feature is ignored. The parser might not be protected,
   * if the feature assignment fails.
   *
   * @param dbf The factory to configure.
   */
  protected static void enableSecureProcessing(DocumentBuilderFactory dbf) {
    try {
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      dbf.setAttribute(JAXP_ACCESS_EXTERNAL_SCHEMA, resolveAccessExternalSchemaProperty());
    } catch (ParserConfigurationException | IllegalArgumentException ignored) {
      // ignored
    }
  }

  /*
   * JAXP allows users to override the default value via system properties and
   * a central properties file (see https://docs.oracle.com/javase/tutorial/jaxp/properties/scope.html).
   * However, both are overridden by an explicit configuration in code, as we apply it.
   * Since we want users to customize the value, we take the system property into account.
   * The properties file is not supported at the moment.
   */
  protected static String resolveAccessExternalSchemaProperty() {
    String systemProperty = System.getProperty(JAXP_ACCESS_EXTERNAL_SCHEMA_SYSTEM_PROPERTY);

    if (systemProperty != null) {
      return systemProperty;
    } else {
      return JAXP_ACCESS_EXTERNAL_SCHEMA_ALL;
    }
  }
}
