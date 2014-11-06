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

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.camunda.spin.impl.xml.dom.format.DomXmlDataFormat;

/**
 * Provider for the Marshallers and Unmarshallers the {@link DomXmlDataFormat} uses to
 * map Java objects to XML and vice versa. Implementations typically manage a JAXBContext.
 * The JAXBContext caches information about the types it is capable of processing.
 * Since the context is expensive to create, it is useful to cache
 * it. Different applications may require different caching strategies.
 *
 * @author Daniel Meyer
 *
 */
public interface JaxBContextProvider {

  /**
   * Obtain a Marshaller that can map the provided types.
   *
   * @param types the Java Types that are going to be marshalled
   * @return the Marshaller of marshalling the provided types to XML.
   */
  public Marshaller createMarshaller(Class<?>... types);

  /**
   * Obtain an Unmarshaller that can map the provided types.
   *
   * @param types the Java Types that are going to be unmarshalled
   * @return the Marshaller of unmarshalling the provided types from XML.
   */
  public Unmarshaller createUnmarshaller(Class<?>... types);

}
