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
import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReport;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CleanableHistoricCaseInstanceReportDto extends AbstractQueryDto<CleanableHistoricCaseInstanceReport> {

  protected String[] caseDefinitionIdIn;
  protected String[] caseDefinitionKeyIn;
  protected String[] tenantIdIn;
  protected Boolean withoutTenantId;
  protected Boolean withoutFinishedZero;

  public CleanableHistoricCaseInstanceReportDto() {
  }

  public CleanableHistoricCaseInstanceReportDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam(value = "caseDefinitionIdIn", converter = StringArrayConverter.class)
  public void setCaseDefinitionIdIn(String[] caseDefinitionIdIn) {
    this.caseDefinitionIdIn = caseDefinitionIdIn;
  }

  @CamundaQueryParam(value = "caseDefinitionKeyIn", converter = StringArrayConverter.class)
  public void setCaseDefinitionKeyIn(String[] caseDefinitionKeyIn) {
    this.caseDefinitionKeyIn = caseDefinitionKeyIn;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringArrayConverter.class)
  public void setTenantIdIn(String[] tenantIdIn) {
    this.tenantIdIn = tenantIdIn;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @CamundaQueryParam(value = "withoutFinishedZero", converter = BooleanConverter.class)
  public void setWithoutFinishedZero(Boolean withoutFinishedZero) {
    this.withoutFinishedZero = withoutFinishedZero;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return false;
  }

  @Override
  protected CleanableHistoricCaseInstanceReport createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createCleanableHistoricCaseInstanceReport();
  }

  @Override
  protected void applyFilters(CleanableHistoricCaseInstanceReport query) {
    if (caseDefinitionIdIn != null && caseDefinitionIdIn.length > 0) {
      query.caseDefinitionIdIn(caseDefinitionIdIn);
    }
    if (caseDefinitionKeyIn != null && caseDefinitionKeyIn.length > 0) {
      query.caseDefinitionKeyIn(caseDefinitionKeyIn);
    }
    if (Boolean.TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (tenantIdIn != null && tenantIdIn.length > 0) {
      query.tenantIdIn(tenantIdIn);
    }
    if (Boolean.TRUE.equals(withoutFinishedZero)) {
      query.withoutFinishedZero();
    }

  }

  @Override
  protected void applySortBy(CleanableHistoricCaseInstanceReport query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
  }
}
