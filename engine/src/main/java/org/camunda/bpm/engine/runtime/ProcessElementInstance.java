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

/**
 * <p>A ProcessElementInstance is an instance of a process construct 
 * such as an Activity (see {@link ActivityInstance}) or a transition 
 * (see {@link TransitionInstance}).
 * 
 * @author Daniel Meyer
 *
 */
public interface ProcessElementInstance {

  /** The id of the process element instance */
  String getId();

  /** The id of the parent activity instance. */
  String getParentActivityInstanceId();
  
  /** the process definition id */
  String getProcessDefinitionId();
  
  /** the id of the process instance this process element is part of */
  String getProcessInstanceId();

}
