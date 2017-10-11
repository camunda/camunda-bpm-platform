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

package org.camunda.bpm.engine.rest.dto.history;

import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReport;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CleanableHistoricProcessInstanceReportDto extends AbstractQueryDto<CleanableHistoricProcessInstanceReport> {

  protected String[] processDefinitionIdIn;
  protected String[] processDefinitionKeyIn;
  protected String[] tenantIdIn;
  protected Boolean withoutTenantId;

  public CleanableHistoricProcessInstanceReportDto() {
  }

  public CleanableHistoricProcessInstanceReportDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam(value = "processDefinitionIdIn", converter = StringArrayConverter.class)
  public void setProcessDefinitionIdIn(String[] processDefinitionIdIn) {
    this.processDefinitionIdIn = processDefinitionIdIn;
  }

  @CamundaQueryParam(value = "processDefinitionKeyIn", converter = StringArrayConverter.class)
  public void setProcessDefinitionKeyIn(String[] processDefinitionKeyIn) {
    this.processDefinitionKeyIn = processDefinitionKeyIn;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringArrayConverter.class)
  public void setTenantIdIn(String[] tenantIdIn) {
    this.tenantIdIn = tenantIdIn;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }
  @Override
  protected boolean isValidSortByValue(String value) {
    return false;
  }

  @Override
  protected CleanableHistoricProcessInstanceReport createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createCleanableHistoricProcessInstanceReport();
  }

  @Override
  protected void applyFilters(CleanableHistoricProcessInstanceReport query) {
    if (processDefinitionIdIn != null && processDefinitionIdIn.length > 0) {
      query.processDefinitionIdIn(processDefinitionIdIn);
    }
    if (processDefinitionKeyIn != null && processDefinitionKeyIn.length > 0) {
      query.processDefinitionKeyIn(processDefinitionKeyIn);
    }
    if (Boolean.TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (tenantIdIn != null && tenantIdIn.length > 0) {
      query.tenantIdIn(tenantIdIn);
    }

  }

  @Override
  protected void applySortBy(CleanableHistoricProcessInstanceReport query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
  }
}
