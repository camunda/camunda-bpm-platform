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
package org.camunda.bpm.engine.test.api.mgmt;

import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.jobByPriority;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySortingAndCount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class JobQueryByPriorityTest extends PluggableProcessEngineTest {

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/jobPrioExpressionProcess.bpmn20.xml")
  @Test
  public void testOrderByPriority() {
    // given five jobs with priorities from 1 to 5
    List<ProcessInstance> instances = new ArrayList<ProcessInstance>();

    for (int i = 0; i < 5; i++) {
      instances.add(runtimeService.startProcessInstanceByKey("jobPrioExpressionProcess",
          Variables.createVariables().putValue("priority", i)));
    }

    // then querying and ordering by priority works
    verifySortingAndCount(managementService.createJobQuery().orderByJobPriority().asc(), 5, jobByPriority());
    verifySortingAndCount(managementService.createJobQuery().orderByJobPriority().desc(), 5, inverted(jobByPriority()));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/jobPrioExpressionProcess.bpmn20.xml")
  @Test
  public void testFilterByJobPriorityLowerThanOrEquals() {
    // given five jobs with priorities from 1 to 5
    List<ProcessInstance> instances = new ArrayList<ProcessInstance>();

    for (int i = 0; i < 5; i++) {
      instances.add(runtimeService.startProcessInstanceByKey("jobPrioExpressionProcess",
          Variables.createVariables().putValue("priority", i)));
    }

    // when making a job query and filtering by job priority
    // then the correct jobs are returned
    List<Job> jobs = managementService.createJobQuery().priorityLowerThanOrEquals(2).list();
    assertEquals(3, jobs.size());

    Set<String> processInstanceIds = new HashSet<String>();
    processInstanceIds.add(instances.get(0).getId());
    processInstanceIds.add(instances.get(1).getId());
    processInstanceIds.add(instances.get(2).getId());

    for (Job job : jobs) {
      assertTrue(job.getPriority() <= 2);
      assertTrue(processInstanceIds.contains(job.getProcessInstanceId()));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/jobPrioExpressionProcess.bpmn20.xml")
  @Test
  public void testFilterByJobPriorityLowerThanOrEqualsAndHigherThanOrEqual() {
    // given five jobs with priorities from 1 to 5
    List<ProcessInstance> instances = new ArrayList<ProcessInstance>();

    for (int i = 0; i < 5; i++) {
      instances.add(runtimeService.startProcessInstanceByKey("jobPrioExpressionProcess",
          Variables.createVariables().putValue("priority", i)));
    }

    // when making a job query and filtering by disjunctive job priority
    // then the no jobs are returned
    assertEquals(0, managementService.createJobQuery().priorityLowerThanOrEquals(2).priorityHigherThanOrEquals(3).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/jobPrioExpressionProcess.bpmn20.xml")
  @Test
  public void testFilterByJobPriorityHigherThanOrEquals() {
    // given five jobs with priorities from 1 to 5
    List<ProcessInstance> instances = new ArrayList<ProcessInstance>();

    for (int i = 0; i < 5; i++) {
      instances.add(runtimeService.startProcessInstanceByKey("jobPrioExpressionProcess",
          Variables.createVariables().putValue("priority", i)));
    }

    // when making a job query and filtering by job priority
    // then the correct jobs are returned
    List<Job> jobs = managementService.createJobQuery().priorityHigherThanOrEquals(2L).list();
    assertEquals(3, jobs.size());

    Set<String> processInstanceIds = new HashSet<String>();
    processInstanceIds.add(instances.get(2).getId());
    processInstanceIds.add(instances.get(3).getId());
    processInstanceIds.add(instances.get(4).getId());

    for (Job job : jobs) {
      assertTrue(job.getPriority() >= 2);
      assertTrue(processInstanceIds.contains(job.getProcessInstanceId()));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/jobPrioExpressionProcess.bpmn20.xml")
  @Test
  public void testFilterByJobPriorityLowerAndHigher() {
    // given five jobs with priorities from 1 to 5
    List<ProcessInstance> instances = new ArrayList<ProcessInstance>();

    for (int i = 0; i < 5; i++) {
      instances.add(runtimeService.startProcessInstanceByKey("jobPrioExpressionProcess",
          Variables.createVariables().putValue("priority", i)));
    }

    // when making a job query and filtering by job priority
    // then the correct job is returned
    Job job = managementService.createJobQuery().priorityHigherThanOrEquals(2L)
        .priorityLowerThanOrEquals(2L).singleResult();
    assertNotNull(job);
    assertEquals(2, job.getPriority());
    assertEquals(instances.get(2).getId(), job.getProcessInstanceId());
  }
}
