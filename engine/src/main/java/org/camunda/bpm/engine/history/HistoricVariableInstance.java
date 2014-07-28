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
package org.camunda.bpm.engine.history;

/**
 * A single process variable containing the last value when its process instance has finished.
 * It is only available when HISTORY_LEVEL is set >= VARIABLE
 *
 * @author Christian Lipphardt (camunda)
 * @author ruecker
 */
public interface HistoricVariableInstance {

  /**
   * @return the Id of this variable instance
   */
  String getId();

  /**
   * Returns the name of <code>this</code> variable instance.
   */
  String getVariableName();

  /**
   * Returns the name of the type of <code>this</code> variable instance
   *
   */
  String getVariableTypeName();

  /**
   * Returns the value of <code>this</code> variable instance.
   */
  Object getValue();

  /**
   * The process instance reference.
   */
  String getProcessInstanceId();

  /**
   * Returns the corresponding activity instance id.
   */
  String getActivtyInstanceId();

  /**
   * If the variable value could not be loaded, this returns the error message.
   * @return an error message indicating why the variable value could not be loaded.
   */
  String getErrorMessage();

  /**
   * Returns the variable's raw value as it is stored in the database. This is the same
   * as {@link #getValue()} but no conversion to the stored type is applied.
   */
  Object getRawValue();

  /**
   * Returns the identifier of the data format that is used to serialize the variable.
   */
  String getDataFormatId();

}
