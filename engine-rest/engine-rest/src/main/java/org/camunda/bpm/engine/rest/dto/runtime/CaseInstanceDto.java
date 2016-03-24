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

import org.camunda.bpm.engine.rest.dto.LinkableDto;
import org.camunda.bpm.engine.runtime.CaseInstance;

/**
 * @author Roman Smirnov
 *
 */
public class CaseInstanceDto extends LinkableDto {

  protected String id;
  protected String caseDefinitionId;
  protected String businessKey;
  protected String tenantId;
  protected boolean active;
  protected boolean completed;
  protected boolean terminated;

  public String getId() {
    return id;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getTenantId() {
    return tenantId;
  }

  public boolean isActive() {
    return active;
  }

  public boolean isCompleted() {
    return completed;
  }

  public boolean isTerminated() {
    return terminated;
  }

  public static CaseInstanceDto fromCaseInstance(CaseInstance instance) {
    CaseInstanceDto result = new CaseInstanceDto();

    result.id = instance.getId();
    result.caseDefinitionId = instance.getCaseDefinitionId();
    result.businessKey = instance.getBusinessKey();
    result.tenantId = instance.getTenantId();
    result.active = instance.isActive();
    result.completed = instance.isCompleted();
    result.terminated = instance.isTerminated();

    return result;
  }

}
