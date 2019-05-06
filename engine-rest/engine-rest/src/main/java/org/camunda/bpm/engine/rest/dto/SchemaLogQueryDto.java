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
package org.camunda.bpm.engine.rest.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.management.SchemaLogQuery;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Miklas Boskamp
 *
 */
public class SchemaLogQueryDto extends AbstractQueryDto<SchemaLogQuery>{

  private static final String SORT_BY_TIMESTAMP_VALUE = "timestamp";

  private static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_TIMESTAMP_VALUE);
  }

  String version;
  
  public SchemaLogQueryDto() {
  }

  public SchemaLogQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  public String getVersion() {
    return version;
  }

  @CamundaQueryParam("version")
  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected SchemaLogQuery createNewQuery(ProcessEngine engine) {
    return engine.getManagementService().createSchemaLogQuery();
  }

  @Override
  protected void applyFilters(SchemaLogQuery query) {
    if(this.version != null) {
      query.version(this.version);
    }
  }

  @Override
  protected void applySortBy(SchemaLogQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if(sortBy.equals(SORT_BY_TIMESTAMP_VALUE)) {
      query.orderByTimestamp();
    }
  }
}
