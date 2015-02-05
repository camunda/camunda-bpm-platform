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
package org.camunda.bpm.application;

import java.util.Comparator;
import java.util.ServiceLoader;

import org.camunda.bpm.engine.impl.javax.el.ELResolver;

/**
 * <p>SPI interface that allows providing a custom ElResolver implementation.</p>
 *
 * <p>Implementations of this interface are looked up through the Java SE {@link ServiceLoader} facilities.
 * If you want to provide a custom implementation in your application, place a file named
 * <code>META-INF/org.camunda.bpm.application.ProcessApplicationElResolver</code> inside your application
 * which contains the fully qualified classname of your implementation.</p>
 *
 * @author Daniel Meyer
 *
 */
public interface ProcessApplicationElResolver {

  // precedences for known providers
  static int SPRING_RESOLVER = 100;
  static int CDI_RESOLVER = 200;

  /**
   *  Allows to set a precedence to the ElResolver. Resolver with a lower precedence will be invoked first.
   */
  Integer getPrecedence();

  /**
   * return the Resolver. May be null.
   */
  ELResolver getElResolver(AbstractProcessApplication processApplication);

  /**
   * Comparator used for sorting providers
   *
   * @see ProcessApplicationElResolver#getPrecedence()
   */
  public static class ProcessApplicationElResolverSorter implements Comparator<ProcessApplicationElResolver> {

    public int compare(ProcessApplicationElResolver o1, ProcessApplicationElResolver o2) {
      return (-1) * o1.getPrecedence().compareTo(o2.getPrecedence());
    }

  }

}
