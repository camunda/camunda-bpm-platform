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

import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.camunda.spin.impl.xml.dom.DomXmlLogger;
import org.camunda.spin.spi.DataFormatWriter;
import org.camunda.spin.xml.SpinXmlElementException;
import org.w3c.dom.Node;

/**
 * A writer for XML DOM.
 *
 * @author Daniel Meyer
 *
 */
public class DomXmlDataFormatWriter implements DataFormatWriter {

  protected static final DomXmlLogger LOG = DomXmlLogger.XML_DOM_LOGGER;

  protected DomXmlDataFormat domXmlDataFormat;

  public DomXmlDataFormatWriter(DomXmlDataFormat domXmlDataFormat) {
    this.domXmlDataFormat = domXmlDataFormat;
  }

  public void writeToWriter(Writer writer, Object input) {
    writeResult(new StreamResult(writer), input);
  }

  protected void writeResult(StreamResult streamResult, Object input) {
    Node node = (Node) input;
    DOMSource domSource = new DOMSource(node);
    try {
      getTransformer().transform(domSource, streamResult);
    } catch (TransformerException e) {
      throw LOG.unableToTransformElement(node, e);
    }
  }

  /**
   * Returns a configured transformer to write XML.
   *
   * @return the XML configured transformer
   * @throws SpinXmlElementException if no new transformer can be created
   */
  protected Transformer getTransformer() {
    TransformerFactory transformerFactory = domXmlDataFormat.getTransformerFactory();
    try {
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      return transformer;
    }
    catch (TransformerConfigurationException e) {
      throw LOG.unableToCreateTransformer(e);
    }
  }

}
