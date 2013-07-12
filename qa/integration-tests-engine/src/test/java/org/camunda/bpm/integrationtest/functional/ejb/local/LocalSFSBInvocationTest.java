package org.camunda.bpm.integrationtest.functional.ejb.local;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.functional.ejb.local.bean.BusinessInterface;
import org.camunda.bpm.integrationtest.functional.ejb.local.bean.LocalSFSBClientDelegateBean;
import org.camunda.bpm.integrationtest.functional.ejb.local.bean.LocalSFSBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * This test verifies that a CDI Java Bean Delegate is able to inject and invoke the 
 * local business interface of a SFSB from a different application
 * 
 * Note:
 * - works on Jboss 
 * - not implemented on Glassfish
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class LocalSFSBInvocationTest extends AbstractFoxPlatformIntegrationTest {
 
  @Deployment(name="pa", order=2)
  public static WebArchive processArchive() {    
    return initWebArchiveDeployment()
      .addClass(LocalSFSBClientDelegateBean.class)        
      .addAsResource("org/camunda/bpm/integrationtest/functional/ejb/local/LocalSFSBInvocationTest.testInvokeBean.bpmn20.xml")      
      .addAsWebInfResource("org/camunda/bpm/integrationtest/functional/ejb/local/jboss-deployment-structure.xml","jboss-deployment-structure.xml");
  }
  
  @Deployment(order=1)
  public static WebArchive delegateDeployment() {    
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "service.war")
      .addAsLibraries(DeploymentHelper.getEjbClient())
      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
      .addClass(AbstractFoxPlatformIntegrationTest.class)
      .addClass(LocalSFSBean.class) // the EJB 
      .addClass(BusinessInterface.class); // the business interface
    
    TestContainer.addContainerSpecificResourcesForNonPa(webArchive);
    
    return webArchive;
  }
    
  @Test
  @OperateOnDeployment("pa")
  public void testInvokeBean() throws Exception{
    
    // this testcase first resolves the Bean synchronously and then from the JobExecutor
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testInvokeBean");
    
    Assert.assertEquals(runtimeService.getVariable(pi.getId(), "result"), true);
    
    runtimeService.setVariable(pi.getId(), "result", false);
    
    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());
    
    waitForJobExecutorToProcessAllJobs(6000);
    
    Assert.assertEquals(runtimeService.getVariable(pi.getId(), "result"), true);
    
    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());
  }
  
  @Test
  public void testMultipleInvocations() {
    
    // this is greater than any Datasource / EJB / Thread Pool size -> make sure all resources are released properly.
    int instances = 100;    
    String[] ids = new String[instances];
    
    for(int i=0; i<instances; i++) {    
      ids[i] = runtimeService.startProcessInstanceByKey("testInvokeBean").getId();    
      Assert.assertEquals(runtimeService.getVariable(ids[i], "result"), true);      
      runtimeService.setVariable(ids[i], "result", false);
      taskService.complete(taskService.createTaskQuery().processInstanceId(ids[i]).singleResult().getId());
    }
        
    waitForJobExecutorToProcessAllJobs(60*1000);
    
    for(int i=0; i<instances; i++) {    
      Assert.assertEquals(runtimeService.getVariable(ids[i], "result"), true);    
      taskService.complete(taskService.createTaskQuery().processInstanceId(ids[i]).singleResult().getId());
    }
    
  }

}
