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
package org.camunda.bpm.model.xml.instance;

import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.w3c.dom.Element;

/**
 * An instance of a {@link ModelElementType}
 *
 * @author Daniel Meyer
 *
 */
public interface ModelElementInstance {

  /**
   * Returns the represented DOM {@link Element}.
   *
   * @return the DOM element
   */
  Element getDomElement();

  /**
   * Returns the model instance which contains this type instance.
   *
   * @return the model instance
   */

  ModelInstance getModelInstance();
  /**
   * Returns the parent element of this.
   *
   * @return the parent element
   */
  ModelElementInstance getParentElement();

  /**
   * Returns the element type of this.
   *
   * @return the element type
   */
  ModelElementType getElementType();

  /**
   * Returns the attribute value for the attribute name.
   *
   * @param attributeName  the name of the attribute
   * @return the value of the attribute
   */
  String getAttributeValue(String attributeName);

  /**
   * Sets attribute value by name.
   *
   * @param attributeName  the name of the attribute
   * @param xmlValue  the value to set
   * @param isIdAttribute  true if the attribute is an ID attribute, false otherwise
   */
  void setAttributeValue(String attributeName, String xmlValue, boolean isIdAttribute);

  /**
   * Removes attribute by name.
   *
   * @param attributeName  the name of the attribute
   */
  void removeAttribute(String attributeName);

  /**
   * Returns the attribute value for the given attribute name and namespace URI.
   *
   * @param attributeName  the attribute name of the attribute
   * @param namespaceUri  the namespace URI of the attribute
   * @return the value of the attribute
   */
  String getAttributeValueNs(String attributeName, String namespaceUri);

  /**
   * Sets the attribute value by name and namespace.
   *
   * @param attributeName  the name of the attribute
   * @param namespaceUri  the namespace URI of the attribute
   * @param xmlValue  the XML value to set
   * @param isIdAttribute  true if the attribute is an ID attribute, false otherwise
   */
  void setAttributeValueNs(String attributeName, String namespaceUri, String xmlValue, boolean isIdAttribute);

  /**
   * Removes the attribute by name and namespace.
   *
   * @param attributeName  the name of the attribute
   * @param namespaceUri  the namespace URI of the attribute
   */
  void removeAttributeNs(String attributeName, String namespaceUri);

  /**
   * Returns the text content of the DOM element without leading and trailing spaces. For
   * raw text content see {@link ModelElementInstanceImpl#getRawTextContent()}.
   *
   * @return text content of underlying DOM element with leading and trailing whitespace trimmed
   */
  String getTextContent();

  /**
   * Returns the raw text content of the DOM element including all whitespaces.
   *
   * @return raw text content of underlying DOM element
   */
  String getRawTextContent();

  /**
   * Sets the text content of the DOM element
   *
   * @param  textContent the new text content
   */
  void setTextContent(String textContent);

  /**
   * Replaces this element with a new element and updates references.
   *
   * @param newElement  the new element to replace with
   */
  void replaceWithElement(ModelElementInstance newElement);
}
