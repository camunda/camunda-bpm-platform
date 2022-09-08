/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.dto.batch;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.batch.BatchStatisticsQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BatchStatisticsQueryDto extends AbstractQueryDto<BatchStatisticsQuery> {

  private static final String SORT_BY_BATCH_ID_VALUE = "batchId";
  private static final String SORT_BY_TENANT_ID_VALUE = "tenantId";
  private static final String SORT_BY_START_TIME_VALUE = "startTime";

  protected String batchId;
  protected String type;
  protected List<String> tenantIds;
  protected Boolean withoutTenantId;
  protected Boolean suspended;
  protected String userId;
  protected Date startedBefore;
  protected Date startedAfter;
  protected Boolean withFailures;
  protected Boolean withoutFailures;

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<>();
    VALID_SORT_BY_VALUES.add(SORT_BY_BATCH_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_START_TIME_VALUE);
  }

  public BatchStatisticsQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("batchId")
  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }

  @CamundaQueryParam("type")
  public void setType(String type) {
    this.type = type;
  }

  @CamundaQueryParam(value = "tenantIdIn", converter = StringListConverter.class)
  public void setTenantIdIn(List<String> tenantIds) {
    this.tenantIds = tenantIds;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @CamundaQueryParam(value="suspended", converter = BooleanConverter.class)
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @CamundaQueryParam(value="createdBy")
  public void setCreateUserId(String userId) {
    this.userId = userId;
  }

  @CamundaQueryParam(value = "startedBefore", converter = DateConverter.class)
  public void setStartedBefore(Date startedBefore) {
    this.startedBefore = startedBefore;
  }

  @CamundaQueryParam(value = "startedAfter", converter = DateConverter.class)
  public void setStartedAfter(Date startedAfter) {
    this.startedAfter = startedAfter;
  }

  @CamundaQueryParam(value = "withFailures", converter = BooleanConverter.class)
  public void setWithFailures(final Boolean withFailures) {
    this.withFailures = withFailures;
  }

  @CamundaQueryParam(value = "withoutFailures", converter = BooleanConverter.class)
  public void setWithoutFailures(final Boolean withoutFailures) {
    this.withoutFailures = withoutFailures;
  }

  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  protected BatchStatisticsQuery createNewQuery(ProcessEngine engine) {
    return engine.getManagementService().createBatchStatisticsQuery();
  }

  protected void applyFilters(BatchStatisticsQuery query) {
    if (batchId != null) {
      query.batchId(batchId);
    }
    if (type != null) {
      query.type(type);
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (tenantIds != null && !tenantIds.isEmpty()) {
      query.tenantIdIn(tenantIds.toArray(new String[tenantIds.size()]));
    }
    if (TRUE.equals(suspended)) {
      query.suspended();
    }
    if (FALSE.equals(suspended)) {
      query.active();
    }
    if (userId != null) {
      query.createdBy(userId);
    }
    if (startedBefore != null) {
      query.startedBefore(startedBefore);
    }
    if (startedAfter != null) {
      query.startedAfter(startedAfter);
    }
    if (TRUE.equals(withFailures)) {
      query.withFailures();
    }
    if (TRUE.equals(withoutFailures)) {
      query.withoutFailures();
    }
  }

  protected void applySortBy(BatchStatisticsQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_BATCH_ID_VALUE)) {
      query.orderById();
    }
    else if (sortBy.equals(SORT_BY_TENANT_ID_VALUE)) {
      query.orderByTenantId();
    }
    else if (sortBy.equals(SORT_BY_START_TIME_VALUE)) {
      query.orderByStartTime();
    }
  }

}
