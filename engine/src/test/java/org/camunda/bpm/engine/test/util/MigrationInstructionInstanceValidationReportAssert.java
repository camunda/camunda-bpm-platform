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

import org.camunda.bpm.engine.migration.MigrationInstructionInstanceValidationFailure;
import org.camunda.bpm.engine.migration.MigrationInstructionInstanceValidationReport;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;

public class MigrationInstructionInstanceValidationReportAssert {

  protected MigrationInstructionInstanceValidationReport actual;

  public MigrationInstructionInstanceValidationReportAssert(MigrationInstructionInstanceValidationReport report) {
    this.actual = report;
  }

  public MigrationInstructionInstanceValidationReportAssert isNotNull() {
    assertNotNull("Expected report to be not null", actual);

    return this;
  }

  public MigrationInstructionInstanceValidationReportAssert hasProcessInstance(ProcessInstance processInstance) {
    return hasProcessInstanceId(processInstance.getId());
  }

  public MigrationInstructionInstanceValidationReportAssert hasProcessInstanceId(String processInstanceId) {
    isNotNull();

    assertEquals("Expected report to be for process instance", processInstanceId, actual.getProcessInstanceId());

    return this;
  }

  public MigrationInstructionInstanceValidationReportAssert hasFailures() {
    isNotNull();
    assertTrue("Expected report to contain failures", actual.getValidationFailures().isEmpty());

    return this;
  }

  public MigrationInstructionInstanceValidationReportAssert hasFailures(int numberOfFailures) {
    isNotNull();
    assertEquals("Expected report to contain failures", numberOfFailures, actual.getValidationFailures().size());

    return this;
  }

  public MigrationInstructionInstanceValidationReportAssert hasFailure(String activityId, String errorMessage) {
    isNotNull();

    boolean failureFound = false;

    for (MigrationInstructionInstanceValidationFailure failure : actual.getValidationFailures()) {
      if (failure.getMigrationInstruction().getSourceActivityIds().get(0).equals(activityId)) {
        Assert.assertThat(failure.getErrorMessage(), CoreMatchers.containsString(errorMessage));
        Assert.assertNotNull(failure.getActivityInstanceIds());
        Assert.assertEquals(1, failure.getActivityInstanceIds().size());
        failureFound = true;
        break;
      }
    }

    if (!failureFound) {
      fail("Unable to find failure for activity id '" + activityId + "'");
    }

    return this;
  }

  public static MigrationInstructionInstanceValidationReportAssert assertThat(MigrationInstructionInstanceValidationReport report) {
    return new MigrationInstructionInstanceValidationReportAssert(report);
  }
}
