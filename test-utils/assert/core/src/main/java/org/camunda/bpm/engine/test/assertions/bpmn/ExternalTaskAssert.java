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

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTask;

/**
 * Assertions for an {@link ExternalTask}.
 */
public class ExternalTaskAssert extends AbstractProcessAssert<ExternalTaskAssert, ExternalTask> {

  protected ExternalTaskAssert(final ProcessEngine engine, final ExternalTask actual) {
    super(engine, actual, ExternalTaskAssert.class);
  }

  protected static ExternalTaskAssert assertThat(ProcessEngine engine, ExternalTask actual) {
    return new ExternalTaskAssert(engine, actual);
  }

  @Override
  protected ExternalTask getCurrent() {
    return externalTaskQuery().externalTaskId(actual.getId()).singleResult();
  }

  /**
   * Verifies the topic name of an {@link ExternalTask}.
   *
   * @param topicName
   *          the expected value of the topic
   * @return this {@link ExternalTaskAssert}
   */
  public ExternalTaskAssert hasTopicName(final String topicName) {
    final ExternalTask current = getExistingCurrent();
    Assertions.assertThat(topicName).isNotNull();
    Assertions.assertThat(current.getTopicName())
      .overridingErrorMessage("Expecting %s to have topic name '%s', but found it to be '%s'!",
        toString(current),
        topicName,
        current.getTopicName())
      .isEqualTo(topicName);
    return this;
  }

  /**
   * Verifies the activity id of an {@link ExternalTask}.
   *
   * @param activityId
   *          the expected value of the external task activityId attribute
   * @return this {@link ExternalTaskAssert}
   */
  public ExternalTaskAssert hasActivityId(final String activityId) {
    ExternalTask current = getExistingCurrent();
    Assertions.assertThat(activityId).isNotNull();
    Assertions.assertThat(current.getActivityId())
      .overridingErrorMessage("Expecting %s to have activity id '%s', but found it to have '%s'!",
        toString(current),
        activityId,
        current.getActivityId()
      ).isEqualTo(activityId);
    return this;
  }


  @Override
  protected String toString(ExternalTask task) {
    return task != null ?
      String.format("%s {" +
          "id='%s', " +
          "processInstanceId='%s', " +
          "topicName='%s'}",
        ExternalTask.class.getSimpleName(),
        task.getId(),
        task.getProcessInstanceId(),
        task.getTopicName()
      ) : null;
  }

}