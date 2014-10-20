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
package org.camunda.spin.spi;

/**
 * Maps a java object to the data format's internal representation and vice versa.
 *
 * @author Thorben Lindhauer
 */
public interface DataFormatMapper {

  /**
   * Returns true if this mapper can map the provided Java Object.
   *
   * @param parameter the java object to check
   * @return true if this object can be mapped.
   */
  public boolean canMap(Object parameter);

  /**
   * Maps a java object to a data format's internal data representation.
   *
   * @param parameter object that is mapped
   * @return the data format's internal representation of that object
   */
  public Object mapJavaToInternal(Object parameter);

  /**
   * Maps the internal representation of a data format to a java object of the
   * desired class.
   *
   * @param parameter the object to be mapped
   * @param type the class to map the object to
   * @return a java object of the specified class that was populated with the input
   * parameter
   */
  public <T> T mapInternalToJava(Object parameter, Class<T> type);

  /**
   * Maps the internal representation of a data format to a java object of the
   * desired class. The type identifier is given in a data format specific format. Its
   * interpretation is data-format-specific. For example, it can be used to express generic
   * type information that cannot be expressed by a {@link Class} object.
   *
   * @param parameter the object to be mapped
   * @param typeIdentifier a data-format-specific type identifier that describes
   *   the class to map to
   * @return a java object of the specified type that was populated with the input
   *   parameter
   */
  public <T> T mapInternalToJava(Object parameter, String typeIdentifier);

  String getCanonicalTypeName(Object object);
}
