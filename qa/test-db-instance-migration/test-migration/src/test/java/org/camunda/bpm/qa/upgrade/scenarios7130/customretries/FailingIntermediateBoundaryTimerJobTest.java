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
package org.camunda.bpm.qa.upgrade.scenarios7130.customretries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class FailingIntermediateBoundaryTimerJobTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  ManagementService managementService;

  @Before
  public void assignServices() {
    managementService = engineRule.getManagementService();
  }

  @Test
  public void shouldNotLockFailingIntermidiateBoundaryTimerJobWithCustomJobRetries() throws ParseException {
    try {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      ClockUtil.setCurrentTime(simpleDateFormat.parse("2019-01-01T12:00:01"));
      List<Job> list = managementService.createJobQuery().processDefinitionKey("failingTimer").list();
      for (Job job : list) {
        if (job.getRetries() == 1) {
          assertNotNull(((JobEntity) job).getLockExpirationTime());
        }
        try {
          managementService.executeJob(job.getId());
        } catch (Exception e) {
          // ignore
        }
      }

      List<Job> jobs = managementService.createJobQuery().processDefinitionKey("failingTimer").list();
      assertEquals(3, jobs.size());
      for (Job job : jobs) {
        Date expectedDate = null;
        if (job.getRetries() == 0) { // the first job already failed twice
          expectedDate = simpleDateFormat.parse("2019-01-01T12:00:02");
        } else if (job.getRetries() == 1) { // the second job failed only once
          expectedDate = simpleDateFormat.parse("2019-01-01T12:00:02");
        } else if (job.getRetries() == 3) { // the third job didn't run so far
          expectedDate = simpleDateFormat.parse("2019-01-01T13:00:00");
        } else {
          fail("Unexpected job");
        }
        assertEquals(expectedDate, job.getDuedate());
        assertNull(((JobEntity) job).getLockExpirationTime());
        assertNull(((JobEntity) job).getLockOwner());
      }
    } finally {
      ClockUtil.reset();
    }
  }
}
