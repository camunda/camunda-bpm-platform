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
import org.camunda.spin.spi.DataFormatMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import static org.camunda.spin.impl.util.SpinEnsure.ensureNotNull;

/**
 * {@link DataFormatMapper} using JAXB for mapping Java Objects to XML and vice-versa.
 *
 * @author Stefan Hentschel.
 * @author Daniel Meyer
 */
public class XmlDomDataFormatMapper implements DataFormatMapper {

  protected XmlDomDataFormat format;

  private static final XmlDomLogger LOG = SpinLogger.XML_DOM_LOGGER;

  public XmlDomDataFormatMapper(XmlDomDataFormat format) {
    this.format = format;
  }

  public Object mapJavaToInternal(Object parameter) {
    ensureNotNull("Parameter", parameter);
    try {
      Marshaller marshaller = format.getConfiguredMarshaller(parameter.getClass());

      DOMResult domResult = new DOMResult();
      marshaller.marshal(parameter, domResult);

      Node node = domResult.getNode();
      return ((Document)node).getDocumentElement();

    } catch (JAXBException e) {
      throw LOG.unableToMapInput(parameter, e);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T mapInternalToJava(Object parameter, Class<T> javaClass) {
    ensureNotNull("Parameter", parameter);
    ensureNotNull("Type", javaClass);

    SpinXmlDomElement xmlNode = (SpinXmlDomElement) parameter;
    try {
      Unmarshaller unmarshaller = format.getConfiguredUnmarshaller(javaClass);

      return (T) unmarshaller.unmarshal(new DOMSource(xmlNode.unwrap()));
    } catch (JAXBException e) {
      throw LOG.unableToDeserialize(parameter, javaClass.getCanonicalName(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T mapInternalToJava(Object parameter, String classIdentifier) {
    ensureNotNull("Parameter", parameter);
    ensureNotNull("classIdentifier", classIdentifier);

    try {
      Class<?> javaClass = Class.forName(classIdentifier);
      return (T) mapInternalToJava(parameter, javaClass);
    } catch (ClassNotFoundException e) {
      throw LOG.unableToDeserialize(parameter, classIdentifier, e);
    }
  }
}
