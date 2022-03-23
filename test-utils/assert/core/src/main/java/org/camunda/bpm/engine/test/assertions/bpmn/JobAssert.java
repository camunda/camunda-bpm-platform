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
package org.camunda.bpm.engine.test.assertions.bpmn;

import java.util.Date;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;

/**
 * Assertions for a {@link Job}.
 */
public class JobAssert extends AbstractProcessAssert<JobAssert, Job> {

  protected JobAssert(final ProcessEngine engine, final Job actual) {
    super(engine, actual, JobAssert.class);
  }

  protected static JobAssert assertThat(final ProcessEngine engine, final Job actual) {
    return new JobAssert(engine, actual);
  }

  @Override
  protected Job getCurrent() {
    return jobQuery().jobId(actual.getId()).singleResult();
  }

  /**
   * Verifies the expectation of a specific id for the {@link Job}.
   *
   * @param   expectedId the expected job id
   * @return  this {@link JobAssert}
   * @see     org.camunda.bpm.engine.runtime.Job#getId()
   */
  public JobAssert hasId(final String expectedId) {
    Job current = getExistingCurrent();
    Assertions.assertThat(expectedId).isNotEmpty();
    final String actualId = actual.getId();
    Assertions.assertThat(actualId)
      .overridingErrorMessage(
        "Expecting %s to have id '%s', but found it to be '%s'",
        toString(current), expectedId, actualId
      )
      .isEqualTo(expectedId);
    return this;
  }

  /**
   * Verifies the expectation of a specific due date for the {@link Job}.
   *
   * @param   expectedDueDate the expected due date
   * @return  this {@link JobAssert}
   * @see     org.camunda.bpm.engine.runtime.Job#getDuedate()
   */
  public JobAssert hasDueDate(final Date expectedDueDate) {
    Job current = getExistingCurrent();
    Assertions.assertThat(expectedDueDate).isNotNull();
    final Date actualDuedate = current.getDuedate();
    Assertions.assertThat(actualDuedate)
      .overridingErrorMessage(
        "Expecting %s to be due at '%s', but found it to be due at '%s'",
        toString(current), expectedDueDate, actualDuedate
      )
      .isEqualTo(expectedDueDate);
    return this;
  }

  /**
   * Verifies the expectation of a specific process instance id for the {@link Job}.
   *
   * @param   expectedProcessInstanceId the expected process instance id
   * @return  this {@link JobAssert}
   * @see     org.camunda.bpm.engine.runtime.Job#getProcessInstanceId()
   */
  public JobAssert hasProcessInstanceId(final String expectedProcessInstanceId) {
    Job current = getExistingCurrent();
    Assertions.assertThat(expectedProcessInstanceId).isNotNull();
    final String actualProcessInstanceId = current.getProcessInstanceId();
    Assertions.assertThat(actualProcessInstanceId)
      .overridingErrorMessage(
        "Expecting %s to have process instance id '%s', but found it to be '%s'",
        toString(current), expectedProcessInstanceId, actualProcessInstanceId
      )
      .isEqualTo(expectedProcessInstanceId);
    return this;
  }

  /**
   * Verifies the expectation of a specific execution id for the {@link Job}.
   *
   * @param   expectedExecutionId the expected execution id
   * @return  this {@link JobAssert}
   * @see     org.camunda.bpm.engine.runtime.Job#getExecutionId()
   */
  public JobAssert hasExecutionId(final String expectedExecutionId) {
    Job current = getExistingCurrent();
    Assertions.assertThat(expectedExecutionId).isNotNull();
    final String actualExecutionId = current.getExecutionId();
    Assertions.assertThat(actualExecutionId)
      .overridingErrorMessage(
        "Expecting %s to have execution id '%s', but found it to be '%s'",
        toString(current), expectedExecutionId, actualExecutionId
      )
      .isEqualTo(expectedExecutionId);
    return this;
  }

  /**
   * Verifies the expectation of a specific number of retries left for the {@link Job}.
   * @param   expectedRetries the expected number of retries
   * @return  this {@link JobAssert}
   * @see     org.camunda.bpm.engine.runtime.Job#getExecutionId()
   */
  public JobAssert hasRetries(final int expectedRetries) {
    Job current = getExistingCurrent();
    final int actualRetries = current.getRetries();
    Assertions.assertThat(actualRetries)
      .overridingErrorMessage(
        "Expecting %s to have %s retries left, but found %s retries",
        toString(current), expectedRetries, actualRetries
      )
      .isEqualTo(expectedRetries);
    return this;
  }

  /**
   * Verifies the expectation of the existence of an exception message
   * for the {@link Job}.
   *
   * @return  this {@link JobAssert}
   * @see     org.camunda.bpm.engine.runtime.Job#getExceptionMessage()
   */
  public JobAssert hasExceptionMessage() {
    Job current = getExistingCurrent();
    final String actualExceptionMessage = current.getExceptionMessage();
    Assertions.assertThat(actualExceptionMessage)
      .overridingErrorMessage(
        "Expecting %s to have an exception message, but found it to be null or empty: '%s'",
        toString(current), actualExceptionMessage
      )
      .isNotEmpty();
    return this;
  }

  /**
   * Verifies the expectation of a specific deployment id for the {@link Job}.
   *
   * @param   expectedDeploymentId the expected deployment id
   * @return  this {@link JobAssert}
   * @see     org.camunda.bpm.engine.runtime.Job#getDeploymentId()
   */
  public JobAssert hasDeploymentId(final String expectedDeploymentId) {
    Job current = getExistingCurrent();
    Assertions.assertThat(expectedDeploymentId).isNotNull();
    final String actualDeploymentId = current.getDeploymentId();
    Assertions.assertThat(actualDeploymentId)
      .overridingErrorMessage(
        "Expecting %s to have deployment id '%s', but found it to be '%s'",
        toString(current), expectedDeploymentId, actualDeploymentId
      )
      .isEqualTo(expectedDeploymentId);
    return this;
  }

  public JobAssert hasActivityId(final String activityId) {
    Execution execution = executionQuery().activityId(activityId).active().singleResult();
    Assertions.assertThat(activityId).isNotNull();
    String failureMessage = "Expecting %s to correspond to activity with id '%s', " +
      "but did not find that to be true";
    Assertions.assertThat(execution)
      .overridingErrorMessage(failureMessage, toString(getExistingCurrent()), activityId)
      .isNotNull();
    Assertions.assertThat(execution.getId())
      .overridingErrorMessage(failureMessage, toString(getExistingCurrent()), activityId)
      .isEqualTo(actual.getExecutionId());
    return this;
  }

  @Override
  protected String toString(Job job) {
     return job != null ?
       String.format("%s {" +
         "id='%s', " +
         "processInstanceId='%s', " +
         "executionId='%s'}",
         Job.class.getSimpleName(),
         job.getId(),
         job.getProcessInstanceId(),
         job.getExecutionId())
       : null;
  }

  /*
   * JobQuery, automatically narrowed to {@link ProcessInstance} of actual {@link job}
   */
  @Override
  protected JobQuery jobQuery() {
    return super.jobQuery().processInstanceId(actual.getProcessInstanceId());
  }

  /* ExecutionQuery, automatically narrowed to {@link ProcessInstance} of actual
   * {@link job}
   */
  @Override
  protected ExecutionQuery executionQuery() {
    return super.executionQuery().processInstanceId(actual.getProcessInstanceId());
  }

}
