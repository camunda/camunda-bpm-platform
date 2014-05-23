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

package org.camunda.spin.impl.xml.dom;

import org.camunda.spin.Spin;
import org.camunda.spin.logging.SpinLogger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Wrapper of a xml dom attribute.
 *
 * @author Sebastian Menski
 */
public class SpinXmlDomAttribute extends Spin<SpinXmlDomAttribute> {

  private final static XmlDomLogger LOG = SpinLogger.XML_DOM_LOGGER;

  private final Attr attributeNode;

  /**
   * Create a new wrapper.
   *
   * @param attributeNode the dom xml attribute to wrap
   */
  public SpinXmlDomAttribute(Attr attributeNode) {
    this.attributeNode = attributeNode;
  }

  /**
   * @return the xml dom data format name
   */
  public String getDataFormatName() {
    return XmlDomDataFormat.INSTANCE.getName();
  }

  /**
   * Returns the local name of the attribute without namespace or prefix.
   *
   * @return the name of the attribute
   */
  public String name() {
    return attributeNode.getLocalName();
  }

  /**
   * Returns the namespace uri of the attribute and not the prefix.
   *
   * @return the namespace of the attribute
   */
  public String namespace() {
    String namespaceURI = attributeNode.getNamespaceURI();
    if (namespaceURI != null) {
      return namespaceURI;
    }
    else {
      return attributeNode.lookupNamespaceURI(attributeNode.getPrefix());
    }
  }

  /**
   * Checks if the attribute has the same namespace.
   *
   * @param namespace the namespace to check
   * @return true if the attribute has the same namespace
   */
  public boolean hasNamespace(String namespace) {
    if (namespace == null) {
      return attributeNode.getNamespaceURI() == null;
    }
    else {
      return namespace.equals(namespace());
    }
  }

  /**
   * Returns the value of the attribute as {@link String}.
   *
   * @return the string value of the attribute
   */
  public String value() {
    return attributeNode.getValue();
  }

  /**
   * Sets the value of the attribute.
   *
   * @param value the value to set
   * @return the wrapped xml dom attribute
   * @throws SpinXmlDomAttributeException if the value is null
   */
  public SpinXmlDomAttribute value(String value) {
    if (value == null) {
      throw LOG.unableToSetAttributeValueToNull(namespace(), name());
    }
    attributeNode.setValue(value);
    return this;
  }

  /**
   * Removes the attribute.
   *
   * @return the wrapped owner xml dom element
   */
  public SpinXmlDomElement remove() {
    Element ownerElement = attributeNode.getOwnerElement();
    ownerElement.removeAttributeNode(attributeNode);
    return new SpinXmlDomElement(ownerElement);
  }

}
