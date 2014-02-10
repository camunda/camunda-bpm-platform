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
package org.camunda.bpm.model.xml;

import org.camunda.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

import java.util.Collection;

/**
 * An instance of a model
 *
 * @author Daniel Meyer
 *
 */
public interface ModelInstance {

  /**
   * Returns the wrapped {@link DomDocument}.
   *
   * @return the DOM document
   */
  DomDocument getDocument();

  /**
   * Returns the {@link ModelElementInstanceImpl ModelElement} corresponding to the document
   * element of this model or null if no document element exists.
   *
   * @return the document element or null
   */
  ModelElementInstance getDocumentElement();

  /**
   * Updates the document element.
   *
   * @param documentElement  the new document element to set
   */
  void setDocumentElement(ModelElementInstance documentElement);

  /**
   * Creates a new instance of type class.
   *
   * @param type  the class of the type to create
   * @param <T>   instance type
   * @return the new created instance
   */
  <T extends ModelElementInstance> T newInstance(Class<T> type);

  /**
   * Creates a new instance of type.
   *
   * @param type  the type to create
   * @param <T>   instance type
   * @return the new created instance
   */
  <T extends ModelElementInstance> T newInstance(ModelElementType type);

  /**
   * Returns the underlying model.
   *
   * @return the model
   */
  Model getModel();

  /**
   * Find a unique element of the model by id.
   *
   * @param id  the id of the element
   * @return the element with the id or null
   */
  ModelElementInstance getModelElementById(String id);

  /**
   * Find all elements of a type.
   *
   * @param referencingType  the type of the elements
   * @return the collection of elements of the type
   */
  Collection<ModelElementInstance> getModelElementsByType(ModelElementType referencingType);

}
