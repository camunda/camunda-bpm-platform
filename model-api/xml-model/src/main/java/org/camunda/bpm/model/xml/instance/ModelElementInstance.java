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
import org.camunda.bpm.model.xml.impl.type.ModelElementTypeImpl;
import org.camunda.bpm.model.xml.type.ModelElementType;

/**
 * An instance of a {@link ModelElementTypeImpl}
 *
 * @author Daniel Meyer
 *
 */
public interface ModelElementInstance {

  String getAttributeValue(String attributeName);

  void setAttributeValue(String attributeName, String xmlValue, boolean isIdAttribute);

  void removeAttribute(String attributeName);

  String getAttributeValueNs(String attributeName, String namespaceUri);

  void setAttributeValueNs(String attributeName, String namespaceUri, String xmlValue, boolean isIdAttribute);

  void removeAttributeNs(String attributeName, String namespaceUri);

  ModelElementType getElementType();

  ModelElementInstance getParentElement();

  ModelInstance getModelInstance();

  /**
   * Replaces this element with a new element and updates references.
   *
   * @param newElement  the new element to replace with
   */
  void replaceWithElement(ModelElementInstance newElement);

  String getTextContent();

  String getRawTextContent();

  void setTextContent(String textContent);
}
