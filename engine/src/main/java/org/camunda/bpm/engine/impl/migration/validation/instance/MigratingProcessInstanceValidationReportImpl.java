/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationReport;

public class MigratingProcessInstanceValidationReportImpl implements MigratingProcessInstanceValidationReport {

  protected MigratingProcessInstance migratingProcessInstance;
  protected List<MigratingActivityInstanceValidationReport> reports = new ArrayList<MigratingActivityInstanceValidationReport>();
  protected List<String> failures = new ArrayList<String>();

  public MigratingProcessInstance getMigratingProcessInstance() {
    return migratingProcessInstance;
  }

  public void setMigratingProcessInstance(MigratingProcessInstance migratingProcessInstance) {
    this.migratingProcessInstance = migratingProcessInstance;
  }

  public void addInstanceReport(MigratingActivityInstanceValidationReport instanceReport) {
    reports.add(instanceReport);
  }

  public List<MigratingActivityInstanceValidationReport> getReports() {
    return reports;
  }

  public void addFailure(String failure) {
    failures.add(failure);
  }

  public List<String> getFailures() {
    return failures;
  }

  public boolean hasFailures() {
    return !failures.isEmpty() || !reports.isEmpty();
  }

  public void writeTo(StringBuilder sb) {
    sb.append("Cannot migrate process instance '")
      .append(migratingProcessInstance.getProcessInstanceId())
      .append("':\n");

    for (String failure : failures) {
      sb.append("\t").append(failure).append("\n");
    }

    for (MigratingActivityInstanceValidationReport report : reports) {
      sb.append("\tCannot migrate activity instance '")
        .append(report.getMigratingActivityInstanceId())
        .append("':\n");

      for (String failure : report.getFailures()) {
        sb.append("\t\t").append(failure).append("\n");
      }
    }
  }

}
