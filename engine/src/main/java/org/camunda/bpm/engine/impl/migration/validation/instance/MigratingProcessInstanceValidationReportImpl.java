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
package org.camunda.bpm.engine.impl.migration.validation.instance;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.migration.MigratingActivityInstanceValidationReport;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationReport;
import org.camunda.bpm.engine.migration.MigratingTransitionInstanceValidationReport;

public class MigratingProcessInstanceValidationReportImpl implements MigratingProcessInstanceValidationReport {

  protected String processInstanceId;
  protected List<MigratingActivityInstanceValidationReport> activityInstanceReports =
      new ArrayList<MigratingActivityInstanceValidationReport>();
  protected List<MigratingTransitionInstanceValidationReport> transitionInstanceReports =
      new ArrayList<MigratingTransitionInstanceValidationReport>();
  protected List<String> failures = new ArrayList<String>();

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public void addActivityInstanceReport(MigratingActivityInstanceValidationReport instanceReport) {
    activityInstanceReports.add(instanceReport);
  }

  public void addTransitionInstanceReport(MigratingTransitionInstanceValidationReport instanceReport) {
    transitionInstanceReports.add(instanceReport);
  }

  public List<MigratingActivityInstanceValidationReport> getActivityInstanceReports() {
    return activityInstanceReports;
  }

  @Override
  public List<MigratingTransitionInstanceValidationReport> getTransitionInstanceReports() {
    return transitionInstanceReports;
  }

  public void addFailure(String failure) {
    failures.add(failure);
  }

  public List<String> getFailures() {
    return failures;
  }

  public boolean hasFailures() {
    return !failures.isEmpty() || !activityInstanceReports.isEmpty() || !transitionInstanceReports.isEmpty();
  }

  public void writeTo(StringBuilder sb) {
    sb.append("Cannot migrate process instance '")
      .append(processInstanceId)
      .append("':\n");

    for (String failure : failures) {
      sb.append("\t").append(failure).append("\n");
    }

    for (MigratingActivityInstanceValidationReport report : activityInstanceReports) {
      sb.append("\tCannot migrate activity instance '")
        .append(report.getActivityInstanceId())
        .append("':\n");

      for (String failure : report.getFailures()) {
        sb.append("\t\t").append(failure).append("\n");
      }
    }

    for (MigratingTransitionInstanceValidationReport report : transitionInstanceReports) {
      sb.append("\tCannot migrate transition instance '")
        .append(report.getTransitionInstanceId())
        .append("':\n");

      for (String failure : report.getFailures()) {
        sb.append("\t\t").append(failure).append("\n");
      }
    }
  }

}
