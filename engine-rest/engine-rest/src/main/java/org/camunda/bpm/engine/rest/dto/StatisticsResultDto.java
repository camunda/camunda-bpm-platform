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

import org.camunda.bpm.engine.rest.dto.repository.ActivityStatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.IncidentStatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionStatisticsResultDto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ActivityStatisticsResultDto.class),
    @JsonSubTypes.Type(value = ProcessDefinitionStatisticsResultDto.class)
})
public abstract class StatisticsResultDto {

  protected String id;
  protected Integer instances;
  protected Integer failedJobs;
  protected List<IncidentStatisticsResultDto> incidents;

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Integer getInstances() {
    return instances;
  }
  public void setInstances(Integer instances) {
    this.instances = instances;
  }
  public Integer getFailedJobs() {
    return failedJobs;
  }
  public void setFailedJobs(Integer failedJobs) {
    this.failedJobs = failedJobs;
  }
  public List<IncidentStatisticsResultDto> getIncidents() {
    return incidents;
  }
  public void setIncidents(List<IncidentStatisticsResultDto> incidents) {
    this.incidents = incidents;
  }

}
