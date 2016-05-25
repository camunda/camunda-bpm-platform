/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.cmmn.timer;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author smirnov
 * @author subhro
 *
 */
public class TimerEventListenerTest extends CmmnProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/timer/TimerEventListenerTest.testSimple.cmmn"})
  public void testSimple() {
    CaseInstance ci = createCaseInstanceByKey("case");
    assertNotNull(ci);
    assertEquals(1, managementService.createJobQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/timer/TimerEventListenerTest.testTimerJobData.cmmn"})
  public void testTimerJobData(){
    CaseInstance ci = createCaseInstanceByKey("case");
    assertNotNull(ci);
    assertEquals(1, managementService.createJobQuery().count());
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertNotNull(job.getCaseDefinitionId());
    assertNotNull(job.getCaseDefinitionKey());
    assertNotNull(job.getCaseExecutionId());
    assertNotNull(job.getCaseInstanceId());
    assertEquals("case", job.getCaseDefinitionKey());
    assertTrue((job instanceof TimerEntity));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/timer/TimerEventListenerTest.testTimerJobData.cmmn"})
  public void testTimerEventListenerJobDefinition(){
    CaseInstance ci = createCaseInstanceByKey("case");
    assertNotNull(ci);
    CaseDefinition cd = repositoryService.createCaseDefinitionQuery().caseDefinitionKey("case").singleResult();
    List<JobDefinition> allJobs = managementService.createJobDefinitionQuery().list();
    assertNotNull(allJobs);
    assertTrue(!allJobs.isEmpty());
    assertTrue(allJobs.size()==1);
    assertNotNull(allJobs.get(0));
    assertTrue(allJobs.get(0).getJobType().equals("timer-event-listener"));
    assertNotNull(allJobs.get(0).getCaseDefinitionKey());
    assertNotNull(allJobs.get(0).getCaseDefinitionId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/timer/TimerEventListenerTest.testTimerJobData.cmmn"})
  public void testTimerEventListenerJobDefinitionIsDeploymentOld(){
    processEngineConfiguration.getDeploymentCache().discardCaseDefinitionCache();
    CaseInstance ci = createCaseInstanceByKey("case");
    assertNotNull(ci);
    List<CaseDefinition> cd = repositoryService.createCaseDefinitionQuery().caseDefinitionKey("case").list();
    List<JobDefinition> allJobs = managementService.createJobDefinitionQuery().list();
    assertNotNull(allJobs);
    assertTrue(!allJobs.isEmpty());
    assertTrue(allJobs.size()==1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/timer/TimerEventListenerTest.testTimerOccur.cmmn"})
  public void testTimerEventListenerOccurExecution(){
    CaseInstance ci = createCaseInstanceByKey("case");
    assertNotNull(ci);
    List<JobDefinition> allJobs = managementService.createJobDefinitionQuery().list();
    assertNotNull(allJobs);
    assertTrue(!allJobs.isEmpty());
    assertTrue(allJobs.size()==1);

    Date startTime = new Date();
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobs(5000L);
    assertEquals(0L, managementService.createJobQuery().count());

    CaseExecution exe = processEngine.getCaseService().createCaseExecutionQuery().caseInstanceId(ci.getCaseInstanceId()).singleResult();
    assertNotNull(exe);
    Map<String, Object> vars = processEngine.getCaseService().getVariables(exe.getId());
    assertNotNull(vars);
    assertTrue(vars.containsKey("occur"));
  }

}
