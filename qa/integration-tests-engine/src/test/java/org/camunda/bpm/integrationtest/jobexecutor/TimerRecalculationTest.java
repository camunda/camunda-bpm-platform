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
package org.camunda.bpm.integrationtest.jobexecutor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.integrationtest.jobexecutor.beans.TimerExpressionBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Tobias Metzke
 */
@RunWith(Arquillian.class)
public class TimerRecalculationTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    WebArchive archive = initWebArchiveDeployment()
            .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/TimerRecalculation.bpmn20.xml")
            .addClass(TimerExpressionBean.class);

    return archive;
  }

  @Test
  public void testTimerRecalculationBasedOnProcessVariable() {
    // given
    Map<String, Object> variables = new HashMap<>();
    variables.put("timerExpression", "PT10S");
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("TimerRecalculationProcess", variables);

    ProcessInstanceQuery instancesQuery = runtimeService.createProcessInstanceQuery().processInstanceId(instance.getId());
    JobQuery jobQuery = managementService.createJobQuery();
    assertEquals(1, instancesQuery.count());
    assertEquals(1, jobQuery.count());
    
    Job job = jobQuery.singleResult();
    Date oldDueDate = job.getDuedate();
    
    // when
    runtimeService.setVariable(instance.getId(),  "timerExpression", "PT1S");
    managementService.recalculateJobDuedate(job.getId(), true);

    // then
    assertEquals(1, jobQuery.count());
    Job jobRecalculated = jobQuery.singleResult();
    assertNotEquals(oldDueDate, jobRecalculated.getDuedate());
    
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(jobRecalculated.getCreateTime());
    calendar.add(Calendar.SECOND, 1);
    Date expectedDate = calendar.getTime();
    assertEquals(expectedDate, jobRecalculated.getDuedate());
    
    waitForJobExecutorToProcessAllJobs();

    assertEquals(0, instancesQuery.count());
  }
}
