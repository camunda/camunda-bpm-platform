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

import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.xml.tree.SpinXmlTreeAttribute;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Wrapper of a xml dom attribute.
 *
 * @author Sebastian Menski
 */
public class SpinXmlDomAttribute extends SpinXmlTreeAttribute {

  private final static XmlDomLogger LOG = SpinLogger.XML_DOM_LOGGER;

  protected final Attr attributeNode;

  protected final XmlDomDataFormat dataFormat;

  /**
   * Create a new wrapper.
   *
   * @param attributeNode the dom xml attribute to wrap
   * @param xmlDomDataFormat
   */
  public SpinXmlDomAttribute(Attr attributeNode, XmlDomDataFormat dataFormat) {
    this.attributeNode = attributeNode;
    this.dataFormat = dataFormat;
  }

  public String getDataFormatName() {
    return dataFormat.getName();
  }

  public Attr unwrap() {
    return attributeNode;
  }

  public String name() {
    return attributeNode.getLocalName();
  }

  public String namespace() {
    String namespaceURI = attributeNode.getNamespaceURI();
    if (namespaceURI != null) {
      return namespaceURI;
    }
    else {
      return attributeNode.lookupNamespaceURI(attributeNode.getPrefix());
    }
  }

  public boolean hasNamespace(String namespace) {
    if (namespace == null) {
      return attributeNode.getNamespaceURI() == null;
    }
    else {
      return namespace.equals(namespace());
    }
  }

  public String value() {
    return attributeNode.getValue();
  }

  public SpinXmlDomAttribute value(String value) {
    if (value == null) {
      throw LOG.unableToSetAttributeValueToNull(namespace(), name());
    }
    attributeNode.setValue(value);
    return this;
  }

  public SpinXmlDomElement remove() {
    Element ownerElement = attributeNode.getOwnerElement();
    ownerElement.removeAttributeNode(attributeNode);
    return dataFormat.createElementWrapper(ownerElement);
  }

}
