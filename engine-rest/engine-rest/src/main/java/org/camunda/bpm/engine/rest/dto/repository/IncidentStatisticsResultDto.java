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
package org.camunda.bpm.engine.rest.dto.repository;

import org.camunda.bpm.engine.management.IncidentStatistics;

/**
 * @author roman.smirnov
 */
public class IncidentStatisticsResultDto {
  
  protected String incidentType;
  protected Integer incidentCount;
  
  public IncidentStatisticsResultDto() {}
  
  public String getIncidentType() {
    return incidentType;
  }

  public void setIncidentType(String incidentType) {
    this.incidentType = incidentType;
  }

  public Integer getIncidentCount() {
    return incidentCount;
  }

  public void setIncidentCount(Integer incidentCount) {
    this.incidentCount = incidentCount;
  }
  
  public static IncidentStatisticsResultDto fromIncidentStatistics(IncidentStatistics statistics) {
    IncidentStatisticsResultDto dto = new IncidentStatisticsResultDto();
    dto.setIncidentType(statistics.getIncidentType());
    dto.setIncidentCount(statistics.getIncidentCount());
    return dto;
  }

}
