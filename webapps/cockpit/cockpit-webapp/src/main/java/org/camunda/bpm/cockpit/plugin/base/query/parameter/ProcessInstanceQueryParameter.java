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
package org.camunda.bpm.cockpit.plugin.base.query.parameter;

import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.plugin.base.persistence.entity.ProcessInstanceDto;

public class ProcessInstanceQueryParameter extends QueryParameters<ProcessInstanceDto> {

  protected String processDefinitionId;
  protected String orderBy = "HISTORY.START_TIME_ desc";
  
  public ProcessInstanceQueryParameter() {
  }
  
  public ProcessInstanceQueryParameter(int firstResult, int maxResults) {
    this.firstResult = firstResult;
    this.maxResults = maxResults;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  
  public String getOrderBy() {
    return orderBy;
  }
  
  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
  }

}
