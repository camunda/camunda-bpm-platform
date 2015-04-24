package org.camunda.bpm.integrationtest.functional.ejb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.integrationtest.functional.ejb.beans.SLSBClientDelegate;
import org.camunda.bpm.integrationtest.functional.ejb.beans.SLSBThrowExceptionDelegate;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testcase verifying that if an exception is thrown inside an EJB the original
 * exception reaches the caller
 *
 * @author Ronny Bräunlich
 *
 */
@RunWith(Arquillian.class)
public class SLSBExceptionInDelegateTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment().addClass(SLSBThrowExceptionDelegate.class).addClass(SLSBClientDelegate.class)
        .addAsResource("org/camunda/bpm/integrationtest/functional/ejb/SLSBExceptionInDelegateTest.testOriginalExceptionFromEjbReachesCaller.bpmn20.xml")
        .addAsResource("org/camunda/bpm/integrationtest/functional/ejb/SLSBExceptionInDelegateTest.callProcess.bpmn20.xml");
  }

  @Test
  public void testOriginalExceptionFromEjbReachesCaller() {
      runtimeService.startProcessInstanceByKey("callProcessWithExceptionFromEjb");
      Job job = managementService.createJobQuery().singleResult();
      managementService.setJobRetries(job.getId(), 1);
      
      waitForJobExecutorToProcessAllJobs();
      
      Incident incident = runtimeService.createIncidentQuery().activityId("servicetask1").singleResult();
      assertThat(incident.getIncidentMessage(), is("error"));
  }

}
