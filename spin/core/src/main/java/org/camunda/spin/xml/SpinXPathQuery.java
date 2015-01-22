/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.spin.xml;

import org.camunda.spin.SpinList;

import java.util.Map;

/**
 * @author Sebastian Menski
 */
public abstract class SpinXPathQuery {

  /**
   * Returns the XPath query result as element.
   *
   * @return the wrapped XML element
   */
  public abstract SpinXmlElement element();

  /**
   * Returns the XPath query result as a element list.
   *
   * @return the wrapped XML element list
   */
  public abstract SpinList<SpinXmlElement> elementList();

  /**
   * Returns the XPath query result as attribute.
   *
   * @return the wrapped XML attribute
   */
  public abstract SpinXmlAttribute attribute();

  /**
   * Returns the XPath query result as a attribute list.
   *
   * @return the wrapped XML attribute list
   */
  public abstract SpinList<SpinXmlAttribute> attributeList();

  /**
   * Returns the XPath query result as a string.
   *
   * @return the string result
   */
  public abstract String string();

  /**
   * Returns the XPath query result as a double.
   *
   * @return the double result
   */
  public abstract Double number();

  /**
   * Returns the XPath query result as a boolean.
   *
   * @return the boolean result
   */
  public abstract Boolean bool();

  /**
   * Maps a prefix, uri pair to a namespace.
   *
   * @param prefix prefix for the namespace.
   * @param namespace uri of the namespace
   * @return itself
   */
  public abstract SpinXPathQuery ns(String prefix, String namespace);

  /**
   * Maps a map of prefix, uri pairs to namespaces
   * @param namespaces Map of the prefix, uri pairs.
   * @return itself
   */
  public abstract SpinXPathQuery ns(Map<String, String> namespaces);

}
