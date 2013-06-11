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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * 
 * @author Daniel Meyer
 * 
 */
public class ActivityInstanceImpl implements ActivityInstance {

  protected String id;
  protected String activityName;
  protected String parentActivityInstanceId;
  protected String processInstanceId;
  protected String businessKey;
  protected String processDefinitionId;
  protected String activityId;
  
  protected List<ActivityInstance> childInstances = new ArrayList<ActivityInstance>();
  protected List<String> executionIds = new ArrayList<String>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getActivityName() {
    return activityName;
  }

  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }

  public List<ActivityInstance> getChildInstances() {
    return childInstances;
  }

  public void setChildInstances(List<ActivityInstance> childInstances) {
    this.childInstances = childInstances;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }
    
  public String getParentActivityInstanceId() {
    return parentActivityInstanceId;
  }
  
  public void setParentActivityInstanceId(String parentActivityInstanceId) {
    this.parentActivityInstanceId = parentActivityInstanceId;
  }

  public List<String> getExecutionIds() {
    return executionIds;
  }

  public void setExecutionIds(List<String> executionIds) {
    this.executionIds = executionIds;
  }

}
