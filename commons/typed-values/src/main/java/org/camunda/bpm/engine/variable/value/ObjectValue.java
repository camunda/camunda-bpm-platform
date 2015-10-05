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
package org.camunda.bpm.engine.variable.value;

/**
 * <p>A typed value representing a Java Object.</p>
 *
 * @author Daniel Meyer
 * @since 7.2
 */
public interface ObjectValue extends SerializableValue {

  /**
   * Returns true in case the object is deserialized. If this method returns true,
   * it is safe to call the methods
   * <ul>
   *   <li>{@link #getValue()} and {@link #getValue(Class)}</li>
   *   <li>{@link #getObjectType()}</li>
   * </ul>
   *
   * @return true if the object is deserialized.
   */
  boolean isDeserialized();

  /**
   * Returns the Object or null in case the value is null.
   *
   * @return the object represented by this TypedValue.
   * @throws IllegalStateException in case the object is not deserialized. See {@link #isDeserialized()}.
   */
  Object getValue();

  /**
   * Returns the object provided by this VariableValue. Allows type-safe access to objects
   * by passing in the class.
   *
   * @param type the java class the value should be cast to
   * @return the object represented by this TypedValue.
   * @throws IllegalStateException in case the object is not deserialized. See {@link #isDeserialized()}.
   */
  <T> T getValue(Class<T> type);

  /**
   * Returns the Class this object is an instance of.
   *
   * @return the Class this object is an instance of
   * @throws IllegalStateException in case the object is not deserialized. See {@link #isDeserialized()}.
   */
  Class<?> getObjectType();

  /**
   * A String representation of the Object's type name.
   * Usually the canonical class name of the Java Class this object
   * is an instance of.
   *
   * @return the Object's type name.
   */
  String getObjectTypeName();

}
