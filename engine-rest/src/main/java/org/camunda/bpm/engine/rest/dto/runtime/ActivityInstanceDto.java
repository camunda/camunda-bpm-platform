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
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * <p>An activity instance is the instance of an activity</p>
 * 
 * @author roman.smirnov
 *
 */
public class ActivityInstanceDto {
  
  protected String id;
  protected String parentActivityInstanceId;
  protected String activityId;
  protected String activtyName;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String businessKey;
  protected List<ActivityInstanceDto> childInstances;
  protected List<String> executionIds;

  /** The id of the activity instance */
  public String getId() {
    return id;
  }

  /** The id of the parent activity instance. If the activity is the process definition, 
  {@link #getId()} and {@link #getParentActivityInstanceId()} return the same value */
  public String getParentActivityInstanceId() {
    return parentActivityInstanceId;
  }

  /** the id of the activity */
  public String getActivityId() {
    return activityId;
  }
  
  /** The name of the activity */
  public String getActivityName() {
    return activtyName;
  }
  
  /** the process instance id */
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  /** the process definition id */
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  /** the business key */
  public String getBusinessKey() {
    return businessKey;
  }

  /** Returns the child activity instances.
   * Returns an empty list if there are no child instances. */
  public List<ActivityInstanceDto> getChildInstances() {
    return childInstances;
  }
  
  /** the list of executions that are currently waiting in this activity instance */
  public List<String> getExecutionIds() {
    return executionIds; 
  }
  
  public static ActivityInstanceDto fromActivityInstance(ActivityInstance instance) {
    ActivityInstanceDto result = new ActivityInstanceDto();
    result.id = instance.getId();
    result.parentActivityInstanceId = instance.getParentActivityInstanceId();
    result.activityId = instance.getActivityId();
    result.activtyName = instance.getActivityName();
    result.processInstanceId = instance.getProcessInstanceId();
    result.processDefinitionId = instance.getProcessDefinitionId();
    result.businessKey = instance.getBusinessKey();
    result.childInstances = fromListOfActivityInstance(instance.getChildInstances());
    result.executionIds = new ArrayList<String>(instance.getExecutionIds());
    return result;
  }
  
  public static List<ActivityInstanceDto> fromListOfActivityInstance(List<ActivityInstance> instances) {
    List<ActivityInstanceDto> result = new ArrayList<ActivityInstanceDto>();
    
    for (ActivityInstance instance : instances) {
      ActivityInstanceDto instanceDto = fromActivityInstance(instance);
      result.add(instanceDto);
    }
    
    return result;
  }

}
