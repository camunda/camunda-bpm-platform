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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.impl.migration.validation.MigrationPlanValidationReportImpl;
import org.camunda.bpm.engine.migration.MigrationPlanValidationFailure;
import org.camunda.bpm.engine.migration.MigrationPlanValidationReport;
import org.hamcrest.CoreMatchers;
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

  public MigrationPlanValidationReportAssert hasFailures() {
    isNotNull();
    assertTrue("Expected report to contain failures", ((MigrationPlanValidationReportImpl) actual).hasFailures());

    return this;
  }

  public MigrationPlanValidationReportAssert hasFailures(int numberOfFailures) {
    isNotNull();
    assertEquals("Expected report to contain failures", numberOfFailures, actual.getValidationFailures().size());

    return this;
  }

  public MigrationPlanValidationReportAssert hasFailure(String activityId, String errorMessage) {
    isNotNull();

    boolean failureFound = false;

    for (MigrationPlanValidationFailure failure : actual.getValidationFailures()) {
      if (failure.getMigrationInstruction().getSourceActivityIds().contains(activityId)) {
        Assert.assertThat(failure.getErrorMessage(), CoreMatchers.containsString(errorMessage));
        failureFound = true;
        break;
      }
    }

    if (!failureFound) {
      fail("Unable to find failure for activity id '" + activityId + "'");
    }

    return this;
  }

  public static MigrationPlanValidationReportAssert assertThat(MigrationPlanValidationReport report) {
    return new MigrationPlanValidationReportAssert(report);
  }

}
