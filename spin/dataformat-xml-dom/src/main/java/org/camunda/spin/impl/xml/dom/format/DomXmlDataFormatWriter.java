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

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.camunda.spin.impl.xml.dom.DomXmlLogger;
import org.camunda.spin.spi.DataFormatWriter;
import org.camunda.spin.xml.SpinXmlElementException;
import org.w3c.dom.Node;

/**
 * A writer for XML DOM.
 *
 * @author Daniel Meyer
 */
public class DomXmlDataFormatWriter implements DataFormatWriter {

  protected static final DomXmlLogger LOG = DomXmlLogger.XML_DOM_LOGGER;

  protected static final String STRIP_SPACE_XSL = "org/camunda/spin/impl/xml/dom/format/strip-space.xsl";

  protected DomXmlDataFormat domXmlDataFormat;
  protected Templates formattingTemplates;

  public DomXmlDataFormatWriter(DomXmlDataFormat domXmlDataFormat) {
    this.domXmlDataFormat = domXmlDataFormat;
    this.formattingTemplates = reloadFormattingTemplates();
  }

  @Override
  public void writeToWriter(Writer writer, Object input) {
    writeResult(new StreamResult(writer), input);
  }

  protected void writeResult(StreamResult streamResult, Object input) {
    Node node = (Node) input;
    DOMSource domSource = new DOMSource(node);
    try {
      if (domXmlDataFormat.isPrettyPrint()) {
        getFormattingTransformer().transform(domSource, streamResult);
      } else {
        getTransformer().transform(domSource, streamResult);
      }
    } catch (TransformerException e) {
      throw LOG.unableToTransformElement(node, e);
    }
  }

  /**
   * Return a {@link Templates} instance for formatting configuration.
   * Uses the configured {@link TransformerFactory} from the {@link DomXmlDataFormat}.
   * Uses the formatting configuration from the {@link DomXmlDataFormat} if defined,
   * falls back to a default otherwise.
   *
   * @return the templates instance for the formatting configuration.
   */
  protected Templates reloadFormattingTemplates() {
    final TransformerFactory transformerFactory = this.domXmlDataFormat.getTransformerFactory();

    try (final InputStream formattingConfiguration = getFormattingConfiguration()) {
      if (formattingConfiguration == null) {
        throw LOG.unableToFindStripSpaceXsl(STRIP_SPACE_XSL);
      }
      return transformerFactory.newTemplates(new StreamSource(formattingConfiguration));
    } catch (final IOException | TransformerConfigurationException ex) {
      throw LOG.unableToLoadFormattingTemplates(ex);
    }
  }

  /**
   * Set the {@link Templates} which used for creating the transformer.
   * @param formattingTemplates
   */
  protected void setFormattingTemplates(Templates formattingTemplates) {
    this.formattingTemplates = formattingTemplates;
  }

  private InputStream getFormattingConfiguration() {
    final InputStream importedConfiguration = this.domXmlDataFormat.getFormattingConfiguration();
    if (importedConfiguration != null) {
      return importedConfiguration;
    }
    // default strip-spaces.xsl
    return DomXmlDataFormatWriter.class.getClassLoader().getResourceAsStream(STRIP_SPACE_XSL);
  }

  /**
   * Returns a configured transformer to write XML and apply indentation (pretty-print) to the xml.
   *
   * @return the XML configured transformer
   * @throws SpinXmlElementException if no new transformer can be created
   */
  protected Transformer getFormattingTransformer() {
    try {
      Transformer transformer = formattingTemplates.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      return transformer;
    } catch (TransformerConfigurationException e) {
      throw LOG.unableToCreateTransformer(e);
    }
  }

  /**
   * Returns a configured transformer to write XML as is.
   *
   * @return the XML configured transformer
   * @throws SpinXmlElementException if no new transformer can be created
   */
  protected Transformer getTransformer() {
    TransformerFactory transformerFactory = domXmlDataFormat.getTransformerFactory();
    try {
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      return transformer;
    } catch (TransformerConfigurationException e) {
      throw LOG.unableToCreateTransformer(e);
    }
  }

}
