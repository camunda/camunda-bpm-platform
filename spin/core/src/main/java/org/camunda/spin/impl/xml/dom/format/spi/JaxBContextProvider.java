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

/**
 * Provider for the JAXBContext. The JAXBContext cashes information about the types
 * it is capable of processing. Since the context is expensive to create, it is useful to cache
 * it. Different applications may require different caching strategies.
 *
 * @author Daniel Meyer
 *
 */
public interface JaxBContextProvider {

  /**
   * Obtain a JAXBContext for the provided types.
   *
   * @param types the Java Types to construct the context for
   * @return the JAXBContext capable of handing the provided types.
   */
  public JAXBContext getContext(Class<?>... types);

}
