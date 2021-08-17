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
package org.camunda.bpm.engine.test.jobexecutor;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

import org.camunda.bpm.engine.impl.persistence.entity.AcquirableJobEntity;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;

@Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/JobExecutorAcquireJobsForPriorityRangeTest.jobPrioProcess.bpmn20.xml")
public class JobExecutorAcquireJobsForPriorityRangeTest extends AbstractJobExecutorAcquireJobsTest {

  @Before
  public void setUp() {
    configuration.setJobExecutorAcquireByPriority(true);
    createJobs();
  }

  @Test
  public void shouldAcquireAllJobsNoBounds() {
    // given
    configuration.setJobExecutorPriorityRangeMin(null);
    configuration.setJobExecutorPriorityRangeMax(null);

    // when
    List<AcquirableJobEntity> acquirableJobs = findAcquirableJobs();

    // then
    assertThat(acquirableJobs).hasSize(10);
    for (int i = 0; i < 5; i++) {
      assertThat(findJobById(acquirableJobs.get(i).getId()).getPriority()).isEqualTo(10);
    }
    for (int i = 5; i < 10; i++) {
      assertThat(findJobById(acquirableJobs.get(i).getId()).getPriority()).isEqualTo(5);
    }
  }

  @Test
  public void shouldAcquireOnlyJobsInRangeWithUpperBound() {
    // given
    configuration.setJobExecutorPriorityRangeMin(null);
    configuration.setJobExecutorPriorityRangeMax(7L);

    // when
    List<AcquirableJobEntity> acquirableJobs = findAcquirableJobs();

    // then
    assertThat(acquirableJobs).hasSize(5);
    for (int i = 0; i < 5; i++) {
      assertThat(findJobById(acquirableJobs.get(i).getId()).getPriority()).isEqualTo(5);
    }
  }

  @Test
  public void shouldAcquireOnlyJobsInRangeWithLowerBound() {
    // given
    configuration.setJobExecutorPriorityRangeMin(7L);
    configuration.setJobExecutorPriorityRangeMax(null);

    // when
    List<AcquirableJobEntity> acquirableJobs = findAcquirableJobs();

    // then
    assertThat(acquirableJobs).hasSize(5);
    for (int i = 0; i < 5; i++) {
      assertThat(findJobById(acquirableJobs.get(i).getId()).getPriority()).isEqualTo(10);
    }
  }

  @Test
  public void shouldAcquireOnlyJobsInBoundWithUpperAndLowerBound() {
    // given
    configuration.setJobExecutorPriorityRangeMin(7L);
    configuration.setJobExecutorPriorityRangeMax(12L);

    // when
    List<AcquirableJobEntity> acquirableJobs = findAcquirableJobs();

    // then
    assertThat(acquirableJobs).hasSize(5);
    for (int i = 0; i < 5; i++) {
      assertThat(findJobById(acquirableJobs.get(i).getId()).getPriority()).isEqualTo(10);
    }
  }

  @Test
  public void shouldAcquireOnlyJobsInBoundWithUpperAndLowerBoundNoJobsFound() {
    // given
    configuration.setJobExecutorPriorityRangeMin(12L);
    configuration.setJobExecutorPriorityRangeMax(15L);

    // when
    List<AcquirableJobEntity> acquirableJobs = findAcquirableJobs();

    // then
    assertThat(acquirableJobs).hasSize(0);
  }

  private void createJobs() {
    // jobs with priority 10
    startProcess("jobPrioProcess", "task1", 5);

    // jobs with priority 5
    startProcess("jobPrioProcess", "task2", 5);
  }
}
