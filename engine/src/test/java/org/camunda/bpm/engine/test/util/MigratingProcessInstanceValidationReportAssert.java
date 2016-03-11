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

package org.camunda.bpm.engine.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.impl.migration.validation.instance.MigratingActivityInstanceValidationReport;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationReport;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;

public class MigratingProcessInstanceValidationReportAssert {

  protected MigratingProcessInstanceValidationReport actual;

  public MigratingProcessInstanceValidationReportAssert(MigratingProcessInstanceValidationReport report) {
    this.actual = report;
  }

  public MigratingProcessInstanceValidationReportAssert isNotNull() {
    assertNotNull("Expected report to be not null", actual);

    return this;
  }

  public MigratingProcessInstanceValidationReportAssert hasProcessInstance(ProcessInstance processInstance) {
    return hasProcessInstanceId(processInstance.getId());
  }

  public MigratingProcessInstanceValidationReportAssert hasProcessInstanceId(String processInstanceId) {
    isNotNull();

    assertEquals("Expected report to be for process instance", processInstanceId, actual.getProcessInstanceId());

    return this;
  }

  public MigratingProcessInstanceValidationReportAssert hasFailures(String... expectedFailures) {
    isNotNull();

    List<String> actualFailures = actual.getFailures();

    Collection<Matcher<? super String>> matchers = new ArrayList<Matcher<? super String>>();
    for (String expectedFailure : expectedFailures) {
      matchers.add(Matchers.containsString(expectedFailure));
    }

    Assert.assertThat("Expected failures:\n" + joinFailures(Arrays.asList(expectedFailures)) +
        "But found failures:\n" + joinFailures(actualFailures),
      actualFailures, Matchers.containsInAnyOrder(matchers));

    return this;
  }

  public MigratingProcessInstanceValidationReportAssert hasActivityInstanceFailures(String sourceScopeId, String... expectedFailures) {
    isNotNull();

    MigratingActivityInstanceValidationReport actualReport = null;
    for (MigratingActivityInstanceValidationReport instanceReport : actual.getReports()) {
      if (sourceScopeId.equals(instanceReport.getSourceScopeId())) {
        actualReport = instanceReport;
        break;
      }
    }

    assertNotNull("No validation report found for source scope: " + sourceScopeId, actualReport);

    List<String> actualFailures = actualReport.getFailures();

    Collection<Matcher<? super String>> matchers = new ArrayList<Matcher<? super String>>();
    for (String expectedFailure : expectedFailures) {
      matchers.add(Matchers.containsString(expectedFailure));
    }

    Assert.assertThat("Expected failures for source scope: " + sourceScopeId + "\n" + joinFailures(Arrays.asList(expectedFailures)) +
        "But found failures:\n" + joinFailures(actualFailures),
      actualFailures, Matchers.containsInAnyOrder(matchers));

    return this;
  }

  public static MigratingProcessInstanceValidationReportAssert assertThat(MigratingProcessInstanceValidationReport report) {
    return new MigratingProcessInstanceValidationReportAssert(report);
  }

  public String joinFailures(List<String> failures) {
    StringBuilder builder = new StringBuilder();
    for (Object failure : failures) {
      builder.append("\t\t").append(failure).append("\n");
    }

    return builder.toString();
  }



}
