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

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.migration.MigrationInstructionValidationReport;
import org.camunda.bpm.engine.migration.MigrationPlanValidationReport;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;

public class MigrationPlanValidationReportAssert {

  protected MigrationPlanValidationReport actual;

  public MigrationPlanValidationReportAssert(MigrationPlanValidationReport report) {
    this.actual = report;
  }

  public MigrationPlanValidationReportAssert isNotNull() {
    assertNotNull("Expected report to be not null", actual);

    return this;
  }

  public MigrationPlanValidationReportAssert hasInstructionFailures(String activityId, String... expectedFailures) {
    isNotNull();

    List<String> failuresFound = new ArrayList<String>();

    for (MigrationInstructionValidationReport instructionReport : actual.getInstructionReports()) {
      String sourceActivityId = instructionReport.getMigrationInstruction().getSourceActivityId();
      if ((activityId == null && sourceActivityId == null) || (activityId != null && activityId.equals(sourceActivityId))) {
        failuresFound.addAll(instructionReport.getFailures());
      }
    }

    Collection<Matcher<? super String>> matchers = new ArrayList<Matcher<? super String>>();
    for (String expectedFailure : expectedFailures) {
      matchers.add(Matchers.containsString(expectedFailure));
    }

    Assert.assertThat("Expected failures for activity id '" + activityId + "':\n" + joinFailures(expectedFailures) +
      "But found failures:\n" + joinFailures(failuresFound.toArray()),
      failuresFound, Matchers.containsInAnyOrder(matchers));

    return this;
  }

  public static MigrationPlanValidationReportAssert assertThat(MigrationPlanValidationReport report) {
    return new MigrationPlanValidationReportAssert(report);
  }

  public String joinFailures(Object[] failures) {
    StringBuilder builder = new StringBuilder();
    for (Object failure : failures) {
      builder.append("\t\t").append(failure).append("\n");
    }

    return builder.toString();
  }

}
