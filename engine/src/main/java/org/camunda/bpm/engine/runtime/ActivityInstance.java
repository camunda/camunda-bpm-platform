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

import java.util.List;

import org.camunda.bpm.engine.RuntimeService;

/**
 * <p>An activity instance represents an instance of an activity.</p>
 * 
 * <p>For documentation, see {@link RuntimeService#getActivityInstance(String)}</p>
 * 
 * @author Daniel Meyer
 *
 */
public interface ActivityInstance {

  /** The id of the activity instance */
  String getId();

  /** The id of the parent activity instance. If the activity is the process definition, 
   {@link #getId()} and {@link #getParentActivityInstanceId()} return the same value */
  String getParentActivityInstanceId();

  /** the id of the activity */
  String getActivityId();
  
  /** The name of the activity */
  String getActivityName();
  
  /** the process instance id */
  String getProcessInstanceId();
  
  /** the process definition id */
  String getProcessDefinitionId();
  
  /** the business key */
  String getBusinessKey();

  /** Returns the child activity instances.
   * Returns an empty list if there are no child instances. */
  List<ActivityInstance> getChildInstances();
  
  /** the list of executions that are currently waiting in this activity instance */
  List<String> getExecutionIds();


}