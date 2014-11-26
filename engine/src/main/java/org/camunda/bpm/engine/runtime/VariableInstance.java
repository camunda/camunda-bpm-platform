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
package org.camunda.bpm.engine.runtime;

import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * A {@link VariableInstance} represents a variable in the execution of
 * a process instance.
 *
 * @author roman.smirnov
 *
 */
public interface VariableInstance {

  /**
   * @return the Id of this variable instance
   */
  String getId();

  /**
   * Returns the name of this variable instance.
   */
  String getName();

  /**
   * Returns the name of the type of this variable instance
   *
   * @return the type name of the variable
   */
  String getTypeName();

  /**
   * Returns the value of this variable instance.
   */
  Object getValue();

  /**
   * Returns the TypedValue of this variable instance.
   */
  TypedValue getTypedValue();

  /**
   * Returns the corresponding process instance id.
   */
  String getProcessInstanceId();

  /**
   * Returns the corresponding execution id.
   */
  String getExecutionId();

  /**
   * Returns the corresponding case instance id.
   */
  String getCaseInstanceId();

  /**
   * Returns the corresponding case execution id.
   */
  String getCaseExecutionId();

  /**
   * Returns the corresponding task id.
   */
  String getTaskId();

  /**
   * Returns the corresponding activity instance id.
   */
  String getActivityInstanceId();

  /**
   * If the variable value could not be loaded, this returns the error message.
   * @return an error message indicating why the variable value could not be loaded.
   */
  String getErrorMessage();

}
