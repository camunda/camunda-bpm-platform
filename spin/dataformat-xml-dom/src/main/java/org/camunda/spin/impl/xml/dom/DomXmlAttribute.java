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

import java.io.IOException;
import java.io.Writer;

import org.camunda.spin.impl.xml.dom.format.DomXmlDataFormat;
import org.camunda.spin.spi.DataFormatMapper;
import org.camunda.spin.xml.SpinXmlAttribute;
import org.camunda.spin.xml.SpinXmlElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Wrapper of a xml dom attribute.
 *
 * @author Sebastian Menski
 */
public class DomXmlAttribute extends SpinXmlAttribute {

  private static final DomXmlLogger LOG = DomXmlLogger.XML_DOM_LOGGER;

  protected final Attr attributeNode;

  protected final DomXmlDataFormat dataFormat;

  /**
   * Create a new wrapper.
   *
   * @param attributeNode the dom xml attribute to wrap
   * @param dataFormat the xml dom data format
   */
  public DomXmlAttribute(Attr attributeNode, DomXmlDataFormat dataFormat) {
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
    return attributeNode.getNamespaceURI();
  }

  public String prefix() {
    return attributeNode.getPrefix();
  }

  public boolean hasPrefix(String prefix) {
    String attributePrefix = attributeNode.getPrefix();
    if(attributePrefix == null) {
      return prefix == null;
    } else {
      return attributePrefix.equals(prefix);
    }
  }

  public boolean hasNamespace(String namespace) {
    String attributeNamespace = attributeNode.getNamespaceURI();
    if (attributeNamespace == null) {
      return namespace == null;
    }
    else {
      return attributeNamespace.equals(namespace);
    }
  }

  public String value() {
    return attributeNode.getValue();
  }

  public SpinXmlAttribute value(String value) {
    if (value == null) {
      throw LOG.unableToSetAttributeValueToNull(namespace(), name());
    }
    attributeNode.setValue(value);
    return this;
  }

  public SpinXmlElement remove() {
    Element ownerElement = attributeNode.getOwnerElement();
    ownerElement.removeAttributeNode(attributeNode);
    return dataFormat.createElementWrapper(ownerElement);
  }

  public String toString() {
    return value();
  }

  public void writeToWriter(Writer writer) {
    try {
      writer.write(toString());
    } catch (IOException e) {
      throw LOG.unableToWriteAttribute(this, e);
    }
  }

  public <C> C mapTo(Class<C> javaClass) {
    DataFormatMapper mapper = dataFormat.getMapper();
    return mapper.mapInternalToJava(this, javaClass);
  }

  public <C> C mapTo(String javaClass) {
    DataFormatMapper mapper = dataFormat.getMapper();
    return mapper.mapInternalToJava(this, javaClass);
  }

}
