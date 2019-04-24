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
package org.camunda.bpm.engine.rest.dto.migration;

import java.util.List;

import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationReport;

public class MigratingProcessInstanceValidationReportDto {

  protected String processInstanceId;
  protected List<String> failures;
  protected List<MigratingActivityInstanceValidationReportDto> activityInstanceValidationReports;
  protected List<MigratingTransitionInstanceValidationReportDto> transitionInstanceValidationReports;

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public List<String> getFailures() {
    return failures;
  }

  public void setFailures(List<String> failures) {
    this.failures = failures;
  }

  public List<MigratingActivityInstanceValidationReportDto> getActivityInstanceValidationReports() {
    return activityInstanceValidationReports;
  }

  public void setActivityInstanceValidationReports(List<MigratingActivityInstanceValidationReportDto> activityInstanceValidationReports) {
    this.activityInstanceValidationReports = activityInstanceValidationReports;
  }

  public List<MigratingTransitionInstanceValidationReportDto> getTransitionInstanceValidationReports() {
    return transitionInstanceValidationReports;
  }

  public void setTransitionInstanceValidationReports(List<MigratingTransitionInstanceValidationReportDto> transitionInstanceValidationReports) {
    this.transitionInstanceValidationReports = transitionInstanceValidationReports;
  }

  public static MigratingProcessInstanceValidationReportDto from(MigratingProcessInstanceValidationReport validationReport) {
    MigratingProcessInstanceValidationReportDto dto = new MigratingProcessInstanceValidationReportDto();
    dto.setProcessInstanceId(validationReport.getProcessInstanceId());
    dto.setFailures(validationReport.getFailures());
    dto.setActivityInstanceValidationReports(MigratingActivityInstanceValidationReportDto.from(validationReport.getActivityInstanceReports()));
    dto.setTransitionInstanceValidationReports(MigratingTransitionInstanceValidationReportDto.from(validationReport.getTransitionInstanceReports()));
    return dto;
  }

}
