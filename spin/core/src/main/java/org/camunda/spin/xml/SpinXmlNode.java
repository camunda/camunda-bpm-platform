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
package org.camunda.spin.xml;

import org.camunda.spin.Spin;

/**
 * An element in a tree-oriented XML data format.
 *
 * @author Sebastian Menski
 * @author Daniel Meyer
 *
 */
public abstract class SpinXmlNode<T extends SpinXmlNode<?>> extends Spin<T> {

  /**
   * Returns the local name of the node without namespace or prefix.
   *
   * @return the name of the node
   */
  public abstract String name();

  /**
   * Returns the namespace uri of the node and not the prefix.
   *
   * @return the namespace of the node
   */
  public abstract String namespace();

  /**
   * Returns the prefix of the node and not the namespace uri.
   *
   * @return the prefix of the node
   */
  public abstract String prefix();

  /**
   * Checks if the node has a given prefix.
   *
   * @param prefix the prefix to check
   * @return true if the name has the same prefix
   */
  public abstract boolean hasPrefix(String prefix);

  /**
   * Checks if the node has a given namespace.
   *
   * @param namespace the namespace to check
   * @return true if the node has the same namespace
   */
  public abstract boolean hasNamespace(String namespace);

  /**
   * Maps XML into specific class
   *
   * @param type Class to which the Xml should be mapped
   * @return mapped Class
   *
   * @throws SpinDataFormatException in case the input cannot be mapped to class for several reasons
   */
  public abstract <C> C mapTo(Class<C> type);

  /**
   * Maps Xml into specific class
   *
   * @param canonicalName canonical name of the class
   * @return mapped class
   *
   * @throws SpinDataFormatException in case the input cannot be mapped to class for several reasons
   */
  public abstract <C> C mapTo(String canonicalName);

}
