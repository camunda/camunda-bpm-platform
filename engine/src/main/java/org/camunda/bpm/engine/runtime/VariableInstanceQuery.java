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

import org.camunda.bpm.engine.query.Query;

/**
 * @author roman.smirnov
 */
public interface VariableInstanceQuery extends Query<VariableInstanceQuery, VariableInstance> {
  
  /** Only select variable instances which have the variable name. **/
  VariableInstanceQuery variableName(String variableName);
  
  /** Only select variable instances which have the name like the assigned variable name. **/
  VariableInstanceQuery variableNameLike(String variableNameLike);

  /** Only select variable instances which have the the assigned variable value. **/
  VariableInstanceQuery variableValueEquals(Object variableValue);
  
  /** Only select variable instances which have the variable name and their value is equal the assigned variable value. **/
  VariableInstanceQuery variableValueEquals(String variableName, Object variableValue);
  
  /** Only select variable instances which have one of the executions ids. **/
  VariableInstanceQuery executionIdIn(String... executionIds);
  
  /** Only select variable instances which have one of the process instance ids. **/
  VariableInstanceQuery processInstanceIdIn(String... processInstanceIds);
  
  /** Only select variable instances which have one of the task ids. **/
  VariableInstanceQuery taskIdIn(String... taskIds);
  
  /** Only select variable instances which have one of the activity instance ids. **/
  VariableInstanceQuery activityInstanceIdIn(String... activityInstanceIds);
  
  /** Order by variable name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  VariableInstanceQuery orderByVariableName();
  
  /** Order by variable type (needs to be followed by {@link #asc()} or {@link #desc()}). */
  VariableInstanceQuery orderByVariableType();
  
  /** Order by activity instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  VariableInstanceQuery orderByActivityInstanceId();

}
