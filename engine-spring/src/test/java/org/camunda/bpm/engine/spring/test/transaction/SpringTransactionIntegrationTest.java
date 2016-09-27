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

package org.camunda.bpm.engine.spring.test.transaction;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.spring.impl.test.SpringProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;


/**
 * @author Tom Baeyens
 */
@ContextConfiguration("classpath:org/camunda/bpm/engine/spring/test/transaction/SpringTransactionIntegrationTest-context.xml")
public class SpringTransactionIntegrationTest extends SpringProcessEngineTestCase {

  @Autowired
  protected UserBean userBean;

  @Autowired
  protected DataSource dataSource;

  private static long WAIT_TIME_MILLIS = TimeUnit.MILLISECONDS.convert(20L, TimeUnit.SECONDS);

  @Deployment
  public void testBasicActivitiSpringIntegration() {
    userBean.hello();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertEquals("Hello from Printer!", runtimeService.getVariable(processInstance.getId(), "myVar"));
  }

  @Deployment
  public void testRollbackTransactionOnActivitiException() {

    // Create a table that the userBean is supposed to fill with some data
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute("create table MY_TABLE (MY_TEXT varchar);");

    // The hello() method will start the process. The process will wait in a user task
    userBean.hello();
    assertEquals(0, jdbcTemplate.queryForLong("select count(*) from MY_TABLE"));

    // The completeTask() method will write a record to the 'MY_TABLE' table and complete the user task
    try {
      userBean.completeTask(taskService.createTaskQuery().singleResult().getId());
      fail();
    } catch (Exception e) { }

    // Since the service task after the user tasks throws an exception, both
    // the record and the process must be rolled back !
    assertEquals("My Task", taskService.createTaskQuery().singleResult().getName());
    assertEquals(0, jdbcTemplate.queryForLong("select count(*) from MY_TABLE"));

    // Cleanup
    jdbcTemplate.execute("drop table MY_TABLE if exists;");
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/spring/test/transaction/SpringTransactionIntegrationTest.testErrorPropagationOnExceptionInTransaction.bpmn20.xml",
      "org/camunda/bpm/engine/spring/test/transaction/SpringTransactionIntegrationTest.throwExceptionProcess.bpmn20.xml"
  })
  public void testErrorPropagationOnExceptionInTransaction(){
      runtimeService.startProcessInstanceByKey("process");
      waitForJobExecutorToProcessAllJobs(WAIT_TIME_MILLIS);
      Incident incident = runtimeService.createIncidentQuery().activityId("servicetask").singleResult();
      assertThat(incident.getIncidentMessage(), is("error"));
  }

  @Deployment
  public void testTransactionRollbackInServiceTask() throws Exception {

    runtimeService.startProcessInstanceByKey("txRollbackServiceTask");

    waitForJobExecutorToProcessAllJobs(WAIT_TIME_MILLIS);

    Job job = managementService.createJobQuery().singleResult();

    assertNotNull(job);
    assertEquals(0, job.getRetries());
    assertEquals("Transaction rolled back because it has been marked as rollback-only", job.getExceptionMessage());

    String stacktrace = managementService.getJobExceptionStacktrace(job.getId());
    assertNotNull(stacktrace);
    assertTrue("unexpected stacktrace, was <" + stacktrace + ">", stacktrace.contains("Transaction rolled back because it has been marked as rollback-only"));
  }

  @Deployment
  public void testTransactionRollbackInServiceTaskWithCustomRetryCycle() throws Exception {

    runtimeService.startProcessInstanceByKey("txRollbackServiceTaskWithCustomRetryCycle");

    waitForJobExecutorToProcessAllJobs(WAIT_TIME_MILLIS);

    Job job = managementService.createJobQuery().singleResult();

    assertNotNull(job);
    assertEquals(0, job.getRetries());
    assertEquals("Transaction rolled back because it has been marked as rollback-only", job.getExceptionMessage());

    String stacktrace = managementService.getJobExceptionStacktrace(job.getId());
    assertNotNull(stacktrace);
    assertTrue("unexpected stacktrace, was <" + stacktrace + ">", stacktrace.contains("Transaction rolled back because it has been marked as rollback-only"));
  }

  @Deployment
  public void testFailingTransactionListener() throws Exception {

    runtimeService.startProcessInstanceByKey("failingTransactionListener");

    waitForJobExecutorToProcessAllJobs(WAIT_TIME_MILLIS);

    Job job = managementService.createJobQuery().singleResult();

    assertNotNull(job);
    assertEquals(0, job.getRetries());
    assertEquals("exception in transaction listener", job.getExceptionMessage());

    String stacktrace = managementService.getJobExceptionStacktrace(job.getId());
    assertNotNull(stacktrace);
    assertTrue("unexpected stacktrace, was <" + stacktrace + ">", stacktrace.contains("java.lang.RuntimeException: exception in transaction listener"));
  }


}
