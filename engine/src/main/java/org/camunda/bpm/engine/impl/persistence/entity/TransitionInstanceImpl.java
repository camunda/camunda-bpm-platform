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

import org.camunda.bpm.engine.runtime.TransitionInstance;

/**
 * @author Daniel Meyer
 *
 */
public class TransitionInstanceImpl extends ProcessElementInstanceImpl implements TransitionInstance {
  
  protected String targetActivityId;
  protected String executionId;

  public String getTargetActivityId() {
    return targetActivityId;
  }
  public void setTargetActivityId(String targetActivityId) {
    this.targetActivityId = targetActivityId;
  }
  public String getExecutionId() {
    return executionId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[executionId=" + executionId
           + ", targetActivityId=" + targetActivityId
           + ", id=" + id
           + ", parentActivityInstanceId=" + parentActivityInstanceId
           + ", processInstanceId=" + processInstanceId
           + ", processDefinitionId=" + processDefinitionId
           + "]";
  }

}
