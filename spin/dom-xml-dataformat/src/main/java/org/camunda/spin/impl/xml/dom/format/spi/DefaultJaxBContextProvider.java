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
package org.camunda.spin.impl.xml.dom.format.spi;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.camunda.spin.impl.xml.dom.DomXmlLogger;

/**
 * Simple implementation for the JaxBContextProvider interface returning a new context
 * each time it is invoked. This implementation does not perform any kind of caching.
 *
 * @author Daniel Meyer
 *
 */
public class DefaultJaxBContextProvider implements JaxBContextProvider {

  private static final DomXmlLogger LOG = DomXmlLogger.XML_DOM_LOGGER;

  public JAXBContext getContext(Class<?>... types) {
    try {
      return JAXBContext.newInstance(types);
    }
    catch (JAXBException e) {
      throw LOG.unableToCreateContext(e);
    }
  }

  @Override
  public Marshaller createMarshaller(Class<?>... types) {
    try {
      return getContext(types).createMarshaller();
    } catch (JAXBException e) {
      throw LOG.unableToCreateMarshaller(e);
    }
  }

  @Override
  public Unmarshaller createUnmarshaller(Class<?>... types) {
    try {
      return getContext(types).createUnmarshaller();
    } catch (JAXBException e) {
      throw LOG.unableToCreateUnmarshaller(e);
    }
  }

}
