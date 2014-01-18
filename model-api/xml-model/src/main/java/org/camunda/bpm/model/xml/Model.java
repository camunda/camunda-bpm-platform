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

import java.util.Collection;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

/**
 * Use {@link ModelBuilder} to create an instance of a {@link Model}.
 *
 * @author Daniel Meyer
 *
 */
public interface Model {

  /**
   * Gets the collection of all {@link ModelElementType} defined in the model.
   *
   * @return the list of all defined element types of this model
   */
  public Collection<ModelElementType> getTypes();

  /**
   * Gets the defined {@link ModelElementType} of a {@link ModelElementInstance}.
   *
   * @param instanceClass  the instance class to find the type for
   * @return the corresponding element type or null if no type is defined for the instance
   */
  public ModelElementType getType(Class<? extends ModelElementInstance> instanceClass);

  /**
   * Gets the defined {@link ModelElementType} for a type by its name.
   *
   * @param typeName  the name of the type
   * @return the element type or null if no type is defined for the name
   */
  public ModelElementType getTypeForName(String typeName);

  /**
   * Gets the defined {@link ModelElementType} for a type by its name and namespace URI.
   *
   * @param typeName  the name of the type
   * @param namespaceUri  the namespace URI for the type
   * @return the element type or null if no type is defined for the name and namespace URI
   */
  public ModelElementType getTypeForName(String typeName, String namespaceUri);

  /**
   * Returns the model name, which is the identifier of this model.
   *
   * @return the model name
   */
  String getModelName();

}
