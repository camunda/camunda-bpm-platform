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
package org.camunda.bpm.engine.delegate;


public interface PersistentVariableInstance extends CoreVariableInstance {


  /**
   * If the variable value could not be loaded, this returns the error message.
   * @return an error message indicating why the variable value could not be loaded.
   */
  String getErrorMessage();

  /**
   * Returns the value of this variable in its serialized form, represented by a
   * {@link SerializedVariableValue}.
   */
  SerializedVariableValue getSerializedValue();

  /**
   * Returns the name of the type of <code>this</code> variable instance;
   * corresponds to the types defined in {@link ProcessEngineVariableType}.
   */
  String getTypeName();

  /**
   * Returns the simple name of the class of the persisted variable
   */
  String getValueTypeName();

  /**
   * Returns whether this variable potentially stores a custom java object (i.e. an instance
   * of a non-JDK class). Return value is determined based on the underlying variable type
   * (cf. {@link ProcessEngineVariableType}).
   */
  boolean storesCustomObjects();
}
