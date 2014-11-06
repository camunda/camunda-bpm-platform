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

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.camunda.spin.impl.xml.dom.DomXmlLogger;
import org.camunda.spin.impl.xml.dom.format.spi.JaxBContextProvider;
import org.camunda.spin.spi.DataFormatMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * {@link DataFormatMapper} using JAXB for mapping Java Objects to XML and vice-versa.
 *
 * @author Stefan Hentschel.
 * @author Daniel Meyer
 */
public class DomXmlDataFormatMapper implements DataFormatMapper {

  protected DomXmlDataFormat format;

  private static final DomXmlLogger LOG = DomXmlLogger.XML_DOM_LOGGER;

  public DomXmlDataFormatMapper(DomXmlDataFormat format) {
    this.format = format;
  }

  public boolean canMap(Object parameter) {
    if(parameter == null) {
      return false;
    } else {
      // Check for @XmlRootElement to be present on the type
      return parameter.getClass().getAnnotation(XmlRootElement.class) != null;
    }
  }

  public String getCanonicalTypeName(Object object) {
    // simply returns the the name of the class.
    return object.getClass().getName();
  }

  public Object mapJavaToInternal(Object parameter) {
    ensureNotNull("Parameter", parameter);
    try {
      Marshaller marshaller = getMarshaller(parameter.getClass());

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

    Node xmlNode = (Node) parameter;
    try {
      Unmarshaller unmarshaller = getUnmarshaller(javaClass);

      return (T) unmarshaller.unmarshal(new DOMSource(xmlNode));
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

  protected Marshaller getMarshaller(Class<?> parameter) throws JAXBException {
    JaxBContextProvider jaxBContextProvider = format.getJaxBContextProvider();
    return jaxBContextProvider.createMarshaller(parameter);
  }

  protected Unmarshaller getUnmarshaller(Class<?> parameter) throws JAXBException {
    JaxBContextProvider jaxBContextProvider = format.getJaxBContextProvider();
    return jaxBContextProvider.createUnmarshaller(parameter);
  }
}
