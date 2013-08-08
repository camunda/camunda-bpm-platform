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

import org.camunda.bpm.engine.runtime.ProcessElementInstance;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessElementInstanceImpl implements ProcessElementInstance {
  
  protected String id;
  protected String parentActivityInstanceId;
  protected String processInstanceId;
  protected String processDefinitionId;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getParentActivityInstanceId() {
    return parentActivityInstanceId;
  }
  public void setParentActivityInstanceId(String parentActivityInstanceId) {
    this.parentActivityInstanceId = parentActivityInstanceId;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", parentActivityInstanceId=" + parentActivityInstanceId
           + ", processInstanceId=" + processInstanceId
           + ", processDefinitionId=" + processDefinitionId
           + "]";
  }

}
