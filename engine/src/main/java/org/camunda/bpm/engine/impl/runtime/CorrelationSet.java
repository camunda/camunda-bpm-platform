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

package org.camunda.bpm.engine.impl.runtime;

import java.util.Map;

import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;

public class CorrelationSet {

  protected final String businessKey;
  protected final Map<String, Object> correlationKeys;
  protected final String processInstanceId;
  protected final String processDefinitionId;
  protected final String tenantId;
  protected final boolean isTenantIdSet;

  public CorrelationSet(MessageCorrelationBuilderImpl builder) {
    this.businessKey = builder.getBusinessKey();
    this.processInstanceId = builder.getProcessInstanceId();
    this.correlationKeys = builder.getCorrelationProcessInstanceVariables();
    this.processDefinitionId = builder.getProcessDefinitionId();
    this.tenantId = builder.getTenantId();
    this.isTenantIdSet = builder.isTenantIdSet();
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public Map<String, Object> getCorrelationKeys() {
    return correlationKeys;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

  @Override
  public String toString() {
    return "CorrelationSet [businessKey=" + businessKey + ", processInstanceId=" + processInstanceId + ", processDefinitionId=" + processDefinitionId
        + ", correlationKeys=" + correlationKeys + ", tenantId=" + tenantId + ", isTenantIdSet=" + isTenantIdSet + "]";
  }

}
