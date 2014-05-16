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
package org.camunda.spin.impl.util;

import org.camunda.spin.SpinRuntimeException;

import java.util.*;

import static org.camunda.spin.impl.util.SpinEnsure.ensureNotNull;
import static org.camunda.spin.logging.SpinCoreLogger.LOG;


/**
 * A simple facility for using a {@link ServiceLoader}.
 *
 * @author Daniel Meyer
 *
 */
public class Service {

  /**
   * Returns the service provider for the given interface. This method allows passing
   * in a default parameter which is used if no service implementation can be located
   * via the associated service provider.
   *
   * @param serviceInterface the interface of the service
   * @param defaultImplementation the default implementation of the service.
   * @return the provider
   *
   * @throws SpinRuntimeException in case
   */
  public static <T> T get(Class<T> serviceInterface, Class<? extends T> defaultImplementation) {
    ensureNotNull("serviceInterface", serviceInterface);
    ensureNotNull("defaultImplementation", defaultImplementation);

    ServiceLoader<T> loader = ServiceLoader.load(serviceInterface);
    Iterator<T> providerIterator = loader.iterator();

    if(providerIterator.hasNext()) {
      return providerIterator.next();

    } else {
      return createInstance(defaultImplementation);

    }
  }

  /**
   * Returns all implementations of a given service interface.
   *
   * @param serviceInterface the service interface
   * @return a collection containing all implementations of the interface
   */
  public static <T> Collection<T> getAll(Class<T> serviceInterface) {
    ServiceLoader<T> loader = ServiceLoader.load(serviceInterface);
    Iterator<T> providerIterator = loader.iterator();

    List<T> providers = new ArrayList<T>();
    while (providerIterator.hasNext()) {
      providers.add(providerIterator.next());
    }

    return providers;
  }

  /**
   * Instantiates the class provided as parameter.
   *
   * @param clazz the class to create an instance of
   * @return an instance of the class.
   *
   * @throws SpinRuntimeException in case no instance of the class could be constructed
   */
  private static <T> T createInstance(Class<T> clazz) {
    try {
      return clazz.newInstance();

    } catch (InstantiationException e) {
      throw LOG.unableToInstantiateClass(clazz.getName(), e);

    } catch (IllegalAccessException e) {
      throw LOG.unableToInstantiateClass(clazz.getName(), e);
    }
  }


}
